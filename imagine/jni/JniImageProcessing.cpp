/*
 * ImImageProcessing.cpp
 *
 *  Created on: 26.11.2013
 *      Author: Lukas
 */

#include "JniImageProcessing.h"

#include <jni.h>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <stdlib.h>
#include <math.h>
#include <limits>
//#include <opencv2/core/mat.hpp>

namespace ld {

/*float calculateDownsampleFactor(int fullWidth, int fullHeight, int reqWidth, int reqHeight) {
	float downsampleFactor = 1.0f;

	if (fullHeight > reqHeight || fullWidth > reqWidth) {
		// Calculate the largest inSampleSize value that is a power of 2 and keeps both
		// height and width larger than the requested height and width.
		while ( fullHeight * downsampleFactor > reqHeight
				&& fullWidth * downsampleFactor > reqWidth) {
			downsampleFactor /= 2.0f;
		}
	}

	return downsampleFactor;
}*/


void loadImage(const char *imagePath, cv::Mat& dstImg, int width, int height, float rotation) {
	cv::Mat fullSizeImg = cv::imread(imagePath);

	if (!(fullSizeImg.type() == CV_8UC3)) {
		fullSizeImg.convertTo(fullSizeImg, CV_8UC3);
	}

	if (rotation != 0) {
		cv::Point2f fullSizeImgCenter(fullSizeImg.cols/2.0f, fullSizeImg.rows/2.0f);
		cv::Mat rotMat = cv::getRotationMatrix2D(fullSizeImgCenter, rotation, 1.0);

		if (rotation == 180) {
			cv::warpAffine(fullSizeImg, fullSizeImg, rotMat, fullSizeImg.size());
		} else if (rotation == 90 || rotation == 270) {
			// Change height/width
			cv::warpAffine(fullSizeImg, fullSizeImg, rotMat, cv::Size(fullSizeImg.cols, fullSizeImg.rows));
		}

		rotMat.release();
	}

	if (!(fullSizeImg.cols == width && fullSizeImg.rows == height)) {
		// Resize the image to the proper size
		if (rotation == 90 || rotation == 270) {
			// change height and width
			cv::resize(fullSizeImg, dstImg, cv::Size(height, width), 0, 0, cv::INTER_NEAREST);
		} else {
			cv::resize(fullSizeImg, dstImg, cv::Size(width, height), 0, 0, cv::INTER_NEAREST);
		}
	}

	cv::cvtColor(dstImg, dstImg, cv::COLOR_BGR2RGB);

	fullSizeImg.release();
}

void sepiaEffect(const cv::Mat& srcImg, cv::Mat& dstImg) {
	cv::Mat tmpMat(srcImg.size(), CV_32FC3);
	srcImg.convertTo(tmpMat, CV_32FC3);

	cv::Mat sepiaKernel = (cv::Mat_<float>(3,3)  << 0.393, 0.769, 0.189,
													0.349, 0.686, 0.168,
												    0.272, 0.534, 0.131);

	cv::transform(tmpMat, tmpMat, sepiaKernel);
	tmpMat.convertTo(dstImg, CV_8UC3);
	tmpMat.release();
}

void edgeDetection(const cv::Mat& srcImg, cv::Mat& dstImg, int thickness) {
	cv::Mat grayscale(srcImg.rows, srcImg.cols, CV_8UC1);
	cv::Mat gradX, gradY;

	cv::cvtColor(srcImg, grayscale, cv::COLOR_RGB2GRAY);
	cv::GaussianBlur(grayscale, grayscale, cv::Size(3, 3), 0, 0, cv::BORDER_DEFAULT);

	cv::Sobel(grayscale, gradX, CV_16S, 1, 0, 3, 1, 0);
	cv::convertScaleAbs(gradX, gradX);

	cv::Sobel(grayscale, gradY, CV_16S, 0, 1, 3, 1, 0);
	cv::convertScaleAbs(gradY, gradY);

	cv::addWeighted(gradX, 0.5, gradY, 0.5, 0, dstImg);

	if (thickness > 0) {
		cv::Mat element = cv::getStructuringElement(cv::MORPH_ELLIPSE,
		                                       	    cv::Size(2*thickness + 1, 2*thickness+1),
		                                       	    cv::Point(thickness, thickness));
		cv::dilate(dstImg, dstImg, element);
	}

	gradX.release();
	gradY.release();
	grayscale.release();
}

void cartoonize(const cv::Mat& srcImg, cv::Mat& dstImg, int colorCount, float edgeWeight, int edgeThickness) {
	int attempts = 1;
	cv::Mat labels, centers;
	cv::TermCriteria criteria(cv::TermCriteria::EPS + cv::TermCriteria::MAX_ITER, 5000, 0.001);

	int width = srcImg.cols;
	int height = srcImg.rows;
	int nChannels = srcImg.channels();

	int smallWidth = 50;
	int smallHeight = smallWidth * srcImg.rows / srcImg.cols;

	cv::Mat smallImg(smallHeight, smallWidth, CV_8UC3);
	cv::resize(srcImg, smallImg, cv::Size(smallWidth, smallHeight), 0, 0, cv::INTER_NEAREST);

	cv::Mat quantInput = smallImg.reshape(1, smallWidth * smallHeight);
	quantInput.convertTo(quantInput, CV_32FC1);
	cv::kmeans(quantInput, colorCount, labels, criteria, attempts, cv::KMEANS_PP_CENTERS, centers);

	// Convert the centers to the correct image format, otherwise the distance measure does not work
	centers.convertTo(centers, CV_8UC1);

	int row, col, clust, minColDistIdx;
	float minColDist = std::numeric_limits<float>::infinity();
	float actDist;
	uchar* dstImgPtr;
	uchar* clustPtr; // = centers.data;
	const uchar* srcImgPtr;

	for(row = 0; row < height; row++) {
		dstImgPtr = dstImg.ptr<uchar>(row);
		srcImgPtr = srcImg.ptr<uchar>(row);

		for (col = 0; col < width * nChannels; col += 3) {
			for (clust = 0; clust < colorCount; clust ++) {
				clustPtr = centers.ptr<uchar>(clust);

				actDist = std::abs(srcImgPtr[col] - clustPtr[0]) +
						  std::abs(srcImgPtr[col+1] - clustPtr[1]) +
						  std::abs(srcImgPtr[col+2] - clustPtr[2]);

				if (actDist < minColDist) {
					minColDist = actDist;
					minColDistIdx = clust;
				}
 			}

			clustPtr = centers.ptr<uchar>(minColDistIdx);
			dstImgPtr[col] 	 = clustPtr[0];
			dstImgPtr[col+1] = clustPtr[1];
			dstImgPtr[col+2] = clustPtr[2];

			minColDist = std::numeric_limits<float>::infinity();
		}
	}

	// Blur the quantized image a little bit (better look)
	cv::GaussianBlur(dstImg, dstImg, cv::Size(3,3), 0, 0, cv::BORDER_DEFAULT);

	if (edgeWeight != 0) {
		// Enhance the edges if this is wanted
		cv::Mat edgeImg(height, width, CV_8UC1);
		edgeDetection(srcImg, edgeImg, edgeThickness);

		cvtColor(edgeImg, edgeImg, CV_GRAY2RGB);
		cv::addWeighted(dstImg, 1.0, edgeImg, edgeWeight, 0, dstImg);
	}
}
}



