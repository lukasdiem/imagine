package at.ac.tuwien.caa.cvl.imagine.ui.fragment;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;
import at.ac.tuwien.caa.cvl.imagine.image.OnImageChangedListener;
import at.ac.tuwien.caa.cvl.imagine.ui.view.PinchableImageView;

public class ImageViewFragment extends Fragment implements OnClickListener, OnImageChangedListener {
	private final static String TAG = ImageViewFragment.class.getSimpleName();
	
	private static final int INTENT_SELECT_IMAGE = 1;
	
	private ImImage image;
	
	private PinchableImageView imageView;
	
	private ProgressBar imageViewLoading;
	
	private ImageButton btnOpenImage;
	
	private ActionBar actionBar;
	
	private OnImageViewAttachedListener imageViewAttachedListener; 
	
	private BaseLoaderCallback openCvLoaderCallback = new BaseLoaderCallback(this.getActivity()) {
	    @Override
	    public void onManagerConnected(int status) {
	    	removeLoadingState();
	    	
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS: {
	                Log.i(TAG, "OpenCV loaded successfully");
	                // Set the correct state!
	                image.setOpenCvLoaded(true);
	            } break;
	            default: {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
	
    public interface OnImageViewAttachedListener {
        public void onImageViewAttachedListener(ImImage image);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            imageViewAttachedListener = (OnImageViewAttachedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnImageLoadListener");
        }
    }
	
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
        imageViewAttachedListener.onImageViewAttachedListener(image);
        
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
        
        // Get the actionbar
        actionBar = ((ActionBarActivity)this.getActivity()).getSupportActionBar();
        if (actionBar != null && imageView != null) {
        	imageView.setActionBar(actionBar);
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    	showLoadingState();
    	
    	image.setOpenCvLoaded(false);
    	
    	if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this.getView().getContext(), openCvLoaderCallback)) {
    	    Log.e(TAG, "Cannot connect to the OpenCV Manager");
    	    
    	    removeLoadingState();
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
	    			// Set the call to load the image
	    			if (image != null) {
	    				image.loadImage(data.getData());
	    			}
	    		} else if (resultCode == Activity.RESULT_CANCELED) {
	    			// Inform the user that he cancelled the image selection
	    			Toast.makeText(this.getView().getContext(), "Image selection cancelled.", Toast.LENGTH_SHORT).show();
	    		}
	    		break;    		
    	}
    }
    
    private void showLoadingState() {
    	imageViewLoading.setVisibility(View.VISIBLE);
    	btnOpenImage.setVisibility(View.GONE);
    	
    	Log.d(TAG, "Showing loading spinner...");
    	// TODO: Disable the whole user interface
    }
    
    private void removeLoadingState() {
    	imageViewLoading.setVisibility(View.GONE);
    	
    	if (image.isImageLoaded()) {
    		btnOpenImage.setVisibility(View.GONE);
    	} else {
    		btnOpenImage.setVisibility(View.VISIBLE);
    	}
    }

	@Override
	public void onImageManipulated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoadingNewImage() {
		showLoadingState();
	}

	@Override
	public void onNewImageLoaded() {
		removeLoadingState();
	}
}
