package com.sterea.sleepcyclealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 *
 * Inner class where notification behaves.
 *
 ***************************************************/

public class AlarmReceiver extends BroadcastReceiver {
    /**Key to verify if an action has been sent to receiver.
     * The action can be one of the following:<br>
     *     {@link Notification#actionDismiss(int requestCode)}<br>
     *     {@link Notification#actionSnooze()}*/
    static final String ACTION_RECEIVED = AlarmReceiver.class.getSimpleName();
    private String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationID = intent.getExtras().getInt(Alarm.REQUEST_CODE_KEY);
        Notification notification;
        if(notificationID == Configurator.wakeUpTimeKnownConf.getRequestCode() ||
                notificationID == Configurator.bedTimeKnownConf.getRequestCode() ||
                notificationID == 3){
            notification = new Notification(context, Notification.ALARM, notificationID);
        } else {
            notification = new Notification(context, Notification.SNOOZE, notificationID);
        }

        boolean actionReceived = intent.getExtras().getBoolean(AlarmReceiver.ACTION_RECEIVED);
        if(actionReceived){
            boolean isDismissed = intent.getExtras().getBoolean(Alarm.IS_DISMISSED);
            boolean isSnoozed = intent.getExtras().getBoolean(Alarm.IS_SNOOZED);
            boolean isSwiped = intent.getExtras().getBoolean(Alarm.IS_SWIPED);
            if(isDismissed){
                notification.actionDismiss(notificationID);
            } else if(isSnoozed) {
                notification.actionSnooze();
            } else if (isSwiped) {
                notification.actionDismiss(notificationID);
                Toast toast = Toast.makeText(context, context.getResources().getString(R.string.alarmDismissed), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            notification.trigger();
        }

        Log.d("screen state onReceive", "screen state receiver notification ID " + notificationID + " actionReceived " + actionReceived);
    }
}
