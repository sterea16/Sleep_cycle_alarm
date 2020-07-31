package com.sterea.sleepcyclealarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Objects;

final class Alarm {

    private Calendar time;
    private Context context;
    private int requestCode; /*1 is for known wake up scenario
                               2 for known bed time
                               3 is for nap time
                               4 is for snooze
                               5 is for bed time reminder;
                                same values will be used for notification ID.*/
    static final String REQUEST_CODE_KEY = Alarm.class.getName();
    static final String IS_SNOOZED = Alarm.class.getName() + "SNOOZED";
    static final String IS_DISMISSED = Alarm.class.getName() + "DISMISSED";
    static final String IS_SWIPED = Alarm.class.getName() + "SWIPED";

    Alarm (Calendar time, Context context, int requestCode){
        this.time = time;
        this.context = context;
        this.requestCode = requestCode;
    }

    Alarm (Context context){
        this.context = context;
    }

    void registerAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
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
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                    intent, 0);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
        settingRebootReceiver(false);
    }

    void snoozeAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        time.add(Calendar.MINUTE, 1);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
        AlarmNotification snoozeNotification = new AlarmNotification(context, AlarmNotification.SNOOZE, requestCode);
        snoozeNotification.startNotify();
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
