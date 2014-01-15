package at.ac.tuwien.caa.cvl.imagine.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;
import at.ac.tuwien.caa.cvl.imagine.image.OnImageChangedListener;

public abstract class ImImageControls extends FrameLayout implements OnImageChangedListener {
	ImImage image;
		
	public ImImageControls(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}



	public ImImageControls(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}



	public ImImageControls(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setImage(ImImage image) {
		this.image = image;
		this.image.setOnImageChangedListener(this);
		
		Log.d("Abstract class", "image and notifier set");
	}
	
	@Override
	public void onImageManipulated() {
		// Nothing todo
	}

	@Override
	public void onLoadingNewImage() {
		resetControls();
		disableControls();
	}

	@Override
	public void onNewImageLoaded() {
		enableControls();
		Log.d("Abstract class", "image loaded notification");
	}
	
	public abstract void resetControls();
	public abstract void disableControls();
	public abstract void enableControls();	
}
