package com.sterea.sleepcyclealarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

/**
 *
 * Inner class to create notification for the alarms
 *
 ***************************************************/
class Notification {
    final static String ALARM = Notification.class.getSimpleName() + " ALARM";
    final static String NAP = Notification.class.getSimpleName() + " NAP";
    final static String SNOOZE = Notification.class.getSimpleName() + " SNOOZE";
    final static String REMINDER = Notification.class.getSimpleName() + " REMINDER";
    final static int ALARM_CALLER = 1;
    final static int SNOOZE_CALLER = 2;
    final static int FULL_SCREEN_CALLER = 3;
    static MediaPlayer ringtone = null;
    private static AlarmActivity alarmActivityInstance = null;
    private float volume = 0;
    private final Timer timer = new Timer(true);

    /** This has the same value as {@link Configurator#getRequestCode()}.<br>
     * It's used to distinguish a notification from another in the notification service ({@link Context#NOTIFICATION_SERVICE}).*/
    private int notificationId;
    private String type;
    private String channelId;
    private Context context;
    private Configurator configurator;
    static  ScreenStateReceiver.BedTime bedTimeScreenStateReceiver;
    static  ScreenStateReceiver.WakeUp wakeUpScreenStateReceiver;
    static  ScreenStateReceiver.NapTime napTimeScreenStateReceiver;

    private void setConfigurator(int notificationId){
        if(notificationId == Configurator.wakeUpTimeKnownConf.getRequestCode()){
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if (notificationId == Configurator.bedTimeKnownConf.getRequestCode()) {
            configurator = Configurator.bedTimeKnownConf;
        } else if (notificationId == Configurator.napTimeConf.getRequestCode()){
            configurator = Configurator.napTimeConf;
        }
    }

    Notification(Context context, String type, int notificationId){
        if(type.equals(ALARM)){
            channelId = ChannelBuilder.ALARM_CHANNEL_ID;
        } else if(type.equals(SNOOZE)) {
            channelId = ChannelBuilder.SNOOZE_CHANNEL_ID;
        } else if (type.equals(NAP)){
            channelId = ChannelBuilder.SNOOZE_CHANNEL_ID;
        } else if(type.equals(REMINDER)) {
            channelId = ChannelBuilder.REMINDER_CHANNEL_ID;
        }
        this.type = type;
        this.notificationId = notificationId;
        this.context = context;
        setConfigurator(notificationId);
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
                    .setFullScreenIntent(pendingIntents.get(3), true)
                    .setDeleteIntent(pendingIntents.get(4))
                    .setSound(null)
                    .setAutoCancel(true);
        } else if (type.equals(SNOOZE)) {
            notificationBuilder
                    .setSmallIcon(R.drawable.baseline_snooze_24)
                    .setContentTitle(context.getResources().getString(R.string.notification_title_snooze))
                    .setContentText(context.getResources().getString(R.string.notification_content_snooze))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(dismissAction)
                    .setContentIntent(pendingIntents.get(1))
                    .setAutoCancel(true);
        } else if (type.equals(NAP)) {

            String endTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(configurator.getAlarmTime().getTime());
            Intent onTapNap = new Intent(context, MainActivity.class);
            onTapNap.putExtra(MainActivity.REQUESTED_TAB, configurator.getRequestCode());
            PendingIntent onTapNapPendingIntent = PendingIntent.getActivity(context, notificationId + 90, onTapNap,0);

            notificationBuilder
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                    .setSmallIcon(R.drawable.ic_nap_time)
                    .setLargeIcon(getBitmap(R.drawable.ic_nap_time))
                    .setContentTitle("Taking a nap")
                    .setContentText("Nap time ends at " + endTime)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .addAction(dismissAction)
                    .setContentIntent(onTapNapPendingIntent);
        }
        return notificationBuilder;
    }

    private ArrayList<PendingIntent> buildIntents(String type){
        ArrayList<PendingIntent> pendingIntents = new ArrayList<>();

        Intent dismissIntent = new Intent(context, AlarmReceiver.class)
                .putExtra(Alarm.IS_DISMISSED, true)
                .putExtra(Alarm.IS_SNOOZED, false)
                .putExtra(Alarm.IS_SWIPED, false)
                .putExtra(Alarm.REQUEST_CODE_KEY, notificationId)
                .putExtra(AlarmReceiver.ACTION_RECEIVED, true);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, notificationId + 20, dismissIntent, 0);
        pendingIntents.add(dismissPendingIntent);//0

        if(type.equals(ALARM)){
            Intent snoozeIntent = new Intent(context, AlarmReceiver.class)
                    .putExtra(Alarm.IS_DISMISSED, false)
                    .putExtra(Alarm.IS_SNOOZED, true)
                    .putExtra(Alarm.IS_SWIPED, false)
                    .putExtra(Alarm.REQUEST_CODE_KEY, notificationId)
                    .putExtra(AlarmReceiver.ACTION_RECEIVED, true);
            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, notificationId + 30, snoozeIntent, 0);
            pendingIntents.add(snoozePendingIntent);//1st

            Intent onTapIntent = new Intent(context, AlarmActivity.class)
                    .putExtra(Alarm.REQUEST_CODE_KEY, notificationId)
                    .putExtra(AlarmActivity.CALLER, Notification.ALARM_CALLER)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent onTapPendingIntent = PendingIntent.getActivity(context, notificationId + 40, onTapIntent, 0);
            pendingIntents.add(onTapPendingIntent);//2nd

            Intent fullScreenIntent = new Intent(context, AlarmActivity.class)
                    .putExtra(Alarm.REQUEST_CODE_KEY, notificationId)
                    .putExtra(AlarmActivity.CALLER, Notification.FULL_SCREEN_CALLER)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, notificationId + 50, fullScreenIntent, 0);
            pendingIntents.add(fullScreenPendingIntent);//3th

            Intent onSwipeIntent = new Intent(context, AlarmReceiver.class)
                    .putExtra(Alarm.REQUEST_CODE_KEY, notificationId)
                    .putExtra(Alarm.IS_SNOOZED, false)
                    .putExtra(Alarm.IS_DISMISSED, false)
                    .putExtra(Alarm.IS_SWIPED, true)
                    .putExtra(AlarmReceiver.ACTION_RECEIVED, true);
            PendingIntent onSwipePendingIntent = PendingIntent.getBroadcast(context, notificationId + 70, onSwipeIntent, 0);
            pendingIntents.add(onSwipePendingIntent);//4th

        } else if (type.equals(SNOOZE)) {
            Intent onTapIntent = new Intent(context, AlarmActivity.class)
                    .putExtra(AlarmActivity.CALLER, Notification.SNOOZE_CALLER)
                    .putExtra(Alarm.REQUEST_CODE_KEY, notificationId);
            //a new request code must be set to have 2 different pending intents in system so the AlarmActivity will look different
            PendingIntent onTapPendingIntent = PendingIntent.getActivity(context, notificationId + 80,
                    onTapIntent, 0);
            pendingIntents.add(onTapPendingIntent);//1st
        }
        return pendingIntents;
    }

    private Bitmap getBitmap(int resId){
        final Bitmap[] bitmap = new Bitmap[1];
        Runnable runnable = () -> {
            Drawable drawable = ContextCompat.getDrawable(context, resId);
            bitmap[0] = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap[0]);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        };
        Thread thread = new Thread(runnable);
        thread.start();
        return bitmap[0];
    }

    void trigger(){
        android.app.Notification notification = createBuilder().build();

        NotificationManager notificationManager;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
            notificationManager = context.getSystemService(NotificationManager.class);
        } else {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        notificationManager.cancel(notificationId);
        notificationManager.notify(notificationId, notification);

        if(!type.equals(SNOOZE) && ringtone == null && !type.equals(NAP)) {
            playRingtone();
            registerScreenStateReceiver();
        }
    }

    void actionDismiss(int requestCode){
        SharedPreferences savedConfiguration = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedConfiguration.edit();
        if(configurator == null){
            if(requestCode == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE){
                configurator = Configurator.wakeUpTimeKnownConf;
            } else if (requestCode == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE){
                configurator = Configurator.bedTimeKnownConf;
            } else if (requestCode == Configurator.NAP_TIME_ALARM_REQ_CODE) {
                configurator = Configurator.napTimeConf;
                configurator.setConfigured(false);
                editor.putBoolean(configurator.getIsConfiguredKey(), false);
            }
        }

        configurator.setAlarmState(false)
                    .setConfChanged(true);

        editor.putBoolean(configurator.getAlarmStateKey(), false)
                .putBoolean(configurator.getSnoozeStateKey(), false)
                .apply();
        int hour = savedConfiguration.getInt(configurator.getAlarmHourKey(), 0);
        int minutes = savedConfiguration.getInt(configurator.getAlarmMinutesKey(), 0);
        configurator.buildAlarmTime(hour, minutes);
        Alarm alarm = new Alarm(configurator.getAlarmTime(), context, notificationId);
        alarm.cancel();

        Notification.cancel(requestCode, context);
        unregisterScreenStateReceiver(context);
        stopRingtone();
        finishAlarmActivity();
        timer.cancel();
        timer.purge();
    }

    void actionSnooze(){
        Calendar snoozeTime = Calendar.getInstance();
        Alarm alarm = new Alarm(snoozeTime, context, notificationId);
        alarm.snooze();
        unregisterScreenStateReceiver(context);
        stopRingtone();
        finishAlarmActivity();
    }

    private void playRingtone(){
        SharedPreferences savedPreferences = context.getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        //if the ringtone is going to play, then the alarm state can't be snooze
        SharedPreferences.Editor editor = savedPreferences.edit();
        editor.putBoolean(configurator.getSnoozeStateKey(), false)
                .apply();
        //access the audio file using its name and its path name
        String fileName = savedPreferences.getString(configurator.getRawFileSongNameKey(), context.getResources().getResourceName(R.raw.ceausescu_alo));
        int songId = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());

        ringtone = MediaPlayer.create(context, songId);
        ringtone.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
        ringtone.setLooping(true);
        ringtone.setVolume(volume, volume);
        ringtone.start();
        startFadeIn();
    }

    private void startFadeIn(){
        final int FADE_DURATION = 30000; //The duration of the fade
        //The amount of time between volume changes. The smaller this is, the smoother the fade
        final int FADE_INTERVAL = 150;
        final int MAX_VOLUME = 1; //The volume will increase from 0 to 1
        int numberOfSteps = FADE_DURATION/FADE_INTERVAL; //Calculate the number of fade steps
        //Calculate by how much the volume changes each step
        final float deltaVolume = MAX_VOLUME / (float)numberOfSteps;

        //Create a new Timer and Timer task to run the fading outside the main UI thread
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fadeInStep(deltaVolume); //Do a fade step
                //Cancel and Purge the Timer if the desired volume has been reached
                if(volume>=1f){
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(timerTask, FADE_INTERVAL, FADE_INTERVAL);
    }

    private void fadeInStep(float deltaVolume){
        if(ringtone != null) {
            ringtone.setVolume(volume, volume);
            volume += deltaVolume;
        } else {
            timer.cancel();
            timer.purge();
        }

    }

    static void stopRingtone(){
        if (ringtone != null) {
            ringtone.pause();
            ringtone.reset();
            ringtone.release();
            ringtone = null;
        }
    }

    static void cancel(int notificationId, Context context){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.cancel(notificationId);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }

    /**Register a screen of receiver so the alarm wil be snoozed if the user presses power button.
     * Each scenario has its own screen state receiver.<br>
     * The screen state receiver will be unregister in its {@link ScreenStateReceiver#onReceive(Context, Intent)} method.*/
    private void registerScreenStateReceiver(){
        IntentFilter screenStateIntent = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        if(configurator == Configurator.bedTimeKnownConf) {
            bedTimeScreenStateReceiver = new ScreenStateReceiver.BedTime();
            context.getApplicationContext().registerReceiver(bedTimeScreenStateReceiver, screenStateIntent);
        } else if (configurator == Configurator.wakeUpTimeKnownConf) {
            wakeUpScreenStateReceiver = new ScreenStateReceiver.WakeUp();
            context.getApplicationContext().registerReceiver(wakeUpScreenStateReceiver, screenStateIntent);
        } else if (configurator == Configurator.napTimeConf) {
            napTimeScreenStateReceiver = new ScreenStateReceiver.NapTime();
            context.getApplicationContext().registerReceiver(napTimeScreenStateReceiver, screenStateIntent);
        }
    }

    static void unregisterScreenStateReceiver(Context context){
        /*the broadcast receiver may or may not have been already registered
        * so for this reason it needs a try catch block, otherwise the app will crash
        * and the configuration may not be properly saved*/
        if(bedTimeScreenStateReceiver != null) {
            try {
                context.getApplicationContext().unregisterReceiver(bedTimeScreenStateReceiver);
            } catch (IllegalArgumentException exception) {
                exception.printStackTrace();
            }
        } else if (wakeUpScreenStateReceiver != null) {
            try {
                context.getApplicationContext().unregisterReceiver(wakeUpScreenStateReceiver);
            } catch (IllegalArgumentException exception) {
                exception.printStackTrace();
            }
        } else if (napTimeScreenStateReceiver != null) {
            try {
                context.getApplicationContext().unregisterReceiver(napTimeScreenStateReceiver);
            } catch (IllegalArgumentException exception) {
                exception.printStackTrace();
            }
        }
    }

    static void setAlarmActivityInstance(AlarmActivity alarmActivity){
        alarmActivityInstance = alarmActivity;
    }

    public static AlarmActivity getAlarmActivityInstance() {
        return alarmActivityInstance;
    }

    static void finishAlarmActivity(){
        if(alarmActivityInstance != null)
            alarmActivityInstance.finishAndRemoveTask();
    }

    /*************************************************************************
     *
     * This inner class is used to set up and register notifications channels.
     *
     ************************************************************************/
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
                channel.setSound(null, null);
                channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
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
