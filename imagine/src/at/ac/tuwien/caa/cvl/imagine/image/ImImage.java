package at.ac.tuwien.caa.cvl.imagine.image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
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
	
	protected Context 	context;
	
	// The listeners
	private List<OnImageChangedListener> onImageChangeListenerList;
	
	// Base path
	protected String 	imagePath;
	protected Uri		imageUri;
	protected String	imageName;
	
	// The real images
	protected Bitmap 	scaledBitmap;
	protected Mat	 	imageMat;
	
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
			if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 || 
					exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {

				width = options.outHeight;
				height = options.outWidth;
			}
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
		float rotation = 0;
		
		switch(exifOrientation) {
			case ExifInterface.ORIENTATION_NORMAL: rotation = 0;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90: rotation = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180: rotation = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270: rotation = 270;
				break;
			default: Log.d(TAG, "Do not knot the exif orientation flag: " + exifOrientation);
		}
		
		// Set the rotation matrix accordingly
		exifRotationMatrix.setRotate(rotation);
		
		return rotation;
	}
	
	public void changeBrightnessContrast(float brightness, float contrast) {
		byte buff[] = new byte[(int)imageMat.total() * imageMat.channels()];
		
		// Read the data
		imageMat.get(0, 0, buff);
		
		float val;
		for (int i=0; i < buff.length; i++) {
			val = contrast * (float)buff[i] + brightness;
			
			if (val > 255)
				buff[i] = (byte)255;
			else if (val < 0)
				buff[i] = 0;
			else
				buff[i] = (byte)Math.round(val);
		}
		
		imageMat.put(0, 0, buff);
		
		Utils.matToBitmap(imageMat, scaledBitmap);
		
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
    	imageMat.release();
    	
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
		notifyOnLoadingNewImage();
		
		reset();
		
		// Initialize the file paths
		this.imageUri = uri;
		this.imagePath = FileUtils.getRealPathFromURI(context, uri);
		this.imageName = uri.getLastPathSegment();
		
		// Read in the image size
		updateImageSize();
		
		Log.d(TAG, "New image size: " + width + "x" + height);
		
		// TODO load bitmap/load Mat
		if (scaledBitmapHeight == UNKNOWN_SIZE || scaledBitmapWidth == UNKNOWN_SIZE) {
			bitmapLoaderParams = new BitmapLoader.TaskParams(context, uri, width, height);
		} else {
			bitmapLoaderParams = new BitmapLoader.TaskParams(context, uri, scaledBitmapWidth, scaledBitmapHeight);
		}
		
		// execute the task
		bitmapLoader = new BitmapLoader();
		bitmapLoader.addOnBitmapLoadedListener(this);
		
		// execute the loading task
		bitmapLoader.execute(bitmapLoaderParams);
		
		// load the opencv mat
		imageMat = Highgui.imread(imagePath);
		
		if (imageMat.empty()) {
			Log.w(TAG, "Could not load the image to a OpenCV mat.");
		} else {
			Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2RGB);
		}
		
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

	/*-----------------------------------------------------------------------------
	 * Listeners
	 *-----------------------------------------------------------------------------*/	
	@Override
	public void onBitmapLoaded(Bitmap bitmap) {
		this.scaledBitmap = bitmap;
		notifyOnNewImageLoaded();		
	}
	
	/*-----------------------------------------------------------------------------
	 * Listener interface implementation
	 *-----------------------------------------------------------------------------*/
	public void addOnImageChangedListener(OnImageChangedListener listener) {
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
}
