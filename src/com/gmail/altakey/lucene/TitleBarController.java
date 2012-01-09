package com.gmail.altakey.lucene;

import android.app.Activity;
import android.app.ActionBar;
import android.view.Window;

public final class TitleBarController
{
	private final Activity activity;

	public TitleBarController(final Activity activity)
	{
		this.activity = activity;
	}

	public void onCreate()
	{
		try
		{
			final ActionBar bar = this.activity.getActionBar();
		}
		catch (NoSuchMethodError e)
		{
			this.activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		}
	}

	public void hide()
	{
		try
		{
			this.activity.getActionBar().hide();
		}
		catch (NoSuchMethodError e)
		{
		}
	}

	public void show()
	{
		try
		{
			this.activity.getActionBar().show();
		}
		catch (NoSuchMethodError e)
		{
		}
	}
}
