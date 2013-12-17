/*
 * Utils.cpp
 *
 *  Created on: 07.12.2013
 *      Author: Lukas
 */

#include "Utils.h"

/*static void log_v_fixed_length(const GLchar* source, const GLint length) {
        if (LOGGING_ON) {
                char log_buffer[length + 1];
                memcpy(log_buffer, source, length);
                log_buffer[length] = '\0';

                DEBUG_LOG_WRITE_V(TAG, log_buffer);
        }
}

static void log_shader_info_log(GLuint shader_object_id) {
        if (LOGGING_ON) {
                GLint log_length;
                glGetShaderiv(shader_object_id, GL_INFO_LOG_LENGTH, &log_length);
                GLchar log_buffer[log_length];
                glGetShaderInfoLog(shader_object_id, log_length, NULL, log_buffer);

                DEBUG_LOG_WRITE_V(TAG, log_buffer);
        }
}

static void log_program_info_log(GLuint program_object_id) {
        if (LOGGING_ON) {
                GLint log_length;
                glGetProgramiv(program_object_id, GL_INFO_LOG_LENGTH, &log_length);
                GLchar log_buffer[log_length];
                glGetProgramInfoLog(program_object_id, log_length, NULL, log_buffer);

                DEBUG_LOG_WRITE_V(TAG, log_buffer);
        }
}*/
