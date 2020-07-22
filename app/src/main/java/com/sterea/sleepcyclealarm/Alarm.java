package com.sterea.sleepcyclealarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
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
    private int requestCode; // 1 is for known wake up scenario, 2 for known bed time and 3 is for nap time; same values will be used for notification ID.
    static final String REQUEST_CODE_KEY = Alarm.class.getSimpleName();
    static final String IS_SNOOZED = Alarm.class.getName() + "SNOOZED";
    private final String TAG = Alarm.class.getSimpleName() + "TAG";

    Alarm (Calendar time, Context context, int requestCode){
        this.time = time;
        this.context = context;
        this.requestCode = requestCode;
    }

    Alarm (Context context){
        this.context = context;
    }

    void startAlarm (){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        intent.putExtra(AlarmNotification.NOTIFICATION_CATEGORY_KEY, NotificationCompat.CATEGORY_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        if(time.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            time.add(Calendar.DATE,1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);

        settingRebootReceiver(true);
        Log.d(TAG, "Alarm should be on! Macin");
    }

    void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                    intent, 0);
        alarmManager.cancel(pendingIntent);
        settingRebootReceiver(false);
    }

    void snoozeAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        intent.putExtra(AlarmNotification.NOTIFICATION_CATEGORY_KEY, NotificationCompat.CATEGORY_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        time.add(Calendar.MINUTE, 1);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);

        Intent notificationIntent = new Intent(context, AlarmActivity.class);
        notificationIntent.putExtra(Alarm.IS_SNOOZED, true);
        notificationIntent.putExtra(REQUEST_CODE_KEY, requestCode);
        //a new request code must be set to have 2 different pending intents in system
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, requestCode + 10,
                notificationIntent, 0);
        NotificationCompat.Builder snoozeNotificationBuilder = new NotificationCompat.Builder(context, AlarmNotification.SNOOZE_CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_snooze_24)
                .setContentTitle(context.getResources().getString(R.string.notification_title_snooze))
                .setContentInfo(context.getResources().getString(R.string.notification_content_snooze))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);

        Notification snoozeNotification = snoozeNotificationBuilder.build();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(AlarmNotification.SNOOZE_NOTIFICATION_ID, snoozeNotification);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(AlarmNotification.SNOOZE_NOTIFICATION_ID, snoozeNotification);
        }
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

    /* This inner class is used to set up and register notifications channels. */
    static class AlarmNotification {

        static final String ALARM_CHANNEL_ID = AlarmNotification.class.getSimpleName();
        static final String REMINDER_CHANNEL_ID = AlarmNotification.class.getSimpleName() + "REMINDER";
        static final String SNOOZE_CHANNEL_ID = AlarmNotification.class.getSimpleName() + "SNOOZE";
        // NOTIFICATION_CATEGORY_KEY is used as a key for extras of intents sent to broadcast receiver to identify the notification category.
        static final String NOTIFICATION_CATEGORY_KEY = AlarmNotification.class.getSimpleName() + "CATEGORY";
        static final int SNOOZE_NOTIFICATION_ID = 3;

        static void createNotificationChannel(Context context, String channelId, String channelName, String channelDescription, int importance) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDescription);
                channel.enableLights(true);
                if ( importance == NotificationManager.IMPORTANCE_HIGH)
                    channel.canBypassDnd();
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
