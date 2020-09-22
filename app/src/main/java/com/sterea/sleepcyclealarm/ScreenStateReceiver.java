package com.sterea.sleepcyclealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

abstract class ScreenStateReceiver extends BroadcastReceiver {
    Configurator configurator;

    private ScreenStateReceiver(Configurator configurator){
        Log.d("screen state catalin", "in constructor");
        this.configurator = configurator;
    }

    @Override
    public void onReceive(Context context, Intent intent){
        //make sure that this receiver is used only once
        context.getApplicationContext().unregisterReceiver(this);

        SharedPreferences savedConfiguration = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, Context.MODE_PRIVATE);
        boolean alarmState = savedConfiguration.getBoolean(configurator.getAlarmStateKey(), false);
        boolean snoozeState = savedConfiguration.getBoolean(configurator.getSnoozeStateKey(), false);

        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && alarmState && !snoozeState) {
            Notification.stopRingtone();

            SharedPreferences.Editor editor = savedConfiguration.edit();
            editor.putBoolean(Configurator.DEVICE_UNLOCKED, false);
            editor.apply();

            Notification.cancel(configurator.getRequestCode(), context);
            Notification.getAlarmActivityInstance().finishAndRemoveTask();

            Calendar snoozeTime = Calendar.getInstance();
            Alarm alarm = new Alarm(snoozeTime, context, configurator.getRequestCode());
            alarm.snooze();
            Log.d("Screen onReceive()", "screen state on receive true");
        }
    }

    public static class BedTime extends ScreenStateReceiver {

        BedTime(){
            super(Configurator.bedTimeKnownConf);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }

    public static class WakeUp extends ScreenStateReceiver {

        WakeUp() {
            super(Configurator.wakeUpTimeKnownConf);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }

    public static class NapTime extends ScreenStateReceiver {

        NapTime(){
            super(Configurator.napTimeConf);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }
}
