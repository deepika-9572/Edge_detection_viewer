#include <jni.h>
#include <string>
#include <memory>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include "ImageProcessor.h"

#define LOG_TAG "EdgeDetection-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global image processor instance
static std::unique_ptr<ImageProcessor> gImageProcessor;

extern "C" {

    // Initialize the processor
    JNIEXPORT void JNICALL
    Java_com_example_edgedetection_ImageProcessingJNI_initProcessor(JNIEnv *env, jclass clazz) {
        LOGI("Initializing ImageProcessor");
        gImageProcessor = std::make_unique<ImageProcessor>();
    }

    // Process frame with Canny edge detection
    JNIEXPORT jbyteArray JNICALL
    Java_com_example_edgedetection_ImageProcessingJNI_processFrameCanny(
            JNIEnv *env, jclass clazz, jbyteArray inputData,
            jint width, jint height, jint threshold1, jint threshold2) {
        try {
            if (!gImageProcessor) {
                LOGE("ImageProcessor not initialized");
                return nullptr;
            }

            // Convert Java byte array to OpenCV Mat
            jbyte *inputBytes = env->GetByteArrayElements(inputData, nullptr);
            cv::Mat inputMat(height, width, CV_8UC4, inputBytes);

            // Process the frame
            cv::Mat processed = gImageProcessor->processFrameCanny(inputMat, threshold1, threshold2);

            // Convert back to byte array
            jbyteArray outputArray = env->NewByteArray(processed.total() * processed.channels());
            env->SetByteArrayRegion(outputArray, 0, processed.total() * processed.channels(),
                                    (jbyte *) processed.data);

            env->ReleaseByteArrayElements(inputData, inputBytes, JNI_ABORT);

            LOGI("Frame processed successfully: %dx%d", width, height);
            return outputArray;
        } catch (const std::exception &e) {
            LOGE("Error in processFrameCanny: %s", e.what());
            return nullptr;
        }
    }

    // Process frame with grayscale
    JNIEXPORT jbyteArray JNICALL
    Java_com_example_edgedetection_ImageProcessingJNI_processFrameGrayscale(
            JNIEnv *env, jclass clazz, jbyteArray inputData,
            jint width, jint height) {
        try {
            if (!gImageProcessor) {
                LOGE("ImageProcessor not initialized");
                return nullptr;
            }

            jbyte *inputBytes = env->GetByteArrayElements(inputData, nullptr);
            cv::Mat inputMat(height, width, CV_8UC4, inputBytes);

            cv::Mat processed = gImageProcessor->processFrameGrayscale(inputMat);

            jbyteArray outputArray = env->NewByteArray(processed.total() * processed.channels());
            env->SetByteArrayRegion(outputArray, 0, processed.total() * processed.channels(),
                                    (jbyte *) processed.data);

            env->ReleaseByteArrayElements(inputData, inputBytes, JNI_ABORT);

            return outputArray;
        } catch (const std::exception &e) {
            LOGE("Error in processFrameGrayscale: %s", e.what());
            return nullptr;
        }
    }

    // Get last processing time
    JNIEXPORT jdouble JNICALL
    Java_com_example_edgedetection_ImageProcessingJNI_getLastProcessingTime(
            JNIEnv *env, jclass clazz) {
        if (gImageProcessor) {
            return gImageProcessor->getLastProcessingTime();
        }
        return 0.0;
    }

}
