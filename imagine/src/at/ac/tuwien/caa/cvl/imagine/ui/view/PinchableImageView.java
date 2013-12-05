package at.ac.tuwien.caa.cvl.imagine.ui.view;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
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
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;
import at.ac.tuwien.caa.cvl.imagine.image.OnImageChangedListener;

public class PinchableImageView extends ImageView implements 
		ScaleGestureDetector.OnScaleGestureListener, 
		OnTouchListener, OnImageChangedListener {
	
	private static final String TAG = PinchableImageView.class.getSimpleName(); 
	
	
	private static enum EnumGesture {
		NONE, ZOOM, DRAG
	}
		
	private EnumGesture lastGesture;
	
	private ImImage image;

	private Matrix mMatrix = new Matrix();
	private Matrix initialMatrix = new Matrix();
	float[] matrixValues = new float[9];
	float[] initialMatrixValues = new float[9];
	
	private RectF viewBounds;
	private RectF imageBounds;
	private RectF imageRect;
	
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
	private float rotation = 0;
		
	public PinchableImageView(Context context) {
		super(context);
		
		initialize(context);
	}
	
	public PinchableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        initialize(context);
    }
	
	public PinchableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize(context);
	}
	
	private void initialize(Context context) {
		// Set the proper on touch listener
		this.setOnTouchListener(this);
		
		// Set the Scale Gesture listener
		mScaleDetector = new ScaleGestureDetector(context, this);
		gestureDetector = new GestureDetector(context, new MyGestureListener());
		
		// Initialize the edge effect classes needed to deal with overscroll effects
		mEdgeEffectTop = new EdgeEffectCompat(context);
		mEdgeEffectBottom = new EdgeEffectCompat(context);
		mEdgeEffectLeft = new EdgeEffectCompat(context);
		mEdgeEffectRight = new EdgeEffectCompat(context);
		
		imageRect = new RectF();
		
		lastGesture = EnumGesture.NONE;
		
		this.setWillNotDraw(false);
	}
	
	public ImImage getImage() {
		return image;
	}

	public void setImage(ImImage image) {
		Log.d(TAG, "New image class is set!");
		
		this.image = image;
		
		// Set myself to listen to events of this image class!
		image.addOnImageChangedListener(this);
	}
	
	@Override
    public void setImageBitmap(Bitmap bitmap){
		super.setImageBitmap(bitmap);
				
		// Recenter the image
        centerImageInView();
        
        Log.d(TAG, "Set image bitmap called");
    }
	
	public void setImageRotation(float rotation) {
		this.setScaleType(ScaleType.MATRIX);
		
		this.rotation = rotation;		
		mMatrix.set(this.getImageMatrix());
		mMatrix.postRotate(rotation, this.getWidth()/2, this.getHeight()/2);
		this.setImageMatrix(mMatrix);
	}
	
	/*
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
        
        //initialMatrix.set(this.getImageMatrix());
        //initialMatrix.getValues(initialMatrixValues);
        
        Log.d(TAG, "Size changed...");
        Log.d(TAG, "Matrix: " + this.getImageMatrix().toString());
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	
        //initialMatrix.set(this.getImageMatrix());
        //initialMatrix.getValues(initialMatrixValues);
    	
        Log.d(TAG, "on measure");
    }
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		//Log.d(TAG, "Scale gesture: Scaling, Scale factor: " + detector.getScaleFactor());
		
		scale = detector.getScaleFactor();
		// scale the image according to the gesture
		if (scaleImage(scale)) {
			this.invalidate();
		}
				
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
	    
	    boolean needsInvalidate = false;
		
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
		        	needsInvalidate = translateImage(event.getX() - lastFingerPos.x, event.getY() - lastFingerPos.y);
		        	
		        	// Remember this pos
		        	lastFingerPos.set(event.getX(), event.getY());
		        	
		        	Log.d(TAG, "Moving image, x: " + (event.getX() - lastFingerPos.x));
		        }
		    	
		        break;
		    }
		    
		    case MotionEvent.ACTION_UP | MotionEvent.ACTION_CANCEL: {
		    	if (event.getPointerCount() == 0) {
		    		// Reset the gestures
		    		lastGesture = EnumGesture.NONE;
		    	}
		    		
	    		if (!mEdgeEffectTop.onRelease()) {
	    			needsInvalidate = true;
	    		}
	    		
	    		if (!mEdgeEffectBottom.onRelease()) {
	    			needsInvalidate = true;
	    		}
	    		
	    		if (!mEdgeEffectLeft.onRelease()) {
	    			needsInvalidate = true;
	    		}
	    		
	    		if (!mEdgeEffectRight.onRelease()) {
	    			needsInvalidate = true;
	    		}
		    	
		    }
	    }
	    
	    // Check if we have to invalidate this view
	    if (needsInvalidate) {
	    	this.invalidate();
	    }
	    
		return true;
	}
	
	private boolean translateImage(float deltaX, float deltaY) {
		boolean needsInvalidate = false; 
		boolean translateX = true;
		boolean translateY = true;
		
		// Update the matrix
    	mMatrix.set(this.getImageMatrix());
    	// Get the values as floats
    	mMatrix.getValues(matrixValues);
    	
    	// Get the position and size of the image with the newly added offset
    	imageRect.left =  matrixValues[Matrix.MTRANS_X] + deltaX;
    	imageRect.right =  imageRect.left + getDrawable().getIntrinsicWidth() * matrixValues[Matrix.MSCALE_X];
    	imageRect.top = matrixValues[Matrix.MTRANS_Y] + deltaY;
    	imageRect.bottom = imageRect.top + getDrawable().getIntrinsicHeight() * matrixValues[Matrix.MSCALE_Y];
    	
    	if (imageRect.left > viewBounds.left) {
    		Log.d(TAG, "Left boarder inside");
    		
    		// Do not translate further!
    		if (deltaX > 0) {
    			translateX = false;
    			mEdgeEffectLeft.onPull(deltaX);
    			needsInvalidate = true;
    		}
    	}
    	
    	if (imageRect.right < viewBounds.right) {
    		Log.d(TAG, "Right boarder inside");
    		
    		// Do not translate further!
    		if (deltaX < 0) {
    			translateX = false;
    			mEdgeEffectRight.onPull(-deltaX);
    			needsInvalidate = true;
    		}
    	}
    	
    	if (imageRect.top > viewBounds.top) {
    		Log.d(TAG, "Top boarder inside");
    		
    		// Do not translate further!
    		if (deltaY > 0) {
    			translateY = false;
    			mEdgeEffectTop.onPull(deltaY);
    			needsInvalidate = true;
    		}
    	}
    	
    	if (imageRect.bottom < viewBounds.bottom) {
    		Log.d(TAG, "Bottom boarder inside");
    		
    		// Do not translate further!
    		if (deltaY < 0) {
    			translateY = false;
    			mEdgeEffectBottom.onPull(-deltaY);
    			needsInvalidate = true;
    		}
    	}
    	    	
    	if (translateX || translateY) {
	    	// Add translation parameters
	    	if (translateX) {
	    		mMatrix.postTranslate(deltaX, 0);
	    	} 
	    	
	    	if (translateY) {
	    		mMatrix.postTranslate(0, deltaY);
	    	} 
	    	// Translate the image
	    	this.setImageMatrix(mMatrix);
    	}
    	
    	return needsInvalidate;
	}
	
	private boolean scaleImage(float deltaScale) {
		boolean needsInvalidate = false;
		
		// Update the matrix
    	mMatrix.set(this.getImageMatrix());
    	// scale the matrix around the view center
    	mMatrix.postScale(deltaScale, deltaScale, viewBounds.centerX(), viewBounds.centerY());
    	//mMatrix.postScale(deltaScale, deltaScale);
    	    	
    	// Always allow upscale => we do not have to check anything in this case
    	if (deltaScale > 1.0f) {
    		this.setImageMatrix(mMatrix);
    	} else {
    		// if we downscale we have to check some conditions

    		// Get the values as floats
        	mMatrix.getValues(matrixValues);
        	
        	// Get the position and size of the image with the newly added offset
        	imageRect.left =  matrixValues[Matrix.MTRANS_X];
        	imageRect.right =  imageRect.left + getDrawable().getIntrinsicWidth() * matrixValues[Matrix.MSCALE_X];
        	imageRect.top = matrixValues[Matrix.MTRANS_Y];
        	imageRect.bottom = imageRect.top + getDrawable().getIntrinsicHeight() * matrixValues[Matrix.MSCALE_Y];
    		
	    	if (!viewBounds.contains(imageRect)) {
	    		Log.d(TAG, "Downscale - Checking boundary conditions");
	    		
	    		if (!(imageRect.left >= viewBounds.left && imageRect.right <= viewBounds.right)) {
		    		if (imageRect.left > viewBounds.left) {
		        		mMatrix.postTranslate(viewBounds.left - imageRect.left, 0);
		        	}
		        	
		        	if (imageRect.right < viewBounds.right) {
		        		mMatrix.postTranslate(viewBounds.right - imageRect.right, 0);
		        	}
	    		}
	        	
	    		if (!(imageRect.top >= viewBounds.top && imageRect.bottom <= viewBounds.bottom)) {
		        	if (imageRect.top > viewBounds.top) {
		        		mMatrix.postTranslate(0, viewBounds.top - imageRect.top);
		        	}
		        	
		        	if (imageRect.bottom < viewBounds.bottom) {
		        		mMatrix.postTranslate(0, viewBounds.bottom - imageRect.bottom);
		        	}
	    		}
	        	
	        	// update the image matrix
	        	this.setImageMatrix(mMatrix);
	    	} else {
	    		this.centerImageInView();
	    		
	    		// minimum scale reached! => indicate this state
	    		mEdgeEffectLeft.onPull(-deltaScale);
	    		mEdgeEffectRight.onPull(-deltaScale);
	    		mEdgeEffectTop.onPull(-deltaScale);
	    		mEdgeEffectBottom.onPull(-deltaScale);
    			needsInvalidate = true;
	    	}
    	}
		
		
		return needsInvalidate;
	}
	
	public void centerImageInView() {
		this.setScaleType(ScaleType.MATRIX);
		imageBounds = new RectF(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
    	
    	if (viewBounds == null) {
    		viewBounds = new RectF(0, 0, this.getWidth(), this.getHeight());
    	}
    	
    	mMatrix.setRectToRect(imageBounds, viewBounds, Matrix.ScaleToFit.CENTER);
    	//mMatrix.postRotate(rotation, viewBounds.centerX(), viewBounds.centerY());
    	
    	// Reset the image matrix to the newly created center matrix
    	setImageMatrix(mMatrix);
    	
    	this.invalidate();
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

        	imageBounds = new RectF(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        	
        	mMatrix.setRectToRect(imageBounds, viewBounds, Matrix.ScaleToFit.CENTER);
        	
        	// Reset the image matrix to the newly created center matrix
        	setImageMatrix(mMatrix);

        	
        	return true;
        }
        
        @Override
     	public boolean onSingleTapConfirmed(MotionEvent e) {
        	Log.d(TAG, "single tap detected");
        	
        	/*Mat imageMat = new Mat();
        	Bitmap imageBitmap = ((BitmapDrawable)getDrawable()).getBitmap();
        	Utils.bitmapToMat(imageBitmap, imageMat);
        	Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
        	//Imgproc.GaussianBlur(imageMat, imageMat, new Size(3, 3), 0);
        	//Imgproc.adaptiveThreshold(imageMat, imageMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);
        	Utils.matToBitmap(imageMat, imageBitmap);
        	// Cleanup
        	imageMat.release();
        	// Invalidate the view => redraw!
        	invalidate();*/
        	
        	//image.changeBrightnessContrast(20, 2);
        	//image.convertToGrayscale();
        	image.changeBrightnessContrast(10, 1);
        	
        	return true;
        }
    }

	@Override
	public void onImageManipulated() {
		// Redraw!
		this.invalidate();
	}

	@Override
	public void onLoadingNewImage() {
		// TODO Show a spinner during the loading time!
		Log.d(TAG, "Loading a new image");		
	}

	@Override
	public void onNewImageLoaded() {
		Log.d(TAG, "New image is loaded completely");
		
		if(image.getScaledBitmap() != null) {
			super.setImageBitmap(image.getScaledBitmap());
			
			// Recenter the image
	        centerImageInView();
		} else {
			Log.w(TAG, "New Bitmap is NULL!!!");
		}
	}


}
