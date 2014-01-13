#pragma once

#include <jni.h>
#include <android/log.h>
#include <opencv2/core/core.hpp>

#define LOG_TAG "Imagine/NativeImageProcessing"
#define  LOGD(...)  ((void)__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__))
//#define  LOGI(...)  ((void)__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__))
//#define  LOGW(...)  ((void)__android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__))
//#define  LOGE(...)  ((void)__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__))

namespace ld {
	void cartoonize(const cv::Mat& srcImg, cv::Mat& dstImg, int colorCount, float edgeWeight, int edgeThickness);
	void edgeDetection(const cv::Mat& srcImg, cv::Mat& dstImg, int thickness=0);
}

extern "C" {
	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_cartoonize (JNIEnv * jenv, jobject thiz, jlong srcImg, jlong dstImg, jint colorCount, jfloat edgeWeight, jint edgeThickness) {
		ld::cartoonize(*(cv::Mat*)srcImg, *(cv::Mat*)dstImg, (int)colorCount, (float)edgeWeight, (int)edgeThickness);
	}
}
