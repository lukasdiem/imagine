package at.ac.tuwien.caa.cvl.imagine.image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import at.ac.tuwien.caa.cvl.imagine.image.BitmapLoader.OnBitmapLoaded;
import at.ac.tuwien.caa.cvl.imagine.utils.FileUtils;

public class ImImage implements OnBitmapLoaded {
	//@SuppressWarnings("unused")
	private static final String TAG = ImImage.class.getSimpleName();
	private static final int UNKNOWN_SIZE = -1;
	private static final int DOWNSAMPLED_SIZE = 1024;
	
	protected Context 	context;
	
	private ImImageLoader loaderRunnable;
		
	// The listeners
	private List<OnImageChangedListener> onImageChangeListenerList;
	
	protected boolean	openCvLoaded = false;
	protected boolean	pendingImageToLoad = false;
	protected boolean	imageLoaded = false;
	
	// Base path
	protected String 	imagePath;
	protected Uri		imageUri;
	protected String	imageName;
	protected String	imageFileExt;
	
	// The real images
	protected Bitmap 	scaledBitmap;
	protected Mat	 	origImageMat;
	protected Mat		procImageMat;
	
	// Exif meta information
	protected ExifInterface exifData;
	protected String 		exifDate;
	protected int 			exifOrientation;
	protected float 		exifRotation;
	protected Matrix		exifRotationMatrix;

	// Size vars
	protected int width = UNKNOWN_SIZE;
	protected int height = UNKNOWN_SIZE;
	protected int scaledBitmapWidth = UNKNOWN_SIZE;
	protected int scaledBitmapHeight = UNKNOWN_SIZE;
	
	// Bitmap loader
	protected BitmapLoader bitmapLoader;
	protected BitmapLoader.TaskParams bitmapLoaderParams;
	
	public ImImage(Context context) {
		initialize(context);
	}
	
	public ImImage(Context context, Uri uri) {
		initialize(context);
		
		// Initialize the file paths
		this.imageUri = uri;
		this.imagePath = FileUtils.getRealPathFromURI(context, uri);
		this.imageName = uri.getLastPathSegment();
		
		// Read in the image size
		updateImageSize();
	}
	
	private void initialize(Context context) {
		this.context = context;
		this.exifRotationMatrix = new Matrix();
		
		onImageChangeListenerList = new ArrayList<OnImageChangedListener>();
	}
	
	private void updateImageSize() {
		// First decode with inJustDecodeBounds=true to check dimensions => does not load image!!!
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    
		try {
			// Load the resource parameters
		    InputStream imageStream;
			imageStream = context.getContentResolver().openInputStream(imageUri);
			BitmapFactory.decodeStream(imageStream, null, options);
	    
			// Close the opened stream
			imageStream.close();
			
			// Load the exif information to correctly flip the width and height
			loadExifInformation();
			
			width = options.outWidth;
			height = options.outHeight;
			
			// Change width and height if the orientation is 90 or 270			
			/*if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 || 
					exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {

				width = options.outHeight;
				height = options.outWidth;
			}*/
		} catch (FileNotFoundException eFnF) {
			Log.e(TAG, "Could not read width and height from the file");
			eFnF.printStackTrace();
		}  catch (IOException eIo) {
			Log.e(TAG, "Could not close the image input stream properly");
			eIo.printStackTrace();
		}	    
	}
	
	public ExifInterface loadExifInformation() {
		try {
			exifData = new ExifInterface(imagePath);
			
			exifOrientation = exifData.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			exifRotation = exifOrientationToRotation(exifOrientation);
			exifDate = exifData.getAttribute(ExifInterface.TAG_DATETIME);
		} catch (IOException e) {
			exifData = null;
			Log.w(TAG, "Could not load the exif information");
			e.printStackTrace();
		}
		
		return exifData;
	}
	
	private float exifOrientationToRotation(int exifOrientation) {
		float rotation = 0f;
		
		switch(exifOrientation) {
			case ExifInterface.ORIENTATION_NORMAL: rotation = 0f;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90: rotation = 90f;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180: rotation = 180f;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270: rotation = 270f;
				break;
			default: Log.d(TAG, "Do not knot the exif orientation flag: " + exifOrientation);
		}
		
		// Set the rotation matrix accordingly
		exifRotationMatrix.setRotate(rotation);
		
		return rotation;
	}
	
	public void changeBrightnessContrast(float brightness, float contrast) {
		long startTime = System.currentTimeMillis();
			
		origImageMat.convertTo(procImageMat, -1, contrast, brightness);
		
		long startMatBit = System.currentTimeMillis();
		Utils.matToBitmap(procImageMat, scaledBitmap);
		long endTime = System.currentTimeMillis();
		
		Log.d(TAG, "Mat2Bitmap took: " + (endTime-startMatBit) + " ms");
		Log.d(TAG, "Changing Brightness/Contrast took: " + (endTime - startTime) + " ms");
		
		this.notifyOnImageManipulated();
	}
	
	public void convertToGrayscale() {
    	Mat tmpMat = new Mat();
		Utils.bitmapToMat(scaledBitmap, tmpMat);
    	Imgproc.cvtColor(tmpMat, tmpMat, Imgproc.COLOR_BGR2GRAY);
    	//Imgproc.GaussianBlur(imageMat, imageMat, new Size(3, 3), 0);
    	//Imgproc.adaptiveThreshold(imageMat, imageMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 4);
    	Utils.matToBitmap(tmpMat, scaledBitmap);
    	// Cleanup
    	origImageMat.release();
    	
    	this.notifyOnImageManipulated();
	}
	
	public void cartoonize2() {
		/*Mat tmpMat = new Mat();
		procImageMat.convertTo(tmpMat, CvType.CV_32FC3);
		tmpMat = tmpMat.reshape(1, tmpMat.rows()*tmpMat.cols());
		
		Mat labels = new Mat();
		Core.kmeans(tmpMat, 10, labels, criteria, attempts, flags)*/
		
		
		Imgproc.GaussianBlur( procImageMat, procImageMat, new Size(3,3), 0);
		
		Mat grayscale = new Mat(procImageMat.rows(), procImageMat.cols(), CvType.CV_8U);
		Imgproc.cvtColor(procImageMat, grayscale, Imgproc.COLOR_RGB2GRAY);
		
		Mat gradX = new Mat();
		Mat gradY = new Mat();
		Mat grad = new Mat(procImageMat.rows(), procImageMat.cols(), CvType.CV_8UC1);
		
		Imgproc.Sobel(grayscale, gradX, CvType.CV_16S, 1, 0, 3, 1, 0);
		Core.convertScaleAbs(gradX, gradX);
		
		Imgproc.Sobel(grayscale, gradY, CvType.CV_16S, 0, 1, 3, 1, 0);
		Core.convertScaleAbs(gradY, gradY);
		
		Core.addWeighted(gradX, 0.5, gradY, 0.5, 0, grad);
		
		

		//Imgproc.Canny(procImageMat, edgeMat, 100, 150);
		Imgproc.medianBlur(procImageMat, procImageMat, 11);
		
		Imgproc.cvtColor(grad, grad, Imgproc.COLOR_GRAY2RGB);
		Core.add(procImageMat, grad, procImageMat);
		
		// Update the bitmap
		//Utils.matToBitmap(procImageMat, scaledBitmap);
		Utils.matToBitmap(procImageMat, scaledBitmap);
		
		this.notifyOnImageManipulated();
	}
	
	public void cartoonize(int colorCount, float edgeWeight, int edgeThickness) {
		ImJniImageProcessing.cartoonize(procImageMat.nativeObj, procImageMat.nativeObj, colorCount, edgeWeight, edgeThickness);
		
		Utils.matToBitmap(procImageMat, scaledBitmap);
		
		this.notifyOnImageManipulated();
	}
	
	public void edgeEffect(int edgeThickness) {
		ImJniImageProcessing.edgeEffect(procImageMat.nativeObj, procImageMat.nativeObj, edgeThickness);
		
		Imgproc.cvtColor(procImageMat, procImageMat, Imgproc.COLOR_GRAY2RGB);
		
		Utils.matToBitmap(procImageMat, scaledBitmap);
		
		this.notifyOnImageManipulated();
	}
	
	public void sepiaEffect() {
		ImJniImageProcessing.sepiaEffect(procImageMat.nativeObj, procImageMat.nativeObj);
		
		Utils.matToBitmap(procImageMat, scaledBitmap);
		
		origImageMat.release();
		origImageMat = procImageMat.clone();
		
		this.notifyOnImageManipulated();
	}
	
	public void reset() {
		this.width = UNKNOWN_SIZE;
		this.height = UNKNOWN_SIZE;
		this.exifOrientation = 0;
		this.exifDate = "";
		this.exifRotation = 0;
		this.exifRotationMatrix.reset();
	}
	
	/*-----------------------------------------------------------------------------
	 * GETTER/SETTER
	 *-----------------------------------------------------------------------------*/
	public void loadImage(Uri uri) {
		reset();
		
		// Initialize the file paths
		this.imageUri = uri;
		this.imagePath = FileUtils.getRealPathFromURI(context, uri);
		this.imageFileExt = imagePath.substring(imagePath.lastIndexOf(".") + 1, imagePath.length());
		this.imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length() - imageFileExt.length() - 1);
		
		Log.d(TAG, "Image name: " + imageName);
		Log.d(TAG, "Image extension: " + imageFileExt);
		
		if (openCvLoaded) {
			loadImage();
		} else {
			pendingImageToLoad = true;
		}
		
	}
	
	/*private void loadImage() {
		pendingImageToLoad = false;
		
		notifyOnLoadingNewImage();
				
		// Read in the image size
		updateImageSize();
		
		Log.d(TAG, "New image size: " + width + "x" + height);
		
		// TODO load bitmap/load Mat
		if (scaledBitmapHeight == UNKNOWN_SIZE || scaledBitmapWidth == UNKNOWN_SIZE) {
			bitmapLoaderParams = new BitmapLoader.TaskParams(context, imageUri, width, height);
		} else {
			bitmapLoaderParams = new BitmapLoader.TaskParams(context, imageUri, scaledBitmapWidth, scaledBitmapHeight);
		}
		
		// execute the task
		bitmapLoader = new BitmapLoader();
		bitmapLoader.addOnBitmapLoadedListener(this);
		
		// execute the loading task
		bitmapLoader.execute(bitmapLoaderParams);
		
		// load the opencv mat
		origImageMat = Highgui.imread(imagePath);
		
		if (origImageMat.empty()) {
			Log.w(TAG, "Could not load the image to a OpenCV mat.");
			imageLoaded = false;
		} else {
			Imgproc.cvtColor(origImageMat, origImageMat, Imgproc.COLOR_BGR2RGB);
			procImageMat = origImageMat.clone();
			imageLoaded = true;
		}
		
		notifyOnNewImageLoaded();
	}*/
	
	private void loadImage() {
		pendingImageToLoad = false;
		
		notifyOnLoadingNewImage();
				
		if (loaderRunnable == null) {
			loaderRunnable = new ImImageLoader();
		}
		
		new Thread(loaderRunnable).start();
		
		//notifyOnNewImageLoaded();
	}
	
	public boolean saveImage(String path) {
		if (imageLoaded) {
			String fullSavePath = path + "/" + imageName + "." + imageFileExt;
			
			// TODO: This check does not work => compare if the file is overwritten in the destination!!!
			// TODO: Do this check in the activity to notify the user properly
			if (imagePath != fullSavePath) {
				try {
					Log.d(TAG, "Trying to save: " + imageName);
					Log.d(TAG, "Save location: " + fullSavePath);
					
					Mat saveImageMat = new Mat();
										
					if (!procImageMat.empty()) {
						Imgproc.cvtColor(procImageMat, saveImageMat, Imgproc.COLOR_RGB2BGR);
					} else if (!origImageMat.empty()) {
						Imgproc.cvtColor(origImageMat, saveImageMat, Imgproc.COLOR_RGB2BGR);
					} else {
						Log.e(TAG, "Could not save the image, because both the original and manipulated image are empty");
						return false;
					}
					
					Highgui.imwrite(fullSavePath, saveImageMat);
				} catch (Exception e) {
					Log.e(TAG, "I am sorry I could not save the image.");
					e.printStackTrace();
				}
			} else {
				Log.w(TAG, "Can not save file, because it already exists: " + imageName);
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public Mat getManipulatedImageMat() {
		return procImageMat;
	}
	
	
	public int getScaledBitmapWidth() {
		return scaledBitmapWidth;
	}

	public void setScaledBitmapWidth(int scaledBitmapWidth) {
		this.scaledBitmapWidth = scaledBitmapWidth;
	}

	public int getScaledBitmapHeight() {
		return scaledBitmapHeight;
	}

	public void setScaledBitmapHeight(int scaledBitmapHeight) {
		this.scaledBitmapHeight = scaledBitmapHeight;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getFullPath() {
		return imagePath;
	}
	
	public InputStream getFullSizeImageStream() throws FileNotFoundException {
		return context.getContentResolver().openInputStream(imageUri);
	}
	
	public Uri getFullSizeImageUri() {
		return imageUri;
	}
	
	public Bitmap getScaledBitmap() {
		return scaledBitmap;
	}
		
	public Matrix getExifRotationMatrix() {
		return exifRotationMatrix;
	}
	
	public void setOpenCvLoaded(boolean loaded) {
		this.openCvLoaded = loaded;
		
		// Load an image that has been set bevore the opencv library was fully available
		if (loaded && pendingImageToLoad)
			loadImage();
	}
	
	public boolean isImageLoaded() {
		return imageLoaded;
	}

	/*-----------------------------------------------------------------------------
	 * Listeners
	 *-----------------------------------------------------------------------------*/	
	@Override
	public void onBitmapLoaded(Bitmap bitmap) {
		this.scaledBitmap = bitmap;
		//notifyOnNewImageLoaded();		
	}
	
	/*-----------------------------------------------------------------------------
	 * Listener interface implementation
	 *-----------------------------------------------------------------------------*/
	public void setOnImageChangedListener(OnImageChangedListener listener) {
		if (!onImageChangeListenerList.contains(listener))
			onImageChangeListenerList.add(listener);
	}
	
	private void notifyOnLoadingNewImage() {
		Log.d(TAG, "Notifying " + onImageChangeListenerList.size() + " listernes: Loading image");		
		for (OnImageChangedListener listener:onImageChangeListenerList) {
			listener.onLoadingNewImage();
		}
	}
	
	private void notifyOnNewImageLoaded() {
		Log.d(TAG, "Notifying " + onImageChangeListenerList.size() + " listernes: Image Loaded completly");
		for (OnImageChangedListener listener:onImageChangeListenerList) {
			listener.onNewImageLoaded();
		}
	}
	
	private void notifyOnImageManipulated() {
		for (OnImageChangedListener listener:onImageChangeListenerList) {
			listener.onImageManipulated();
		}
	}
	
	
	class ImImageLoader implements Runnable {
		@Override
		public void run() {
			// Update the image size
			updateImageSize();
			
			Log.d(TAG, "Exif orientation: " + exifRotation);
			
			// Calculate the downsampled size
			calcDownsampledSize();
			
			if (origImageMat == null) origImageMat = new Mat();
			
			// Load the image with opencv			
			ImJniImageProcessing.loadImage(imagePath, origImageMat.nativeObj, scaledBitmapWidth, scaledBitmapHeight, exifRotation);
			procImageMat = origImageMat.clone();
			
			if (scaledBitmap != null) {
				scaledBitmap.recycle();
			}
			
			scaledBitmap = Bitmap.createBitmap(scaledBitmapWidth, scaledBitmapHeight, Config.ARGB_8888);
			
			Utils.matToBitmap(origImageMat, scaledBitmap);
			
			// Notify the listeners that a new image is available
			/*activity.runOnUiThread(new Runnable() {
			    public void run() {
			        Toast.makeText(activity, "Hello, world!", Toast.LENGTH_SHORT).show();
			    }
			});*/
			
			imageLoaded = true;
			
			((Activity)context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					notifyOnNewImageLoaded();
				}
			});
			
		}
		
		private void calcDownsampledSize() {
			if (width > height) {
				scaledBitmapWidth = DOWNSAMPLED_SIZE;
				scaledBitmapHeight = (int)Math.floor((double)height * (double)DOWNSAMPLED_SIZE / (double)width);
			} else {
				scaledBitmapWidth = (int)Math.floor((double)width * (double)DOWNSAMPLED_SIZE / (double)height);
				scaledBitmapHeight= DOWNSAMPLED_SIZE;
			}
			
			Log.d(TAG, "Downsampled size: " + scaledBitmapWidth + "x" + scaledBitmapHeight);
			Log.d(TAG, "Original size:    " + width + "x" + height);
		}
	}
}
