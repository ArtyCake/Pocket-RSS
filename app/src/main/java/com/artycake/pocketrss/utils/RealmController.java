package com.artycake.pocketrss.utils;

import android.content.Context;

import com.artycake.pocketrss.models.Article;
import com.artycake.pocketrss.models.Category;
import com.artycake.pocketrss.models.Source;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by artycake on 2/27/17.
 */
public class RealmController {
    private static RealmController instance;
    private Realm realm;

    public static RealmController getInstance(Context context) {
        if (instance == null) {
            instance = new RealmController(context);
        }
        return instance;
    }

    private RealmController(Context context) {
        Realm.init(context);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
        realm = Realm.getDefaultInstance();
    }

    public Realm getRealm() {
        return realm;
    }

    public RealmResults<Category> getCategories() {
        return realm.where(Category.class).findAll();
    }

    public RealmResults<Category> getNotEmptyCategories() {
        return realm.where(Category.class).not().isEmpty("sources").findAll();
    }

    public RealmResults<Source> getSources() {
        return realm.where(Source.class).findAll();
    }

    public RealmResults<Article> getArticles() {
        return realm.where(Article.class).findAllSorted("date", Sort.DESCENDING);
    }

    public long getArticlesCount() {
        return realm.where(Article.class).count();
    }

    public Article getArticle(String guid) {
        return realm.where(Article.class).equalTo("guid", guid).findFirst();
    }

    public RealmResults<Article> getArticles(Source source) {
        return realm.where(Article.class).equalTo("source.url", source.getUrl()).findAllSorted("date", Sort.DESCENDING);
    }

    public Source getSource(String urlValue) {
        return realm.where(Source.class).equalTo("url", urlValue).findFirst();
    }

    public RealmResults<Article> getFavoriteArticles() {
        return realm.where(Article.class).equalTo("favorite", true).findAllSorted("date", Sort.DESCENDING);
    }

    public void clearArticles(int storageTime) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -storageTime);
        final Date limitDate = calendar.getTime();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Article> articles = realm.where(Article.class).lessThan("date", limitDate).findAll();
                articles.deleteAllFromRealm();
            }
        });
    }

    public void clearAll() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Article> articles = realm.where(Article.class).findAll();
                articles.deleteAllFromRealm();
                RealmResults<Source> sources = realm.where(Source.class).findAll();
                sources.deleteAllFromRealm();
            }
        });
    }

    public long getSourcesCount() {
        return realm.where(Source.class).count();
    }
}
