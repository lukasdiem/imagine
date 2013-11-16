package at.ac.tuwien.caa.cvl.imagine.image;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;
import at.ac.tuwien.caa.cvl.imagine.utils.FileUtils;



public class ImImage {
	//@SuppressWarnings("unused")
	private static final String TAG = ImImage.class.getSimpleName();
	private static final int UNKNOWN_SIZE = -1;
	
	protected Context context;
	
	// Base path
	protected String imagePath;
	protected Uri imageUri;
	protected String imageName;
	
	// Exif meta information
	protected ExifInterface exifData;
	protected String exifDate;
	protected int exifOrientation;
	protected float exifRotation;
	
	// Size vars
	protected int width = UNKNOWN_SIZE;
	protected int height = UNKNOWN_SIZE;
	
	public ImImage(Context context, Uri uri) {
		this.context = context;
		this.imageUri = uri;
		this.imagePath = FileUtils.getRealPathFromURI(context, uri);
		this.imageName = uri.getLastPathSegment();
		
		loadExifInformation();
		updateImageSize();
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
			
			if (exifOrientation == ExifInterface.ORIENTATION_NORMAL || 
					exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
				width = options.outWidth;
				height = options.outHeight;
			} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 || 
					exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
				// Change width and height if the orientation is 90 or 270
				width = options.outWidth;
				height = options.outHeight;
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
	
	private static float exifOrientationToRotation(int exifOrientation) {
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
		
		return rotation;
	}
	
	public int getWidth() {
		return 0;
	}
	
	public int getHeight() {
		return 0;
	}
	
	public String getFullPath() {
		return null;
	}
	
	public InputStream getFullSizeImageStream() {
		return null;
	}
	
	public Uri getFullSizeImageUri() {
		return null;
	}
	
	public Bitmap getScaledBitmap(int targetWidth, int targetHeight) {
		return null;
	}
	
	
}
