package com.sterea.sleepcyclealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    static final String ACTION_RECEIVED = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationID = intent.getExtras().getInt(Alarm.REQUEST_CODE_KEY);
        AlarmNotification notification;
        if(notificationID == 1 || notificationID == 2 || notificationID == 3){
            notification = new AlarmNotification(context, AlarmNotification.ALARM, notificationID);
        } else {
            notification = new AlarmNotification(context, AlarmNotification.SNOOZE, notificationID);
        }

        boolean actionReceived = intent.getExtras().getBoolean(AlarmReceiver.ACTION_RECEIVED);
        if(actionReceived){
            boolean isDismissed = intent.getExtras().getBoolean(Alarm.IS_DISMISSED);
            boolean isSnoozed = intent.getExtras().getBoolean(Alarm.IS_SNOOZED);
            boolean isSwiped = intent.getExtras().getBoolean(Alarm.IS_SWIPED);
            if(isDismissed){
                notification.actionDismiss();
            } else if(isSnoozed) {
                notification.actionSnooze();
            } else if (isSwiped) {
                notification.actionDismiss();
                Toast toast = Toast.makeText(context, context.getResources().getString(R.string.alarmDismissed), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            notification.startNotify();
        }
    }
}
