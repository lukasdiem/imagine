package at.ac.tuwien.caa.cvl.imagine.ui.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;
import at.ac.tuwien.caa.cvl.imagine.image.OnImageChangedListener;
import at.ac.tuwien.caa.cvl.imagine.ui.activity.MainActivity.MainActivityGestureListener;
import at.ac.tuwien.caa.cvl.imagine.ui.view.HistogramView;
import at.ac.tuwien.caa.cvl.imagine.ui.view.PinchableImageView;

public class ImageViewFragment extends Fragment implements OnImageChangedListener, OnClickListener {
	private final static String TAG = ImageViewFragment.class.getSimpleName();
	
	private static final int INTENT_SELECT_IMAGE = 1;
	
	private ActionBar actionBar;
	
	private ImImage image;
	
	private PinchableImageView imageView;
	
	private ProgressBar imageViewLoading;
	
	private ImageButton btnOpenImage;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	// This view adds Options to the action bar!
    	this.setHasOptionsMenu(true);
    	
    	// Inflate the fragment design
    	return inflater.inflate(R.layout.fragment_image_view, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
    	// Get the progress bar
        imageViewLoading = (ProgressBar) getView().findViewById(R.id.pbImageView);
        
        // Initialize the image
        image = new ImImage(getView().getContext());
        image.setOnImageChangedListener(this);
        
        // Get the image view
        imageView = (PinchableImageView) getView().findViewById(R.id.pinchableImageView);
        if (imageView != null) {
        	imageView.setImage(image);
        }
        
        // Get the open image button
        btnOpenImage = (ImageButton) getView().findViewById(R.id.btnOpenImage);
        if (btnOpenImage != null) {
        	btnOpenImage.setOnClickListener(this);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "Inflating menu icons");
    	
    	inflater.inflate(R.menu.menu_actionbar_image_view, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		
        switch (itemId) {
        case R.id.action_open_image: openImage();
        	break;
        case R.id.action_save_image: saveImage();
        	break;
        default:
        	return super.onOptionsItemSelected(item);
        }
        
        return true;
    }
    
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnOpenImage) {
			openImage();
		}
	}
	
	public void saveImage() {
		if (!image.isImageLoaded()) {
			Log.w(TAG, "Can not save an image if nothing is loaded.");
			return;
		}
		
		Log.d(TAG, "Saving the image: " + image.getFullPath());
		
		// Load the directory
		File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Imagine/");
		// Create the directory tree if not already done
		path.mkdirs();
		
		// Save the image
		image.saveImage(path.getAbsolutePath());
	}
	
	public void openImage() {
    	Log.d(TAG, "Open image");
    	
    	// Start an image intent
    	Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        
        // Start the intent
        startActivityForResult(Intent.createChooser(intent, "Select an image via"), INTENT_SELECT_IMAGE);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.d(TAG, "Intent result received!"); 

    	switch (requestCode) {
	    	case INTENT_SELECT_IMAGE:
	    		if(resultCode == Activity.RESULT_OK) {      
	    			final Uri selectedImageUri = data.getData();
	    			Log.d(TAG, "Image uri: " + selectedImageUri.toString());
	    			
	    			image.loadImage(selectedImageUri);
	    			
	    			/*    				    				
    				// Sometimes the image view seems to be cleaned up during the user selects its image => reinitialize it!
					// Get the image view
				    final PinchableImageView finalImageView = (PinchableImageView) findViewById(R.id.pinchableImageView);
	    			//final Context callerContext = this; 
				    
				    ViewTreeObserver vto = finalImageView.getViewTreeObserver();
				    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				        @Override
				        public void onGlobalLayout() {
				        	image.loadImage(selectedImageUri);
				        	/*try {
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
				    */
	    		} else if (resultCode == Activity.RESULT_CANCELED) {
	    			// Inform the user that he cancelled the image selection
	    			Toast.makeText(this.getView().getContext(), "Image selection cancelled.", Toast.LENGTH_SHORT).show();
	    		}
	    		break;    		
    	}
    }

	@Override
	public void onImageManipulated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoadingNewImage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewImageLoaded() {
		// TODO Auto-generated method stub
		
	}
}
