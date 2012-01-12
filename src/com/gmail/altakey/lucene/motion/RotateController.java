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

package com.gmail.altakey.lucene.motion;

import android.widget.ImageView;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.view.MotionEvent;

public final class RotateController
{
	private static final int UNLOCKED = -1;
	private static final float UNDETERMINED = 999.0f;

	private final ImageView view;

	private PointF[] points = new PointF[2];
	private int pivot_id = UNLOCKED;
	private int pointer_id = UNLOCKED;
	private float angle = UNDETERMINED;

	public RotateController(final ImageView view)
	{
		this.view = view;
		this.points[0] = new PointF();
		this.points[1] = new PointF();
	}

	public void begin(final MotionEvent e)
	{
		this.down(e);
	}

	public void update(final MotionEvent e)
	{
		if (this.pivot_id != UNLOCKED && this.pointer_id != UNLOCKED)
		{
			int pivot_index = e.findPointerIndex(this.pivot_id);
			int pointer_index = e.findPointerIndex(this.pointer_id);

			this.points[0].x = e.getX(pivot_index);
			this.points[0].y = e.getY(pivot_index);
			this.points[1].x = e.getX(pointer_index);
			this.points[1].y = e.getY(pointer_index);

			float angle = this.findAngle();
			PointF pivot = this.findPivot();
			
			if (this.angle != UNDETERMINED)
				this.apply(pivot, angle - this.angle);
			
			this.angle = angle;
		}
	}

	public void down(final MotionEvent e)
	{
		int index = e.getActionIndex();
		int id = e.getPointerId(index);
		if (this.pivot_id == UNLOCKED)
		{
			this.pivot_id = id;
			this.points[0].x = e.getX(index);
			this.points[0].y = e.getY(index);
		}
		else if (this.pointer_id == UNLOCKED)
		{
			this.pointer_id = id;
			this.points[1].x = e.getX(index);
			this.points[1].y = e.getY(index);
		}
	}

	public void up(final MotionEvent e)
	{
		int id = e.getPointerId(e.getActionIndex());
		if (this.pivot_id == id)
		{
			this.pivot_id = UNLOCKED;
			this.angle = UNDETERMINED;
		}

		if (this.pointer_id == id)
		{
			this.pointer_id = UNLOCKED;
			this.angle = UNDETERMINED;
		}			
	}

	public void end()
	{
		this.angle = UNDETERMINED;
		this.pivot_id = UNLOCKED;
		this.pointer_id = UNLOCKED;
	}

	private void apply(final PointF pivot, float dtheta)
	{
		Matrix m = new Matrix(this.view.getImageMatrix());
		if (dtheta > 270)
			dtheta -= 360;
		if (dtheta < -270)
			dtheta += 360;

		m.postRotate(dtheta, pivot.x, pivot.y);
		this.view.setImageMatrix(m);
	}

	private float findAngle()
	{
		final double RAD_TO_DEG = 180/Math.PI;
		PointF vector = new PointF(this.points[1].x - this.points[0].x, this.points[1].y - this.points[0].y);			
		return (float)(Math.atan2(vector.y, vector.x) * RAD_TO_DEG);
	}

	private PointF findPivot()
	{
		return new PointF(
			(this.points[1].x + this.points[0].x) / 2,
			(this.points[1].y + this.points[0].y) / 2
		);
	}
}
