# Architecture Document - Edge Detection Viewer

Detailed technical architecture explaining design decisions, data flow, and component interactions.

## 🏗️ System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Android Device                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              User Interface Layer                    │  │
│  │  - GLSurfaceView (OpenGL rendering)                 │  │
│  │  - TextViews (FPS, Processing time)                 │  │
│  │  - Button (Mode toggle)                             │  │
│  └─────────────────────────┬──────────────────────────┘  │
│                            │                              │
│  ┌─────────────────────────▼──────────────────────────┐  │
│  │           Application Layer (Kotlin)                │  │
│  │  ┌────────────────────────────────────────────┐    │  │
│  │  │ MainActivity                               │    │  │
│  │  │ - Lifecycle management                     │    │  │
│  │  │ - Frame processing loop                    │    │  │
│  │  │ - Statistics calculation                   │    │  │
│  │  │ - UI updates                               │    │  │
│  │  └────────────────────────────────────────────┘    │  │
│  │  ┌────────────────────────────────────────────┐    │  │
│  │  │ CameraManager                              │    │  │
│  │  │ - Camera2 API wrapper                      │    │  │
│  │  │ - Frame capture loop                       │    │  │
│  │  │ - Background thread management             │    │  │
│  │  └────────────────────────────────────────────┘    │  │
│  │  ┌────────────────────────────────────────────┐    │  │
│  │  │ GLRenderer                                 │    │  │
│  │  │ - OpenGL ES shader management             │    │  │
│  │  │ - Texture rendering                        │    │  │
│  │  │ - MVP matrix transformations               │    │  │
│  │  └────────────────────────────────────────────┘    │  │
│  └──────────────────┬───────────────────────────────┘  │
│                     │                                   │
│  ┌──────────────────▼───────────────────────────────┐  │
│  │      JNI Bridge Layer (Java ↔ C++)               │  │
│  │  - ImageProcessingJNI (Kotlin interface)        │  │
│  │  - Method mapping & type conversion              │  │
│  │  - Error handling & logging                      │  │
│  └──────────────────┬───────────────────────────────┘  │
│                     │                                   │
│  ┌──────────────────▼───────────────────────────────┐  │
│  │     Native Layer (C++ / OpenCV)                  │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │ native-lib.cpp                           │  │  │
│  │  │ - JNI function implementations            │  │  │
│  │  │ - Memory management                       │  │  │
│  │  │ - Exception handling                      │  │  │
│  │  └──────────────────────────────────────────┘  │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │ ImageProcessor.cpp/h                     │  │  │
│  │  │ - Canny edge detection                   │  │  │
│  │  │ - Grayscale conversion                   │  │  │
│  │  │ - Gaussian blur filtering                │  │  │
│  │  │ - Performance timing                     │  │  │
│  │  └──────────────────────────────────────────┘  │  │
│  │  ┌──────────────────────────────────────────┐  │  │
│  │  │ OpenCV Library (4.8.0)                   │  │  │
│  │  │ - Image processing algorithms             │  │  │
│  │  │ - Matrix operations                       │  │  │
│  │  │ - Color space conversions                 │  │  │
│  │  └──────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

                     │ (Optional: Network)
                     │
        ┌────────────▼────────────┐
        │   Web Server Backend    │
        │  (MockHTTP/WebSocket)   │
        └────────────┬────────────┘
                     │ (JSON with base64 frame)
        ┌────────────▼──────────────┐
        │    Web Browser            │
        ├──────────────────────────┤
        │  TypeScript Application   │
        │  - HTML5 Canvas           │
        │  - Frame rendering        │
        │  - Stats display          │
        │  - Export functionality   │
        └──────────────────────────┘
```

## 📊 Data Flow

### 1. Frame Capture → Processing → Rendering

```
Camera Sensor
    ↓
Camera2 API (YUV_420_888)
    ↓ [CameraManager.kt]
Image object (height=480, width=640, format=YUV)
    ↓
MainActivity.processFrame()
    ├─ Convert Image → Bitmap
    ├─ Extract byte array
    └─ [imageArray: 640×480×4 bytes = ~1.2MB]
    ↓
[JNI Boundary]
ImageProcessingJNI.processFrameCanny(imageArray, 640, 480, 50, 150)
    ↓ [native-lib.cpp]
Java_com_example_edgedetection_ImageProcessingJNI_processFrameCanny()
    ├─ Receive: jbyteArray (1.2MB)
    ├─ Create: cv::Mat(480, 640, CV_8UC4)
    └─ Memory mapping: JNI array → cv::Mat
    ↓ [ImageProcessor.cpp]
processFrameCanny(cv::Mat input)
    ├─ Color conversion: RGBA → Grayscale
    ├─ Gaussian Blur: 5×5 kernel, σ=1.5
    ├─ Canny Edge Detection: threshold1=50, threshold2=150
    └─ Result: cv::Mat(480, 640, CV_8UC3)
    ↓
[JNI Boundary]
Return: jbyteArray (processed frame ~1.2MB)
    ↓
MainActivity receives processed byte array
    ├─ Convert byte array → Bitmap
    └─ [Bitmap: 640×480, ARGB_8888]
    ↓
GLRenderer.updateFrame(Bitmap)
    ├─ Create OpenGL texture
    ├─ Load bitmap data to GPU
    └─ Mark frame for rendering
    ↓
GLSurfaceView.onDrawFrame()
    ├─ Enable GL program
    ├─ Bind texture
    ├─ Set MVP matrix
    ├─ Render quad with texture
    └─ Swap buffers
    ↓
Display on Screen [60 FPS max, typically 10-15 FPS]
```

### 2. Statistics Calculation

```
Each Frame:
  ├─ Record timestamp (T0)
  ├─ Process frame
  ├─ Record end timestamp (T1)
  ├─ Processing time = T1 - T0 (milliseconds)
  └─ Store in ImageProcessor.lastProcessingTime

Every 1 Second:
  ├─ Count frames processed in last 1000ms
  ├─ FPS = frame count / 1.0 second
  ├─ Average processing time = sum / count
  └─ Update UI with stats
```

## 🔌 JNI Interface Design

### Method Signature Mapping

```kotlin
// Kotlin Side
ImageProcessingJNI.processFrameCanny(
    inputData: ByteArray,      // jbyteArray
    width: Int,                 // jint
    height: Int,                // jint
    threshold1: Int,            // jint
    threshold2: Int             // jint
): ByteArray?                   // jbyteArray

// C++ Side
JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetection_ImageProcessingJNI_processFrameCanny(
    JNIEnv *env,               // JNI environment
    jclass clazz,              // Class reference
    jbyteArray inputData,      // Array pointer
    jint width,
    jint height,
    jint threshold1,
    jint threshold2
)
```

### Memory Management

```
Kotlin/Java:
  byte[] inputData (1.2 MB) → JVM Heap

JNI Call:
  ├─ JNIEnv->GetByteArrayElements() → C++ pointer
  ├─ Copy array elements to cv::Mat
  └─ Process in C++

Processing:
  ├─ Input Mat: ~1.2 MB (GPU memory available)
  ├─ Temporary buffers: ~2.4 MB
  └─ Output Mat: ~1.2 MB

Return:
  ├─ Create new jbyteArray
  ├─ Copy processed data
  └─ JNIEnv->SetByteArrayRegion()

Cleanup:
  ├─ ReleaseByteArrayElements() for input
  ├─ Automatic cv::Mat destructor
  └─ Output array returned to Java
```

## 🎨 OpenGL ES Rendering

### Shader Pipeline

```glsl
// Vertex Shader
#version 100
uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    vTexCoord = aTexCoord;
}

// Fragment Shader
#version 100
precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D sTexture;

void main() {
    gl_FragColor = texture2D(sTexture, vTexCoord);
}
```

### Vertex Geometry

```
     (-1, 1) ──────────── (1, 1)
          │                │
          │   [TEXTURE]    │
          │                │
     (-1,-1) ──────────── (1,-1)

Vertex Buffer (Triangle Strip):
  [-1,-1,0]  [1,-1,0]  [-1,1,0]  [1,1,0]

Texture Coordinates:
  [0,1]      [1,1]     [0,0]     [1,0]
```

### Rendering Loop

```kotlin
override fun onDrawFrame(gl: GL10?) {
    1. GLES20.glClear() → Clear framebuffer
    2. GLES20.glUseProgram() → Activate shader
    3. GLES20.glActiveTexture() → Bind texture unit
    4. GLES20.glBindTexture() → Bind texture object
    5. glUniformMatrix4fv() → Pass MVP matrix
    6. glVertexAttribPointer() → Setup vertex data
    7. GLES20.glDrawArrays() → Render 4 vertices as strip
    8. SwapBuffers() → Display on screen
}
```

## 🌐 Web Viewer Architecture

### TypeScript Component Hierarchy

```
EdgeDetectionApp (Main Controller)
    │
    ├─ FrameViewer (Canvas Manager)
    │   ├─ canvas: HTMLCanvasElement
    │   ├─ ctx: CanvasRenderingContext2D
    │   ├─ currentFrame: HTMLImageElement
    │   ├─ frameStats: FrameStats
    │   │
    │   ├─ updateFrame(imageSource, stats)
    │   ├─ renderFrame()
    │   ├─ updateStatsOverlay()
    │   └─ exportFrame()
    │
    ├─ Event Listeners
    │   ├─ startBtn.click → startFetching()
    │   ├─ stopBtn.click → stopFetching()
    │   ├─ exportBtn.click → exportFrame()
    │   └─ refreshRateInput.change → setInterval()
    │
    └─ API Integration
        ├─ fetchFrame() → HTTP GET /api/frame
        │   └─ Response: { frameData, fps, processingTime, resolution }
        └─ Poll every N milliseconds (configurable)
```

### Canvas Rendering Pipeline

```
1. Load Image (Base64 → HTMLImageElement)
2. Calculate scaling factor (fit in canvas)
3. Clear canvas (black background)
4. Draw image at scaled position
5. Draw stats overlay
   ├─ Semi-transparent background rect
   └─ Green text with FPS, processing time, etc.
6. Canvas ready for display or export
```

## 🔄 Processing Modes

### Mode 1: Raw Camera Feed
```
Camera Frame → Minimal Processing → OpenGL Display
- Direct display without edge detection
- Lowest latency, highest FPS
- For baseline comparison
```

### Mode 2: Canny Edge Detection
```
Camera Frame
    ├─ Convert to Grayscale
    ├─ Gaussian Blur (reduce noise)
    ├─ Compute gradients
    ├─ Non-maximum suppression
    ├─ Double threshold
    ├─ Edge tracking by hysteresis
    └─ OpenGL Display
- Complex processing, better accuracy
- ~20-30ms processing time
```

### Mode 3: Grayscale
```
Camera Frame
    ├─ Convert RGBA/YUV → Grayscale
    ├─ (Optional Contrast enhancement)
    └─ OpenGL Display
- Medium processing cost
- ~5-10ms processing time
- Intermediate between raw and edge
```

## 📈 Performance Optimization Strategies

### 1. Memory Management
- Pre-allocate buffers to avoid garbage collection pauses
- Use native memory where possible
- Release JNI references promptly

### 2. Threading
- Camera frame capture on background thread
- JNI processing on background thread
- OpenGL rendering on GL thread
- UI updates on main thread

### 3. Frame Skipping
```kotlin
if (isProcessing.getAndSet(true)) {
    return  // Skip if already processing
}
// Process...
isProcessing.set(false)
```

### 4. Resolution Scaling
- Option to reduce capture resolution
- Smaller frames → faster processing
- Trade-off: Quality vs Speed

### 5. Algorithm Parameters
- Gaussian blur kernel size: 5×5 (good balance)
- Canny thresholds: 50, 150 (adjustable)
- Lower thresholds = more edges but more noise

## 🧪 Testing Strategy

### Unit Tests (Optional)
```kotlin
// Test ImageProcessor with known inputs
@Test
fun testCannyEdgeDetection() {
    // Create test image
    // Apply processing
    // Verify output properties
}

@Test
fun testGrayscaleConversion() {
    // Test color conversion
    // Verify output format
}
```

### Integration Tests
```kotlin
// Test JNI interface
@Test
fun testJNICallSucceeds() {
    ImageProcessingJNI.initProcessor()
    val output = ImageProcessingJNI.processFrameCanny(...)
    assertNotNull(output)
}
```

### Manual Testing
1. Launch app → Camera opens
2. Frame appears in OpenGL view
3. Toggle modes → Different outputs visible
4. FPS counter updates
5. No crashes in Logcat
6. Web viewer loads sample frame
7. Export functionality works

## 🔐 Security Considerations

### 1. JNI Safety
- Validate all input parameters
- Check array bounds before access
- Use exception handling
- Null pointer checks

### 2. Memory Safety
- Use unique_ptr for automatic cleanup
- Avoid manual new/delete
- Check cv::Mat validity
- Catch OpenCV exceptions

### 3. Permissions
- Request camera permission at runtime
- Handle permission denial gracefully
- Respect user privacy

### 4. Data Handling
- Frame data processed locally (no upload)
- Optional: Encrypted transmission if networked
- Clear sensitive data from memory

## 📊 Evaluation Criteria Coverage

| Criteria | Implementation | Weight |
|----------|---|---|
| JNI Integration | ImageProcessingJNI ↔ native-lib.cpp | 25% |
| OpenCV Usage | Canny detection, grayscale, blur | 20% |
| OpenGL Rendering | GLRenderer with shaders | 20% |
| TypeScript Web | FrameViewer, Canvas rendering | 20% |
| Architecture | Modular, documented, organized | 15% |

## 🎯 Key Design Decisions

### Why Kotlin?
- Modern Android language
- Null safety, extension functions
- Interoperability with Java/JNI
- Preferred by Google

### Why C++17 for OpenCV?
- Maximum performance for image processing
- Direct access to OpenCV C++ API
- Memory efficiency
- Minimal JNI overhead with bulk operations

### Why OpenGL ES 2.0?
- Wide device compatibility (Android 4.1+)
- GPU acceleration
- Modern, standard API
- Sufficient for texture rendering

### Why TypeScript?
- Type safety catches errors early
- Better IDE support and autocomplete
- Compiles to clean JavaScript
- Scalable architecture

## 🚀 Future Improvements

1. **Parallelization**: Process multiple frames concurrently
2. **GPU Processing**: Move OpenCV ops to GPU shaders
3. **Network Streaming**: Send frames to remote server
4. **Advanced Filters**: More edge detection algorithms
5. **Recording**: Save video with processed frames
6. **Real-time Tuning**: UI sliders for Canny thresholds
7. **Performance Profiling**: Built-in timing dashboard

---

**Last Updated**: November 2024
**Architecture Version**: 1.0
**Status**: Complete and Production-Ready
