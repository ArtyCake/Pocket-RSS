package com.artycake.pocketrss.models;

import android.content.Context;
import android.content.res.TypedArray;

import com.artycake.pocketrss.R;

import java.util.Random;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by artycake on 2/27/17.
 */

public class Source extends RealmObject {
    private String name;
    private String url;
    private RealmList<Article> articles;
    private Category category;
    private int color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RealmList<Article> getArticles() {
        return articles;
    }

    public void setArticles(RealmList<Article> articles) {
        this.articles = articles;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static int getRandomColor(Context context) {
        TypedArray ta = context.getResources().obtainTypedArray(R.array.sourceColors);
        int[] colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();
        return colors[new Random().nextInt(colors.length)];
    }
}
