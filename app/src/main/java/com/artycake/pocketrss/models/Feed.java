package com.artycake.pocketrss.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by artycake on 2/27/17.
 */

@Root(name = "rss", strict = false)
public class Feed {
    @Element(name = "title")
    @Path("channel")
    private String title;
    @ElementList(name = "item", inline = true)
    @Path("channel")
    private List<Article> articles;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}
