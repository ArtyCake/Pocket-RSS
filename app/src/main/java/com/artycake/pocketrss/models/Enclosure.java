package com.artycake.pocketrss.models;

import org.simpleframework.xml.Attribute;

import io.realm.RealmObject;

/**
 * Created by artycake on 3/1/17.
 */
public class Enclosure extends RealmObject {
    @Attribute(name = "url")
    private String url;

    @Attribute(name = "length")
    private long length;

    @Attribute(name = "type")
    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
