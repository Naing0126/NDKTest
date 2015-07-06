LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED

include C:/cygwin64/home/User/OpenCV-2.4.11-android-sdk/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := main.cpp
LOCAL_LDLIBS += -llog

LOCAL_MODULE    := NDKTest

include $(BUILD_SHARED_LIBRARY)
