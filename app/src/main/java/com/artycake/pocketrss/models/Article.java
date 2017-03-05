package com.artycake.pocketrss.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by artycake on 2/27/17.
 */
@Root(name = "item", strict = false)
public class Article extends RealmObject {
    @Element(name = "title")
    private String title;
    @Element(name = "link")
    private String link;
    @PrimaryKey
    @Element(name = "guid", required = false)
    private String guid;
    @Element(name = "description")
    private String description;
    @Element(name = "enclosure", required = false)
    private Enclosure enclosure;
    @Element(name = "pubDate")
    @Ignore
    private String dateString;

    private Date date;
    private Category category;
    private Source source;
    private boolean read = false;
    private boolean favorite = false;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @Commit
    private void parseDateString() {
        if (dateString != null) {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            try {
                date = sourceFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                dateString = null;
            }
        }
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Enclosure getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(Enclosure enclosure) {
        this.enclosure = enclosure;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
