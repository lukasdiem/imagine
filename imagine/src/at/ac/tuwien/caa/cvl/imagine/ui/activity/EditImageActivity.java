package at.ac.tuwien.caa.cvl.imagine.ui.activity;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.R.layout;
import at.ac.tuwien.caa.cvl.imagine.R.menu;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;
import at.ac.tuwien.caa.cvl.imagine.ui.fragment.EffectControlsFragment;
import at.ac.tuwien.caa.cvl.imagine.ui.fragment.EffectControlsFragment.OnEffectControlsAttachedListener;
import at.ac.tuwien.caa.cvl.imagine.ui.fragment.ImageViewFragment.OnImageViewAttachedListener;

public class EditImageActivity extends ActionBarActivity implements OnClickListener, OnImageViewAttachedListener, OnEffectControlsAttachedListener {
	private static final String TAG = EditImageActivity.class.getSimpleName();
	
	private ActionBar actionBar;
	
	private EffectControlsFragment effectControls;
	
	private ImImage image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_edit_image);
		
		View wholeContent = findViewById(R.id.fragmentEditImage);
		if (wholeContent != null) {
			wholeContent.setOnClickListener(this);
		}
		
		effectControls = (EffectControlsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentEffectControls);
		
        // Hide the Action bar
        actionBar = getSupportActionBar();
        actionBar.hide();
	}

	@Override
	public void onClick(View v) {			
		if (actionBar != null) {
			if (actionBar.isShowing()) {
				actionBar.hide();
			} else {
				actionBar.show();
			}
		}
	}

	@Override
	public void onImageViewAttachedListener(ImImage image) {
		this.image = image;

		if (effectControls != null) {
			effectControls.setImage(image);
		}
	}

	@Override
	public void onEffectControlsAttachedListener(EffectControlsFragment fragment) {
		if (image != null) {
			fragment.setImage(image);
		} else {
			Log.w(TAG, "Could not attach the image to the controls because the image is NULL!!");
		}
	}
	
}
