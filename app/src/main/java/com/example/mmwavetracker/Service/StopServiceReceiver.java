package com.example.mmwavetracker.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent MonitoringServiceIntent = new Intent(context, MonitoringService.class);

        context.stopService(MonitoringServiceIntent);

    }
}
