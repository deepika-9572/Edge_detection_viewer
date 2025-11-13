# How to Run the Project

Complete guide to run both the Android app and web viewer.

---

## 🚀 Quick Start (Choose One)

### Option A: Run Android App (Recommended)
```bash
# Prerequisites: Android device connected via USB with USB Debugging enabled

./gradlew installDebug
./gradlew run
```

### Option B: Run Web Viewer Only
```bash
# Prerequisites: Node.js installed

cd web
npm install
npm run build
npm run serve
```

---

## 📱 Running the Android App (Full Guide)

### Prerequisites Checklist
- [ ] Android Studio installed
- [ ] Android SDK (API 21+) installed
- [ ] NDK 23.x installed
- [ ] OpenCV Android SDK 4.8.0 downloaded
- [ ] Physical Android device OR Android Emulator
- [ ] USB Debugging enabled on device

### Step 1: Setup Configuration

**Create `local.properties` file** in project root:

```properties
sdk.dir=/path/to/Android/Sdk
ndk.dir=/path/to/Android/Sdk/ndk/23.1.7779620
opencv.dir=/path/to/opencv-android-sdk
```

#### For Different Operating Systems:

**Windows**:
```properties
sdk.dir=C:\Users\YourName\AppData\Local\Android\Sdk
ndk.dir=C:\Users\YourName\AppData\Local\Android\Sdk\ndk\23.1.7779620
opencv.dir=C:\path\to\opencv-android-sdk
```

**macOS**:
```properties
sdk.dir=/Users/YourName/Library/Android/sdk
ndk.dir=/Users/YourName/Library/Android/sdk/ndk/23.1.7779620
opencv.dir=/Users/YourName/opencv-android-sdk
```

**Linux**:
```properties
sdk.dir=/home/YourName/Android/Sdk
ndk.dir=/home/YourName/Android/Sdk/ndk/23.1.7779620
opencv.dir=/home/YourName/opencv-android-sdk
```

### Step 2: Download OpenCV Android SDK

```bash
# Visit: https://opencv.org/releases
# Download: OpenCV Android SDK 4.8.0
# Extract to a convenient location
# Note the path for local.properties
```

### Step 3: Connect Android Device

**For Physical Device**:
```bash
# Enable USB Debugging on device:
# Settings → Developer Options → USB Debugging → Enable

# Connect device via USB
# Verify connection:
adb devices
# Should show your device listed
```

**For Emulator**:
```bash
# Android Studio → AVD Manager
# Create or select emulator
# Launch emulator
```

### Step 4: Build and Run

**Option 1: Using Gradle Command Line**
```bash
# From project root directory

# Clean build
./gradlew clean build

# Install on device
./gradlew installDebug

# Run app
./gradlew run
```

**Option 2: Using Android Studio**
```
File → Open → Select project root
Build → Make Project
Run → Run 'app'
Or: Shift+F10
```

**Option 3: Full Build & Install (One Command)**
```bash
./gradlew clean build installDebug run
```

### Step 5: Verify Installation

✅ App should:
1. Launch on device/emulator
2. Request camera permission (tap "Allow")
3. Show black GLSurfaceView
4. Display stats (FPS, processing time)
5. Show "Mode: CANNY" button

### Step 6: Test Features

**Try these**:
```
1. Point camera at objects
2. Watch frame display
3. Tap mode button to toggle processing
4. Check FPS counter updates
5. Verify edge detection works
```

---

## 🌐 Running the Web Viewer

### Prerequisites Checklist
- [ ] Node.js 16+ installed
- [ ] npm or yarn installed
- [ ] TypeScript compiler (installed via npm)

### Step 1: Check Node.js Installation

```bash
node --version
# Should show: v16.0.0 or higher

npm --version
# Should show: 8.0.0 or higher
```

### Step 2: Install Dependencies

```bash
cd web

npm install
# Downloads TypeScript and dependencies
# Creates node_modules folder
```

### Step 3: Compile TypeScript

```bash
# One-time build
npm run build

# Or watch for changes
npm run watch
```

**Output**:
- Creates `dist/main.js` and `dist/FrameViewer.js`

### Step 4: Serve Web Pages

**Option 1: Using npm (Recommended)**
```bash
npm run serve
# Opens at http://localhost:8080
```

**Option 2: Using Python 3**
```bash
# From web directory
python3 -m http.server 8000
# Opens at http://localhost:8000
```

**Option 3: Using PHP**
```bash
php -S localhost:8000
# Opens at http://localhost:8000
```

**Option 4: Using Node.js http-server**
```bash
npm install -g http-server
http-server dist -p 8080
# Opens at http://localhost:8080
```

### Step 5: Open in Browser

```
Open browser to: http://localhost:8000 (or 8080)
```

### Step 6: Verify Web Viewer

✅ You should see:
1. Canvas with green-edged frame (sample)
2. Buttons: Start Stream, Stop Stream, Export Frame
3. FPS: 24 counter
4. Processing time display
5. Export button saves PNG file

---

## 🔄 Full Workflow (Both Android + Web)

### In Terminal 1 (Android App):
```bash
cd /path/to/EdgeDetectionViewer

# Setup local.properties with your paths
# Configure Android device (USB Debugging ON)

./gradlew clean build installDebug run
# Wait for app to launch on device
```

### In Terminal 2 (Web Viewer):
```bash
cd /path/to/EdgeDetectionViewer/web

npm install
npm run build
npm run serve

# Opens web viewer at http://localhost:8080
```

### Result:
- Android app running on device capturing and processing frames
- Web viewer running in browser showing sample processed frame
- Both demonstrating the complete pipeline

---

## 📊 Troubleshooting Commands

### Android Build Issues

```bash
# Clean everything
./gradlew clean

# Rebuild from scratch
./gradlew clean build

# Check dependencies
./gradlew dependencies

# View detailed build output
./gradlew clean build --info

# Force rebuild native code
./gradlew clean build --recompile-all-native
```

### Check Device Connection
```bash
# List connected devices
adb devices

# Check logs from app
adb logcat | grep EdgeDetection

# Clear app data
adb shell pm clear com.example.edgedetection
```

### Web Viewer Build Issues

```bash
# Clean npm cache
npm cache clean --force

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install

# Check TypeScript version
npx tsc --version

# Compile with verbose output
npx tsc --listFiles
```

---

## 🎬 Expected Output

### Android App (On Device Screen)
```
┌──────────────────────────────────┐
│                                  │
│     [OpenGL Frame Display]       │
│     [Green edges on black]       │
│                                  │
│  ┌────────────────────────────┐  │
│  │ FPS: 24                    │  │
│  │ Processing: 15.50 ms       │  │
│  │ Mode: CANNY                │  │
│  │ [Mode: CANNY] Button       │  │
│  └────────────────────────────┘  │
│                                  │
└──────────────────────────────────┘
```

### Web Viewer (In Browser)
```
┌─────────────────────────────────────────────┐
│  🎬 Edge Detection Viewer                   │
├─────────────────────────────────────────────┤
│                                             │
│  ┌─────────────────────────────────────┐   │
│  │   Canvas with green-edged frame     │   │
│  │   FPS: 24                           │   │
│  │   Processing: 15.50ms               │   │
│  │   Resolution: 640x480               │   │
│  └─────────────────────────────────────┘   │
│                                             │
│  [▶ Start] [⏹ Stop] [💾 Export]           │
│  Refresh: [1000 ms]                        │
│                                             │
└─────────────────────────────────────────────┘
```

---

## ⚡ Common Issues & Solutions

### Issue: "SDK not found"
```bash
# Solution: Check local.properties

# On Windows:
type local.properties

# On macOS/Linux:
cat local.properties

# Should show correct paths
```

### Issue: "NDK not found"
```bash
# Solution: Install NDK via SDK Manager

# Or set in local.properties:
ndk.dir=C:\Users\Name\AppData\Local\Android\Sdk\ndk\23.1.7779620
```

### Issue: "OpenCV library not found"
```bash
# Solution: Verify OpenCV SDK location

# Check if this exists:
# Windows: C:\path\to\opencv-android-sdk\modules
# macOS: /path/to/opencv-android-sdk/modules
# Linux: /path/to/opencv-android-sdk/modules

# Update opencv.dir in local.properties
```

### Issue: "Camera permission denied"
```bash
# Solution: Grant permission on device

# Device: Settings → Apps → Edge Detection → Permissions → Camera → Allow

# Or command line:
adb shell pm grant com.example.edgedetection android.permission.CAMERA
```

### Issue: "Cannot find npm"
```bash
# Solution: Ensure Node.js is installed

# Download from: https://nodejs.org/
# Verify:
npm --version
node --version
```

### Issue: "Port 8000/8080 already in use"
```bash
# Kill process using port 8000:
# Windows:
netstat -ano | findstr :8000
taskkill /PID <PID> /F

# macOS/Linux:
lsof -i :8000
kill -9 <PID>

# Or use different port:
python3 -m http.server 8888
```

---

## 🔍 Monitoring & Debugging

### Android App Logs

```bash
# Real-time logs from app
adb logcat -s EdgeDetection

# Save logs to file
adb logcat -s EdgeDetection > logs.txt

# Grep for errors
adb logcat | grep -i "error\|exception"

# Filter by tag
adb logcat tag:EdgeDetection-JNI
```

### Web Viewer Debug

```bash
# Open browser DevTools
F12 or Ctrl+Shift+I

# Check Console tab for errors
# Check Network tab for API calls
# Use TypeScript source maps (generated in dist/)
```

---

## ✅ Verification Checklist

### Android App
- [ ] Build completes successfully: `BUILD SUCCESSFUL`
- [ ] App installs: `com.example.edgedetection installed`
- [ ] App launches on device/emulator
- [ ] Camera permission dialog appears
- [ ] Camera frame displays in OpenGL view
- [ ] FPS counter shows numbers
- [ ] Mode button toggles between modes
- [ ] Edge detection is visible
- [ ] No errors in logcat

### Web Viewer
- [ ] npm install completes: `up to date, X packages`
- [ ] TypeScript compiles: `Successfully compiled`
- [ ] Server starts: `Listening on http://localhost:8080`
- [ ] Browser loads without errors
- [ ] Canvas displays sample frame
- [ ] FPS counter shows 24
- [ ] Export button downloads PNG
- [ ] Responsive design works on mobile

---

## 📚 Additional Resources

- **Android Studio Docs**: https://developer.android.com/docs
- **OpenCV Android**: https://docs.opencv.org/4.8.0/d5/d3d/classcv_1_1Mat.html
- **NDK Build System**: https://developer.android.com/ndk/guides/cmake
- **TypeScript Handbook**: https://www.typescriptlang.org/docs/
- **OpenGL ES**: https://developer.android.com/guide/topics/graphics/opengl

---

## 🎯 Quick Command Reference

| Task | Command |
|------|---------|
| Build Android | `./gradlew clean build` |
| Install Android | `./gradlew installDebug` |
| Run Android | `./gradlew run` |
| Build + Install + Run | `./gradlew clean build installDebug run` |
| Check device | `adb devices` |
| View logs | `adb logcat -s EdgeDetection` |
| Install web deps | `cd web && npm install` |
| Build web | `npm run build` |
| Serve web | `npm run serve` |
| View on web | Open `http://localhost:8000` in browser |

---

**Status**: Ready to Run ✅
**Last Updated**: November 13, 2024
