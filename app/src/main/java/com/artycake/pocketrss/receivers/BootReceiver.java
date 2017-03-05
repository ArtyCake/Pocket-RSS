package com.artycake.pocketrss.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.artycake.pocketrss.services.FeedService;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, FeedService.class));
    }
}
