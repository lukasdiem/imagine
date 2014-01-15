#pragma once

#include <jni.h>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <string>

#define LOG_TAG "Imagine/NativeImageProcessing"
#define  LOGD(...)  ((void)__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__))
//#define  LOGI(...)  ((void)__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__))
//#define  LOGW(...)  ((void)__android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__))
//#define  LOGE(...)  ((void)__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__))

namespace ld {
	void cartoonize(const cv::Mat& srcImg, cv::Mat& dstImg, int colorCount, float edgeWeight, int edgeThickness);
	void edgeDetection(const cv::Mat& srcImg, cv::Mat& dstImg, int thickness=0);
	void sepiaEffect(const cv::Mat& srcImg, cv::Mat& dstImg);
	void loadImage(const char *imagePath, cv::Mat& dstImg, int width, int height, float rotation);
}

extern "C" {
	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_cartoonize (JNIEnv *jenv, jobject thiz, jlong srcImg, jlong dstImg, jint colorCount, jfloat edgeWeight, jint edgeThickness) {
		ld::cartoonize(*(cv::Mat*)srcImg, *(cv::Mat*)dstImg, (int)colorCount, (float)edgeWeight, (int)edgeThickness);
	}

	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_edgeEffect (JNIEnv *jenv, jobject thiz, jlong srcImg, jlong dstImg, jint edgeThickness) {
		ld::edgeDetection(*(cv::Mat*)srcImg, *(cv::Mat*)dstImg, (int)edgeThickness);
	}

	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_sepiaEffect (JNIEnv *jenv, jobject thiz, jlong srcImg, jlong dstImg) {
		ld::sepiaEffect(*(cv::Mat*)srcImg, *(cv::Mat*)dstImg);
	}

	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_loadImage (JNIEnv *jenv, jobject thiz, jstring imagePath, jlong dstImg, jint width, jint height, jfloat rotation) {
		const char *nativePath = jenv->GetStringUTFChars(imagePath, JNI_FALSE);
		ld::loadImage(nativePath, *(cv::Mat*)dstImg, (int) width, (int) height, (float) rotation);

		jenv->ReleaseStringUTFChars(imagePath, nativePath);
	}
}
