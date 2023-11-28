project(ncnnyolov7)

cmake_minimum_required(VERSION 3.10)

#set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-2.4.13.7-android/sdk/native/jni)
#find_package(OpenCV REQUIRED core imgproc)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20210525-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

add_library(ncnnyolov7 SHARED yoloncnn.cpp yolo.cpp)

target_link_libraries(ncnnyolov7 ncnn jnigraphics)
