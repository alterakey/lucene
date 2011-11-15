package com.gmail.altakey.lucene;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import com.google.ads.AdView;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;

public class AdLoader
{
	private static final int AD_VIEW_ID = 0xdeadbeef;
	private static final String AD_UNIT_ID = "a14ebe7748e6551";

	private Activity activity;
	private ViewGroup layout;

	public AdLoader(Activity activity, ViewGroup layout)
	{
		this.activity = activity;
		this.layout = layout;
	}

	public static AdLoader create(Activity activity, ViewGroup layout)
	{
		return new AdLoader(activity, layout);
	}

	public boolean isEnabled()
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.activity);

		return pref.getBoolean("show_ad", true);
	}

	public void load()
	{
		if (this.isEnabled())
			this.show();
		else
			this.hide();
	}

	public void show()
	{
		AdView adView = (AdView)this.activity.findViewById(R.id.adView);
		adView.setVisibility(View.VISIBLE);

		AdRequest req = new AdRequest();
		req.addTestDevice(AdRequest.TEST_EMULATOR);
		req.addTestDevice(this.activity.getString(R.string.test_device));
		adView.loadAd(req);
	}

	public void hide()
	{
		AdView adView = (AdView)this.activity.findViewById(R.id.adView);
		adView.stopLoading();
		adView.setVisibility(View.GONE);
	}
}
