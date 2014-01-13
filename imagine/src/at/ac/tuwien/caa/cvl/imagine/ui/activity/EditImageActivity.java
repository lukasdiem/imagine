package at.ac.tuwien.caa.cvl.imagine.ui.activity;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import at.ac.tuwien.caa.cvl.imagine.R;
import at.ac.tuwien.caa.cvl.imagine.R.layout;
import at.ac.tuwien.caa.cvl.imagine.R.menu;

public class EditImageActivity extends ActionBarActivity implements View.OnClickListener {
	ActionBar actionBar;
	View wholeLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_image);
			
		wholeLayout = findViewById(R.id.fragmentEditImage);
		if (wholeLayout != null) {
			wholeLayout.setOnClickListener(this);
		}
		
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
}
