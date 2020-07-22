package com.sterea.sleepcyclealarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class AlarmReceiver extends BroadcastReceiver{
    private final String TAG = AlarmReceiver.class.getSimpleName() + " ";

    @Override
    public void onReceive(Context context, Intent intent) {
        int requestCode = intent.getExtras().getInt(Alarm.REQUEST_CODE_KEY);
        String notificationCategory = intent.getExtras().getString(Alarm.AlarmNotification.NOTIFICATION_CATEGORY_KEY);
        boolean isSnoozed = intent.getExtras().getBoolean(Alarm.IS_SNOOZED);
        Log.d(TAG, "Macin " + requestCode);

        Intent newIntent = new Intent(context, AlarmActivity.class);
        newIntent.putExtra(Alarm.REQUEST_CODE_KEY, requestCode);
        newIntent.putExtra(Alarm.IS_SNOOZED, isSnoozed);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, newIntent, 0);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, Alarm.AlarmNotification.ALARM_CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_alarm_24)
                        .setContentTitle("Alarm")
                        .setContentText("Wake up!")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(notificationCategory)
                        .setContentIntent(pendingIntent)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        /*.addAction(R.drawable.baseline_alarm_off_24, R.string.snooze, )*/
                        // Use a full-screen intent only for the highest-priority alerts where you
                        // have an associated activity that you would like to launch after the user
                        // interacts with the notification. Also, if your app targets Android 10
                        // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                        // order for the platform to invoke this notification.
                        .setFullScreenIntent(pendingIntent, true)
                        .setAutoCancel(true);
        Notification incomingAlarmNotification = notificationBuilder.build();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(requestCode, incomingAlarmNotification);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(requestCode, incomingAlarmNotification);
        }
    }
}
