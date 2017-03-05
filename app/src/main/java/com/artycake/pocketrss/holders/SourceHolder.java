package com.artycake.pocketrss.holders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.adapters.CategoriesAdapter;
import com.artycake.pocketrss.models.Source;
import com.artycake.pocketrss.utils.TextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by artycake on 3/2/17.
 */

public class SourceHolder {
    @BindView(R.id.source_name)
    TextView sourceName;
    @BindView(R.id.source_cover)
    ImageView sourceCover;
    @BindView(R.id.source_cover_text)
    TextView sourceCoverText;
    @BindView(R.id.source_settings)
    ImageButton sourceSettings;

    public SourceHolder(View view) {
        ButterKnife.bind(this, view);
        sourceSettings.setFocusable(false);
    }

    public void updateUI(Source source) {
        sourceName.setText(source.getName());
        sourceCover.setColorFilter(source.getColor());
        sourceCoverText.setText(TextHelper.getShortSourceName(source));
    }

    public void setSettingsClickListener(View.OnClickListener onClickListener) {
        sourceSettings.setOnClickListener(onClickListener);
    }
}
