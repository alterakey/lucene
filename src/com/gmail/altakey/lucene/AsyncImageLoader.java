package com.gmail.altakey.lucene;

import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.widget.*;
import android.view.*;
import android.util.*;
import android.content.*;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.net.*;
import java.io.*;

public class AsyncImageLoader extends AsyncTask<Void, Void, BitmapDrawable>
{
	Intent intent;
	ImageView view;
	ProgressDialog progress;

	public AsyncImageLoader(ImageView v, Intent intent)
	{
		this.view = v;
		this.intent = intent;
	}

	public static AsyncImageLoader create(ImageView v, Intent intent)
	{
		return new AsyncImageLoader(v, intent);
	}

	protected void onPreExecute()
	{
		this.progress = new ProgressDialog(this.view.getContext());
		this.progress.setTitle("Loading");
		this.progress.setMessage("Please wait...");
		this.progress.show();
	}

	protected void onProgressUpdate(Void... args)
	{
	}

	protected void onPostExecute(BitmapDrawable bitmap)
	{
		this.view.setImageDrawable(bitmap);
		this.progress.dismiss();
	}

	protected BitmapDrawable doInBackground(Void... args)
	{
		Resources res = this.view.getContext().getResources();

		try
		{
			InputStream in = this.read();
			BitmapDrawable bitmap = new BitmapDrawable(res, in);
			return bitmap;
		}
		catch (FileNotFoundException e)
		{
			BitmapDrawable bitmap = new BitmapDrawable(res);
			return bitmap;
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
