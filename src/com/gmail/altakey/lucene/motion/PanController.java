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

package com.gmail.altakey.lucene.motion;

import android.widget.ImageView;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.view.MotionEvent;

public class PanController
{
	private static final int UNLOCKED = -1;

	private ImageView view;
	private int pointer_id = UNLOCKED;
	private float x;
	private float y;

	public PanController(ImageView view)
	{
		this.view = view;
	}

	public void begin(MotionEvent e)
	{
		if (this.pointer_id == UNLOCKED)
		{
			int index = e.getActionIndex();
			this.pointer_id = e.getPointerId(index);
			this.x = e.getX(index);
			this.y = e.getY(index);
		}
	}

	public void update(MotionEvent e)
	{
		if (this.pointer_id != UNLOCKED)
		{
			int index = e.findPointerIndex(this.pointer_id);
			try
			{
				this.apply(e.getX(index) - this.x, e.getY(index) - this.y);
				this.x = e.getX(index);
				this.y = e.getY(index);
			}
			catch (IllegalArgumentException exc)
			{
				return;
			}
			catch (ArrayIndexOutOfBoundsException exc)
			{
				return;
			}
		}
	}

	public void end()
	{
		this.pointer_id = UNLOCKED;
		this.x = 0.0f;
		this.y = 0.0f;
	}

	private void apply(float dx, float dy)
	{
		Matrix m = new Matrix(this.view.getImageMatrix());
		m.postTranslate(dx, dy);
		this.view.setImageMatrix(m);
	}
}
