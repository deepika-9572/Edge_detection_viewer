#ifndef EDGE_DETECTION_IMAGE_PROCESSOR_H
#define EDGE_DETECTION_IMAGE_PROCESSOR_H

#include <opencv2/core/core.hpp>
#include <vector>

class ImageProcessor {
public:
    ImageProcessor();
    ~ImageProcessor();

    // Process frame with Canny edge detection
    cv::Mat processFrameCanny(const cv::Mat& inputFrame, int threshold1 = 50, int threshold2 = 150);

    // Convert to grayscale
    cv::Mat processFrameGrayscale(const cv::Mat& inputFrame);

    // Get processing time in milliseconds
    double getLastProcessingTime() const { return lastProcessingTime; }

    // Get frame statistics
    int getFrameWidth() const { return frameWidth; }
    int getFrameHeight() const { return frameHeight; }

private:
    double lastProcessingTime;
    int frameWidth;
    int frameHeight;

    // Helper functions
    cv::Mat convertToOpenCVMat(const cv::Mat& inputFrame);
};

#endif // EDGE_DETECTION_IMAGE_PROCESSOR_H
