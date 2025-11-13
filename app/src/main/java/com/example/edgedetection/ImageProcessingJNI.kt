package com.example.edgedetection

object ImageProcessingJNI {
    init {
        System.loadLibrary("native-lib")
    }

    // JNI Methods
    external fun initProcessor()

    external fun processFrameCanny(
        inputData: ByteArray,
        width: Int,
        height: Int,
        threshold1: Int = 50,
        threshold2: Int = 150
    ): ByteArray?

    external fun processFrameGrayscale(
        inputData: ByteArray,
        width: Int,
        height: Int
    ): ByteArray?

    external fun getLastProcessingTime(): Double
}
