package at.ac.tuwien.caa.cvl.imagine.ui.view;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

class ClearRenderer implements GLSurfaceView.Renderer {
    private float mRed;
    private float mGreen;
    private float mBlue;
	
	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Do nothing special.
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int w, int h) {
    	Log.d("ClearRenderer", "Size: " + w + "x" + h);
    	GLES20.glViewport(0, 0, w, h);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
    	GLES20.glClearColor(mRed, mGreen, mBlue, 1.0f);
    	GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    public void setColor(float r, float g, float b) {
        mRed = r;
        mGreen = g;
        mBlue = b;
    }
}
