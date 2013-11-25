package at.ac.tuwien.caa.cvl.imagine.ui.view;

import java.util.Arrays;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class HistogramView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = HistogramView.class.getSimpleName();

	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;
	private static final int ALPHA = 3;
	
	protected Context context;
	protected SurfaceHolder surfaceHolder;
	protected boolean isCreated = false;
	
	protected int width = -1;
	protected int height = -1;
	
	private int histStartX;
	private int histStartY;
	private int histMaxHeight;
	private float strokeWidth;
	
	protected Mat imgMat;
	protected int imgChannels;
	protected int histNumBins;
	protected MatOfFloat histRange;
	
	protected Mat histogram;
	protected float[] histValues;
	
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
	    
	    // Set the background transparent
	    //this.setBackgroundColor(0xFF000000);
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
	
	public void setImageMat(Mat imageMat) {
		this.imgMat = imageMat;
		
		if (imgMat.channels() > 3) {
			this.imgChannels = 3;
		} else {			
			this.imgChannels = imgMat.channels();
		}
		
		this.histRange = new MatOfFloat(0.0f, 256.0f);
		this.histValues = new float[histNumBins*imgChannels];
		
		if (isCreated) {
			if (this.histogram == null) {
				//this.histogram = new Mat(1, imgMat.channels() * width, CvType.CV_32F);
				this.histogram = new Mat();
			} else {
				//this.histogram.create(1, imgMat.channels() * width, CvType.CV_32F);
			}
			
			this.updateHistogram();
		}
	}
		
	private void drawRGBHist(Canvas canvas) {
		Log.d(TAG, "Drawing RGB - Histogram");
		
	    //clear previous drawings => must be black because of the additive color blending
        canvas.drawColor(Color.BLACK);
        
        
        // calculate the needed stroke width
        strokeWidth = (width > 256) ? (float)width/256f : 1f;
        
        for (int channel = 0; channel < 3; channel++) {      
        	Imgproc.calcHist(Arrays.asList(imgMat), new MatOfInt(channel), new Mat(), histogram, new MatOfInt(histNumBins), histRange);
    		Core.normalize(histogram, histogram, 0.0f, (float)histMaxHeight, Core.NORM_MINMAX);
            		
    		// Get the histogram values
            histogram.get(0, 0, histValues);        	
        	
            paintHist[channel].setStrokeWidth(strokeWidth);
            
        	for (int bin = 0; bin < histNumBins; bin++) {
        		canvas.drawLine(histStartX+bin*strokeWidth, histMaxHeight, histStartX+bin*strokeWidth, histMaxHeight-(int)histValues[bin], paintHist[channel]);
        	}
        }
	}
	
	public void updateHistogram() {				
		Canvas canvas = null;		
		try {
            canvas = surfaceHolder.lockCanvas(null);
            
            if(isCreated) {
                drawRGBHist(canvas);
            }
        }
        catch(Exception e) {
            Log.e("SurfaceView", "exception");
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
		// Reinit the size
		this.width = width;
		this.height = height;
		this.histMaxHeight = height;
		
		if (width > 256)
			this.histNumBins = 256;
		else
			this.histNumBins = width;
		this.histValues = new float[histNumBins];
		
		Log.d(TAG, "Surface changed, width: " + width);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		isCreated = true;
		
		Log.d(TAG, "Surface created");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		isCreated = false;
	}
		
	
}
