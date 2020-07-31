package com.sterea.sleepcyclealarm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

public class ScreenStateReceiver extends BroadcastReceiver {
    private final int SNOOZE_ALL_NOTIFICATION_ID = 6;
    @Override
    public void onReceive(Context context, Intent intent) {
        context.getApplicationContext().unregisterReceiver(this);

        if (AlarmNotification.ringtone != null) {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.cancelAll();
            } else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            }

            Calendar snoozeTime = Calendar.getInstance();
            Alarm alarm = new Alarm(snoozeTime, context, SNOOZE_ALL_NOTIFICATION_ID);
            alarm.snoozeAlarm();

            Log.d(ScreenStateReceiver.class.getSimpleName(), "screen state receiver snoozed the alarm Macin");
            /*The snooze alarm method will create a new snooze notification.
            * The ringtone needs to be playing so the new notification won't register a new receiver.*/
            AlarmNotification.stopRingtone();
            AlarmNotification.finishAlarmActivity();
        }

        Log.d(ScreenStateReceiver.class.getSimpleName(), "screen state receiver is still on Macin");
    }
}
