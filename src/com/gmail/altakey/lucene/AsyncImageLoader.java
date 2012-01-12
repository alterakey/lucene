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

public final class AsyncImageLoader extends AsyncTask<Void, Long, BitmapDrawable>
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
	
	private final Intent intent;
	private final HWImageView view;
	private final AsyncImageLoader.Callback cb;

	private ProgressDialog progress;
	private Toast oomMessage;
	private AndroidHttpClient httpClient;

	public AsyncImageLoader(final HWImageView v, final Intent intent, final AsyncImageLoader.Callback cb)
	{
		this.view = v;
		this.intent = intent;
		if (cb != null)
			this.cb = cb;
		else
			this.cb = NullCallback;
	}

	public static AsyncImageLoader create(final HWImageView v, final Intent intent)
	{
		return create(v, intent, null);
	}

	public static AsyncImageLoader create(final HWImageView v, final Intent intent, final AsyncImageLoader.Callback cb)
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
		final Context context = this.view.getContext();
		final Resources res = context.getResources();

		try
		{
			InputStream in = this.read(new ProgressReportingInputStream.ProgressListener() {
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
		final int maxWidth = this.view.getAcceleration().getMaximumBitmapWidth();
		final int maxHeight = this.view.getAcceleration().getMaximumBitmapHeight();
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

	private InputStream read(ProgressReportingInputStream.ProgressListener listener) throws FileNotFoundException
	{
		final Context context = this.view.getContext();

		if (Intent.ACTION_SEND.equals(this.intent.getAction()))
		{
			final Bundle extras = this.intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM))
				return new ProgressReportingInputStream(context.getContentResolver().openInputStream((Uri)extras.getParcelable(Intent.EXTRA_STREAM)), listener);
			if (extras.containsKey(Intent.EXTRA_TEXT))
			{
				try
				{
					final HttpGet req = new HttpGet(extras.getCharSequence(Intent.EXTRA_TEXT).toString());
					return new ProgressReportingInputStream(this.httpClient.execute(req).getEntity().getContent(), listener);
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
			return new ProgressReportingInputStream(context.getContentResolver().openInputStream(this.intent.getData()), listener);

		return null;
	}
}
