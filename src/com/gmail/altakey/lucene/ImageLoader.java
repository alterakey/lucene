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
import android.webkit.*;
import java.io.*;

public class ImageLoader
{
	Intent intent;
	WebView view;

	public ImageLoader(WebView v, Intent intent)
	{
		this.view = v;
		this.intent = intent;
	}

	public static ImageLoader create(WebView v, Intent intent)
	{
		return new ImageLoader(v, intent);
	}

	public void load()
	{
		final String mimeType = "text/html";
		final String encoding = "utf-8";
		String html = String.format("<img src=\"%s\" />", this.getUri().toString());

		this.view.loadDataWithBaseURL("fake://not/needed", html, mimeType, encoding, "");
	}

	private Uri getUri()
	{
		Context context = this.view.getContext();

		if (Intent.ACTION_SEND.equals(this.intent.getAction()))
		{
			Bundle extras = this.intent.getExtras();
			if (extras.containsKey(Intent.EXTRA_STREAM))
				return (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
		}

		if (Intent.ACTION_VIEW.equals(this.intent.getAction()))
			return this.intent.getData();

		return null;
	}
}
