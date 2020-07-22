package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
//TODO check reboot task activity; remains in background?!

public class AlarmActivity extends AppCompatActivity {
    private MediaPlayer r;
    private final String TAG = AlarmActivity.class.getSimpleName();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_layout);

        /* Pops out the activity even if the phone is on lock screen.
         * screen state here:
         * https://developer.android.com/reference/android/view/Display#STATE_DOZE_SUSPEND
         * STATE_OFF = 1
         * STATE_ON = 2
         * STATE_DOZE = 3
         * STATE_DOZE_SUSPENDED = 4 */
        int displayState = getWindowManager().getDefaultDisplay().getState();
        if(displayState != 2) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        int requestCode = bundle.getInt(Alarm.REQUEST_CODE_KEY);
        boolean isSnoozed = bundle.getBoolean(Alarm.IS_SNOOZED);
        Log.d(TAG, "isSnoozed " + isSnoozed + " request code " + requestCode);

        final int alarmType = requestCode;

        SharedPreferences savedPreferences = getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        String fileName = savedPreferences.getString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP, getResources().getResourceName(R.raw.ceausescu_alo));

        //setting up the ringtone notification
        int songId = getResources().getIdentifier(fileName, "raw", getPackageName());

        if (r == null && !isSnoozed) {
            r = MediaPlayer.create(this, songId);
            r.setLooping(true);
            r.start();
        }
        TextView textView = findViewById(R.id.text_Wake_Up);
        if(isSnoozed) {
            textView.setText(R.string.alarm_snoozed);
        } else {
            textView.setText(R.string.wakeUp);
        }

        Button dismissButton = findViewById(R.id.dismissButton);
        if(isSnoozed) {
            dismissButton.setText(R.string.cancel_alarm);
        } else {
            dismissButton.setText(R.string.dismiss);
        }
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Configurator.knownWakeUpTimeConf.setAlarmState(false);
                Configurator.knownWakeUpTimeConf.setConfChanged(true);
                SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                SharedPreferences.Editor editor = savedPreferences.edit();
                editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
                editor.putBoolean(Configurator.SNOOZE_STATE_KNOWN_WAKE_UP, false);
                editor.apply();
                int hour = savedPreferences.getInt(Configurator.HOUR_KNOWN_WAKE_UP, 0);
                int minutes = savedPreferences.getInt(Configurator.MINUTES_KNOWN_WAKE_UP, 0);
                Configurator.knownWakeUpTimeConf.setWakeUpTime(hour, minutes);
                Alarm alarm = new Alarm(Configurator.knownWakeUpTimeConf.getWakeUpTime(), AlarmActivity.this, alarmType);
                alarm.cancelAlarm();

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1){
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    // notificationId is a unique int for each notification that you must define
                    notificationManager.cancel(alarmType);
                    notificationManager.cancel(Alarm.AlarmNotification.SNOOZE_NOTIFICATION_ID);
                } else {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(alarmType);
                    notificationManager.cancel(Alarm.AlarmNotification.SNOOZE_NOTIFICATION_ID);
                }
                if(r != null && r.isPlaying()) {
                    r.pause();
                    r.reset();
                    r.release();
                    r = null;
                }
                finish();
            }
        });

        if(!isSnoozed) {
            String text = getResources().getString(R.string.snooze);
            SpannableString s = new SpannableString(text);
            s.setSpan(new StyleSpan(Typeface.ITALIC), 6, 13, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new RelativeSizeSpan(0.85f), 6, 13, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            Button snoozeButton = findViewById(R.id.snooze_button);
            snoozeButton.setVisibility(View.VISIBLE);
            snoozeButton.setText(s);
            snoozeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO snooze the alarm when lock screen button pressed
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.cancel(alarmType);
                        notificationManager.cancel(Alarm.AlarmNotification.SNOOZE_NOTIFICATION_ID);
                    } else {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(alarmType);
                        notificationManager.cancel(Alarm.AlarmNotification.SNOOZE_NOTIFICATION_ID);
                    }
                    Calendar snoozeTime = Calendar.getInstance();
                    Alarm alarm = new Alarm(snoozeTime, AlarmActivity.this, alarmType);
                    alarm.snoozeAlarm();

                    if (r != null && r.isPlaying()) {
                        r.pause();
                        r.reset();
                        r.release();
                        r = null;
                    }
                    finish();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(r != null && r.isPlaying()) {
            r.pause();
            r.reset();
            r.release();
            r = null;
        }
    }
}
