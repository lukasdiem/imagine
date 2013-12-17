#include <jni.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>

#include "Utils.h"

#define LOG_TAG "Imagine/NativeOpenGlRendering"

#define BUFFER_OFFSET(i) ((void*)(i))

int frameWidth;
int frameHeight;

GLuint texture;
GLuint buffer;
GLuint program;

GLint a_position_location;
GLint a_texture_coordinates_location;
GLint u_texture_unit_location;


// position X, Y, texture S, T
static const float rect[] = {
		-1.0f, -1.0f, 0.0f, 0.0f,
		-1.0f,  1.0f, 0.0f, 1.0f,
		 1.0f, -1.0f, 1.0f, 0.0f,
		 1.0f,  1.0f, 1.0f, 1.0f
};

cv::Mat renderMat;

void initOpenGl() {
	glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
	//glShadeModel(GL_SMOOTH);
	glClearDepthf(1.0f);
	//glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

	//LOGD("Loading image!");
	//imageMat = cv::imread("mnt/sdcard0/test/test.jpg");
}

/*GLuint create_vbo(const GLsizeiptr size, const GLvoid* data, const GLenum usage) {
    assert(data != NULL);
	GLuint vbo_object;
	glGenBuffers(1, &vbo_object);
	assert(vbo_object != 0);

	glBindBuffer(GL_ARRAY_BUFFER, vbo_object);
	glBufferData(GL_ARRAY_BUFFER, size, data, usage);
	glBindBuffer(GL_ARRAY_BUFFER, 0);

	return vbo_object;
}*/

void createTexture(const cv::Mat& image) {
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

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

	// Set texture clamping method
	//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
	//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

	glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 1024, 1024, 0, GL_RGBA, GL_UNSIGNED_BYTE, image.data);
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
	//createTexture(imageMat);
}


void destroyTexture() {
	glDeleteTextures(1, &texture);
	LOGD("Texture destroyed");
}

void renderFrame(cv::Mat imageMat)
{
	/*createTexture(imageMat);

	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glUseProgram(program);

	glActiveTexture(GL_TEXTURE0);
	glBindTexture(GL_TEXTURE_2D, texture);
	glUniform1i(u_texture_unit_location, 0);

	glBindBuffer(GL_ARRAY_BUFFER, buffer);
	//glVertexAttribPointer(a_position_location, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GL_FLOAT), BUFFER_OFFSET(0));
	//glVertexAttribPointer(a_texture_coordinates_location, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GL_FLOAT), BUFFER_OFFSET(2 * sizeof(GL_FLOAT)));
	glEnableVertexAttribArray(a_position_location);
	glEnableVertexAttribArray(a_texture_coordinates_location);
	glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

	glBindBuffer(GL_ARRAY_BUFFER, 0);*/
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
