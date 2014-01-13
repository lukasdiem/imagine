package at.ac.tuwien.caa.cvl.imagine.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import at.ac.tuwien.caa.cvl.imagine.R;

public class EffectControlsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_effect_controls, container, false);
    }
}
