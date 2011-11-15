package com.gmail.altakey.lucene;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.*;

import com.google.ads.*;

public class ViewActivity extends Activity implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener
{
	private ScaleGestureDetector sgd;
	private ZoomController zc;
	private PanController pc;

	private ImageView view;
	private AdLoader adLoader;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

		this.view = (ImageView)findViewById(R.id.view);
		this.adLoader = new AdLoader(this, (ViewGroup)this.view.getRootView());
		this.zc = new ZoomController(this.view);
		this.pc = new PanController(this.view);
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
