package com.bram.belajarosmdroid.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Service_call ", "You are in alarmReceive class");
        Intent background = new Intent(context, BookingTrackingService.class);
        Log.e("AlaramReceive", "testing called broadcast");
        context.startService(background);
    }
}
