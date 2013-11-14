package at.ac.tuwien.caa.cvl.imagine.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.image.BitmapLoader;
import at.ac.tuwien.caa.cvl.imagine.ui.view.PinchableImageView;
import at.ac.tuwien.caa.cvl.imagine.utils.FileUtils;

public class MainActivity extends ActionBarActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final int INTENT_SELECT_IMAGE = 1;
	
	private PinchableImageView imageView;
	private int imageViewWidth;
	private int imageViewHeight;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Get the image view
        imageView = (PinchableImageView) findViewById(R.id.pinchableImageView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_open_image: {
                openImage();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
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
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "Intent result received!"); 

    	switch (requestCode) {
	    	case INTENT_SELECT_IMAGE:
	    		if(resultCode == RESULT_OK) {      
	    			Uri selectedImageUri = data.getData();
	    			Log.d(TAG, "Image uri: " + selectedImageUri.toString());
	    			
	    			try {	    				
	    				// NEVER try to access the imageview here => sometimes it is not loaded => nullpointer
						Bitmap downsampledBitmap = BitmapLoader.decodeResizedBitmap(this, selectedImageUri, imageViewWidth, imageViewHeight);
						
						// Sometimes the image view seems to be cleaned up during the user selects its image => reinitialize it!
						if (imageView == null) {
							// Get the image view
					        imageView = (PinchableImageView) findViewById(R.id.pinchableImageView);
						}
						
						String imagePath = FileUtils.getRealPathFromURI(this, selectedImageUri);
						
						float imageOrientation = 0.0f;
						try {
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
						} catch (IOException e) {
							Log.e(TAG, "Could not read the exif information!");
							e.printStackTrace();
						}
						
						imageView.setImageBitmap(downsampledBitmap);
						imageView.setOrientation(imageOrientation);
					} catch (FileNotFoundException e1) {
						Log.w(TAG, "Could not load image from: " + selectedImageUri.toString());
						Toast.makeText(this, "Sorry, couldn't load the image!", Toast.LENGTH_SHORT);
					}
	    			
	    		} else if (resultCode == RESULT_CANCELED) {
	    			// Inform the user that he cancelled the image selection
	    			Toast.makeText(this, "Image selection cancelled.", Toast.LENGTH_SHORT).show();
	    		}
	    		break;    		
    	}
    }
}
