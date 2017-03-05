package com.artycake.pocketrss.utils;

import com.artycake.pocketrss.models.Article;
import com.artycake.pocketrss.models.Source;
import com.orhanobut.logger.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by artycake on 2/28/17.
 */

public class TextHelper {
    private final static List<Locale> availableLocales = new ArrayList<>(Arrays.asList(new Locale[]{new Locale("ru", "RU")}));

    public static String getShortSourceName(Source source) {
        String sourceName = source.getName().substring(0, 2);
        return ucFirst(sourceName);
    }

    public static Locale getCurrentLocale() {
        Locale defaultLocale = Locale.getDefault();
        if (availableLocales.contains(defaultLocale)) {
            return defaultLocale;
        }
        return Locale.ENGLISH;
    }

    public static String ucFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String getSourceDomain(Source source) {
        try {
            URI uri = new URI(source.getUrl());
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getArticleDate(Article article) {
        SimpleDateFormat resultFormat = new SimpleDateFormat("EEE, dd.MM", getCurrentLocale());
        return resultFormat.format(article.getDate());
    }

    public static String getFirstImageFromHTML(String html) {
        String imgRegex = "(?i)<img[^>]+?src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
        Pattern p = Pattern.compile(imgRegex);
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static String clearHtmlFromImg(String html) {
        return html.replaceAll("<img.+?>", "");
    }

    public static String clearHtmlFromFirstImg(String html) {
        return html.replaceFirst("<img.+?>", "");
    }
}
