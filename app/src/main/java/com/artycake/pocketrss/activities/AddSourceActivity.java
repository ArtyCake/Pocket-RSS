package com.artycake.pocketrss.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.models.Category;
import com.artycake.pocketrss.models.Source;
import com.artycake.pocketrss.requests.FeedRequest;
import com.artycake.pocketrss.requests.SourceRequest;
import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.UserPrefs;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class AddSourceActivity extends AppCompatActivity {

    @BindView(R.id.source_url)
    EditText sourceUrl;
    @BindView(R.id.source_name)
    EditText sourceName;
    @BindView(R.id.source_categories)
    Spinner sourceCategory;

    private List<Category> categories = new ArrayList<>();
    private List<String> categoriesValues = new ArrayList<>();

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.USE_DARK_THEME, false)) {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_source);
        ButterKnife.bind(this);
        RealmResults<Category> categoryRealmResults = RealmController.getInstance(this).getCategories();
        realm = RealmController.getInstance(this).getRealm();
        for (Category category : categoryRealmResults) {
            categoriesValues.add(category.getName());
            categories.add(category);
        }
        sourceCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoriesValues));
        Uri data = getIntent().getData();
        if (data != null) {
            sourceUrl.setText(data.toString());
        }
    }

    @OnClick(R.id.source_add)
    public void addSource(final View view) {
        final String sourceUrlValue = sourceUrl.getText().toString().trim();
        final String sourceNameValue = sourceName.getText().toString().trim();
        final String finalSourceUrlValue;
        if (sourceUrlValue.endsWith("/")) {
            finalSourceUrlValue = sourceUrlValue.substring(0, sourceUrlValue.length() - 1);
        } else {
            finalSourceUrlValue = sourceUrlValue;
        }
        int sourceCategorySelected = sourceCategory.getSelectedItemPosition();
        final Category category = categories.get(sourceCategorySelected);
        if (sourceUrlValue.isEmpty() || sourceNameValue.isEmpty()) {
            Snackbar.make(view, R.string.source_empty_values, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if (!URLUtil.isNetworkUrl(finalSourceUrlValue)) {
            Snackbar.make(view, R.string.source_invalid_url, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        Source source = RealmController.getInstance(this).getSource(finalSourceUrlValue);
        if (source != null) {
            Snackbar.make(view, R.string.source_already_exists, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        view.setEnabled(false);
        new SourceRequest().checkSourceUrl(finalSourceUrlValue, new SourceRequest.OnDataLoaded() {
            @Override
            public void onSuccess() {
                realm.beginTransaction();
                Source source = new Source();
                source.setCategory(category);
                source.setName(sourceNameValue);
                source.setUrl(finalSourceUrlValue);
                source.setColor(Source.getRandomColor(AddSourceActivity.this));
                category.getSources().add(source);
                realm.commitTransaction();
                Intent upIntent = NavUtils.getParentActivityIntent(AddSourceActivity.this);
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                UserPrefs.getInstance(AddSourceActivity.this).putPreferences(UserPrefs.SHOULD_UPLOAD_NEWS, true);
                startActivity(upIntent);
                finish();
            }

            @Override
            public void onFailure(@Nullable String message) {
                Snackbar.make(view, R.string.source_invalid_source, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                view.setEnabled(true);
            }
        });
    }
}
