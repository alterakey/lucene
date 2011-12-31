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
	public int maxBitmapWidth = -1;
	public int maxBitmapHeight = -1;

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
		if (!HWAcceleration.isEnabled(canvas))
		{
			if (this.maxBitmapWidth > 0 && this.maxBitmapHeight > 0)
			{
				this.maxBitmapWidth = -1;
				this.maxBitmapHeight = -1;
			}
		}
		else
		{
			if (this.maxBitmapWidth < 0 || this.maxBitmapHeight < 0)
			{
				this.maxBitmapWidth = HWAcceleration.getMaximumBitmapWidth(canvas);
				this.maxBitmapHeight = HWAcceleration.getMaximumBitmapHeight(canvas);
				Log.d("HWIV.oD", String.format("max: (%d, %d)", this.maxBitmapWidth, this.maxBitmapHeight));
			}
		}
	}

	private static class HWAcceleration
	{
		public static boolean isEnabled(Canvas canvas)
		{
			try
			{
				return canvas.isHardwareAccelerated();
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
}
