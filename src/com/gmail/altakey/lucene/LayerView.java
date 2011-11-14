package com.gmail.altakey.lucene;

import android.content.Context;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;

import android.graphics.*;
import java.util.*;

import android.view.ScaleGestureDetector;

public class LayerView extends ImageView
{
	private ScaleGestureDetector sgd;
	private ZoomController zc = new ZoomController();
	private PanController pc = new PanController();

	public LayerView(Context context) {
		super(context);
		this.init();
	}

	public LayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	private void init()
	{
		this.setImageMatrix(new Matrix());
		this.sgd = new ScaleGestureDetector(this.getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
			public boolean onScale(ScaleGestureDetector detector)
			{
				Log.d("LV.OSGL.oS", String.format("scale: %f", detector.getCurrentSpan()));
				zc.update(detector);
				return true;
			}

			public boolean onScaleBegin(ScaleGestureDetector detector)
			{
				Log.d("LV.OSGL.oSB", String.format("scale begin: %f", detector.getCurrentSpan()));
				zc.begin(detector);
				return true;
			}

			public void onScaleEnd(ScaleGestureDetector detector)
			{
				Log.d("LV.OSGL.oSE", String.format("scale end: %f", detector.getCurrentSpan()));
				zc.update(detector);
				zc.end();
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		sgd.onTouchEvent(e);
		switch (e.getActionMasked())
		{
		case MotionEvent.ACTION_DOWN:
			this.pc.begin(e);
			break;
		case MotionEvent.ACTION_MOVE:
			this.pc.update(e);
			break;
		case MotionEvent.ACTION_UP:
			this.pc.end();
			break;
		}
		return true;
	}

	private void focus(PointF focalPoint, float ratioDelta)
	{
		Matrix m = new Matrix(this.getImageMatrix());
		m.postScale(1.0f + ratioDelta, 1.0f + ratioDelta, focalPoint.x, focalPoint.y);
		this.setImageMatrix(m);
	}

	private void translate(float dx, float dy)
	{
		Matrix m = new Matrix(this.getImageMatrix());
		m.postTranslate(dx, dy);
		this.setImageMatrix(m);
	}

	private class PanController
	{
		private float x;
		private float y;

		public void begin(MotionEvent e)
		{
			this.x = e.getX();
			this.y = e.getY();
		}

		public void update(MotionEvent e)
		{
			Log.d("LV.PC.update", String.format("translate: %f, %f", e.getX() - this.x, e.getY() - this.y));
			translate(e.getX() - this.x, e.getY() - this.y);
			this.x = e.getX();
			this.y = e.getY();
		}

		public void end()
		{
			this.x = 0.0f;
			this.y = 0.0f;
		}
	}

	private class ZoomController
	{
		public void begin(ScaleGestureDetector sgd)
		{
		}

		public void update(ScaleGestureDetector sgd)
		{
			float prev = sgd.getPreviousSpan();
			float now = sgd.getCurrentSpan();

			float ratio = (now - prev) / prev;
			Log.d("LV.ZC.update", String.format("ratio: %f", ratio));
			focus(new PointF(sgd.getFocusX(), sgd.getFocusY()), ratio);
		}

		public void end()
		{
		}
	}
}
