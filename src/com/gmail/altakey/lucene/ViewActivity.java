package com.gmail.altakey.lucene;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.*;

import com.google.ads.*;

public class ViewActivity extends Activity implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener
{
	private ScaleGestureDetector sgd;
	private ZoomController zc;
	private PanController pc;
	private RotateController rc;

	private ImageView view;
	private AdLoader adLoader;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

		this.view = (ImageView)findViewById(R.id.view);
		this.adLoader = new AdLoader(this);
		this.zc = new ZoomController(this.view);
		this.pc = new PanController(this.view);
		this.rc = new RotateController(this.view);
		this.sgd = new ScaleGestureDetector(this, this);

		this.view.setImageMatrix(new Matrix());
		this.view.setOnTouchListener(this);
		
		this.adLoader.load();
		AsyncImageLoader.create(this.view, this.getIntent()).execute();
    }

    @Override
    public void onResume()
    {
		super.onResume();
		this.adLoader.load();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId())
		{
		case R.id.menu_preferences:
			startActivity(new Intent(this, ConfigActivity.class));
			return true;
		case R.id.menu_close:
			this.finish();
		}
		return true;
	}

	public boolean onTouch(View v, MotionEvent e)
	{
		this.sgd.onTouchEvent(e);
		switch (e.getActionMasked())
		{
		case MotionEvent.ACTION_DOWN:
			this.pc.begin(e);
			this.rc.begin(e);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			this.rc.down(e);
			break;
		case MotionEvent.ACTION_POINTER_UP:
			this.rc.up(e);
			break;
		case MotionEvent.ACTION_MOVE:
			this.pc.update(e);
			this.rc.update(e);
			break;
		case MotionEvent.ACTION_UP:
			this.pc.end();
			this.rc.end();
			break;
		}
		return true;
	}

	public boolean onScale(ScaleGestureDetector detector)
	{
		this.zc.update(detector);
		return true;
	}
	
	public boolean onScaleBegin(ScaleGestureDetector detector)
	{
		this.zc.begin(detector);
		return true;
	}
	
	public void onScaleEnd(ScaleGestureDetector detector)
	{
		this.zc.update(detector);
		this.zc.end();
	}

	private class RotateController
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

	private class PanController
	{
		private ImageView view;
		private float x;
		private float y;

		public PanController(ImageView view)
		{
			this.view = view;
		}

		public void begin(MotionEvent e)
		{
			this.x = e.getX();
			this.y = e.getY();
		}

		public void update(MotionEvent e)
		{
			this.apply(e.getX() - this.x, e.getY() - this.y);
			this.x = e.getX();
			this.y = e.getY();
		}

		public void end()
		{
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

	private class ZoomController
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
}
