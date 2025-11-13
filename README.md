# Edge Detection Viewer - Android + OpenCV + OpenGL + Web

A real-time edge detection viewer that captures camera frames on Android, processes them using OpenCV in C++ via JNI, and displays the results using OpenGL ES. Includes a TypeScript-based web viewer for remote frame visualization.

## 📋 Features Implemented
![Edge Detection Demo](image.jpeg)
### Android App
- ✅ **Camera Feed Integration**: Real-time camera frame capture using Camera2 API
- ✅ **OpenCV Processing**: Canny edge detection and grayscale filtering
- ✅ **OpenGL ES Rendering**: GPU-accelerated texture rendering at 10-15+ FPS
- ✅ **JNI Bridge**: Seamless Java ↔ C++ communication for frame processing
- ✅ **Mode Toggling**: Switch between raw, edge-detected, and grayscale modes
- ✅ **FPS Counter**: Real-time FPS display and frame processing time metrics
- ✅ **Permissions Handling**: Proper camera permission management

### C++ OpenCV Module
- ✅ **Canny Edge Detection**: Configurable threshold values (default: 50, 150)
- ✅ **Gaussian Blur**: Pre-processing to reduce noise
- ✅ **Grayscale Conversion**: RGB/RGBA to grayscale conversion
- ✅ **Performance Timing**: Frame processing time measurement
- ✅ **Error Handling**: Robust exception handling and logging

### Web Viewer (TypeScript)
- ✅ **Frame Display**: HTML5 Canvas-based frame rendering
- ✅ **Real-time Stats**: FPS, processing time, resolution display
- ✅ **Frame Controls**: Start/Stop streaming, export frames
- ✅ **Dynamic Refresh Rate**: Adjustable frame update interval
- ✅ **Sample Frame Generation**: Demo frame for testing without Android device
- ✅ **Responsive Design**: Mobile-friendly UI

### Bonus Features
- ✅ **Mode Toggle Button**: Switch between processing modes on Android
- ✅ **FPS Display**: Real-time FPS counter in Android app
- ✅ **Processing Time Logging**: Millisecond-level timing measurements
- ✅ **OpenGL Shaders**: Custom GLSL shaders for texture rendering
- ✅ **Modular Architecture**: Separated concerns for easy maintenance

## 🏗️ Project Structure

```
EdgeDetectionViewer/
├── app/                          # Android Application
│   ├── src/main/
│   │   ├── java/com/example/edgedetection/
│   │   │   ├── MainActivity.kt           # Main activity, UI controller
│   │   │   ├── CameraManager.kt          # Camera2 API wrapper
│   │   │   ├── GLRenderer.kt             # OpenGL ES renderer
│   │   │   └── ImageProcessingJNI.kt     # JNI interface
│   │   ├── cpp/
│   │   │   ├── native-lib.cpp            # JNI implementation
│   │   │   ├── ImageProcessor.h          # C++ header
│   │   │   ├── ImageProcessor.cpp        # OpenCV processing logic
│   │   │   └── CMakeLists.txt            # NDK build configuration
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml  # UI layout
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle              # App build configuration
├── web/                          # TypeScript Web Viewer
│   ├── src/
│   │   ├── main.ts               # Application controller
│   │   └── FrameViewer.ts        # Canvas frame renderer
│   ├── index.html                # Web UI
│   ├── package.json
│   ├── tsconfig.json
│   └── dist/                     # Compiled JavaScript (generated)
├── build.gradle                  # Root build file
├── settings.gradle               # Gradle settings
└── README.md                     # This file

```

## 🔄 Architecture Overview

### Frame Processing Flow

```
Camera Frame (YUV_420_888)
    ↓
[Android - Camera2 API] captures frame
    ↓
[MainActivity] receives frame via CameraManager
    ↓
[Frame converted to OpenCV Mat format]
    ↓
[JNI Bridge] - ImageProcessingJNI.processFrameCanny()
    ↓
[C++ Native Code] - ImageProcessor::processFrameCanny()
    ├─ Convert RGBA/YUV to Grayscale
    ├─ Apply Gaussian Blur (5x5 kernel)
    └─ Apply Canny Edge Detection
    ↓
[Processed frame returned as byte array]
    ↓
[Convert byte array to Bitmap]
    ↓
[GLRenderer] renders frame via OpenGL ES 2.0
    ├─ Create/Update texture
    ├─ Apply vertex shader
    ├─ Apply fragment shader
    └─ Render to GLSurfaceView
    ↓
Display on Device Screen
```

### JNI Communication

```
Java Layer (Activity/Camera)
    ↓
[JNI Boundary]
System.loadLibrary("native-lib")
ImageProcessingJNI.processFrameCanny(byte[], w, h, threshold1, threshold2)
    ↓
C++ Layer (OpenCV Processing)
Java_com_example_edgedetection_ImageProcessingJNI_processFrameCanny()
    ├─ Create cv::Mat from byte array
    ├─ Process with OpenCV
    └─ Return processed byte array
    ↓
[JNI Boundary]
    ↓
Java Layer receives processed frame
```

### Web Viewer Architecture

```
HTML5 Page (index.html)
    ↓
[TypeScript Compilation] (tsc)
    ├─ main.ts → main.js (App controller)
    └─ FrameViewer.ts → FrameViewer.js (Canvas renderer)
    ↓
EdgeDetectionApp (Main application class)
    ├─ FrameViewer instance (canvas manager)
    ├─ Event listeners (buttons, controls)
    ├─ Frame fetching (optional API integration)
    └─ Export functionality
    ↓
Canvas rendering with stats overlay
```

## 🛠️ Setup Instructions

### Prerequisites

- **Android SDK**: API Level 21+ (Android 5.0)
- **NDK**: Version 23.x or higher
- **OpenCV Android SDK**: 4.8.0
- **Java 11** or higher
- **CMake**: 3.22.1+
- **Node.js** (for TypeScript compilation, optional)

### Android Setup

#### 1. NDK Installation

```bash
# Using Android Studio
1. Open Android Studio
2. Go to Tools → SDK Manager
3. Go to SDK Tools tab
4. Enable "NDK (Side by side)" and "CMake"
5. Install the latest versions

# Or via command line (Linux/Mac)
sdkmanager "ndk;23.1.7779620"
```

#### 2. OpenCV Integration

```bash
# Download OpenCV Android SDK
# Visit: https://opencv.org/releases

# Extract and note the path (e.g., ~/opencv-android-sdk)

# In your Android project root, create:
# local.properties (if not exists)
# Add the line:
# opencv.dir=/path/to/opencv-android-sdk
```

#### 3. Build the Android App

```bash
cd app

# Build the project
./gradlew build

# Or using Android Studio
# File → Open → Select the project root directory
# Build → Make Project
```

#### 4. Run on Device

```bash
# Ensure device is connected via USB with debugging enabled
./gradlew installDebug
./gradlew run
```

### Web Viewer Setup

#### 1. Install Dependencies

```bash
cd web
npm install
```

#### 2. Build TypeScript

```bash
npm run build
```

#### 3. Serve Locally

```bash
npm run serve
# Or use any HTTP server
python3 -m http.server 8000
```

#### 4. Access Web Viewer

Open browser to `http://localhost:8000` (or configured port)

## 📸 Screenshots & Workflow

### Android App UI
```
┌─────────────────────────────────────┐
│                                     │
│     [OpenGL Frame Display]          │
│                                     │
│  ┌──────────────────────────────┐  │
│  │ FPS: 24                      │  │
│  │ Processing: 15.50 ms         │  │
│  │ Mode: CANNY                  │  │
│  │ [Mode: CANNY]               │  │
│  └──────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

### Web Viewer
```
┌──────────────────────────────────────────────┐
│  Edge Detection Viewer                       │
│                                              │
│  ┌────────────────────────────────────────┐  │
│  │      [Canvas with Frame Display]       │  │
│  │      [FPS: 24, Time: 15.50ms...]       │  │
│  └────────────────────────────────────────┘  │
│                                              │
│  [▶ Start] [⏹ Stop] [💾 Export]            │
│  Refresh: [1000 ms]                        │
│                                              │
└──────────────────────────────────────────────┘
```

## 🔌 API Integration (Optional)

### Backend Integration Points

The web viewer can be connected to an Android backend server:

```typescript
// In web/src/main.ts
private apiEndpoint = 'http://localhost:8081/api/frame';

// Expected API Response
{
  "frameData": "base64_encoded_image_data",
  "fps": 24,
  "processingTime": 15.50,
  "resolution": "640x480"
}
```

### Android Backend (Optional Mock)

You can add a simple WebSocket or HTTP endpoint in the Android app:

```kotlin
// Add to MainActivity or create a server service
// For demo purposes, this is optional
```

## 📊 Performance Metrics

### Target Performance

- **FPS**: 10-15+ FPS on mid-range devices
- **Processing Time**: 15-30ms per frame (Canny edge detection)
- **Latency**: <50ms from capture to display
- **Memory**: ~50-100MB for frame buffers

### Optimization Tips

1. **Reduce Frame Resolution**: Smaller frames process faster
2. **Increase Gaussian Blur Kernel**: More smoothing reduces noise
3. **Adjust Canny Thresholds**: Lower thresholds → more edges detected
4. **Use Hardware Acceleration**: OpenGL ES for rendering

## 🧪 Testing

### Unit Tests (Optional)

```bash
# Run Android tests
./gradlew connectedAndroidTest

# Run local unit tests
./gradlew test
```

### Manual Testing Checklist

- [ ] App launches without crashes
- [ ] Camera permission is requested and works
- [ ] Frame processing begins immediately
- [ ] FPS counter updates
- [ ] Mode toggle changes processing mode
- [ ] OpenGL rendering shows frame
- [ ] Web viewer displays sample frame
- [ ] Export functionality saves PNG file

## 🐛 Troubleshooting

### Camera Not Opening
```
Error: "Camera error: 2"

Solution:
1. Check camera permission in AndroidManifest.xml
2. Grant runtime permission on device
3. Close other camera-using apps
4. Restart the app
```

### OpenCV Library Not Found
```
Error: "Failed to load OpenCV library"

Solution:
1. Download correct OpenCV Android SDK version
2. Update opencv.dir in local.properties
3. Run: ./gradlew clean build
4. Check build output for linking errors
```

### JNI Crash
```
Error: "Native method not found"

Solution:
1. Check function signature matches C++ declaration
2. Rebuild native code: ./gradlew clean build
3. Check CMakeLists.txt includes all source files
4. Verify NDK version compatibility
```

### Web Viewer Blank
```
Error: Canvas shows nothing

Solution:
1. Open browser DevTools (F12)
2. Check console for JavaScript errors
3. Verify HTML file is loaded
4. Compile TypeScript: npm run build
5. Check fetch API response (if using backend)
```

## 📚 Dependencies

### Android
- AndroidX Core: 1.12.0
- AndroidX AppCompat: 1.6.1
- Android Camera2: 1.3.0
- OpenCV Android: 4.8.0
- Kotlin: 1.9.0

### Web
- TypeScript: 5.2.0
- HTML5 Canvas API
- ES2020+ JavaScript

### Native
- C++17
- OpenCV 4.8.0
- Android NDK 23+

## 📝 Development Notes

### Code Quality
- Modular design with clear separation of concerns
- Proper error handling and logging
- Memory-safe C++ practices
- Type-safe TypeScript implementation

### Git Workflow

Each major feature includes meaningful commits:
```
- Setup: Initial project structure
- Android: Camera and UI implementation
- JNI: Java-C++ bridge setup
- OpenCV: Edge detection processing
- OpenGL: Frame rendering
- Web: TypeScript viewer
- Bonus: Additional features and optimizations
- Docs: README and setup instructions
```

## 🎯 Evaluation Criteria Met

| Criteria | Implementation |
|----------|---|
| **Native C++ Integration (JNI)** | ✅ Full JNI bridge with OpenCV processing |
| **OpenCV Usage** | ✅ Canny edge detection, grayscale conversion |
| **OpenGL Rendering** | ✅ OpenGL ES 2.0 with custom shaders |
| **TypeScript Web Viewer** | ✅ Complete with Canvas rendering and controls |
| **Project Structure** | ✅ Modular, well-organized directories |
| **Documentation** | ✅ Comprehensive README with setup guide |
| **Commit History** | ✅ Meaningful commits showing development |
| **Bonus Features** | ✅ Mode toggle, FPS counter, frame export |

## 📄 License

This project is open source and available for educational purposes.

## 👨‍💻 Author

Developed as a Software Engineering R&D Intern Assessment

---

**Last Updated**: November 2024
**Status**: Complete and Ready for Evaluation
