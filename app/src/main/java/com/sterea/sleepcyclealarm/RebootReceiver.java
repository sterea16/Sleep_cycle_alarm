package com.sterea.sleepcyclealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class RebootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences savedPreferences = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            boolean wakeUpKnownAlarmState = savedPreferences.getBoolean(Configurator.ALARM_STATE_WAKE_UP_KNOWN_KEY, false);
            boolean bedTimeKnownAlarmState = savedPreferences.getBoolean(Configurator.ALARM_STATE_KNOWN_BED_TIME_KEY, false);

            if (wakeUpKnownAlarmState){
                int hour = savedPreferences.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP_KEY, 0);
                int minutes = savedPreferences.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP_KEY, 0);
                Configurator.wakeUpTimeKnownConf.buildAlarmTime(hour, minutes);
                // Set the alarm here.
                Alarm alarm = new Alarm(Configurator.wakeUpTimeKnownConf.getAlarmTime(), context, Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE);
                alarm.register();
            }

            if (bedTimeKnownAlarmState){
                int hour = savedPreferences.getInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME_KEY, 0);
                int minutes = savedPreferences.getInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME_KEY, 0);
                Configurator.bedTimeKnownConf.buildAlarmTime(hour, minutes);
                // Set the alarm here.
                Alarm alarm = new Alarm(Configurator.bedTimeKnownConf.getAlarmTime(), context, Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE);
                alarm.register();
            }
        }
    }
}
