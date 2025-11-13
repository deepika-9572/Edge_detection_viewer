# Setup Guide - Edge Detection Viewer

Complete step-by-step instructions for setting up and running the project.

## 📋 Prerequisites Checklist

- [ ] Android Studio (latest version)
- [ ] Android SDK API 21+ (downloaded via SDK Manager)
- [ ] NDK 23.x (downloaded via SDK Manager)
- [ ] CMake 3.22.1+ (downloaded via SDK Manager)
- [ ] Java 11+ (bundled with Android Studio)
- [ ] Node.js 16+ (for TypeScript compilation - optional)
- [ ] Git (for version control)

## 🚀 Quick Start (5-10 minutes)

### 1. Download OpenCV Android SDK

```bash
# Visit https://opencv.org/releases and download
# OpenCV Android SDK (version 4.8.0 or later)

# Extract to a convenient location
# Remember the path for next step
```

### 2. Configure local.properties

```bash
# In project root directory, create local.properties
# Copy from local.properties.example if available

# Windows:
sdk.dir=C:\Users\YourName\AppData\Local\Android\Sdk
ndk.dir=C:\Users\YourName\AppData\Local\Android\Sdk\ndk\23.1.7779620
opencv.dir=C:\path\to\opencv-android-sdk

# macOS:
sdk.dir=/Users/YourName/Library/Android/sdk
ndk.dir=/Users/YourName/Library/Android/sdk/ndk/23.1.7779620
opencv.dir=/Users/YourName/opencv-android-sdk

# Linux:
sdk.dir=/home/YourName/Android/Sdk
ndk.dir=/home/YourName/Android/Sdk/ndk/23.1.7779620
opencv.dir=/home/YourName/opencv-android-sdk
```

### 3. Build the Project

```bash
# Using Gradle wrapper (recommended)
cd EdgeDetectionViewer

# Clean and build
./gradlew clean build

# On Windows:
gradlew.bat clean build

# Wait for compilation to complete
# Should see: "BUILD SUCCESSFUL"
```

### 4. Run on Device

```bash
# Ensure Android device is connected via USB
# Enable USB Debugging on device:
# Settings → About Phone → Tap Build Number 7 times
# Settings → Developer Options → Enable USB Debugging

# Install and run
./gradlew installDebug
./gradlew run

# App should launch automatically
```

## 📱 Android Setup Details

### Step 1: Open Project in Android Studio

```
File → Open → Select project root directory
```

### Step 2: Install Missing Components

Android Studio will prompt to install missing SDKs/Tools:
- Accept all prompts
- Wait for downloads to complete

### Step 3: Configure NDK

```
Tools → SDK Manager → SDK Tools tab
✓ Check "NDK (Side by side)"
✓ Check "CMake 3.22.1+"
→ Apply and OK
```

### Step 4: Set Up CMake Build

The `CMakeLists.txt` is already configured. Verify:
- `app/src/main/cpp/CMakeLists.txt` exists
- It includes `ImageProcessor.cpp` and `native-lib.cpp`
- Finds OpenCV package

### Step 5: Verify Build

```bash
# Terminal in Android Studio (Alt+F12)
./gradlew tasks

# Look for:
# - assemble
# - build
# - installDebug
```

### Step 6: Deploy to Device

```
Run → Run 'app' (Shift+F10)

Or use:
./gradlew installDebug
```

## 💻 Web Viewer Setup

### Step 1: Install Node.js (Optional)

```bash
# Download from https://nodejs.org/
# Version 16 or higher recommended
node --version  # Verify installation
```

### Step 2: Install TypeScript Dependencies

```bash
cd web
npm install
```

### Step 3: Compile TypeScript

```bash
# Build once
npm run build

# Or watch for changes
npm run watch
```

### Step 4: Serve Locally

#### Option A: Using npm (if Node.js installed)
```bash
npm run serve
# Opens at http://localhost:8080
```

#### Option B: Using Python
```bash
python3 -m http.server 8000
# Opens at http://localhost:8000
```

#### Option C: Using PHP
```bash
php -S localhost:8000
# Opens at http://localhost:8000
```

### Step 5: Access Web Viewer

```
Open browser to: http://localhost:8000
(or configured port)
```

## 🔧 Troubleshooting Setup

### Issue: "SDK not found"
```
Solution:
1. Open: local.properties
2. Update sdk.dir path
3. Path should contain /platforms and /build-tools
```

### Issue: "NDK not found"
```
Solution:
1. Check Android Studio → Tools → SDK Manager
2. Go to SDK Tools tab
3. Install NDK 23.x
4. Update ndk.dir in local.properties
```

### Issue: "OpenCV not found"
```
Solution:
1. Download OpenCV Android SDK from opencv.org
2. Extract to known location
3. Update opencv.dir in local.properties
4. Run: ./gradlew clean build
```

### Issue: "CMake not found"
```
Solution:
1. Open Android Studio → Tools → SDK Manager
2. SDK Tools tab
3. Check CMake 3.22.1+ and install
4. Sync Gradle project
```

### Issue: "Build fails with 'native-lib.so not found'"
```
Solution:
1. Check app/src/main/cpp/ contains:
   - CMakeLists.txt
   - native-lib.cpp
   - ImageProcessor.cpp
   - ImageProcessor.h
2. Run: ./gradlew clean build
3. Check build output for errors
```

### Issue: "Camera permission denied on device"
```
Solution:
1. Device must have Android 6.0+ for runtime permissions
2. App will request permission on first launch
3. Tap "Allow" when prompted
4. Check: Settings → Apps → Edge Detection → Permissions → Camera
```

### Issue: "App crashes immediately"
```
Solution:
1. Check Logcat output (Android Studio → Logcat)
2. Look for errors with tag "EdgeDetection"
3. Common issues:
   - Missing OpenCV library
   - Camera permission denied
   - No camera on device
4. Check device requirements
```

## 📊 Verification Steps

After completing setup, verify everything works:

### ✓ Android App
```
1. Launch app on device
2. App should open with GLSurfaceView
3. Camera should start automatically
4. FPS counter updates (bottom left)
5. Tap button to toggle processing modes
6. Each mode shows different output
```

### ✓ Web Viewer
```
1. Open index.html in browser
2. Should see UI with controls
3. Canvas shows sample green-edged frame
4. Click "Start Stream" button
5. Click "Export Frame" to save PNG
6. Change refresh rate (optional)
```

### ✓ Overall Integration
```
1. Android processes frames in real-time
2. Web viewer loads without errors
3. No console errors in browser DevTools
4. Performance is smooth (10-15+ FPS)
```

## 🔄 Build Commands Reference

```bash
# Full build and install
./gradlew clean build installDebug

# Debug build only
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run on connected device
./gradlew run

# View build logs
./gradlew build --info

# Rebuild native code only
./gradlew clean build

# Check dependencies
./gradlew dependencies
```

## 📱 Device Requirements

### Minimum
- Android 5.0 (API 21)
- 1GB RAM
- Rear-facing camera

### Recommended
- Android 8.0+ (API 26+)
- 2GB+ RAM
- 1080p+ camera

## 🌐 Network Setup (Optional)

For Android to Web integration:

### Android Backend Server (Optional)

Add to `MainActivity.kt`:
```kotlin
// Create HTTP endpoint to serve frames
// POST /api/frame → returns JSON with frameData (base64)
```

### Web Viewer Backend Connection

In `web/src/main.ts`:
```typescript
private apiEndpoint = 'http://your-device-ip:8081/api/frame';
```

## 📚 Additional Resources

- [Android Developer Documentation](https://developer.android.com)
- [OpenCV Android Documentation](https://docs.opencv.org/4.8.0/d5/d3d/classcv_1_1Mat.html)
- [OpenGL ES Tutorial](https://developer.android.com/guide/topics/graphics/opengl)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)

## ✅ Final Checklist

Before submission, verify:

- [ ] Project builds without errors
- [ ] App runs on Android device
- [ ] Camera frames display in real-time
- [ ] Edge detection works (different modes visible)
- [ ] FPS counter shows realistic values
- [ ] Web viewer displays sample frame
- [ ] Web viewer UI is responsive
- [ ] All files are properly committed
- [ ] README.md is complete and accurate
- [ ] Setup guide is available
- [ ] Project structure is clean and organized

## 🎯 Success Indicators

✅ All of the following should be true:

1. Android app launches without crashes
2. Camera feeds display in OpenGL surface
3. Processing time < 50ms per frame
4. FPS counter shows 10+
5. Mode toggle button works
6. Web page loads in browser
7. Canvas displays sample/live frames
8. Export button saves PNG files
9. Code is well-structured
10. Git history shows development process

---

**Setup Complete!** You're ready to build and submit. Good luck! 🚀

For questions or issues, check the README.md and main project documentation.
