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
		public void onError();
	}

	private final static Callback NullCallback = new Callback()
	{
		public void onComplete()
		{
		}

		public void onError()
		{
		}
	};
	
	Intent intent;
	ImageView view;
	ProgressDialog progress;
	Toast oomMessage;
	AndroidHttpClient httpClient;
	AsyncImageLoader.Callback cb = NullCallback;

	public AsyncImageLoader(ImageView v, Intent intent, AsyncImageLoader.Callback cb)
	{
		this.view = v;
		this.intent = intent;
		if (cb != null)
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
		this.httpClient = AndroidHttpClient.newInstance(this.getUserAgent());
		this.oomMessage = Toast.makeText(context, R.string.toast_out_of_memory, Toast.LENGTH_SHORT);
		this.progress = new ProgressDialog(context);
		this.progress.setTitle(context.getString(R.string.dialog_loading_title));
		this.progress.setMessage(context.getString(R.string.dialog_loading_message));
		this.progress.show();
	}

	private String getUserAgent()
	{
		Context context = this.view.getContext();
		return String.format("%s/%s", context.getString(R.string.app_name), context.getString(R.string.app_version));
	}

	protected void onProgressUpdate(Void... args)
	{
	}

	protected void onPostExecute(BitmapDrawable bitmap)
	{
		if (bitmap != null)
		{
			this.view.setImageDrawable(bitmap);
			this.cb.onComplete();
		}
		else
		{
			this.cb.onError();
		}
		this.progress.dismiss();
		this.httpClient.close();
	}

	protected BitmapDrawable doInBackground(Void... args)
	{
		Context context = this.view.getContext();
		Resources res = context.getResources();

		try
		{
			InputStream in = this.read();
			BitmapDrawable bitmap = new BitmapDrawable(res, in);
			return bitmap;
		}
		catch (FileNotFoundException e)
		{
			return null;
		}
		catch (OutOfMemoryError e)
		{
			this.oomMessage.show();
			return null;
		}
	}

	private InputStream read() throws FileNotFoundException
	{
		Context context = this.view.getContext();

		if (Intent.ACTION_SEND.equals(this.intent.getAction()))
		{
			Bundle extras = this.intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM))
				return new BitmapInputStream(context.getContentResolver().openInputStream((Uri)extras.getParcelable(Intent.EXTRA_STREAM)));
			if (extras.containsKey(Intent.EXTRA_TEXT))
			{
				try
				{
					HttpGet req = new HttpGet(extras.getCharSequence(Intent.EXTRA_TEXT).toString());
					return new BitmapInputStream(this.httpClient.execute(req).getEntity().getContent());
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
			return new BitmapInputStream(context.getContentResolver().openInputStream(this.intent.getData()));

		return null;
	}


	private static class BitmapInputStream extends FilterInputStream
	{
		private int marked = 0;
		private long position = 0;

		public BitmapInputStream(InputStream in)
		{
			super(in);
		}

		@Override
		public int read(byte[] buffer, int offset, int count) throws IOException
		{
			int advanced = super.read(buffer, offset, count);
			this.position += advanced;
			this.report();
			return advanced;
		}

		@Override
		public synchronized void reset() throws IOException
		{
			super.reset();
			this.position = this.marked;
		}

		@Override
		public synchronized void mark(int readlimit)
		{
			super.mark(readlimit);
			this.marked = readlimit;
		}

		@Override
		public long skip(long byteCount) throws IOException
		{
			long advanced = super.skip(byteCount);
			this.position += advanced;
			this.report();
			return advanced;
		}

		private void report()
		{
			Log.d("BIS", String.format("BIS.report: at %d", this.position));
		}
	}
}
