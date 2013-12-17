/*
 * Utils.h
 *
 *  Created on: 07.12.2013
 *      Author: Lukas
 */

#pragma once

#include <android/log.h>

#define LOGGING_ON 1

#define  LOGD(...)  ((void)__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__))
#define  LOGW(...)  ((void)__android_log_print(ANDROID_LOG_WARNING,LOG_TAG,__VA_ARGS__))
#define  LOGE(...)  ((void)__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__))

static void logD(char* tag, ...);
static void logE(char* tag, ...);
static void logW(char* tag, ...);


