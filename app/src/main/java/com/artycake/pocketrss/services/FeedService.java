package com.artycake.pocketrss.services;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.activities.MainActivity;
import com.artycake.pocketrss.models.Source;
import com.artycake.pocketrss.requests.FeedRequest;
import com.artycake.pocketrss.utils.NetworkHelper;
import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.UserPrefs;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class FeedService extends Service {
    private FeedBinder binder;
    private Realm realm;
    private RealmController realmController;
    private Runnable updateTask;
    private Handler updateHandler;

    private static final int NOTIFICATION_ID = 243;

    public FeedService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new FeedBinder();
        realmController = RealmController.getInstance(this);
        realm = realmController.getRealm();
        final NetworkHelper networkHelper = new NetworkHelper(this);
        final UserPrefs userPrefs = UserPrefs.getInstance(this);
        updateTask = new Runnable() {
            @Override
            public void run() {
                if (networkHelper.isNetworkConnected()) {
                    if (userPrefs.getBoolPref(UserPrefs.UPDATE_WIFI_ONLY, true)) {
                        if (networkHelper.isWifiConnected()) {
                            updateNews(null);
                        }
                    } else {
                        updateNews(null);
                    }
                }
                Integer updatePeriod = Integer.valueOf(userPrefs.getStringPref(UserPrefs.UPDATE_PERIOD, "30"));
                if (updatePeriod == 0) {
                    return;
                }
                updateHandler.postDelayed(updateTask, updatePeriod * 60 * 1000);
            }
        };
        updateHandler = new Handler();
        updateTask.run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void updateNews(final OnUpdate onUpdate) {
        NetworkHelper networkHelper = new NetworkHelper(this);
        if (!networkHelper.isNetworkConnected() && isForeground()) {
            Toast.makeText(this, R.string.no_internet_connetion, Toast.LENGTH_LONG).show();
            return;
        }
        final long was = RealmController.getInstance(this).getArticlesCount();
        final RealmResults<Source> sources = realmController.getSources();
        final int sourcesCount = sources.size();
        if (sourcesCount == 0) {
            if (onUpdate != null) {
                onUpdate.onUpdate();
            }
            return;
        }
        final int[] processed = new int[]{0};
        final FeedRequest request = new FeedRequest();
        request.setOnDataLoaded(new FeedRequest.OnDataLoaded() {
            @Override
            public void onSuccess() {
                processed[0]++;
                if (processed[0] == sourcesCount) {
                    Logger.d(isForeground());
                    if (!isForeground()) {
                        long now = RealmController.getInstance(FeedService.this).getArticlesCount();
                        sendNotification(now - was);
                    }
                    if (onUpdate != null) {
                        onUpdate.onUpdate();
                    }
                }
            }

            @Override
            public void onFailure(@Nullable String message) {
                processed[0]++;
                if (processed[0] == sourcesCount) {
                    if (!isForeground()) {
                        long now = RealmController.getInstance(FeedService.this).getArticlesCount();
                        sendNotification(now - was);
                    }
                    if (onUpdate != null) {
                        onUpdate.onUpdate();
                    }
                }
            }
        });
        for (Source source : sources) {
            request.loadFeed(this, source);
        }
    }

    private void sendNotification(long newArticlesCount) {
        if (newArticlesCount == 0) {
            return;
        }
        Bitmap largeIcon;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            largeIcon = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher, getTheme())).getBitmap();
        } else {
            largeIcon = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(String.format(Locale.getDefault(), getResources().getString(R.string.notification_text), newArticlesCount))
                .setAutoCancel(true);
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public class FeedBinder extends Binder {
        public FeedService getService() {
            return FeedService.this;
        }
    }

    public interface OnUpdate {
        void onUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean isForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningTasks = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : runningTasks) {
            if (info.processName.equals(getPackageName()) && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }
}
