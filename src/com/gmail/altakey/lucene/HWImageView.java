package com.gmail.altakey.lucene;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.*;

import com.google.ads.*;

import com.gmail.altakey.lucene.motion.*;

public class HWImageView extends ImageView
{
	private static final int SOFTWARE = -1;
	private static final int UNKNOWN = -2;

	public int maxBitmapWidth = UNKNOWN;
	public int maxBitmapHeight = UNKNOWN;

	public HWImageView(Context context)
	{
		super(context);
	}
	public HWImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	public HWImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		if (this.maxBitmapWidth == UNKNOWN && this.maxBitmapHeight == UNKNOWN)
		{
			if (!HWAcceleration.isEnabled(this))
			{
				this.maxBitmapWidth = SOFTWARE;
				this.maxBitmapHeight = SOFTWARE;
			}
			else
			{
				this.maxBitmapWidth = HWAcceleration.getMaximumBitmapWidth(canvas);
				this.maxBitmapHeight = HWAcceleration.getMaximumBitmapHeight(canvas);
				Log.d("HWIV.oD", String.format("max: (%d, %d)", this.maxBitmapWidth, this.maxBitmapHeight));
			}
		}
	}

	public void setHardwareAcceleration(boolean enabled) throws ActivityRestartRequired
	{
		try
		{
			int fromType = getLayerType();
			int toType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE;
			
			if (fromType != toType)
			{
				if (this.maxBitmapWidth != UNKNOWN || this.maxBitmapHeight != UNKNOWN)
					throw new ActivityRestartRequired();
			}
			
			this.maxBitmapWidth = UNKNOWN;
			this.maxBitmapHeight = UNKNOWN;
			setLayerType(toType, null);
			invalidate();
		}
		catch (NoSuchMethodError e)
		{
		}
	}

	private static class HWAcceleration
	{
		public static boolean isEnabled(View view)
		{
			try
			{
				return view.isHardwareAccelerated() && (view.getLayerType() != View.LAYER_TYPE_SOFTWARE);
			}
			catch (NoSuchMethodError e)
			{
				return false;
			}
		}
		
		public static int getMaximumBitmapWidth(Canvas canvas)
		{
			try
			{
				return canvas.getMaximumBitmapWidth();
			}
			catch (NoSuchMethodError e)
			{
				return 2048;
			}
		}
		
		public static int getMaximumBitmapHeight(Canvas canvas)
		{
			try
			{
				return canvas.getMaximumBitmapHeight();
			}
			catch (NoSuchMethodError e)
			{
				return 2048;
			}
		}
	}

	public static class ActivityRestartRequired extends Exception
	{
	}
}
