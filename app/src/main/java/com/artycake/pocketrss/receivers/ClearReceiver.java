package com.artycake.pocketrss.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artycake.pocketrss.utils.RealmController;
import com.artycake.pocketrss.utils.UserPrefs;

/**
 * Created by artycake on 3/3/17.
 */

public class ClearReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int storageTime = Integer.valueOf(UserPrefs.getInstance(context).getStringPref(UserPrefs.STORAGE_TIME, "10"));
        if (storageTime == 0) {
            return;
        }
        RealmController.getInstance(context).clearArticles(storageTime);
    }
}
