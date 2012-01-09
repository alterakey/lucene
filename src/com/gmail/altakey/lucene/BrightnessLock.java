package com.gmail.altakey.lucene;

import android.app.Activity;
import android.view.WindowManager;

public final class BrightnessLock
{
	private Activity activity;
	
	public BrightnessLock(Activity activity)
	{
		this.activity = activity;
	}
	
	public void hold()
	{
		WindowManager.LayoutParams lp = this.activity.getWindow().getAttributes();
		lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
		this.activity.getWindow().setAttributes(lp);			
	}
	
	public void release()
	{
		WindowManager.LayoutParams lp = this.activity.getWindow().getAttributes();
		lp.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
		this.activity.getWindow().setAttributes(lp);			
	}
}
