package com.sterea.sleepcyclealarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;
import static com.sterea.sleepcyclealarm.Alarm.REQUEST_CODE_KEY;

class AlarmNotification  {
    final static String ALARM = AlarmNotification.class.getSimpleName() + "ALARM";
    final static String SNOOZE = AlarmNotification.class.getSimpleName() + "SNOOZE";
    final static String REMINDER = AlarmNotification.class.getSimpleName() + "REMINDER";
    static MediaPlayer ringtone = null;
    private static AlarmActivity alarmActivityInstance = null;
    static int dynamicNotificationId;

    private int notificationId;
    private String type;
    private String channelId;
    private Context context;
    AlarmNotification(Context context, String type, int notificationId){

        if(type.equals(ALARM)){
            channelId = ChannelBuilder.ALARM_CHANNEL_ID;
        } else if(type.equals(SNOOZE)) {
            channelId = ChannelBuilder.SNOOZE_CHANNEL_ID;
        } else if(type.equals(REMINDER)) {
            channelId = ChannelBuilder.REMINDER_CHANNEL_ID;
        }
        this.type = type;
        this.notificationId = notificationId;
        this.context = context;
    }

    private NotificationCompat.Builder createBuilder(){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId);
        ArrayList<PendingIntent> pendingIntents = buildIntents(type);
        NotificationCompat.Action dismissAction = new NotificationCompat.Action(R.drawable.baseline_alarm_off_24,
                context.getResources().getString(R.string.dismiss),
                pendingIntents.get(0));

        if(type.equals(ALARM)){

            NotificationCompat.Action snoozeAction = new NotificationCompat.Action(R.drawable.baseline_snooze_24,
                    context.getResources().getString(R.string.snooze),
                    pendingIntents.get(1));

            notificationBuilder.setSmallIcon(R.drawable.baseline_alarm_24)
                    .setContentTitle(context.getResources().getString(R.string.alarm))
                    .setContentText(context.getResources().getString(R.string.wakeUp))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(dismissAction)
                    .addAction(snoozeAction)
                    .setContentIntent(pendingIntents.get(2))
                    .setFullScreenIntent(pendingIntents.get(2), true)
                    .setDeleteIntent(pendingIntents.get(3))
                    .setSound(null)
                    .setAutoCancel(true);
        } else if (type.equals(SNOOZE)) {
            notificationBuilder
                    .setSmallIcon(R.drawable.baseline_snooze_24)
                    .setContentTitle(context.getResources().getString(R.string.notification_title_snooze))
                    .setContentInfo(context.getResources().getString(R.string.notification_content_snooze))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(dismissAction)
                    .setContentIntent(pendingIntents.get(1))
                    .setAutoCancel(true);
        }
        return notificationBuilder;
    }

    private ArrayList<PendingIntent> buildIntents(String type){
        ArrayList<PendingIntent> pendingIntents = new ArrayList<>();

        Intent dismissIntent = new Intent(context, AlarmReceiver.class)
                .putExtra(Alarm.IS_DISMISSED, true)
                .putExtra(Alarm.IS_SNOOZED, false)
                .putExtra(Alarm.IS_SWIPED, false)
                .putExtra(REQUEST_CODE_KEY, notificationId)
                .putExtra(AlarmReceiver.ACTION_RECEIVED, true);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, notificationId + 20, dismissIntent, 0);
        pendingIntents.add(dismissPendingIntent);//0

        if(type.equals(ALARM)){
            Intent snoozeIntent = new Intent(context, AlarmReceiver.class)
                    .putExtra(Alarm.IS_DISMISSED, false)
                    .putExtra(Alarm.IS_SNOOZED, true)
                    .putExtra(Alarm.IS_SWIPED, false)
                    .putExtra(REQUEST_CODE_KEY, notificationId)
                    .putExtra(AlarmReceiver.ACTION_RECEIVED, true);
            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, notificationId + 30, snoozeIntent, 0);
            pendingIntents.add(snoozePendingIntent);//1

            Intent onTapIntent = new Intent(context, AlarmActivity.class)
                    .putExtra(REQUEST_CODE_KEY, notificationId)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent onTapPendingIntent = PendingIntent.getActivity(context, notificationId + 40, onTapIntent, 0);
            pendingIntents.add(onTapPendingIntent);//2

            Intent onSwipeIntent = new Intent(context, AlarmReceiver.class)
                    .putExtra(Alarm.IS_SNOOZED, false)
                    .putExtra(Alarm.IS_DISMISSED, false)
                    .putExtra(Alarm.IS_SWIPED, true)
                    .putExtra(AlarmReceiver.ACTION_RECEIVED, true);
            PendingIntent onSwipePendingIntent = PendingIntent.getBroadcast(context, notificationId + 60, onSwipeIntent, 0);
            pendingIntents.add(onSwipePendingIntent);//3

        } else if (type.equals(SNOOZE)) {
            Intent onTapIntent = new Intent(context, AlarmActivity.class)
                    .putExtra(Alarm.IS_SNOOZED, true)
                    .putExtra(REQUEST_CODE_KEY, notificationId);
            //a new request code must be set to have 2 different pending intents in system so the AlarmActivity will look different
            PendingIntent onTapPendingIntent = PendingIntent.getActivity(context, notificationId + 70,
                    onTapIntent, 0);
            pendingIntents.add(onTapPendingIntent);//1
        }
        return pendingIntents;
    }

    void startNotify(){
        Notification notification = createBuilder().build();
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.cancel(notificationId);
            notificationManager.notify(notificationId, notification);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            notificationManager.notify(notificationId, notification);
        }
        /* If the ringtone isn't null this means this is a snooze notification.
        * In this case there is no need to start a new ringtone and also
        * there isn't the case to register a new ScreeStateReceiver.*/
        if(!type.equals(SNOOZE)) {
            //setting up the ringtone notification
            SharedPreferences savedPreferences = context.getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            String fileName = savedPreferences.getString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP, context.getResources().getResourceName(R.raw.ceausescu_alo));

            int songId = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
            ringtone = MediaPlayer.create(context, songId);
            ringtone.setLooping(true);
            ringtone.start();

            //register a screen of receiver so the alarm wil be snoozed in this case
            dynamicNotificationId = notificationId;
            IntentFilter screenStateIntent = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            context.getApplicationContext().registerReceiver(new ScreenStateReceiver(), screenStateIntent);

        }
    }

    void actionDismiss(){
        Configurator.knownWakeUpTimeConf.setAlarmState(false);
        Configurator.knownWakeUpTimeConf.setConfChanged(true);
        SharedPreferences savedPreferences = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedPreferences.edit();
        editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
        editor.putBoolean(Configurator.SNOOZE_STATE_KNOWN_WAKE_UP, false);
        editor.apply();

        int hour = savedPreferences.getInt(Configurator.HOUR_KNOWN_WAKE_UP, 0);
        int minutes = savedPreferences.getInt(Configurator.MINUTES_KNOWN_WAKE_UP, 0);
        Configurator.knownWakeUpTimeConf.setWakeUpTime(hour, minutes);
        Alarm alarm = new Alarm(Configurator.knownWakeUpTimeConf.getWakeUpTime(), context, notificationId);
        alarm.cancelAlarm();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.cancel(notificationId);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }

        stopRingtone();
        finishAlarmActivity();
    }

    void actionSnooze(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.cancel(notificationId);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
        Calendar snoozeTime = Calendar.getInstance();
        Alarm alarm = new Alarm(snoozeTime, context, notificationId);
        alarm.snoozeAlarm();

        stopRingtone();
        finishAlarmActivity();
    }

    static void stopRingtone(){
        if (ringtone != null) {
            ringtone.pause();
            ringtone.reset();
            ringtone.release();
            ringtone = null;
        }
    }

    static void setAlarmActivityInstance(AlarmActivity alarmActivity){
        alarmActivityInstance = alarmActivity;
    }

    static void finishAlarmActivity(){
        if(alarmActivityInstance != null)
            alarmActivityInstance.finishAndRemoveTask();
    }

    /* This inner class is used to set up and register notifications channels. */
    static class ChannelBuilder {

        static final String ALARM_CHANNEL_ID = ChannelBuilder.class.getSimpleName();
        static final String REMINDER_CHANNEL_ID = ChannelBuilder.class.getSimpleName() + "REMINDER";
        static final String SNOOZE_CHANNEL_ID = ChannelBuilder.class.getSimpleName() + "SNOOZE";

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
