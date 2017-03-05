package com.artycake.pocketrss.requests;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.artycake.pocketrss.models.Article;
import com.artycake.pocketrss.models.Enclosure;
import com.artycake.pocketrss.models.Feed;
import com.artycake.pocketrss.models.Source;
import com.artycake.pocketrss.retrofit.RSS;
import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.TextHelper;
import com.orhanobut.logger.Logger;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by artycake on 2/27/17.
 */

public class FeedRequest {

    private OnDataLoaded onDataLoaded;

    public void setOnDataLoaded(OnDataLoaded onDataLoaded) {
        this.onDataLoaded = onDataLoaded;
    }

    public void loadFeed(final Context context, final Source source) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://square.github.io/retrofit/").addConverterFactory(SimpleXmlConverterFactory.create()).build();
        RSS rss = retrofit.create(RSS.class);
        Call<Feed> call = rss.loadFeed(source.getUrl());
        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                if (response.isSuccessful()) {
                    Feed feed = response.body();
                    Logger.d(response.raw());
                    Realm realm = RealmController.getInstance(context).getRealm();
                    if (feed.getArticles() == null) {
                        Logger.d("%s has no articles ", feed.getTitle());
                    } else {
                        for (Article article : feed.getArticles()) {
                            realm.beginTransaction();
                            if (article.getGuid() == null || article.getGuid().isEmpty()) {
                                article.setGuid(article.getLink());
                            }
                            if (article.getEnclosure() == null) {
                                String enclosureSrc = TextHelper.getFirstImageFromHTML(article.getDescription());
                                if (enclosureSrc != null) {
                                    Enclosure enclosure = realm.createObject(Enclosure.class);
                                    enclosure.setUrl(enclosureSrc);
                                    enclosure.setType("image/jpg");
                                    // set other enclosure fields
                                    article.setDescription(TextHelper.clearHtmlFromFirstImg(article.getDescription()));
                                    article.setEnclosure(enclosure);
                                }
                            }
                            article.setCategory(source.getCategory());
                            article.setSource(source);
                            realm.copyToRealmOrUpdate(article);
                            source.getArticles().add(article);
                            realm.commitTransaction();
                        }
                    }
                    if (onDataLoaded != null) {
                        onDataLoaded.onSuccess();
                    }
                } else {
                    Logger.d(response.errorBody());
                    if (onDataLoaded != null) {
                        onDataLoaded.onFailure(null);
                    }
                }
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                t.printStackTrace();
                if (onDataLoaded != null) {
                    onDataLoaded.onFailure(null);
                }
            }
        });
    }

    public interface OnDataLoaded {
        void onSuccess();

        void onFailure(@Nullable String message);
    }
}
