package com.gmail.altakey.lucene;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.*;

import com.google.ads.*;

import com.gmail.altakey.lucene.motion.*;

public class ViewActivity extends Activity implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener
{
	private boolean locked;

	private GestureDetector gd;
	private ScaleGestureDetector sgd;
	private ZoomController zc;
	private PanController pc;
	private RotateController rc;
	private HorizontalFlipController hfc;
	private VerticalFlipController vfc;

	private HWImageView view;
	private AdLoader adLoader;
	private TitleBarController titleBarController = new TitleBarController(this);
	private BrightnessLock brightnessLock = new BrightnessLock(this);

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

		this.titleBarController.onCreate();

        setContentView(R.layout.view);

		this.view = (HWImageView)findViewById(R.id.view);
		this.view.setImageDrawable(new ColorDrawable(0x00000000));
		this.adLoader = new AdLoader(this);
		this.zc = new ZoomController(this.view);
		this.pc = new PanController(this.view);
		this.rc = new RotateController(this.view);
		this.hfc = new HorizontalFlipController(this.view);
		this.vfc = new VerticalFlipController(this.view);
		this.gd = new GestureDetector(this, new RevertGestureListener());
		this.sgd = new ScaleGestureDetector(this, this);

		this.view.setImageMatrix(new Matrix());
		this.view.setOnTouchListener(this);
		
		this.adLoader.load(this.locked);
		AsyncImageLoader.create(this.view, this.getIntent(), new AsyncImageLoader.Callback() {
			public void onComplete()
			{
				ViewActivity.this.revertTransform();
			}
			public void onError()
			{
				finish();
			}
		}).execute();
	}

	private void toggleLock()
	{
		if (!this.locked)
			this.lock();
		else
			this.unlock();
	}
	
	private void lock()
	{
		this.titleBarController.hide();
		this.brightnessLock.hold();
		
		this.locked = true;
		this.adLoader.load(this.locked);
		Toast.makeText(this, R.string.toast_locked, Toast.LENGTH_SHORT).show();
	}
	
	private void unlock()
	{
		this.titleBarController.show();
		this.brightnessLock.release();
		
		this.locked = false;
		this.adLoader.load(this.locked);
		Toast.makeText(this, R.string.toast_unlocked, Toast.LENGTH_SHORT).show();
	}

    @Override
    public void onResume()
    {
		super.onResume();
		this.adLoader.load(this.locked);
	}


    @Override
    public void onDestroy()
    {
		super.onDestroy();
		ImageUnloader.unload(this.view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.inflateMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		this.inflateMenu(menu);
		return true;
	}

	private void inflateMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		menu.clear();
		inflater.inflate(this.locked ? R.menu.view_locked : R.menu.view, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId())
		{
		case R.id.menu_flip_horizontal:
			this.hfc.toggle();
			return true;
		case R.id.menu_flip_vertical:
			this.vfc.toggle();
			return true;
		case R.id.menu_preferences:
			startActivity(new Intent(this, ConfigActivity.class));
			return true;
		case R.id.menu_toggle_lock:
			this.toggleLock();
			return true;
		case R.id.menu_close:
			this.finish();
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_BACK:
			if (this.locked)
			{
				this.unlock();
				return true;
			}
			break;
		}

		return super.onKeyDown(keyCode, event);
	}

	public boolean onTouch(View v, MotionEvent e)
	{
		if (this.locked)
			return true;

		this.gd.onTouchEvent(e);
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

	private void revertTransform()
	{
		int imageWidth = view.getDrawable().getIntrinsicWidth();
		int imageHeight = view.getDrawable().getIntrinsicHeight();

		Matrix m = new Matrix();

		RectF drawable = new RectF(0, 0, imageWidth, imageHeight);
		RectF viewport = new RectF(0, 0, view.getWidth(), view.getHeight());
		m.setRectToRect(drawable, viewport, Matrix.ScaleToFit.CENTER);
		
		view.setImageMatrix(m);
	}

	private class RevertGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onDoubleTap(MotionEvent e)
		{
			ViewActivity.this.revertTransform();
			return true;
		}
	}
}
