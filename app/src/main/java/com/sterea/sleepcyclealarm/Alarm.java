package com.sterea.sleepcyclealarm;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

final class Alarm {

    private Calendar time;
    private Context context;


    /**1) is for known wake up scenario
     * 2) for known bed time
     * 3) is for nap time
     * 4) is for snooze
     * 5) is for bed time reminder
     * same values will be used for notification ID.*/
    private int requestCode;
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
        Intent intent = new Intent(context, Receiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);
        Log.d("registerAlarm", "Macin " + requestCode);
        if(time.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            time.add(Calendar.DATE,1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
        settingRebootReceiver(true);
    }

    void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Receiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        Log.d("cancelAlarm", "Macin " + requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                    intent, 0);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        settingRebootReceiver(false);
    }

    void snoozeAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Receiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        time.add(Calendar.MINUTE, 1);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
        Notification snoozeNotification = new Notification(context, Notification.SNOOZE, requestCode);
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

    public static class Receiver extends BroadcastReceiver {
        static final String ACTION_RECEIVED = Receiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationID = intent.getExtras().getInt(REQUEST_CODE_KEY);
            Notification notification;
            if(notificationID == 1 || notificationID == 2 || notificationID == 3){
                notification = new Notification(context, Notification.ALARM, notificationID);
            } else {
                notification = new Notification(context, Notification.SNOOZE, notificationID);
            }

            boolean actionReceived = intent.getExtras().getBoolean(Receiver.ACTION_RECEIVED);
            if(actionReceived){
                boolean isDismissed = intent.getExtras().getBoolean(IS_DISMISSED);
                boolean isSnoozed = intent.getExtras().getBoolean(IS_SNOOZED);
                boolean isSwiped = intent.getExtras().getBoolean(IS_SWIPED);
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

    /**
     *
     * Inner class to create notification for the alarms
     *
     * */
    static class Notification {
        final static String ALARM = Notification.class.getSimpleName() + "ALARM";
        final static String SNOOZE = Notification.class.getSimpleName() + "SNOOZE";
        final static String REMINDER = Notification.class.getSimpleName() + "REMINDER";
        static MediaPlayer ringtone = null;
        private static AlarmActivity alarmActivityInstance = null;
        private int notificationId;
        private String type;
        private String channelId;
        private Context context;

        Notification(Context context, String type, int notificationId){
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

            Intent dismissIntent = new Intent(context, Receiver.class)
                    .putExtra(IS_DISMISSED, true)
                    .putExtra(IS_SNOOZED, false)
                    .putExtra(IS_SWIPED, false)
                    .putExtra(REQUEST_CODE_KEY, notificationId)
                    .putExtra(Receiver.ACTION_RECEIVED, true);
            PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, notificationId + 20, dismissIntent, 0);
            pendingIntents.add(dismissPendingIntent);//0

            if(type.equals(ALARM)){
                Intent snoozeIntent = new Intent(context, Receiver.class)
                        .putExtra(IS_DISMISSED, false)
                        .putExtra(IS_SNOOZED, true)
                        .putExtra(IS_SWIPED, false)
                        .putExtra(REQUEST_CODE_KEY, notificationId)
                        .putExtra(Receiver.ACTION_RECEIVED, true);
                PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, notificationId + 30, snoozeIntent, 0);
                pendingIntents.add(snoozePendingIntent);//1

                Intent onTapIntent = new Intent(context, AlarmActivity.class)
                        .putExtra(REQUEST_CODE_KEY, notificationId)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent onTapPendingIntent = PendingIntent.getActivity(context, notificationId + 40, onTapIntent, 0);
                pendingIntents.add(onTapPendingIntent);//2

                Intent onSwipeIntent = new Intent(context, Receiver.class)
                        .putExtra(IS_SNOOZED, false)
                        .putExtra(IS_DISMISSED, false)
                        .putExtra(IS_SWIPED, true)
                        .putExtra(Receiver.ACTION_RECEIVED, true);
                PendingIntent onSwipePendingIntent = PendingIntent.getBroadcast(context, notificationId + 60, onSwipeIntent, 0);
                pendingIntents.add(onSwipePendingIntent);//3

            } else if (type.equals(SNOOZE)) {
                Intent onTapIntent = new Intent(context, AlarmActivity.class)
                        .putExtra(IS_SNOOZED, true)
                        .putExtra(REQUEST_CODE_KEY, notificationId);
                //a new request code must be set to have 2 different pending intents in system so the AlarmActivity will look different
                PendingIntent onTapPendingIntent = PendingIntent.getActivity(context, notificationId + 70,
                        onTapIntent, 0);
                pendingIntents.add(onTapPendingIntent);//1
            }
            return pendingIntents;
        }

        void startNotify(){
            android.app.Notification notification = createBuilder().build();

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.cancel(notificationId);
                notificationManager.notify(notificationId, notification);
            } else {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);
                notificationManager.notify(notificationId, notification);
            }

            if(!type.equals(SNOOZE)) {
                //setting up the ringtone notification
                SharedPreferences savedPreferences = context.getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                String fileName = savedPreferences.getString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP_KEY, context.getResources().getResourceName(R.raw.ceausescu_alo));

                int songId = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
                ringtone = MediaPlayer.create(context, songId);
                ringtone.setLooping(true);
                ringtone.start();

                //register a screen of receiver so the alarm wil be snoozed in this case
                IntentFilter screenStateIntent = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                context.getApplicationContext().registerReceiver(new ScreenStateReceiver(), screenStateIntent);

            }
        }

        void actionDismiss(){
            //TODO needs update for other types of alarm to be dismissed from here
            Configurator.wakeUpTimeKnownConf.setAlarmState(false)
                        .setConfChanged(true);
            SharedPreferences savedPreferences = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            SharedPreferences.Editor editor = savedPreferences.edit();
            editor.putBoolean(Configurator.ALARM_STATE_WAKE_UP_KNOWN_KEY, false)
                    .putBoolean(Configurator.SNOOZE_STATE_KNOWN_WAKE_UP_KEY, false)
                    .apply();
            int hour = savedPreferences.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, 0);
            int minutes = savedPreferences.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, 0);
            Configurator.wakeUpTimeKnownConf.buildAlarmTime(hour, minutes);
            Alarm alarm = new Alarm(Configurator.wakeUpTimeKnownConf.getAlarmTime(), context, notificationId);
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

        /**
         *
         *
         * This inner class is used to set up and register notifications channels.
         *
         *
         * */
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
}
