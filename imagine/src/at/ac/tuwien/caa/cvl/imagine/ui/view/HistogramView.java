package at.ac.tuwien.caa.cvl.imagine.ui.view;

import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class HistogramView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = HistogramView.class.getSimpleName();
	
	protected Context context;
	protected SurfaceHolder surfaceHolder;
	protected boolean isCreated = false;
	
	protected int width = -1;
	protected int height = -1;
	
	private int histStartX;
	private int histStartY;
	private int histMaxHeight;
	
	protected Mat imgMat;
	protected int imgChannels;
	protected int histNumBins;
	protected MatOfFloat histRange;
	
	protected Mat histogram;
	protected float[] histValues;
	
	private Paint[] paintHist;
	
	private enum HistColor {
		RED, GREEN, BLUE, GENERIC
	};
	
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
	}
	
	private void initializePaint() {
		paintHist = new Paint[HistColor.values().length];
		
		paintHist[HistColor.RED.ordinal()] = new Paint();
		paintHist[HistColor.RED.ordinal()].setColor(Color.RED);
		
		paintHist[HistColor.GREEN.ordinal()] = new Paint();
		paintHist[HistColor.GREEN.ordinal()].setColor(Color.GREEN);
		
		paintHist[HistColor.BLUE.ordinal()] = new Paint();
		paintHist[HistColor.BLUE.ordinal()].setColor(Color.BLUE);
		
		paintHist[HistColor.GENERIC.ordinal()] = new Paint();
		paintHist[HistColor.GENERIC.ordinal()].setColor(Color.GRAY);
	}
	
	public void setImageMat(Mat imageMat) {
		this.imgMat = imageMat;
		
		if (imgMat.channels() > 3) {
			this.imgChannels = 3;
		} else {			
			this.imgChannels = imgMat.channels();
		}
		
		this.histRange = new MatOfFloat(0, 255);
		this.histValues = new float[histNumBins*imgChannels];
		
		if (isCreated) {
			if (this.histogram == null) {
				this.histogram = new Mat(1, imgMat.channels() * width, CvType.CV_32F);
			} else {
				this.histogram.create(1, imgMat.channels() * width, CvType.CV_32F);
			}
			
			this.updateHistogram();
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
		if (histogram != null && !histogram.empty()) {
			if (imgMat.channels() > 3 || imgMat.channels() == 3) {
				drawRGBHist(canvas);
			} else {
				Log.w(TAG, "By now only RGB histograms are implemented!");
			}
		}
	}
	
	private void drawRGBHist(Canvas canvas) {
		Log.d(TAG, "Drawing RGB - Histogram");
		
	    //clear previous drawings
        canvas.drawColor(Color.WHITE);
        
        for (int channel = 0; channel < 3; channel++) {        	
        	for (int bin = 0; bin < histNumBins; bin++) {
        		canvas.drawLine(histStartX, histMaxHeight, histStartX, (int)histValues[channel*histNumBins+bin], paintHist[channel]);
        	}
        }
	}
	
	private void drawGenericHist(Canvas canvas) {
		
	}
	
	public void updateHistogram() {		
		// Update the histogram values
		Imgproc.calcHist(Arrays.asList(imgMat), new MatOfInt(imgChannels), new Mat(), histogram, new MatOfInt(histNumBins), histRange);
		//Core.normalize(histogram, histogram, height, 0, Core.NORM_INF);
        // Get the histogram values
        histogram.get(0, 0, histValues);
		
		Canvas canvas = null;		
		try {
            canvas = surfaceHolder.lockCanvas(null);
            
            if(isCreated) {
                draw(canvas);
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
		this.histNumBins = width;
		this.histValues = new float[histNumBins*imgChannels];
		
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
