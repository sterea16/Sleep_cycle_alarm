package com.sterea.sleepcyclealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.Calendar;
import java.util.Objects;

final public class Alarm {
    /*final static String REQUEST_CODE_KEY = "requestCodeOfPendingIntent";*/
    private int requestCode;
    private Calendar time;
    private Context context;

    Alarm (Calendar time, Context context, int requestCode){
        this.time = time;
        this.context = context;
        this.requestCode = requestCode;
    }

    Alarm (Context context){
        this.context = context;
    }

    public Calendar getTime() {
        return time;
    }

    public void changeTime(Calendar time) {
        this.time = time;
    }

    void startAlarm (){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        /*intent.putExtra(REQUEST_CODE_KEY, requestCode);*/
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        if(time.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            time.add(Calendar.DATE,1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
        settingRebootReceiver(true);
    }

    void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        /*intent.putExtra(REQUEST_CODE_KEY, requestCode);*/
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                    intent, 0);
        alarmManager.cancel(pendingIntent);
        settingRebootReceiver(false);
    }

    void snoozeAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        /*intent.putExtra(REQUEST_CODE_KEY, requestCode);*/
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        time.add(Calendar.MINUTE, 1);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

    private void settingRebootReceiver(boolean alarmState){
        ComponentName receiver = new ComponentName(context, RebootReceiver.class);
        PackageManager pm = context.getPackageManager();

        if(alarmState){
            /* Enables a reboot receiver so the alarm remains on even if the device has been rebooted. */
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            /* Disables reboot receiver if the alarm is canceled. */
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}
