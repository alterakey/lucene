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

	private void focus(PointF focalPoint, float ratio)
	{
		Matrix m = new Matrix();
		m.setScale(ratio, ratio, focalPoint.x, focalPoint.y);
		Log.d("LV.sS", String.format("scaling to: %f", ratio));
		this.setImageMatrix(m);
	}

	private void translate(float x, float y)
	{
		Matrix m = new Matrix(this.getImageMatrix());
		m.setTranslate(x, y);
		Log.d("LV.translate", String.format("translate: %f, %f", x, y));
		this.setImageMatrix(m);
	}

	private class PanController
	{
		private PointF origin = new PointF();
		private PointF currentPoint = new PointF();

		public void begin(MotionEvent e)
		{
			this.origin.x = e.getX();
			this.origin.y = e.getY();
		}

		public void update(MotionEvent e)
		{
			if (this.origin.x == 0.0f && this.origin.y == 0.0f)
				this.begin(e);

			translate(e.getX() - this.origin.x + this.currentPoint.x, e.getY() - this.origin.y + this.currentPoint.y);
		}

		public void end()
		{
			this.origin.x = 0.0f;
			this.origin.y = 0.0f;

			float[] mv = new float[9];
			getImageMatrix().getValues(mv);
			this.currentPoint.x = mv[Matrix.MTRANS_X];
			this.currentPoint.y = mv[Matrix.MTRANS_Y];
		}
	}

	private class ZoomController
	{
		private float initial;
		private float now;
		private float currentRatio = 1.0f;

		public void begin(ScaleGestureDetector sgd)
		{
			this.initial = sgd.getCurrentSpan();
			this.now = 0.0f;
		}

		public void update(ScaleGestureDetector sgd)
		{
			this.now = sgd.getCurrentSpan();
			if (this.initial == 0.0f)
				this.initial = this.now;

			float ratio = this.now / this.initial * this.currentRatio;
			focus(new PointF(sgd.getFocusX(), sgd.getFocusY()), ratio);
		}

		public void end()
		{
			this.initial = 0.0f;
			this.now = 0.0f;

			float[] mv = new float[9];
			getImageMatrix().getValues(mv);
			this.currentRatio = mv[Matrix.MSCALE_X];
		}
	}
}
