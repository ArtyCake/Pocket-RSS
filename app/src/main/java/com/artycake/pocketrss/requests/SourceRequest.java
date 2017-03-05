package com.artycake.pocketrss.requests;

import android.support.annotation.Nullable;

import com.artycake.pocketrss.models.Feed;
import com.artycake.pocketrss.retrofit.RSS;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by artycake on 3/1/17.
 */

public class SourceRequest {

    private OnDataLoaded onDataLoaded;

    public void checkSourceUrl(String sourceUrl, final OnDataLoaded onDataLoaded) {
        String finalUrl = sourceUrl + "/";
        Logger.d(finalUrl);
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://square.github.io/retrofit/").addConverterFactory(SimpleXmlConverterFactory.create()).build();
        RSS rss = retrofit.create(RSS.class);
        Call<Feed> call = rss.loadFeed(sourceUrl);
        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                if (response.isSuccessful()) {
                    onDataLoaded.onSuccess();
                } else {
                    try {
                        onDataLoaded.onFailure(response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                onDataLoaded.onFailure(t.toString());
            }
        });
    }

    public interface OnDataLoaded {
        void onSuccess();

        void onFailure(@Nullable String message);
    }
}
