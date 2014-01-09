package at.ac.tuwien.caa.cvl.imagine.ui.view;

import java.util.Arrays;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;
import at.ac.tuwien.caa.cvl.imagine.image.OnImageChangedListener;

public class HistogramView extends SurfaceView implements SurfaceHolder.Callback, OnImageChangedListener {
	private static final String TAG = HistogramView.class.getSimpleName();

	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;
	private static final int ALPHA = 3;
	
	protected Context context;
	protected SurfaceHolder surfaceHolder;
	protected boolean isCreated = false;
	protected boolean cvVarsInitalized = false;
	
	protected ImImage image;
	
	protected int width = -1;
	protected int height = -1;
	
	// Vars used by this class for easier handling
	private int histPaddingLeft = 5;
	private int histPaddingRight = 5;
	private int histPaddingTop = 10;
	private int histPaddingBottom = 0;
	private int histHeight;
	private int histWidth;
	
	private float strokeWidth;
	private float[] histRange;
	protected int imgChannels;
	protected int histNumBins;
	
	// Vars needed for the OpenCV calls
	private Mat cvImgMat;
	private Mat cvHistMat;
	private Mat cvEmptyMat;
	private MatOfFloat cvHistRange;
	private MatOfInt[] cvImgChannels;
	private MatOfInt cvHistNumBins;
	private float[] histValues;
	
	private Paint[] paintHist;

	public HistogramView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public HistogramView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public HistogramView(Context context) {
		super(context);
		initialize(context);
	}
	
	private void initialize(Context context) {
		this.context = context;
		initializePaint();
		
		surfaceHolder = getHolder();
	    surfaceHolder.addCallback(this);

	    // Initialize all the needed vars
	    histRange = new float[] { 0f, 256f };
	}
		
	@SuppressLint("NewApi")
	private void initializePaint() {
		paintHist = new Paint[4];
		
		paintHist[RED] = new Paint();
		paintHist[RED].setColor(0xFFFF0000); // ARGB: red
		
		paintHist[GREEN] = new Paint();
		paintHist[GREEN].setColor(0xFF00FF00); // ARGB: green
		
		paintHist[BLUE] = new Paint();
		paintHist[BLUE].setColor(0xFF0000FF); // ARGB: blue
		
		paintHist[ALPHA] = new Paint();
		paintHist[ALPHA].setColor(Color.GRAY);
		
		 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			 paintHist[RED].setXfermode(new PorterDuffXfermode(Mode.ADD));
			 paintHist[GREEN].setXfermode(new PorterDuffXfermode(Mode.ADD));
			 paintHist[BLUE].setXfermode(new PorterDuffXfermode(Mode.ADD));
			 paintHist[ALPHA].setXfermode(new PorterDuffXfermode(Mode.ADD));
		 }
	}
	
	/**
	 * This method must be called after the OpenCV Manager is initialized successfully.
	 */
	private boolean initializeOpenCvVars() {
		if (!cvVarsInitalized) {
		    try {
				cvEmptyMat = new Mat();
			    cvHistMat = new Mat();
			    
			    cvImgChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
			    
			    cvHistRange = new MatOfFloat(histRange[0], histRange[1]);
			    
			    cvVarsInitalized = true;
		    } catch (Error e) {
		    	Log.e(TAG, "I'm sorry, but I could not initialize the OpenCV variables. Maybe the OpenCV Manager is not initialized correctly.");
		    	
		    	cvVarsInitalized = false;
		    	return cvVarsInitalized;
		    }
		}
		
		return cvVarsInitalized;
	}
	
	public void setImageMat(Mat imageMat) {
		if (initializeOpenCvVars()) {
			this.cvImgMat = imageMat;
					
			if (cvImgMat.channels() > 3) {
				this.imgChannels = 3;
			} else {			
				this.imgChannels = cvImgMat.channels();
			}
			
			cvHistNumBins = new MatOfInt(histNumBins);
			cvHistMat.create(1, histNumBins, CvType.CV_32F);
					
			this.updateHistogram();
		}
	}
			
	private void drawRGBHist(Canvas canvas) {		
		Log.d(TAG, "Drawing RGB - Histogram");
		
	    //clear previous drawings => must be black because of the additive color blending
        canvas.drawColor(Color.BLACK);
        
        
        // calculate the needed stroke width
        strokeWidth = (histWidth > histRange[1]) ? (float)histWidth/histRange[1] : 1f;
        
        for (int channel = 0; channel < this.imgChannels; channel++) {
        	Imgproc.calcHist(Arrays.asList(cvImgMat), cvImgChannels[channel], cvEmptyMat, cvHistMat, cvHistNumBins, cvHistRange);
    		Core.normalize(cvHistMat, cvHistMat, 0.0f, (float)histHeight, Core.NORM_MINMAX);
            		
    		// Get the histogram values
            cvHistMat.get(0, 0, histValues);        	
        	
            paintHist[channel].setStrokeWidth(strokeWidth);
            
        	for (int bin = 0; bin < histNumBins; bin++) {
        		canvas.drawLine(histPaddingLeft+bin*strokeWidth, histPaddingTop+histHeight, 
        				histPaddingLeft+bin*strokeWidth, histPaddingTop+histHeight-(int)histValues[bin], paintHist[channel]);
        	}
        }
	}
	
	public void updateHistogram() {	
		if (!isCreated) {
			Log.w(TAG, "Tried to update the Histogram before the surface is created");
			return;
		}
		
		Canvas canvas = null;		
		try {
            canvas = surfaceHolder.lockCanvas(null);
            
            if(isCreated && canvas != null) {
                drawRGBHist(canvas);
            } else {
            	Log.w(TAG, "I'm sorry, I could not draw to the canvas. Maybe it is locked or not created.");
            }
        }
        catch(Exception e) {
            Log.e(TAG, "The canvas draw call caused an exception!");
            e.printStackTrace();
        } finally {
            if(canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
		
		Log.d(TAG, "Histogram updated!");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		this.width = width;
		this.height = height;
		histWidth = width - histPaddingLeft - histPaddingRight;
		histHeight = height - histPaddingBottom - histPaddingTop;
		
		if (histWidth > histRange[1]) {
			histNumBins = (int)histRange[1];
		} else {
			histNumBins = histWidth;
		}
		
		if (histValues == null || histValues.length != histNumBins) {
			this.histValues = new float[histNumBins];
		}
		
		Log.d(TAG, "Surface changed, width: " + width);
		// Update the histogram
		updateHistogram();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		isCreated = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isCreated = false;
	}
	
	
	@Override
	public void onImageManipulated() {
		if (image.getScaledBitmap() != null) {
			cvImgMat = new Mat();
			Utils.bitmapToMat(image.getScaledBitmap(), cvImgMat);
		}
    	
    	this.setImageMat(cvImgMat);
	}

	@Override
	public void onLoadingNewImage() {
		// Nothint to do in this case
	}

	@Override
	public void onNewImageLoaded() {
		cvImgMat = new Mat();
		Utils.bitmapToMat(image.getScaledBitmap(), cvImgMat);
    	
    	this.setImageMat(cvImgMat);
	}
	
	
	public ImImage getImage() {
		return image;
	}

	public void setImage(ImImage image) {
		this.image = image;
		
		// Add myself to the list of listeners
		image.setOnImageChangedListener(this);
	}
		
	
}
