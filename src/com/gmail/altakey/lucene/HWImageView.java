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
