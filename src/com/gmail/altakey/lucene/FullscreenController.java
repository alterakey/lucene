package com.gmail.altakey.lucene;

import android.app.Activity;
import android.app.ActionBar;
import android.view.Window;
import android.view.WindowManager;

import java.util.*;
import android.view.*;

public final class FullscreenController
{
	private final Activity activity;
	private final View view;

	private Timer restyleTimer;
	private boolean active;

	private final Styler styler = new Styler();

	public FullscreenController(final View view)
	{
		this.view = view;
		this.activity = (Activity)view.getContext();
		this.active = false;
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

	public void activate()
	{
		this.active = true;
		this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
										   WindowManager.LayoutParams.FLAG_FULLSCREEN);
		try
		{
			this.activity.getActionBar().hide();
			this.styler.styleSystemUi();
			this.view.setOnSystemUiVisibilityChangeListener(this.styler);
		}
		catch (NoSuchMethodError e)
		{
		}
	}

	public void deactivate()
	{
		if (!this.active)
			return;

		this.active = false;
		this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		try
		{
			this.activity.getActionBar().show();
			this.styler.styleSystemUi();
			this.view.setOnSystemUiVisibilityChangeListener(null);
		}
		catch (NoSuchMethodError e)
		{
		}
	}

	private final class Styler implements View.OnSystemUiVisibilityChangeListener
	{
		private final FullscreenController fsc = FullscreenController.this;
		private Timer timer;

		@Override
		public void onSystemUiVisibilityChange(int visibility)
		{
			if (visibility != this.getExpectedVisibility())
			{
				if (this.timer != null)
				{
					this.timer.cancel();
					this.timer.purge();
					this.timer = null;
				}
				
				this.timer = new Timer();
				this.timer.schedule(new TimerTask() 
				{
					public void run()
					{
						activity.runOnUiThread(
							new Runnable() 
							{
								public void run() 
								{
									styleSystemUi();
								}
							}
						);
					}
				}, 1000);
			}
		}

		private int getExpectedVisibility()
		{
			if (!fsc.active)
			{
				return View.SYSTEM_UI_FLAG_VISIBLE;
			}
			else
			{
				if (this.canHideSystemUi())
					return View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
				else
					return View.SYSTEM_UI_FLAG_LOW_PROFILE;
			}
		}

		private boolean canHideSystemUi()
		{
			return fsc.activity.getResources().getBoolean(R.bool.cap_can_hide_system_ui);
		}
		
		public void styleSystemUi()
		{
			try
			{
				fsc.view.setSystemUiVisibility(this.getExpectedVisibility());
			}
			catch (NoSuchMethodError e)
			{
			}
		}
	}
}
