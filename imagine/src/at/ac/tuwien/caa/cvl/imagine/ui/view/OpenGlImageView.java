package at.ac.tuwien.caa.cvl.imagine.ui.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;

public class OpenGlImageView extends GLSurfaceView {
	private static final String TAG = OpenGlImageView.class.getSimpleName();
	
	private OpenGlImageRenderer renderer;
	private ClearRenderer renderer2;
	
	private ImImage image;
	
	private OpenGlImageView(Context context) {
		super(context);

		initialize();
	}

	public OpenGlImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize();
	}
	
	private void initialize() {
		Log.d(TAG, "Initializing the OpenGl view");
		
		// Create an OpenGL ES 2.0 context
		setEGLContextClientVersion(2);
		
		Log.d(TAG, "Initialized the version");		
	}
	
	private void initalizeRenderer() {
		renderer = new OpenGlImageRenderer(image);
		setRenderer(renderer);
		
        Log.d(TAG, "Initialized the renderer");
		
		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
	    // Turn on error-checking and logging
	    //setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);

	    Log.d(TAG, "Initialized the render mode");
	}
	
	public void setImage(ImImage image) {
		this.image = image;
		
		initalizeRenderer();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		super.onTouchEvent(event);
		
        queueEvent(new Runnable(){
            public void run() {
                renderer2.setColor(event.getX() / getWidth(),
                        event.getY() / getHeight(), 1.0f);
            }});
            return true;
        }

}
