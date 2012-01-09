package com.gmail.altakey.lucene;

import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;

public final class ImageUnloader
{
	public static void unload(final ImageView view)
	{
		try
		{
			Bitmap bitmap = ((BitmapDrawable)view.getDrawable()).getBitmap();
			view.setImageDrawable(new ColorDrawable(0x00000000));
			bitmap.recycle();
			bitmap = null;
		}
		catch (ClassCastException e)
		{
		}
	}
}
