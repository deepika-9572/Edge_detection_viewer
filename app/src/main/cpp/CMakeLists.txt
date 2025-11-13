cmake_minimum_required(VERSION 3.22.1)
project(EdgeDetection)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find OpenCV
find_package(OpenCV REQUIRED)

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Add native library
add_library(native-lib SHARED
    native-lib.cpp
    ImageProcessor.cpp
)

# Link OpenCV
target_link_libraries(native-lib ${OpenCV_LIBS})
