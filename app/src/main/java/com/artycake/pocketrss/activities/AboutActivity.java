package com.artycake.pocketrss.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import com.artycake.pocketrss.BuildConfig;
import com.artycake.pocketrss.R;
import com.artycake.pocketrss.utils.UserPrefs;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.app_info)
    TextView appInfo;
    @BindView(R.id.rate_text)
    TextView rateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.USE_DARK_THEME, false)) {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        appInfo.setText(getResources().getString(R.string.app_info, getResources().getString(R.string.app_name), BuildConfig.VERSION_NAME));

        String before = getResources().getString(R.string.rate_text_before);
        String rate = getResources().getString(R.string.rate_text_rate);
        String after = getResources().getString(R.string.rate_text_after);
        String text = before + rate + after;
        Spannable spannable = new SpannableString(text);
        int color;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            color = getResources().getColor(R.color.colorAccent, getTheme());
        } else {
            color = getResources().getColor(R.color.colorAccent);
        }
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            }
        };
        spannable.setSpan(new ForegroundColorSpan(color), before.length() + 1, before.length() + rate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(clickableSpan, before.length() + 1, before.length() + rate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        rateText.setText(spannable, TextView.BufferType.SPANNABLE);
    }
}
