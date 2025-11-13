package com.example.edgedetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.opengl.GLSurfaceView
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private lateinit var cameraManager: CameraManager
    private lateinit var statsTextView: TextView
    private lateinit var toggleButton: Button

    private var frameCount = 0
    private var processingMode = ProcessingMode.CANNY
    private var lastFrameTime = System.currentTimeMillis()
    private var fps = 0
    private val isProcessing = AtomicBoolean(false)

    enum class ProcessingMode {
        RAW, CANNY, GRAYSCALE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the JNI processor
        ImageProcessingJNI.initProcessor()

        // Request permissions
        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            setupUI()
        }
    }

    private fun setupUI() {
        // Setup GLSurfaceView
        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glRenderer = GLRenderer()
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // Setup stats textview
        statsTextView = findViewById(R.id.statsTextView)

        // Setup toggle button
        toggleButton = findViewById(R.id.toggleButton)
        toggleButton.setOnClickListener {
            processingMode = when (processingMode) {
                ProcessingMode.CANNY -> ProcessingMode.GRAYSCALE
                ProcessingMode.GRAYSCALE -> ProcessingMode.RAW
                ProcessingMode.RAW -> ProcessingMode.CANNY
            }
            toggleButton.text = "Mode: ${processingMode.name}"
        }

        // Initialize camera
        cameraManager = CameraManager(this)
        cameraManager.startCamera(
            frameCallback = { image -> processFrame(image) },
            errorCallback = { error -> showError(error) }
        )
    }

    private fun processFrame(image: Image) {
        if (isProcessing.getAndSet(true)) {
            return  // Skip if already processing
        }

        try {
            // Convert Image to Bitmap
            val bitmap = imageToBitmap(image)

            // Process based on mode
            val processedBitmap = when (processingMode) {
                ProcessingMode.RAW -> bitmap
                ProcessingMode.CANNY -> {
                    val bitmapData = bitmapToByteArray(bitmap)
                    val processed = ImageProcessingJNI.processFrameCanny(
                        bitmapData,
                        bitmap.width,
                        bitmap.height,
                        50,
                        150
                    )
                    if (processed != null) {
                        byteArrayToBitmap(processed, bitmap.width, bitmap.height)
                    } else {
                        bitmap
                    }
                }
                ProcessingMode.GRAYSCALE -> {
                    val bitmapData = bitmapToByteArray(bitmap)
                    val processed = ImageProcessingJNI.processFrameGrayscale(
                        bitmapData,
                        bitmap.width,
                        bitmap.height
                    )
                    if (processed != null) {
                        byteArrayToBitmap(processed, bitmap.width, bitmap.height)
                    } else {
                        bitmap
                    }
                }
            }

            // Update GL renderer
            glRenderer.updateFrame(processedBitmap)

            // Update FPS counter
            frameCount++
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastFrameTime >= 1000) {
                fps = frameCount
                frameCount = 0
                lastFrameTime = currentTime
                updateStats()
            }
        } catch (e: Exception) {
            showError("Error processing frame: ${e.message}")
        } finally {
            isProcessing.set(false)
        }
    }

    private fun updateStats() {
        runOnUiThread {
            val processingTime = ImageProcessingJNI.getLastProcessingTime()
            statsTextView.text = """
                FPS: $fps
                Processing Time: ${String.format("%.2f", processingTime)} ms
                Mode: ${processingMode.name}
            """.trimIndent()
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.buffer.get(nv21, 0, ySize)
        uPlane.buffer.get(nv21, ySize, uSize)
        vPlane.buffer.get(nv21, ySize + uSize, vSize)

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val size = bitmap.byteCount
        val buffer = java.nio.ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(buffer)
        return buffer.array()
    }

    private fun byteArrayToBitmap(data: ByteArray, width: Int, height: Int): Bitmap {
        val buffer = java.nio.ByteBuffer.wrap(data)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun showError(message: String) {
        runOnUiThread {
            statsTextView.text = "Error: $message"
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setupUI()
            } else {
                showError("Permissions not granted")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.stopCamera()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
