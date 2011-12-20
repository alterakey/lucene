/**
 * Copyright (C) 2011 Takahiro Yoshimura
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
