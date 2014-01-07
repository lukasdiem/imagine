/*
 * ImImage.cpp
 *
 *  Created on: 07.01.2014
 *      Author: Lukas
 */

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>

#include <stdlib.h>
#include <math.h>

namespace ld {
	struct CanvasSettings {
		int width = -1;
		int height = -1;
		int imagePosX = 0;
		int imagePosY = 0;
		float imageScale = 1.0f;
	};

	class ImImage {
	protected:
		cv::Mat origImage;
		cv::Mat procImage;
		cv::Mat curveLut;

		float actBrightness;

		CanvasSettings canvasSettings;

	public:
		void loadImage(const std::string path);

		// Processing functions
		void changeBrightness(double brightness);
		void changeContrast(double contrast);
	};

	void ImImage::loadImage(const std::string path) {
		origImage = cv::imread(path);

		if (!origImage.data) {
			// TODO: Handle image not loaded!
		}

		// initialize the lut
		curveLut = cv::Mat(1, 256, CV_8U);
	}

	void ImImage::changeBrightness(double brightness) {
		origImage.convertTo(procImage, -1, 1.0, brightness);
	}

	void ImImage::changeContrast(double contrast) {
		origImage.convertTo(procImage, -1, contrast, 0.0);
	}
};


