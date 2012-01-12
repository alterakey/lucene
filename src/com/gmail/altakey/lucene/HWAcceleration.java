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

import android.view.View;
import android.util.Log;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.*;

public final class HWAcceleration
{
	private final View view;

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
