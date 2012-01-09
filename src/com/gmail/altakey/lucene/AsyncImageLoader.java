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
import android.graphics.drawable.ColorDrawable;
import android.net.*;
import android.net.http.AndroidHttpClient;
import org.apache.http.client.methods.HttpGet;
import java.io.*;

public class AsyncImageLoader extends AsyncTask<Void, Long, BitmapDrawable>
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
	HWImageView view;
	ProgressDialog progress;
	Toast oomMessage;
	AndroidHttpClient httpClient;
	AsyncImageLoader.Callback cb = NullCallback;

	public AsyncImageLoader(HWImageView v, Intent intent, AsyncImageLoader.Callback cb)
	{
		this.view = v;
		this.intent = intent;
		if (cb != null)
			this.cb = cb;
	}

	public static AsyncImageLoader create(HWImageView v, Intent intent)
	{
		return create(v, intent, null);
	}

	public static AsyncImageLoader create(HWImageView v, Intent intent, AsyncImageLoader.Callback cb)
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
		this.progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.progress.setMessage(context.getString(R.string.dialog_loading_message));
		this.progress.show();

		ImageUnloader.unload(this.view);
	}

	private String getUserAgent()
	{
		Context context = this.view.getContext();
		return String.format("%s/%s", context.getString(R.string.app_name), context.getString(R.string.app_version));
	}

	protected void onProgressUpdate(Long... args)
	{
		int at = args[0].intValue();
		int size = args[1].intValue();
		boolean indeterminate = (size <= 0);

		this.progress.setIndeterminate(indeterminate);
		if (!indeterminate)
		{
			this.progress.setMax(size / 1024);
			this.progress.setProgress(at / 1024);
		}
		else
			this.progress.setProgress(0);
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
			InputStream in = this.read(new BitmapInputStream.ProgressListener() {
				public void onAdvance(long at, long size)
				{
					publishProgress(at, size);
				}
			});
			BitmapFactory.Options bfo = new BitmapFactory.Options();
			bfo.inDither = true;
			bfo.inPreferredConfig = Bitmap.Config.RGB_565;
			try
			{
				bfo.inPreferQualityOverSpeed = true;
			}
			catch (NoSuchFieldError e)
			{
			}
			Bitmap bitmap = BitmapFactory.decodeStream(in, new Rect(-1,-1,-1,-1), bfo);
			return new BitmapDrawable(res, this.scale(bitmap));
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

	private Bitmap scale(Bitmap src)
	{
		int width = src.getWidth();
		int height = src.getHeight();
		final int maxWidth = this.view.getAcceleration().maxBitmapWidth;
		final int maxHeight = this.view.getAcceleration().maxBitmapHeight;
		if (maxWidth < 0 || maxHeight < 0)
			return src;
		if (width < maxWidth && height < maxHeight)
			return src;
		
		if (width > height)
		{
			height = (int)(height * (maxWidth / (float)width));
			width = maxWidth;
		}
		if (width < height)
		{
			width = (int)(width * (maxHeight / (float)height));
			height = maxHeight;
		}
		Log.d("AIL", String.format("scaling: (%d, %d) -> (%d, %d)", src.getWidth(), src.getHeight(), width, height));
		return Bitmap.createScaledBitmap(src, width, height, true);		
	}

	private InputStream read() throws FileNotFoundException
	{
		return this.read(null);
	}

	private InputStream read(BitmapInputStream.ProgressListener listener) throws FileNotFoundException
	{
		Context context = this.view.getContext();

		if (Intent.ACTION_SEND.equals(this.intent.getAction()))
		{
			Bundle extras = this.intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM))
				return new BitmapInputStream(context.getContentResolver().openInputStream((Uri)extras.getParcelable(Intent.EXTRA_STREAM)), listener);
			if (extras.containsKey(Intent.EXTRA_TEXT))
			{
				try
				{
					HttpGet req = new HttpGet(extras.getCharSequence(Intent.EXTRA_TEXT).toString());
					return new BitmapInputStream(this.httpClient.execute(req).getEntity().getContent(), listener);
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
			return new BitmapInputStream(context.getContentResolver().openInputStream(this.intent.getData()), listener);

		return null;
	}


	private static class BitmapInputStream extends FilterInputStream
	{
		public interface ProgressListener
		{
			void onAdvance(long at, long length);
		}

		private int marked = 0;
		private long position = 0;
		private ProgressListener listener;

		public BitmapInputStream(InputStream in, ProgressListener listener)
		{
			super(in);
			this.listener = listener;
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
			if (this.listener == null)
				return;

			try
			{
				this.listener.onAdvance(this.position, this.position + this.in.available());
			}
			catch (IOException e)
			{
				this.listener.onAdvance(this.position, 0);
			}
		}
	}
}
