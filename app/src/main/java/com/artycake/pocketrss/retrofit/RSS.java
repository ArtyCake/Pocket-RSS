package com.artycake.pocketrss.retrofit;

import com.artycake.pocketrss.models.Feed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by artycake on 2/27/17.
 */

public interface RSS {
    @GET
    Call<Feed> loadFeed(@Url String url);
}
