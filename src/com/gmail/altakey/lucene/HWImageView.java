package com.gmail.altakey.lucene;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.graphics.Canvas;
import android.widget.ImageView;
import android.util.AttributeSet;

public final class HWImageView extends ImageView
{
	private final HWAcceleration accel = new HWAcceleration(this);

	public HWImageView(Context context)
	{
		super(context);
	}
	public HWImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	public HWImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		this.accel.note(canvas);
	}

	public final HWAcceleration getAcceleration()
	{
		return this.accel;
	}
}
