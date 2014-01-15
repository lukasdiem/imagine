package at.ac.tuwien.caa.cvl.imagine.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.image.ImJniImageProcessing;

public class CartoonizeControlsView extends ImImageControls implements OnClickListener {
	private static final String TAG = CartoonizeControlsView.class.getSimpleName();
	
	private SeekBar sliderColorCount;
	private SeekBar sliderEdgeWeight;
	private SeekBar sliderEdgeThickness;
	private Button btnApply;
	
	public CartoonizeControlsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context);
	}

	public CartoonizeControlsView(Context context) {
		this(context, null);
		
		initialize(context);
	}
	
	private void initialize(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_cartoonize_controls, this, true);

		Log.d(TAG, "Layout inflated");
		
		sliderColorCount = (SeekBar) findViewById(R.id.sliderColorCount);
		sliderEdgeWeight = (SeekBar) findViewById(R.id.sliderEdgeWeight);
		sliderEdgeThickness = (SeekBar) findViewById(R.id.sliderEdgeThickness);
		
		btnApply = (Button) findViewById(R.id.btnApplyCartoonize);
		btnApply.setOnClickListener(this);
		
		// Disable all controls
		disableControls();
	}

	@Override
	public void resetControls() {
		sliderColorCount.setProgress(8);
		sliderEdgeThickness.setProgress(0);
		sliderEdgeWeight.setProgress(13);
	}

	@Override
	public void disableControls() {
		sliderColorCount.setEnabled(false);
		sliderEdgeThickness.setEnabled(false);
		sliderEdgeWeight.setEnabled(false);
		btnApply.setEnabled(false);
	}

	@Override
	public void enableControls() {
		sliderColorCount.setEnabled(true);
		sliderEdgeThickness.setEnabled(true);
		sliderEdgeWeight.setEnabled(true);
		btnApply.setEnabled(true);
	}

	@Override
	public void onClick(View view) {
		if (view == btnApply) {
			cartoonize();
		}
	}
	
	private void cartoonize() {
		int colorCount = sliderColorCount.getProgress();
		int edgeThickness = sliderEdgeThickness.getProgress();
		float edgeWeight = ((float)sliderEdgeWeight.getProgress() - 10.0f) / 10.0f;
		
		image.cartoonize(colorCount, edgeWeight, edgeThickness);
	}
}
