package com.gmail.altakey.lucene;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.util.*;
import android.webkit.WebView;

public class ViewActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

		WebView v = (WebView)findViewById(R.id.view);
		v.getSettings().setBuiltInZoomControls(true);
		v.getSettings().setDisplayZoomControls(false);
		v.getSettings().setLightTouchEnabled(false);
		v.getSettings().setLoadWithOverviewMode(true);
		v.getSettings().setUseWideViewPort(true);
		
		ImageLoader.create(v, this.getIntent()).load();
    }
}
