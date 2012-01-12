/**
 * Copyright (C) 2011-2012 Takahiro Yoshimura
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * In addition, as a special exception, the copyright holders give
 * permission to link the code of portions of this program with the
 * AdMob library under certain conditions as described in each
 * individual source file, and distribute linked combinations
 * including the two.
 *
 * You must obey the GNU General Public License in all respects for
 * all of the code used other than AdMob.  If you modify file(s) with
 * this exception, you may extend this exception to your version of
 * the file(s), but you are not obligated to do so.  If you do not
 * wish to do so, delete this exception statement from your version.
 * If you delete this exception statement from all source files in the
 * program, then also delete it here.
 */

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

		private final class DeferredVisibilityRestorer implements View.OnSystemUiVisibilityChangeListener
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
					}, 3000);
				}
			}
		}
	}
}
