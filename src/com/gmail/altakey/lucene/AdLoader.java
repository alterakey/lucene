package com.gmail.altakey.lucene;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.View;
import com.google.ads.AdView;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;

public class AdLoader
{
	private static final int AD_VIEW_ID = 0xdeadbeef;
	private static final String AD_UNIT_ID = "a14ebe7748e6551";

	private Activity activity;

	public AdLoader(Activity activity)
	{
		this.activity = activity;
	}

	public static AdLoader create(Activity activity)
	{
		return new AdLoader(activity);
	}

	public void load()
	{
		this.load(false);
	}

	public void load(boolean locked)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.activity);

		if (pref.getBoolean("show_ad", true))
		{
			if (locked && pref.getBoolean("hide_ad_on_lock", false))
				this.hide();
			else
				this.show();
		}
		else
		{
			this.hide();
		}
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
