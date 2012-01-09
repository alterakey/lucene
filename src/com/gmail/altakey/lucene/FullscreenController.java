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

	private boolean active;

	private final SystemUiStyler styler = new SystemUiStyler();

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
			this.styler.listen();
		}
		catch (NoSuchMethodError e)
		{
		}
	}

	public void deactivate()
	{
		this.active = false;
		this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		try
		{
			this.activity.getActionBar().show();
			this.styler.styleSystemUi();
			this.styler.unlisten();
		}
		catch (NoSuchMethodError e)
		{
		}
	}

	private final class SystemUiStyler
	{
		private boolean canHideSystemUi()
		{
			return activity.getResources().getBoolean(R.bool.cap_can_hide_system_ui);
		}
		
		private int getExpectedVisibility()
		{
			if (!active)
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

		public void styleSystemUi()
		{
			try
			{
				view.setSystemUiVisibility(this.getExpectedVisibility());
			}
			catch (NoSuchMethodError e)
			{
			}
		}

		public void listen()
		{
			try
			{
				view.setOnSystemUiVisibilityChangeListener(new DeferredVisibilityRestorer());
			}
			catch (NoSuchMethodError e)
			{
			}
			catch (NoClassDefFoundError e)
			{
			}
		}	

		public void unlisten()
		{
			try
			{
				view.setOnSystemUiVisibilityChangeListener(null);
			}
			catch (NoSuchMethodError e)
			{
			}
		}

		private class DeferredVisibilityRestorer implements View.OnSystemUiVisibilityChangeListener
		{
			private Timer timer;

			@Override
			public void onSystemUiVisibilityChange(int visibility)
			{
				if (visibility != getExpectedVisibility())
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
		}
	}
}
