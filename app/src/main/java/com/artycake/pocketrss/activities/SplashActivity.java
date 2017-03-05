package com.artycake.pocketrss.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.models.Category;
import com.artycake.pocketrss.models.Source;
import com.artycake.pocketrss.receivers.ClearReceiver;
import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.TextHelper;
import com.artycake.pocketrss.utils.UserPrefs;

import java.util.Calendar;

import io.realm.Realm;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.FIRST_LAUNCH, true)) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            Realm realm = RealmController.getInstance(this).getRealm();
            String[] categories = getResources().getStringArray(R.array.default_categories);
            Category lastCategory = null;
            for (String categoryName : categories) {
                realm.beginTransaction();
                Category category = realm.createObject(Category.class);
                category.setName(categoryName);
                realm.commitTransaction();
                lastCategory = category;
            }
            UserPrefs.getInstance(this).putPreferences(UserPrefs.FIRST_LAUNCH, false);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent clearIntent = new Intent(this, ClearReceiver.class);
            PendingIntent clearPendingIntent = PendingIntent.getBroadcast(this, 0, clearIntent, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, clearPendingIntent);
            if (lastCategory != null) {
                addDefaultFeed(lastCategory);
            }
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void addDefaultFeed(Category category) {
        Realm realm = RealmController.getInstance(this).getRealm();
        realm.beginTransaction();
        Source source = realm.createObject(Source.class);
        source.setName("Realm.io");
        source.setUrl("http://feeds.feedburner.com/realmio");
        source.setCategory(category);
        source.setColor(Source.getRandomColor(this));
        category.getSources().add(source);
        realm.commitTransaction();
        UserPrefs.getInstance(this).putPreferences(UserPrefs.SHOULD_UPLOAD_NEWS, true);
    }
}
