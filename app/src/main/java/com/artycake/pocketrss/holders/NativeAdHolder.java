package com.artycake.pocketrss.holders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.utils.UserPrefs;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Created by artycake on 3/3/17.
 */

public class NativeAdHolder extends RecyclerView.ViewHolder {
    private Context context;
    private ViewGroup parent;
    private float density;

    public NativeAdHolder(View itemView, ViewGroup parent) {
        super(itemView);
        this.parent = parent;
        context = itemView.getContext();
        density = context.getResources().getDisplayMetrics().density;
    }

    public void updateAd() {
        final NativeExpressAdView adView = new NativeExpressAdView(context);
        final CardView cardView = (CardView) itemView;
        cardView.removeAllViews();
        int width = (int) ((parent.getWidth()) / density - 10);
        adView.setAdSize(new AdSize(width, 90));
        if (UserPrefs.getInstance(context).getBoolPref(UserPrefs.USE_DARK_THEME, false)) {
            adView.setAdUnitId(context.getResources().getString(R.string.feed_ad_dark));
        } else {
            adView.setAdUnitId(context.getResources().getString(R.string.feed_ad_light));
        }
        adView.loadAd(new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("50179AE3D91390F5DAF2343B351F5FCC")
                .build());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                cardView.addView(adView);
            }
        });
    }
}
