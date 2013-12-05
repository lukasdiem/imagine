LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

# include the OpenCV Library
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC

include C:\MyPrograms\Programmieren\OpenCV-2.4.6-android-sdk\sdk\native\jni\OpenCV.mk

LOCAL_MODULE    := imageprocessing
#LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := JniImageProcessing.cpp
LOCAL_LDLIBS    += -llog

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

# include the OpenCV Library
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
OPENCV_LIB_TYPE:=STATIC

include C:\MyPrograms\Programmieren\OpenCV-2.4.6-android-sdk\sdk\native\jni\OpenCV.mk

LOCAL_MODULE    := openglrenderer
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := JniOpenGlImageRenderer.cpp
LOCAL_LDLIBS    += -llog -lGLESv2

include $(BUILD_SHARED_LIBRARY)