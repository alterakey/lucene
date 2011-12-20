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
		if (!canvas.isHardwareAccelerated())
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
				this.maxBitmapWidth = canvas.getMaximumBitmapWidth();
				this.maxBitmapHeight = canvas.getMaximumBitmapHeight();
				Log.d("HWIV.oD", String.format("max: (%d, %d)", this.maxBitmapWidth, this.maxBitmapHeight));
			}
		}
	}
}
