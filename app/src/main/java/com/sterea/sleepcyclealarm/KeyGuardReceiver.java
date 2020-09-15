package com.sterea.sleepcyclealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

abstract class KeyGuardReceiver extends BroadcastReceiver {
    private Configurator configurator;

    private KeyGuardReceiver(Configurator configurator){
        this.configurator = configurator;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.getApplicationContext().unregisterReceiver(this);
        SharedPreferences savedConfiguration = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, Context.MODE_PRIVATE);
        boolean alarmState = savedConfiguration.getBoolean(configurator.getAlarmStateKey(), false);
        boolean snoozeState = savedConfiguration.getBoolean(configurator.getSnoozeStateKey(), false);

        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){

            SharedPreferences.Editor editor = savedConfiguration.edit();
            editor.putBoolean(Configurator.DEVICE_UNLOCKED, true);
            editor.apply();

            if(Notification.ringtone !=null && alarmState && !snoozeState) {
                Notification.getAlarmActivityInstance().finishAndRemoveTask();
                Notification.cancel(configurator.getRequestCode(), context);

                Notification alarmNotification = new Notification(context, Notification.ALARM, configurator.getRequestCode());
                alarmNotification.trigger();
            }

            Log.d("Keyguard omReceiver()", " receive device unlocked");
        }
    }

    public static class BedTime extends KeyGuardReceiver {

        BedTime(){
            super(Configurator.bedTimeKnownConf);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }

    public static class WakeUp extends KeyGuardReceiver {

        WakeUp() {
            super(Configurator.wakeUpTimeKnownConf);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
        }
    }
}
