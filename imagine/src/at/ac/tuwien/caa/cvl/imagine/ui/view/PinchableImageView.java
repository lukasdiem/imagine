package at.ac.tuwien.caa.cvl.imagine.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import at.ac.tuwien.caa.cvl.imagine.image.BitmapLoader;

public class PinchableImageView extends ImageView implements 
		ScaleGestureDetector.OnScaleGestureListener, 
		OnTouchListener {
	
	private static final String TAG = PinchableImageView.class.getSimpleName(); 
	
	
	private static enum EnumGesture {
		NONE, ZOOM, DRAG
	}
	
	private EnumGesture lastGesture;
	
	private BitmapLoader bitmapLoader;
	
	private Matrix mMatrix = new Matrix();
	private Matrix initialMatrix = new Matrix();
	float[] matrixValues = new float[9];
	float[] initialMatrixValues = new float[9];
	
	private RectF viewBounds;
	
	private PointF lastFingerPos = new PointF(); 
	
	// Gesture listener and helper
	private GestureDetector gestureDetector;
	
	private ScaleGestureDetector mScaleDetector;
	
	// Edge effect / overscroll tracking objects.
	private EdgeEffectCompat mEdgeEffectTop;
	private EdgeEffectCompat mEdgeEffectBottom;
	private EdgeEffectCompat mEdgeEffectLeft;
	private EdgeEffectCompat mEdgeEffectRight;

	
	private float scale = 1.0f;

		
	public PinchableImageView(Context context) {
		super(context);
		
		initialize(context);
	}
	
	public PinchableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        initialize(context);
    }
	
	private void initialize(Context context) {
		// Set the proper on touch listener
		this.setOnTouchListener(this);
		
		// Set the Scale Gesture listener
		mScaleDetector = new ScaleGestureDetector(context, this);
		gestureDetector = new GestureDetector(context, new MyGestureListener());
		
		bitmapLoader = new BitmapLoader();
		
		// Initialize the initial image matrix
		//initialMatrix = this.getImageMatrix();
		
		// Initialize the edge effect classes needed to deal with overscroll effects
		mEdgeEffectTop = new EdgeEffectCompat(context);
		mEdgeEffectBottom = new EdgeEffectCompat(context);
		mEdgeEffectLeft = new EdgeEffectCompat(context);
		mEdgeEffectRight = new EdgeEffectCompat(context);
		
		lastGesture = EnumGesture.NONE;
		
		this.setWillNotDraw(false);
	}

	/*
	@Override
    public void setImageBitmap(Bitmap bitmap){
		super.setImageBitmap(bitmap);
		
        initialMatrix.set(this.getImageMatrix());
        
        Log.d(TAG, "Set image bitmap called");
    }
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		//super.setImageDrawable(drawable);
		
		Log.d(TAG, "Set image drawable called");
		drawable.
		bitmapLoader.setImageDrawable(res, resId, targetWidth, targetHeight);
		
	}*/
	
	public RectF getViewBounds() {
		return viewBounds;
	}

	/*----------------------------------------------------------------------
	 * Implemented Listeners
	 -----------------------------------------------------------------------*/
	
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    	
    	// update the view bounds
        viewBounds = new RectF(0, 0, w, h);
        
        initialMatrix.set(this.getImageMatrix());
        initialMatrix.getValues(initialMatrixValues);
        
        Log.d(TAG, "Size changed...");
        Log.d(TAG, "Matrix: " + this.getImageMatrix().toString());
    }
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		Log.d(TAG, "Scale gesture: Scaling, Scale factor: " + detector.getScaleFactor());
		
		scale = detector.getScaleFactor();
		
		//mMatrix.set(mSavedMatrix);
		mMatrix.postScale(scale, scale, (float)this.getWidth()/2.0f, (float)this.getHeight()/2.0f);
		
		this.setImageMatrix(mMatrix);
		
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {		
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.d(TAG, "Scale gesture: End");
		lastGesture = EnumGesture.ZOOM;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// Let the gesture detectors inspect all events.
	    mScaleDetector.onTouchEvent(event);
	    gestureDetector.onTouchEvent(event);
		
	    if (this.getScaleType() != ImageView.ScaleType.MATRIX) {
	    	this.setScaleType(ImageView.ScaleType.MATRIX);
	    }
	    		
	    final int action = MotionEventCompat.getActionMasked(event); 
	    switch (action) { 
		    case MotionEvent.ACTION_DOWN: { // One finger is on the screen
		        if (event.getPointerCount() == 1) {
		        	lastGesture = EnumGesture.DRAG;
		        	lastFingerPos.set(event.getX(), event.getY());
		        }
		        
		        break;
		    }
		    		            
		    case MotionEvent.ACTION_MOVE: { // The user is dragging
		        if (event.getPointerCount() == 1 && lastGesture == EnumGesture.DRAG) {		        	
		        	translateImage(event.getX() - lastFingerPos.x, event.getY() - lastFingerPos.y);
		        	
		        	// Remember this pos
		        	lastFingerPos.set(event.getX(), event.getY());
		        	
		        	Log.d(TAG, "Moving image, x: " + (event.getX() - lastFingerPos.x));
		        }
		    	
		        break;
		    }
		    
		    case MotionEvent.ACTION_UP: {
		    	if (event.getPointerCount() == 0) {
		    		// Reset the gestures
		    		lastGesture = EnumGesture.NONE;
		    		
		    		if (!mEdgeEffectTop.isFinished())
		    			mEdgeEffectTop.finish();
		    		
		    		if (!mEdgeEffectBottom.isFinished())
		    			mEdgeEffectBottom.finish();
		    		
		    		if (!mEdgeEffectLeft.isFinished())
		    			mEdgeEffectLeft.finish();
		    		
		    		if (!mEdgeEffectRight.isFinished())
		    			mEdgeEffectRight.finish();
		    	}
		    }
	    }       
	    
		return true;
	}
	
	private void translateImage(float deltaX, float deltaY) {
		//Log.d(TAG, "View bounds: " + viewBounds.toString());
		
		//mMatrix.set(this.getImageMatrix());
		
		//mMatrix.getValues(values);//
		
		// Update the matrix
    	mMatrix.set(this.getImageMatrix());
    	// Get the values as floats
    	mMatrix.getValues(matrixValues);
    	
    	Log.d(TAG, "Initial scale: " + initialMatrixValues[Matrix.MSCALE_X] + ", " + initialMatrixValues[Matrix.MSCALE_Y]);
    	Log.d(TAG, "Actual scale:  " + matrixValues[Matrix.MSCALE_X] + ", " + matrixValues[Matrix.MSCALE_Y]);
    	
    	if (matrixValues[Matrix.MSCALE_X] <= initialMatrixValues[Matrix.MSCALE_X] &&
    			matrixValues[Matrix.MSCALE_Y] <= initialMatrixValues[Matrix.MSCALE_Y]) {
    		Log.d(TAG, "Image scale matrix smaller than canvas!");
    		Log.d(TAG, "Delta: " + deltaX + ", " + deltaY);
    		
    		if (deltaX < 0.0f) {
    			Log.d(TAG, "Test");
    			mEdgeEffectRight.onPull(50.0f);
    			//mEdgeEffectRight.setSize(50, (int)viewBounds.height());
    		} else if (deltaX > 0.0f) {
    			mEdgeEffectLeft.onPull(Math.abs(deltaX));
    		}
    		
    		if (deltaY < 0.0f) {
    			mEdgeEffectBottom.onPull(Math.abs(deltaX));
    		} else if (deltaY > 0.0f) {
    			mEdgeEffectTop.onPull(Math.abs(deltaX));
    		}
    		
    		this.invalidate();
    	} else {
	    	// Add translation parameters
	    	mMatrix.postTranslate(deltaX, deltaY);
	    	// Translate the image
	    	this.setImageMatrix(mMatrix);
    	}
	}
	
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
		boolean needsInvalidate = false;
		
		
        if (!mEdgeEffectTop.isFinished()) {
            final int restoreCount = canvas.save();
            final int height = getHeight();
            final int width = getWidth() - getPaddingLeft() - getPaddingRight();

            canvas.translate(getPaddingLeft(), getPaddingTop());
            mEdgeEffectTop.setSize(width, height);
            needsInvalidate |= mEdgeEffectTop.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
		
        if (!mEdgeEffectBottom.isFinished()) {
            final int restoreCount = canvas.save();
            final int height = getHeight();
            final int width = getWidth() - getPaddingLeft() - getPaddingRight();

            canvas.rotate(180);
            canvas.translate(-width, -height);
            mEdgeEffectBottom.setSize(width, height);
            needsInvalidate |= mEdgeEffectBottom.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
		
        if (!mEdgeEffectRight.isFinished()) {
            final int restoreCount = canvas.save();
            final int width = getWidth();
            final int height = getHeight() - getPaddingTop() - getPaddingBottom();
            
            canvas.rotate(90);
            canvas.translate(-getPaddingTop(), -width);
            mEdgeEffectRight.setSize(height, width);
            needsInvalidate |= mEdgeEffectRight.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        
        if (!mEdgeEffectLeft.isFinished()) {
            final int restoreCount = canvas.save();
            final int height = getHeight() - getPaddingTop() - getPaddingBottom();
            final int width = getWidth();

            canvas.rotate(270);
            canvas.translate(-height + getPaddingTop(), 0);
            mEdgeEffectLeft.setSize(height, width);
            needsInvalidate |= mEdgeEffectLeft.draw(canvas);
            canvas.restoreToCount(restoreCount);
        }
        
        
        if (needsInvalidate) {        	
            // Keep animating
            ViewCompat.postInvalidateOnAnimation(this);
        }
	}

	/*----------------------------------------------------------------------
	 * Private gesture listener class
	 -----------------------------------------------------------------------*/	
	class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private final String TAG = MyGestureListener.class.getSimpleName(); 
        
        @Override
        public boolean onDoubleTap (MotionEvent event) {
        	Log.d(TAG, "Double tap detected");

        	// Reset the image matrix to the initial matrix
        	setImageMatrix(initialMatrix);

        	
        	return true;
        }
        
    }


}
