package at.ac.tuwien.caa.cvl.imagine.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.image.ImImage;
import at.ac.tuwien.caa.cvl.imagine.ui.fragment.ImageViewFragment.OnImageViewAttachedListener;
import at.ac.tuwien.caa.cvl.imagine.ui.view.HistogramView;
import at.ac.tuwien.caa.cvl.imagine.ui.view.ImImageControls;

public class EffectControlsFragment extends Fragment {
	private static final String TAG = EffectControlsFragment.class.getSimpleName();
	
	private ImImage image;
	
	private List<ImImageControls> imageControlList;
	private HistogramView histogramView;
	private OnEffectControlsAttachedListener attachedListener;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.fragment_effect_controls, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        imageControlList = new ArrayList<ImImageControls>();
        
        ImImageControls brightnessContrast = (ImImageControls) getView().findViewById(R.id.viewBrightnessContrast);
        if (brightnessContrast != null) {
        	imageControlList.add(brightnessContrast);
        	brightnessContrast.setImage(image);
        }
        
        ImImageControls cartoonizeControls = (ImImageControls) getView().findViewById(R.id.viewCartoonizeControls);
        if (cartoonizeControls != null) {
        	imageControlList.add(cartoonizeControls);
        	cartoonizeControls.setImage(image);
        }
        
        ImImageControls edgeEffectControls = (ImImageControls) getView().findViewById(R.id.viewEdgeEffectControls);
        if (edgeEffectControls != null) {
        	imageControlList.add(edgeEffectControls);
        	edgeEffectControls.setImage(image);
        }
        
        ImImageControls sepiaEffectControls = (ImImageControls) getView().findViewById(R.id.viewSepiaEffectControls);
        if (sepiaEffectControls != null) {
        	imageControlList.add(sepiaEffectControls);
        	sepiaEffectControls.setImage(image);
        }
        
        
        histogramView = (HistogramView) getView().findViewById(R.id.histView);
        histogramView.setImage(image);
    }
    
    public interface OnEffectControlsAttachedListener {
        public void onEffectControlsAttachedListener(EffectControlsFragment fragment);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	attachedListener = (OnEffectControlsAttachedListener) activity;
        	attachedListener.onEffectControlsAttachedListener(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnImageLoadListener");
        }
    }
    
    
    /*public void onImageLoadingStarted() {
    	for (ImImageControls control : imageControlList) {
    		control.resetControls();
    		control.disableControls();
    	}
    }
    
    public void onImageLoadingCompleted(ImImage image) {
    	for (ImImageControls control : imageControlList) {
    		control.setImage(image);
    		control.enableControls();
    	}
    }*/
    
    public void setImage(ImImage image) {
    	this.image = image;
    	/*for (ImImageControls control : imageControlList) {
    		control.setImage(image);
    	}
    	
    	if (histogramView != null)
    		histogramView.setImage(image);*/
    }
}
