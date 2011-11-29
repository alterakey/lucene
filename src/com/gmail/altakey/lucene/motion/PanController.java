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
