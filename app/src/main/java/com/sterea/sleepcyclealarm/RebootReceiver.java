package com.sterea.sleepcyclealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class RebootReceiver extends BroadcastReceiver {
    static final String TAG = RebootReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            SharedPreferences savedPreferences = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            int hour = savedPreferences.getInt(Configurator.HOUR_KNOWN_WAKE_UP, 0);
            int minutes = savedPreferences.getInt(Configurator.MINUTES_KNOWN_WAKE_UP, 0);
            Configurator.knownWakeUpTimeConf.setWakeUpTime(hour, minutes);
            Alarm alarm = new Alarm(Configurator.knownWakeUpTimeConf.getWakeUpTime(), context, Configurator.KNOWN_WAKE_UP_TIME_ALARM_REQ_CODE);
            alarm.startAlarm();
            Log.d(TAG, "reboot receiver on! Macin");
        }
    }
}
