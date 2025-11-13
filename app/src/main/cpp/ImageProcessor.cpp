#include "ImageProcessor.h"
#include <opencv2/imgproc/imgproc.hpp>
#include <chrono>
#include <android/log.h>

#define LOG_TAG "EdgeDetection"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

ImageProcessor::ImageProcessor()
    : lastProcessingTime(0), frameWidth(0), frameHeight(0) {
    LOGI("ImageProcessor initialized");
}

ImageProcessor::~ImageProcessor() {
    LOGI("ImageProcessor destroyed");
}

cv::Mat ImageProcessor::processFrameCanny(const cv::Mat& inputFrame, int threshold1, int threshold2) {
    auto start = std::chrono::high_resolution_clock::now();

    try {
        // Store frame dimensions
        frameWidth = inputFrame.cols;
        frameHeight = inputFrame.rows;

        // Convert to grayscale if needed
        cv::Mat gray;
        if (inputFrame.channels() == 4) {
            cv::cvtColor(inputFrame, gray, cv::COLOR_RGBA2GRAY);
        } else if (inputFrame.channels() == 3) {
            cv::cvtColor(inputFrame, gray, cv::COLOR_RGB2GRAY);
        } else {
            gray = inputFrame.clone();
        }

        // Apply Gaussian blur to reduce noise
        cv::Mat blurred;
        cv::GaussianBlur(gray, blurred, cv::Size(5, 5), 1.5);

        // Apply Canny edge detection
        cv::Mat edges;
        cv::Canny(blurred, edges, threshold1, threshold2);

        // Convert back to 3 channels for display
        cv::Mat result;
        cv::cvtColor(edges, result, cv::COLOR_GRAY2BGR);

        auto end = std::chrono::high_resolution_clock::now();
        lastProcessingTime = std::chrono::duration<double, std::milli>(end - start).count();

        LOGI("Frame processed: %dx%d, Time: %.2f ms", frameWidth, frameHeight, lastProcessingTime);

        return result;
    } catch (const std::exception& e) {
        LOGE("Error in processFrameCanny: %s", e.what());
        return inputFrame.clone();
    }
}

cv::Mat ImageProcessor::processFrameGrayscale(const cv::Mat& inputFrame) {
    auto start = std::chrono::high_resolution_clock::now();

    try {
        frameWidth = inputFrame.cols;
        frameHeight = inputFrame.rows;

        cv::Mat gray;
        if (inputFrame.channels() == 4) {
            cv::cvtColor(inputFrame, gray, cv::COLOR_RGBA2GRAY);
        } else if (inputFrame.channels() == 3) {
            cv::cvtColor(inputFrame, gray, cv::COLOR_RGB2GRAY);
        } else {
            gray = inputFrame.clone();
        }

        // Convert to 3 channels for consistency
        cv::Mat result;
        cv::cvtColor(gray, result, cv::COLOR_GRAY2BGR);

        auto end = std::chrono::high_resolution_clock::now();
        lastProcessingTime = std::chrono::duration<double, std::milli>(end - start).count();

        return result;
    } catch (const std::exception& e) {
        LOGE("Error in processFrameGrayscale: %s", e.what());
        return inputFrame.clone();
    }
}

cv::Mat ImageProcessor::convertToOpenCVMat(const cv::Mat& inputFrame) {
    return inputFrame.clone();
}
