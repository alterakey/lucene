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
import android.net.http.AndroidHttpClient;
import org.apache.http.client.methods.HttpGet;
import java.io.*;

public class AsyncImageLoader extends AsyncTask<Void, Void, BitmapDrawable>
{
	public interface Callback
	{
		public void onComplete();
	}
	
	Intent intent;
	ImageView view;
	ProgressDialog progress;
	AsyncImageLoader.Callback cb;

	public AsyncImageLoader(ImageView v, Intent intent, AsyncImageLoader.Callback cb)
	{
		this.view = v;
		this.intent = intent;
		this.cb = cb;
	}

	public static AsyncImageLoader create(ImageView v, Intent intent)
	{
		return create(v, intent, null);
	}

	public static AsyncImageLoader create(ImageView v, Intent intent, AsyncImageLoader.Callback cb)
	{
		return new AsyncImageLoader(v, intent, cb);
	}

	protected void onPreExecute()
	{
		Context context = this.view.getContext();
		this.progress = new ProgressDialog(context);
		this.progress.setTitle(context.getString(R.string.dialog_loading_title));
		this.progress.setMessage(context.getString(R.string.dialog_loading_message));
		this.progress.show();
	}

	protected void onProgressUpdate(Void... args)
	{
	}

	protected void onPostExecute(BitmapDrawable bitmap)
	{
		this.view.setImageDrawable(bitmap);
		if (this.cb != null)
			this.cb.onComplete();
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
			if (extras.containsKey(Intent.EXTRA_TEXT))
			{
				try
				{
					HttpGet req = new HttpGet(extras.getCharSequence(Intent.EXTRA_TEXT).toString());
					return AndroidHttpClient.newInstance("Lightboxdroid/0.1.2").execute(req).getEntity().getContent();
				}
				catch (IllegalArgumentException e)
				{
					return null;
				}
				catch (IOException e)
				{
					return null;
				}
			}
		}

		if (Intent.ACTION_VIEW.equals(this.intent.getAction()))
			return context.getContentResolver().openInputStream(this.intent.getData());

		return null;
	}
}
