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

public class SepiaEffectControls extends ImImageControls implements OnClickListener {
	private Button btnApply;
	
	public SepiaEffectControls(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context);
	}

	public SepiaEffectControls(Context context) {
		this(context, null);
		
		initialize(context);
	}
	
	private void initialize(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_sepia_effect_controls, this, true);

		btnApply = (Button) findViewById(R.id.btnApplySepiaEffect);
		btnApply.setOnClickListener(this);
		
		// Disable all controls
		disableControls();
	}
	
	@Override
	public void resetControls() {
		
	}

	@Override
	public void disableControls() {
		btnApply.setEnabled(false);
	}

	@Override
	public void enableControls() {
		btnApply.setEnabled(true);
	}

	@Override
	public void onClick(View view) {
		if (view == btnApply) {
			sepiaEffect();
		}
	}
	
	private void sepiaEffect() {
		image.sepiaEffect();
	}

}
