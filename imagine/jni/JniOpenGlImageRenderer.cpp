#include <jni.h>
#include <android/log.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <opencv2/core/core.hpp>

#define LOG_TAG "Imagine/NativeOpenGlRendering"
#define  LOGD(...)  ((void)__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__))

int frameWidth;
int frameHeight;

GLuint texture;
GLfloat textureCoord;

// Initialize the Screen filling quad to reder on
GLfloat vertices[] = {
	-1.0f, -1.0f, 0.0f, // V1 - bottom left
	-1.0f,  1.0f, 0.0f, // V2 - top left
	 1.0f, -1.0f, 0.0f, // V3 - bottom right
	 1.0f,  1.0f, 0.0f  // V4 - top right
};

cv::Mat renderMat;

void initOpenGl() {
	glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
	//glShadeModel(GL_SMOOTH);
	glClearDepthf(1.0f);
	//glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
}

void createTexture() {
	/*textureCoord[0] = ((1024.0f-frameWidth*1.0f)/2.0f)/1024.0f;
	textureCoord[1] = ((1024.0f-frameHeight*1.0f)/2.0f)/1024.0f + (frameHeight*1.0f/1024.0f);
	textureCoord[2] = ((1024.0f-frameWidth*1.0f)/2.0f)/1024.0f + (frameWidth*1.0f/1024.0f);
	textureCoord[3] = ((1024.0f-frameHeight*1.0f)/2.0f)/1024.0f + (frameHeight*1.0f/1024.0f);
	textureCoord[4] = ((1024.0f-frameWidth*1.0f)/2.0f)/1024.0f;
	textureCoord[5] = ((1024.0f-frameHeight*1.0f)/2.0f)/1024.0f;
	textureCoord[6] = ((1024.0f-frameWidth*1.0f)/2.0f)/1024.0f + (frameWidth*1.0f/1024.0f);
	textureCoord[7] = ((1024.0f-frameHeight*1.0f)/2.0f)/1024.0f;*/


	LOGD("Creating the texture ... ");

	glGenTextures(1, &texture);
	glBindTexture(GL_TEXTURE_2D, texture);

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

	// Set texture clamping method
	//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
	//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 1024, 1024, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
	glBindTexture(GL_TEXTURE_2D, 0);

	LOGD("Texture created!");
}


void resizeViewport(int newWidth, int newHeight)
{
	frameWidth = newWidth;
	frameHeight = newHeight;

	glViewport(0, 0, newWidth, newHeight);

	/*glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	float aspect = newWidth / newHeight;
	float bt = (float) tan(45 / 2);
	float lr = bt * aspect;
	glFrustumf(-lr * 0.1f, lr * 0.1f, -bt * 0.1f, bt * 0.1f, 0.1f, 100.0f);
	//glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
	glEnable(GL_TEXTURE_2D);
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	glClearDepthf(1.0f);
	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LEQUAL);*/
	createTexture();
}


void destroyTexture() {
	glDeleteTextures(1, &texture);
	LOGD("Texture destroyed");
}

void renderFrame(cv::Mat imageMat)
{
	// Clear the screen
	glClear(GL_COLOR_BUFFER_BIT);

	// Bind our created texture
	glBindTexture(GL_TEXTURE_2D, texture);


	/*if(imageMat.empty()){
	      LOGD("Image is empty!");
	} else {*/
		cv::flip(imageMat, imageMat, 0);

		glTexSubImage2D(GL_TEXTURE_2D, 0, (1024-frameWidth)/2, (1024-frameHeight)/2, frameWidth, frameHeight,
		   GL_RGB, GL_UNSIGNED_BYTE, imageMat.ptr());

		//glGenerateMipmap(GL_TEXTURE_2D);
	//}
}

/********************************************
 * JNI Part!
 ********************************************/
extern "C" {
	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_ui_view_OpenGlImageRenderer_initOpenGl(JNIEnv * jenv, jobject thiz);
	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_ui_view_OpenGlImageRenderer_resizeOpenGlViewport(JNIEnv * jenv, jobject thiz, jint width, jint height);
	JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_ui_view_OpenGlImageRenderer_renderOpenGl(JNIEnv * jenv, jobject thiz, jlong addrImageMat);
}

JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_ui_view_OpenGlImageRenderer_initOpenGl(JNIEnv * jenv, jobject thiz) {
	initOpenGl();
}

JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_ui_view_OpenGlImageRenderer_resizeOpenGlViewport(JNIEnv * jenv, jobject thiz, jint width, jint height) {
	resizeViewport((int)width, (int)height);
}

JNIEXPORT void JNICALL Java_at_ac_tuwien_caa_cvl_imagine_ui_view_OpenGlImageRenderer_renderOpenGl(JNIEnv * jenv, jobject thiz, jlong addrImageMat) {
	renderFrame(*(cv::Mat*)addrImageMat);
}
