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
            int hour = savedPreferences.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, 0);
            int minutes = savedPreferences.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, 0);
            Configurator.wakeUpTimeKnownConf.buildAlarmTime(hour, minutes);
            Alarm alarm = new Alarm(Configurator.wakeUpTimeKnownConf.getAlarmTime(), context, Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE);
            alarm.registerAlarm();
            Log.d(TAG, "reboot receiver on! Macin");
        }
    }
}
