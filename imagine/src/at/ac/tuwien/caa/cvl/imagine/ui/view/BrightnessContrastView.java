package at.ac.tuwien.caa.cvl.imagine.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.image.OnImageChangedListener;

public class BrightnessContrastView extends ImImageControls implements OnSeekBarChangeListener {
	private static final String TAG = BrightnessContrastView.class.getSimpleName();
	
	private SeekBar sliderBrightness;
	private SeekBar sliderContrast;
	
	public BrightnessContrastView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context);
	}

	public BrightnessContrastView(Context context) {
		this(context, null);
		
		initialize(context);
	}
	
	private void initialize(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_brightness_contrast, this, true);

		Log.d(TAG, "Layout inflated");
		
		sliderBrightness = (SeekBar) findViewById(R.id.sliderBrightness);
		sliderBrightness.setOnSeekBarChangeListener(this);
		sliderContrast = (SeekBar) findViewById(R.id.sliderContrast);
		sliderContrast.setOnSeekBarChangeListener(this);
		
		// Disable all controls
		disableControls();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar == sliderBrightness || seekBar == sliderContrast) {
			setBrightnessContrast();
		}
	}
	
	private void setBrightnessContrast() {
		float brightness = sliderBrightness.getProgress() - 256;
		float contrast = (float)sliderContrast.getProgress() / (float)sliderContrast.getMax() + 0.5f;
		
		Log.d(TAG, "Brighntess: " + brightness + ", contrast: " + contrast);
		
		if (image != null && image.isImageLoaded()) {
			image.changeBrightnessContrast(brightness, contrast);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetControls() {
		sliderBrightness.setProgress(255);
		sliderContrast.setProgress(100);
	}

	@Override
	public void disableControls() {
		sliderBrightness.setEnabled(false);
		sliderContrast.setEnabled(false);
	}

	@Override
	public void enableControls() {
		sliderBrightness.setEnabled(true);
		sliderContrast.setEnabled(true);
		
		Log.d(TAG, "Enabling effect controls");
	}

	/*@Override
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
	}*/
}
