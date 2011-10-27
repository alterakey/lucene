package com.gmail.altakey.lucene;

import android.os.Bundle;
import android.widget.*;
import android.view.*;
import android.util.*;
import android.content.*;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.*;
import java.io.*;

public class ImageLoader
{
	Intent intent;
	ImageView view;

	public ImageLoader(ImageView v, Intent intent)
	{
		this.view = v;
		this.intent = intent;
	}

	public static ImageLoader create(ImageView v, Intent intent)
	{
		return new ImageLoader(v, intent);
	}

	public void load()
	{
		Resources res = this.view.getContext().getResources();

		try
		{
			InputStream in = this.read();
			BitmapDrawable bitmap = new BitmapDrawable(res, in);
			this.view.setImageDrawable(bitmap);
		}
		catch (FileNotFoundException e)
		{
			BitmapDrawable bitmap = new BitmapDrawable(res);
			this.view.setImageDrawable(bitmap);
		}
	}

	private InputStream read() throws FileNotFoundException
	{
		Context context = this.view.getContext();

		if (Intent.ACTION_SEND.equals(this.intent.getAction()))
		{
			Bundle extras = this.intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM))
				return context.getContentResolver().openInputStream((Uri)extras.getParcelable(Intent.EXTRA_STREAM));
		}

		if (Intent.ACTION_VIEW.equals(this.intent.getAction()))
			return context.getContentResolver().openInputStream(this.intent.getData());

		return null;
	}
}
