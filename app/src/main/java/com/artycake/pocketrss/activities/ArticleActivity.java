package com.artycake.pocketrss.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.models.Article;
import com.artycake.pocketrss.utils.NetworkHelper;
import com.artycake.pocketrss.utils.PicassoImageGetter;
import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.TextHelper;
import com.artycake.pocketrss.utils.UserPrefs;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class ArticleActivity extends AppCompatActivity {
    public static final String GUID = "guid";

    @BindView(R.id.article_content)
    TextView articleContent;
    @BindView(R.id.article_title)
    TextView articleTitle;
    @BindView(R.id.article_date)
    TextView articleDate;
    @BindView(R.id.category_name)
    TextView categoryName;
    @BindView(R.id.article_enclosure)
    ImageView articleEnclosure;

    private Article article;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.USE_DARK_THEME, false)) {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ButterKnife.bind(this);
        String guid;
        if (savedInstanceState != null) {
            guid = savedInstanceState.getString(GUID);
        } else {
            guid = getIntent().getStringExtra(GUID);
        }
        article = RealmController.getInstance(this).getArticle(guid);
        if (article == null) {
            finish();
            return;
        }

        if (!article.isRead()) {
            Realm realm = RealmController.getInstance(this).getRealm();
            realm.beginTransaction();
            article.setRead(true);
            realm.commitTransaction();
        }
        getSupportActionBar().setTitle(article.getSource().getName());
        articleContent.setMovementMethod(LinkMovementMethod.getInstance());
        articleTitle.setText(article.getTitle());
        articleDate.setText(TextHelper.getArticleDate(article));
        categoryName.setText(article.getCategory().getName());
        String imageLoadCase = UserPrefs.getInstance(this).getStringPref(UserPrefs.IMAGE_LOADING, UserPrefs.IL_WIFI_ONLY);
        Logger.d("loadcase = %s", imageLoadCase);
        Html.ImageGetter imageGetter = null;
        boolean shouldLoadImages = false;
        NetworkHelper networkHelper = new NetworkHelper(this);
        if (imageLoadCase.equals(UserPrefs.IL_WIFI_ONLY)) {
            Logger.d("wifi is enabled: %s", networkHelper.isWifiConnected());
            if (networkHelper.isWifiConnected()) {
                shouldLoadImages = true;
            }
        } else if (imageLoadCase.equals(UserPrefs.IL_ALWAYS)) {
            if (networkHelper.isNetworkConnected()) {
                shouldLoadImages = true;
            }
        }
        String description;
        if (shouldLoadImages) {
            if (article.getEnclosure() != null && article.getEnclosure().getType().contains("image/")) {
                Picasso.with(this).load(article.getEnclosure().getUrl()).into(articleEnclosure);
            }
            imageGetter = new PicassoImageGetter(articleContent, getResources(), Picasso.with(this));
            description = article.getDescription();
        } else {
            description = TextHelper.clearHtmlFromImg(article.getDescription());
        }

        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
        } else {
            result = Html.fromHtml(description, imageGetter, null);
        }
        Spannable spannable = new SpannableString(result);
        Linkify.addLinks(spannable, Linkify.ALL);
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (URLSpan urlSpan : spans) {
            Logger.d(urlSpan);
            LinkSpan linkSpan = new LinkSpan(urlSpan.getURL());
            int spanStart = spannable.getSpanStart(urlSpan);
            int spanEnd = spannable.getSpanEnd(urlSpan);
            spannable.setSpan(linkSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.removeSpan(urlSpan);
        }
        articleContent.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article, menu);
        this.menu = menu;
        if (article != null && article.isFavorite()) {
            setFavoriteIcon(true);
        }
        return true;
    }

    private void setFavoriteIcon(boolean filled) {
        if (filled) {
            menu.getItem(0).setIcon(R.drawable.ic_star_full);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_star_empty);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_open) {
            openLink(article.getLink());
            return true;
        } else if (item.getItemId() == R.id.option_favorite) {
            Realm realm = RealmController.getInstance(this).getRealm();
            realm.beginTransaction();
            article.setFavorite(!article.isFavorite());
            realm.commitTransaction();
            setFavoriteIcon(article.isFavorite());
        }
        return super.onOptionsItemSelected(item);
    }

    private void openLink(String link) {
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.USE_IN_APP_BROWSER, true)) {
            Intent intent = new Intent(this, BrowserActivity.class);
            intent.putExtra(BrowserActivity.URL, link);
            startActivity(intent);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GUID, article.getGuid());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            String guid = savedInstanceState.getString(GUID);
            article = RealmController.getInstance(this).getArticle(guid);
        }
    }

    private class LinkSpan extends URLSpan {

        public LinkSpan(String url) {
            super(url);
        }

        @Override
        public void onClick(View widget) {
            openLink(getURL());
        }
    }
}
