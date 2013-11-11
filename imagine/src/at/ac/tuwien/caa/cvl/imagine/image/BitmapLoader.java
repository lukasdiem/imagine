package at.ac.tuwien.caa.cvl.imagine.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class BitmapLoader {
	private static final String TAG = BitmapLoader.class.getSimpleName();
	
	public static Bitmap decodeResizedBitmap(Context ctx, Uri uri, int targetWidth, int targetHeight) throws FileNotFoundException {
		// First decode with inJustDecodeBounds=true to check dimensions => does not load image!!!
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    
	    // Load the resource parameters
	    InputStream imageStream = ctx.getContentResolver().openInputStream(uri);
	    BitmapFactory.decodeStream(imageStream, null, options);
	    
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
	
	public static Bitmap decodeResizedBitmap(Resources res, int resId, int targetWidth, int targetHeight) {
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
}
