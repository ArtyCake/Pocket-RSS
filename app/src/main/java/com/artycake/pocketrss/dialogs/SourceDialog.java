package com.artycake.pocketrss.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.models.Article;
import com.artycake.pocketrss.models.Category;
import com.artycake.pocketrss.models.Source;
import com.artycake.pocketrss.requests.SourceRequest;
import com.artycake.pocketrss.utils.RealmController;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by artycake on 3/3/17.
 */

public class SourceDialog {

    @BindView(R.id.source_url)
    EditText sourceUrl;
    @BindView(R.id.source_name)
    EditText sourceName;
    @BindView(R.id.source_categories)
    Spinner sourceCategory;

    private View contentView;
    private AlertDialog dialog;
    private Source source;
    private Realm realm;
    private List<Category> categories = new ArrayList<>();
    private List<String> categoriesValues = new ArrayList<>();

    private OnSourceSaved onSourceSaved;

    public SourceDialog(final Context context, final Source source) {
        contentView = View.inflate(context, R.layout.dialog_source, null);
        ButterKnife.bind(this, contentView);
        RealmResults<Category> categoryRealmResults = RealmController.getInstance(context).getCategories();
        realm = RealmController.getInstance(context).getRealm();
        for (Category category : categoryRealmResults) {
            categoriesValues.add(category.getName());
            categories.add(category);
        }
        sourceCategory.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, categoriesValues));

        setFieldValues(source);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(contentView);
        builder.setTitle(R.string.dialog_source_title);
        builder.setNegativeButton(R.string.dialog_source_negative_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                deleteSourceArticles(source);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        source.deleteFromRealm();
                        dialog.dismiss();
                    }
                });
                if (onSourceSaved != null) {
                    onSourceSaved.onSave();
                }
            }
        });

        builder.setNeutralButton(R.string.dialog_source_neutral_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton(R.string.dialog_source_positive_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newName = sourceName.getText().toString();
                final String newUrl = sourceUrl.getText().toString();
                final Category category = categories.get(sourceCategory.getSelectedItemPosition());
                if (inputIsValid(newName, newUrl, source.getUrl())) {
                    if (source.getUrl().equals(newUrl)) {
                        realm.beginTransaction();
                        source.setName(newName);
                        if (!category.getName().equals(source.getCategory().getName())) {
                            source.getCategory().getSources().remove(source);
                            source.setCategory(category);
                            category.getSources().add(source);
                        }
                        realm.commitTransaction();
                        if (onSourceSaved != null) {
                            onSourceSaved.onSave();
                        }
                        return;
                    }
                    new SourceRequest().checkSourceUrl(newUrl, new SourceRequest.OnDataLoaded() {
                        @Override
                        public void onSuccess() {
                            deleteSourceArticles(source);
                            realm.beginTransaction();
                            source.setName(newName);
                            if (!category.getName().equals(source.getCategory().getName())) {
                                source.getCategory().getSources().remove(source);
                                source.setCategory(category);
                                category.getSources().add(source);
                            }
                            source.setUrl(newUrl);
                            realm.commitTransaction();
                            if (onSourceSaved != null) {
                                onSourceSaved.onSave();
                            }
                        }

                        @Override
                        public void onFailure(@Nullable String message) {
                            Snackbar.make(contentView, R.string.source_invalid_source, Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }
        });

        dialog = builder.create();
    }

    private void setFieldValues(Source source) {
        sourceName.setText(source.getName());
        sourceUrl.setText(source.getUrl());
        sourceCategory.setSelection(categoriesValues.indexOf(source.getCategory().getName()));
    }

    private void deleteSourceArticles(final Source source) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Article> articles = RealmController.getInstance(contentView.getContext()).getArticles(source);
                articles.deleteAllFromRealm();
            }
        });
    }

    private boolean inputIsValid(String newName, String newUrl, String oldUrl) {
        if (newName.isEmpty() || newUrl.isEmpty()) {
            Snackbar.make(contentView, R.string.source_empty_values, BaseTransientBottomBar.LENGTH_LONG).show();
            return false;
        }
        if (oldUrl.equals(newUrl)) {
            return true;
        }
        if (!URLUtil.isNetworkUrl(newUrl)) {
            Snackbar.make(contentView, R.string.source_invalid_url, Snackbar.LENGTH_LONG).show();
            return false;
        }
        Source source = RealmController.getInstance(contentView.getContext()).getSource(newUrl);
        if (source != null) {
            Snackbar.make(contentView, R.string.source_already_exists, Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public SourceDialog setOnSourceSaved(OnSourceSaved onSourceSaved) {
        this.onSourceSaved = onSourceSaved;
        return this;
    }

    public void show() {
        dialog.show();
    }

    public interface OnSourceSaved {
        void onSave();
    }
}
