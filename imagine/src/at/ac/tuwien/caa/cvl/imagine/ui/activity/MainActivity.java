package at.ac.tuwien.caa.cvl.imagine.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.image.BitmapLoader;
import at.ac.tuwien.caa.cvl.imagine.ui.view.HistogramView;
import at.ac.tuwien.caa.cvl.imagine.ui.view.PinchableImageView;
import at.ac.tuwien.caa.cvl.imagine.utils.FileUtils;

public class MainActivity extends ActionBarActivity  {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final int INTENT_SELECT_IMAGE = 1;
	
	private PinchableImageView imageView;
	private int imageViewWidth;
	private int imageViewHeight;
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i(TAG, "OpenCV loaded successfully");
	                
	                // Get the histogram view
	                HistogramView histView = (HistogramView) findViewById(R.id.histView);
	                
	                if (histView != null) {
	                	Bitmap imageBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
	                	Mat imageMat = new Mat(imageBitmap.getWidth(), imageBitmap.getHeight(), CvType.CV_8UC3);
	                	Utils.bitmapToMat(imageBitmap, imageMat);
		                		                
		                histView.setImageMat(imageMat);
	                }
	                
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get the image view
        imageView = (PinchableImageView) findViewById(R.id.pinchableImageView);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback)) {
    	    Log.e(TAG, "Cannot connect to OpenCV Manager");
    	}
    	/*if (!OpenCVLoader.initDebug()) {
    	    Log.e("TEST", "Cannot connect to OpenCV Manager");
    	}*/
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.action_open_image) {
			openImage();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
    
    public void openImage() {
    	Log.d(TAG, "Open image");
    	    	
    	imageViewWidth = (int)imageView.getViewBounds().width();
    	imageViewHeight = (int)imageView.getViewBounds().height();
    	
    	//Log.d(TAG, "View size: " + imageViewWidth + ", " + imageViewHeight);
    	
    	// Start an image intent
    	Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        
        // Start the intent
        startActivityForResult(Intent.createChooser(intent, "Select an image via"), INTENT_SELECT_IMAGE);
    }
    
    @SuppressLint("NewApi")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "Intent result received!"); 

    	switch (requestCode) {
	    	case INTENT_SELECT_IMAGE:
	    		if(resultCode == RESULT_OK) {      
	    			final Uri selectedImageUri = data.getData();
	    			Log.d(TAG, "Image uri: " + selectedImageUri.toString());
	    			
    				    				
    				// Sometimes the image view seems to be cleaned up during the user selects its image => reinitialize it!
					// Get the image view
				    final PinchableImageView finalImageView = (PinchableImageView) findViewById(R.id.pinchableImageView);
					final Context callerContext = this; 
				    
				    ViewTreeObserver vto = finalImageView.getViewTreeObserver();
				    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				        @Override
				        public void onGlobalLayout() {
				        	try {
					        	// NEVER try to access the imageview here => sometimes it is not loaded => nullpointer
								Bitmap downsampledBitmap = BitmapLoader.decodeResizedBitmap(callerContext, 
										selectedImageUri, finalImageView.getWidth(), finalImageView.getHeight());
								
								String imagePath = FileUtils.getRealPathFromURI(callerContext, selectedImageUri);
								
								float imageOrientation = 0.0f;
								ExifInterface exif = new ExifInterface(imagePath);
								int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
															
								switch (exifOrientation) {
									case ExifInterface.ORIENTATION_ROTATE_90: imageOrientation = 90.0f;
										break;
									case ExifInterface.ORIENTATION_ROTATE_180: imageOrientation = 180.0f;
										break;
									case ExifInterface.ORIENTATION_ROTATE_270: imageOrientation = 270.0f;
										break;
								}
								
								imageView.setImageBitmap(downsampledBitmap);
								imageView.setImageRotation(imageOrientation);
							} catch (FileNotFoundException e1) {
								Log.w(TAG, "Could not load image from: " + selectedImageUri.toString());
								Toast.makeText(callerContext, "Sorry, couldn't load the image!", Toast.LENGTH_SHORT).show();
							} catch (IOException e) {
								Log.e(TAG, "Could not read the exif information!");
								e.printStackTrace();
							} 
				        	
				            ViewTreeObserver obs = finalImageView.getViewTreeObserver();
				            // Remove the view tree observer such that is not called again
				            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				                obs.removeOnGlobalLayoutListener(this);
				            } else {
				                obs.removeGlobalOnLayoutListener(this);
				            }
				        }
				    });
	    		} else if (resultCode == RESULT_CANCELED) {
	    			// Inform the user that he cancelled the image selection
	    			Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
	    		}
	    		break;    		
    	}
    }

}
