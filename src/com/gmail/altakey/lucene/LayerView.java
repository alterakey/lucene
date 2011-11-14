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
		return true;
	}

	private void setScale(float ratio)
	{
		Matrix m = new Matrix();
		m.setScale(ratio, ratio);
		Log.d("LV.sS", String.format("scaling to: %f", ratio));
		this.setImageMatrix(m);
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
			setScale(ratio);
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
