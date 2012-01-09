package com.gmail.altakey.lucene.motion;

import android.widget.ImageView;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public final class ZoomController
{
	private final ImageView view;

	public ZoomController(final ImageView view)
	{
		this.view = view;
	}

	public void begin(final ScaleGestureDetector sgd)
	{
	}

	public void update(final ScaleGestureDetector sgd)
	{
		float prev = sgd.getPreviousSpan();
		float now = sgd.getCurrentSpan();

		float ratio = (now - prev) / prev;
		this.apply(new PointF(sgd.getFocusX(), sgd.getFocusY()), ratio);
	}

	public void end()
	{
	}

	private void apply(final PointF focalPoint, final float ratioDelta)
	{
		Matrix m = new Matrix(this.view.getImageMatrix());
		m.postScale(1.0f + ratioDelta, 1.0f + ratioDelta, focalPoint.x, focalPoint.y);
		this.view.setImageMatrix(m);
	}
}
