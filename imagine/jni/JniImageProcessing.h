#include <jni.h>
#include <android/log.h>

#define LOG_TAG "Imagine/NativeImageProcessing"
#define  LOGD(...)  ((void)__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__))
//#define  LOGI(...)  ((void)__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__))
//#define  LOGW(...)  ((void)__android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__))
//#define  LOGE(...)  ((void)__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__))

extern "C" {
	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_brightnessContrast
		(JNIEnv * jenv, jobject thiz, jlong addrSrcMat, jlong addrDstMat, jfloat jcontrast, jfloat jbrightness);
	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_helloJNI (JNIEnv * jenv, jobject thiz);
}
