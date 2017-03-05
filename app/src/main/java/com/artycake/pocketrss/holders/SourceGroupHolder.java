package com.artycake.pocketrss.holders;

import android.view.View;
import android.widget.TextView;

import com.artycake.pocketrss.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by artycake on 3/2/17.
 */

public class SourceGroupHolder {
    @BindView(R.id.category_name)
    TextView groupName;

    public SourceGroupHolder(View view) {
        ButterKnife.bind(this, view);
    }

    public void updateUI(String categoryName) {
        groupName.setText(categoryName);
    }
}
