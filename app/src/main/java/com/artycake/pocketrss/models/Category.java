package com.artycake.pocketrss.models;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by artycake on 2/27/17.
 */

public class Category extends RealmObject {
    private String name;
    private RealmList<Source> sources;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<Source> getSources() {
        return sources;
    }

    public void setSources(RealmList<Source> sources) {
        this.sources = sources;
    }
}
