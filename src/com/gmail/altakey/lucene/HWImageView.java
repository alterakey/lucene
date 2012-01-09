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
	private HWAcceleration accel = new HWAcceleration(this);

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
		this.accel.note(canvas);
	}

	public HWAcceleration getAcceleration()
	{
		return this.accel;
	}

	public static class HWAcceleration
	{
		private View view;

		private static final int SOFTWARE = -1;
		private static final int UNKNOWN = -2;
		
		public int maxBitmapWidth = UNKNOWN;
		public int maxBitmapHeight = UNKNOWN;

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
		
		public void enable(boolean enabled) throws HWImageView.ActivityRestartRequired
		{
			try
			{
				int fromType = this.view.getLayerType();
				int toType = enabled ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_SOFTWARE;
				
				if (fromType != toType)
				{
					if (this.maxBitmapWidth != UNKNOWN || this.maxBitmapHeight != UNKNOWN)
						throw new HWImageView.ActivityRestartRequired();
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
					this.maxBitmapWidth = this.getMaximumBitmapWidth(canvas);
					this.maxBitmapHeight = this.getMaximumBitmapHeight(canvas);
					Log.d("HWIV.HA.oD", String.format("max: (%d, %d)", this.maxBitmapWidth, this.maxBitmapHeight));
				}
			}
		}

		public int getMaximumBitmapWidth(Canvas canvas)
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
		
		public int getMaximumBitmapHeight(Canvas canvas)
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
