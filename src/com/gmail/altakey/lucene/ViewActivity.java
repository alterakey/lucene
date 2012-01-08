/**
 * Copyright (C) 2011-2012 Takahiro Yoshimura
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * In addition, as a special exception, the copyright holders give
 * permission to link the code of portions of this program with the
 * AdMob library under certain conditions as described in each
 * individual source file, and distribute linked combinations
 * including the two.
 *
 * You must obey the GNU General Public License in all respects for
 * all of the code used other than AdMob.  If you modify file(s) with
 * this exception, you may extend this exception to your version of
 * the file(s), but you are not obligated to do so.  If you do not
 * wish to do so, delete this exception statement from your version.
 * If you delete this exception statement from all source files in the
 * program, then also delete it here.
 */

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
	private Restyler restyler = new Restyler();
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
		this.restyler.soft();

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
		this.restyler.hard();
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

	private class Restyler
	{
		public void soft()
		{
			try
			{
				this.restyle();
			}
			catch (HWImageView.ActivityRestartRequired e)
			{
				Log.d("VA.RS.soft", "Ignoring HWIV.ARR");
			}
		}

		public void hard()
		{
			try
			{
				this.restyle();
			}
			catch (HWImageView.ActivityRestartRequired e)
			{
				this.restart();
			}
		}

		private void restyle() throws HWImageView.ActivityRestartRequired
		{
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ViewActivity.this);
			boolean accelerationEnabled = pref.getBoolean("enable_hardware_accel", true);
			ViewActivity.this.view.setHardwareAcceleration(accelerationEnabled);
		}

		private void restart()
		{
			Intent intent = ViewActivity.this.getIntent();
			ViewActivity.this.finish();
			ViewActivity.this.startActivity(intent);
		}
	}
}
