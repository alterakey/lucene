package com.gmail.altakey.lucene;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;

public class MainActivity extends Activity
{
	private AdLoader adLoader;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		this.adLoader = new AdLoader(this);
		this.adLoader.load();
    }

	@Override
	public void onResume()
	{
		super.onResume();
		this.adLoader.load();
	}
}
