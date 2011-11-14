package com.gmail.altakey.lucene;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.graphics.*;
import java.util.*;

public class ViewActivity extends Activity
{
	private ScaleGestureDetector sgd;
	private ZoomController zc = new ZoomController();
	private PanController pc = new PanController();

	private ImageView view;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

		this.view = (ImageView)findViewById(R.id.view);
		ImageLoader.create(this.view, this.getIntent()).load();

		this.init();
    }

	private void focus(PointF focalPoint, float ratioDelta)
	{
		Matrix m = new Matrix(this.view.getImageMatrix());
		m.postScale(1.0f + ratioDelta, 1.0f + ratioDelta, focalPoint.x, focalPoint.y);
		this.view.setImageMatrix(m);
	}

	private void translate(float dx, float dy)
	{
		Matrix m = new Matrix(this.view.getImageMatrix());
		m.postTranslate(dx, dy);
		this.view.setImageMatrix(m);
	}

	private void init()
	{
		this.view.setImageMatrix(new Matrix());
		this.view.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent e)
			{
				sgd.onTouchEvent(e);
				switch (e.getActionMasked())
				{
				case MotionEvent.ACTION_DOWN:
					pc.begin(e);
					break;
				case MotionEvent.ACTION_MOVE:
					pc.update(e);
					break;
				case MotionEvent.ACTION_UP:
					pc.end();
					break;
				}
				return true;
			}
		});

		this.sgd = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
			public boolean onScale(ScaleGestureDetector detector)
			{
				Log.d("VA.OSGL.oS", String.format("scale: %f", detector.getCurrentSpan()));
				zc.update(detector);
				return true;
			}

			public boolean onScaleBegin(ScaleGestureDetector detector)
			{
				Log.d("VA.OSGL.oSB", String.format("scale begin: %f", detector.getCurrentSpan()));
				zc.begin(detector);
				return true;
			}

			public void onScaleEnd(ScaleGestureDetector detector)
			{
				Log.d("VA.OSGL.oSE", String.format("scale end: %f", detector.getCurrentSpan()));
				zc.update(detector);
				zc.end();
			}
		});
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
			focus(new PointF(sgd.getFocusX(), sgd.getFocusY()), ratio);
		}

		public void end()
		{
		}
	}
}
