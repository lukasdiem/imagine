package at.ac.tuwien.caa.cvl.imagine.ui.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;

public class OpenGlImageRenderer implements GLSurfaceView.Renderer {
	private static final String TAG = OpenGlImageRenderer.class.getSimpleName();
	
	ImImage image;
	
	float[] projMatrix = new float[16];
	
	public OpenGlImageRenderer(ImImage image) {
		this.image = image;
	}
	
	@Override
	public void onDrawFrame(GL10 unused) {
		if (image.getImageMat() != null) {
			OpenGlImageRenderer.renderOpenGl(image.getImageMat().nativeObj);
		} else {
			Log.w(TAG, "Can not render the image because it is NULL!");
		}
	}
	

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float ratio = (float) width / height;

	    // this projection matrix is applied to object coordinates
	    // in the onDrawFrame() method
	    Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

		
		OpenGlImageRenderer.resizeOpenGlViewport(width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		OpenGlImageRenderer.initOpenGl();		
	}

	
	static {
		System.loadLibrary("openglrenderer");
	}
	
	private static native void initOpenGl();
	private static native void resizeOpenGlViewport(int width, int height);
	private static native void renderOpenGl(long imageMat);
}
