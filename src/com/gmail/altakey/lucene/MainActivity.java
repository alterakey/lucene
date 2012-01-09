package com.gmail.altakey.lucene;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.*;
import android.widget.LinearLayout;

public final class MainActivity extends Activity
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId())
		{
		case R.id.menu_preferences:
			startActivity(new Intent(this, ConfigActivity.class));
			return true;
		case R.id.menu_close:
			this.finish();
		}
		return true;
	}
}
