package at.ac.tuwien.caa.cvl.imagine.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import at.ac.tuwien.caa.cvl.imagine.R;

public class EdgeEffectControls extends ImImageControls implements OnClickListener {
private static final String TAG = CartoonizeControlsView.class.getSimpleName();
	
	private SeekBar sliderEdgeThickness;
	private Button btnApply;
	
	public EdgeEffectControls(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context);
	}

	public EdgeEffectControls(Context context) {
		this(context, null);
		
		initialize(context);
	}
	
	private void initialize(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_edge_effect_controls, this, true);

		Log.d(TAG, "Layout inflated");
		
		sliderEdgeThickness = (SeekBar) findViewById(R.id.sliderEdgeThicknessEdgeEffect);
		
		btnApply = (Button) findViewById(R.id.btnApplyEdgeEffect);
		btnApply.setOnClickListener(this);
		
		// Disable all controls
		disableControls();
	}
	
	@Override
	public void resetControls() {
		sliderEdgeThickness.setProgress(0);
	}

	@Override
	public void disableControls() {
		sliderEdgeThickness.setEnabled(false);
		btnApply.setEnabled(false);
	}

	@Override
	public void enableControls() {
		sliderEdgeThickness.setEnabled(true);
		btnApply.setEnabled(true);
	}

	@Override
	public void onClick(View view) {
		if (view == btnApply) {
			edgeEffect();
		}
	}
	
	private void edgeEffect() {
		int edgeThickness = sliderEdgeThickness.getProgress();
		
		image.edgeEffect(edgeThickness);
	}

}
