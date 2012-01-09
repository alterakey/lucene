package com.gmail.altakey.lucene.motion;

import android.widget.ImageView;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public final class ZoomController
{
	private ImageView view;

	public ZoomController(ImageView view)
	{
		this.view = view;
	}

	public void begin(ScaleGestureDetector sgd)
	{
	}

	public void update(ScaleGestureDetector sgd)
	{
		float prev = sgd.getPreviousSpan();
		float now = sgd.getCurrentSpan();

		float ratio = (now - prev) / prev;
		this.apply(new PointF(sgd.getFocusX(), sgd.getFocusY()), ratio);
	}

	public void end()
	{
	}

	private void apply(PointF focalPoint, float ratioDelta)
	{
		Matrix m = new Matrix(this.view.getImageMatrix());
		m.postScale(1.0f + ratioDelta, 1.0f + ratioDelta, focalPoint.x, focalPoint.y);
		this.view.setImageMatrix(m);
	}
}
