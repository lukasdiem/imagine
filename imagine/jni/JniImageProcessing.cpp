/*
 * ImImageProcessing.cpp
 *
 *  Created on: 26.11.2013
 *      Author: Lukas
 */

#include "JniImageProcessing.h"

#include <opencv2/core/core.hpp>
#include <stdlib.h>
#include <math.h>
//#include <opencv2/core/mat.hpp>

//using namespace std;
//using namespace cv;

/*extern "C" {
	JNIEXPORT void JNICALL Java_test_JniTest_brightnessContrast (JNIEnv * jenv, jobject thiz, jint contrast, jint brightness);
	JNIEXPORT void JNICALL Java_test_JniTest_helloJNI(JNIEnv * jenv, jobject thiz);
}*/

JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_brightnessContrast
(JNIEnv * jenv, jobject thiz, jlong addrSrcMat, jlong addrDstMat, jfloat jcontrast, jfloat jbrightness)
{
	//LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeCreateObject enter");
	//LOGD(LOG_TAG, std::string("Params, brightness: ") + std::string(brightness) + std::string(", contrast: ") + std::string(contrast));

	// Get the image
	cv::Mat& srcMat = *(cv::Mat*)addrSrcMat;
	cv::Mat& dstMat = *(cv::Mat*)addrDstMat;

	// Adjust brightness and contrast!
	dstMat = srcMat * (float)jcontrast + cv::Scalar(jbrightness, jbrightness, jbrightness, 0);

	/*for( y = 0; y < nRows*nCols; ++y) {
		pixelVal = saturate_cast<uchar>(((float)p[y] * contrast + brightness));

		/*if (pixelVal < 255.0f) {
			p[y] = (uchar)pixelVal;
		} else if (pixelVal < 0.0f) {
			p[y] = (uchar)0;
		} else {
			p[y] = (uchar)255;
		}*/
	//}

	/*
	for( y = 0; y < nRows; ++y) {
		p = imageMat.ptr<uchar>(y);

		for ( x = 0; x < nCols; ++x) {
			pixelVal = ((float)p[x] * contrast + brightness);

			//p[x] = (uchar)pixelVal;
			if (pixelVal < 255.0f) {
				p[x] = (uchar)pixelVal;
			} else if (pixelVal < 0.0f) {
				p[x] = (uchar)0;
			} else {
				p[x] = (uchar)255;
			}

		}
	}*/

	//return 0;
}

JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_image_ImJniImageProcessing_helloJNI(JNIEnv * jenv, jobject thiz) {
	LOGD("Hello JNI!");
}


