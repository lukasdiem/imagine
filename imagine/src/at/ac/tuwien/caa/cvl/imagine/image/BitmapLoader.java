package at.ac.tuwien.caa.cvl.imagine.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import at.ac.tuwien.caa.cvl.imagine.image.BitmapLoader.TaskParams;

public class BitmapLoader extends AsyncTask<TaskParams, Void, Bitmap> {
	private static final String TAG = BitmapLoader.class.getSimpleName();
	
	private List<OnBitmapLoaded> listenerList = new ArrayList<OnBitmapLoaded>();
		
	private Bitmap decodeResizedBitmap(Context ctx, Uri uri, int targetWidth, int targetHeight) throws FileNotFoundException {
		// First decode with inJustDecodeBounds=true to check dimensions => does not load image!!!
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    
	    // Load the resource parameters
	    InputStream imageStream = ctx.getContentResolver().openInputStream(uri);
	    BitmapFactory.decodeStream(imageStream, null, options);
	    
	    // Close the opened stream
	    try {
			imageStream.close();
		} catch (IOException e) {
			Log.e(TAG, "Could not close the image input stream properly");
			e.printStackTrace();
		}
	    
	    
	    Log.d(TAG, "Target size: " + targetWidth + "x" + targetHeight + " px");
	    Log.d(TAG, "Decoded image size: " + options.outWidth + "x" + options.outHeight + " px");
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
	    
	    Log.d(TAG, "Resampled size: " + options.outWidth/options.inSampleSize + "x" + options.outHeight/options.inSampleSize + " px");

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;

	    // Reinitialize the stream
	    imageStream = ctx.getContentResolver().openInputStream(uri);
	    
	    return BitmapFactory.decodeStream(imageStream, null, options);
	}
	
	private static Bitmap decodeResizedBitmap(Resources res, int resId, int targetWidth, int targetHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions => does not load image!!!
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    
	    // Load the resource parameters
	    BitmapFactory.decodeResource(res, resId, options);
	    
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
			
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		
		// Return the loaded Bitmap to the listeners
		for (OnBitmapLoaded listener:listenerList) {
			listener.onBitmapLoaded(result);
		}
		
		Log.d(TAG, "Loaded the bitmap and notified possible listeners");
	}
	
	public void addOnBitmapLoadedListener(OnBitmapLoaded listener) {
		listenerList.add(listener);
	}

	public static class TaskParams {
	    Context ctx;
		Uri uri;
	    int targetWidth;
	    int targetHeight;
	    
	    TaskParams(Context ctx, Uri uri, int targetWidth, int targetHeight) {
	        this.ctx = ctx;
	    	this.uri = uri;
	        this.targetWidth = targetWidth;
	        this.targetHeight = targetHeight;
	    }
	}
	
	public interface OnBitmapLoaded {
		public void onBitmapLoaded(Bitmap bitmap);
	}

	@Override
	protected Bitmap doInBackground(TaskParams... params) {
		if (params == null || params.length == 0) {
			return null;
		} else {
			try {
				// Really load the image!!
				return this.decodeResizedBitmap(params[0].ctx, params[0].uri, params[0].targetWidth, params[0].targetHeight);
			} catch (FileNotFoundException e) {
				Log.w(TAG, "Could not loade the image file, because it could not be found: " + params[0].uri.toString());
				e.printStackTrace();
				return null;
			}
		}
	}
}

