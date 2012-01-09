package com.gmail.altakey.lucene;

import android.view.View;
import android.util.Log;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.*;

public class HWAcceleration
{
	private View view;

	private static final int SOFTWARE = -1;
	private static final int UNKNOWN = -2;
		
	private int maxBitmapWidth = UNKNOWN;
	private int maxBitmapHeight = UNKNOWN;

	public HWAcceleration(View view)
	{
		this.view = view;
	}
		
	public boolean isEnabled()
	{
		try
		{
			return this.view.isHardwareAccelerated() && (this.view.getLayerType() != View.LAYER_TYPE_SOFTWARE);
		}
		catch (NoSuchMethodError e)
		{
			return false;
		}
	}
		
	public void enable(boolean enabled) throws ActivityRestartRequired
	{
		try
		{
			int fromType = this.view.getLayerType();
			int toType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE;
				
			if (fromType != toType)
			{
				if (this.maxBitmapWidth != UNKNOWN || this.maxBitmapHeight != UNKNOWN)
					throw new ActivityRestartRequired();
			}
				
			this.maxBitmapWidth = UNKNOWN;
			this.maxBitmapHeight = UNKNOWN;
			this.view.setLayerType(toType, null);
			this.view.invalidate();
		}
		catch (NoSuchMethodError e)
		{
		}
	}

	public void note(Canvas canvas)
	{
		if (this.maxBitmapWidth == UNKNOWN && this.maxBitmapHeight == UNKNOWN)
		{
			if (!this.isEnabled())
			{
				this.maxBitmapWidth = SOFTWARE;
				this.maxBitmapHeight = SOFTWARE;
			}
			else
			{
				try
				{
					this.maxBitmapWidth = canvas.getMaximumBitmapWidth();
					this.maxBitmapHeight = canvas.getMaximumBitmapHeight();
				}
				catch (NoSuchMethodError e)
				{
					this.maxBitmapWidth = 2048;
					this.maxBitmapHeight = 2048;
				}
				Log.d("HWIV.HA.oD", String.format("max: (%d, %d)", this.maxBitmapWidth, this.maxBitmapHeight));
			}
		}
	}

	public int getMaximumBitmapWidth()
	{
		return this.maxBitmapWidth;
	}
		
	public int getMaximumBitmapHeight()
	{
		return this.maxBitmapHeight;
	}
}
