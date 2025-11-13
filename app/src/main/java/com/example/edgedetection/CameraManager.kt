package com.example.edgedetection

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.os.Handler
import android.os.HandlerThread
import android.util.SparseIntArray
import android.view.Surface
import androidx.annotation.RequiresApi
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class CameraManager(private val context: Context) {
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraId: String? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val cameraOpenCloseLock = Semaphore(1)

    private var frameCallback: ((Image) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null

    fun startCamera(frameCallback: (Image) -> Unit, errorCallback: (String) -> Unit) {
        this.frameCallback = frameCallback
        this.errorCallback = errorCallback

        startBackgroundThread()
        openCamera()
    }

    fun stopCamera() {
        closeCamera()
        stopBackgroundThread()
    }

    private fun openCamera() {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
        try {
            if (cameraManager != null) {
                cameraId = cameraManager.cameraIdList.firstOrNull()
                if (cameraId != null) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId!!)
                    cameraOpenCloseLock.acquire()

                    cameraManager.openCamera(cameraId!!, object : CameraDevice.StateCallback() {
                        override fun onOpened(camera: CameraDevice) {
                            cameraOpenCloseLock.release()
                            cameraDevice = camera
                            createCaptureSession()
                        }

                        override fun onDisconnected(camera: CameraDevice) {
                            cameraOpenCloseLock.release()
                            camera.close()
                        }

                        override fun onError(camera: CameraDevice, error: Int) {
                            cameraOpenCloseLock.release()
                            errorCallback?.invoke("Camera error: $error")
                        }
                    }, backgroundHandler)
                } else {
                    errorCallback?.invoke("No camera available")
                }
            }
        } catch (e: Exception) {
            errorCallback?.invoke("Error opening camera: ${e.message}")
        }
    }

    private fun createCaptureSession() {
        try {
            val device = cameraDevice ?: return
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val characteristics = cameraManager.getCameraCharacteristics(cameraId!!)

            val configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (configMap != null) {
                val outputSizes = configMap.getOutputSizes(ImageFormat.YUV_420_888)
                if (outputSizes.isNotEmpty()) {
                    val previewSize = outputSizes[0]

                    val imageReader = android.media.ImageReader.newInstance(
                        previewSize.width,
                        previewSize.height,
                        ImageFormat.YUV_420_888,
                        2
                    )

                    imageReader.setOnImageAvailableListener({ reader ->
                        val image = reader.acquireLatestImage()
                        if (image != null) {
                            frameCallback?.invoke(image)
                            image.close()
                        }
                    }, backgroundHandler)

                    val surface = imageReader.surface

                    val captureRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequestBuilder.addTarget(surface)

                    device.createCaptureSession(
                        listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                captureSession = session
                                try {
                                    captureRequestBuilder.set(
                                        CaptureRequest.CONTROL_AE_MODE,
                                        CameraMetadata.CONTROL_AE_MODE_ON
                                    )
                                    session.setRepeatingRequest(
                                        captureRequestBuilder.build(),
                                        null,
                                        backgroundHandler
                                    )
                                } catch (e: CameraAccessException) {
                                    errorCallback?.invoke("Camera access error: ${e.message}")
                                }
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                errorCallback?.invoke("Capture session configuration failed")
                            }
                        },
                        backgroundHandler
                    )
                }
            }
        } catch (e: Exception) {
            errorCallback?.invoke("Error creating capture session: ${e.message}")
        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            try {
                captureSession?.close()
                captureSession = null

                cameraDevice?.close()
                cameraDevice = null
            } finally {
                cameraOpenCloseLock.release()
            }
        } catch (e: InterruptedException) {
            errorCallback?.invoke("Interrupted while closing camera")
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").apply {
            start()
            backgroundHandler = Handler(looper)
        }
    }

    private fun stopBackgroundThread() {
        try {
            backgroundThread?.quitSafely()
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            errorCallback?.invoke("Interrupted while stopping background thread")
        }
    }
}
