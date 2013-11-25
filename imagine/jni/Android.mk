LOCAL_PATH:= $(call my-dir)
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
#OPENCV_LIB_TYPE:=STATIC

include $(CLEAR_VARS)

# include the OpenCV Library
include C:\MyPrograms\Programmieren\OpenCV-2.4.6-android-sdk\sdk\native\jni\OpenCV.mk

LOCAL_MODULE    := libimageprocessing
#LOCAL_CFLAGS    := -Werror
#LOCAL_SRC_FILES := gl_code.cpp
LOCAL_SRC_FILES := hello-jni.c
#LOCAL_LDLIBS    := -llog -lGLESv2

include $(BUILD_SHARED_LIBRARY)