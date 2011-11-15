package com.gmail.altakey.lucene;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigActivity extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config);
	}
}
