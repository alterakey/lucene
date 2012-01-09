package com.gmail.altakey.lucene.motion;

import android.graphics.Matrix;
import android.widget.ImageView;

public final class HorizontalFlipController
{
	private final ImageView view;

	public HorizontalFlipController(final ImageView view)
	{
		this.view = view;
	}

	public void toggle()
	{
		Matrix m = new Matrix(this.view.getImageMatrix());
		m.postScale(-1, 1, this.view.getWidth() / 2, this.view.getHeight() / 2);
		this.view.setImageMatrix(m);
	}
}
