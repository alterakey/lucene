package com.gmail.altakey.lucene;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.util.*;

public class ViewActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);
		
		ImageView v = (ImageView)findViewById(R.id.view);
		ImageLoader.create(v, this.getIntent()).load();
    }
}
