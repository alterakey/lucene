package com.gmail.altakey.lucene.motion;

import android.widget.ImageView;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.view.MotionEvent;

public class RotateController
{
	private static final int UNLOCKED = -1;
	private static final float UNDETERMINED = 999.0f;

	private ImageView view;
	private PointF[] points = new PointF[2];
	private int pivot_id = UNLOCKED;
	private int pointer_id = UNLOCKED;
	private float angle = UNDETERMINED;

	public RotateController(ImageView view)
	{
		this.view = view;
		this.points[0] = new PointF();
		this.points[1] = new PointF();
	}

	public void begin(MotionEvent e)
	{
		this.down(e);
	}

	public void update(MotionEvent e)
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

	public void down(MotionEvent e)
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

	public void up(MotionEvent e)
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

	private void apply(PointF pivot, float dtheta)
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
