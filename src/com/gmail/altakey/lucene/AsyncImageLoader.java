/**
 * Copyright (C) 2011 Takahiro Yoshimura
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
	AndroidHttpClient httpClient;
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
		this.httpClient = AndroidHttpClient.newInstance(this.getUserAgent());
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
		this.view.setImageDrawable(bitmap);
		if (this.cb != null)
			this.cb.onComplete();
		this.progress.dismiss();
		this.httpClient.close();
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
					return this.httpClient.execute(req).getEntity().getContent();
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
