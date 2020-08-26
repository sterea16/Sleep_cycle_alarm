package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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

public class AlarmActivity extends AppCompatActivity {
    private int alarmType;
    private boolean fromSnoozeNotification;
    private Configurator configurator;

    private void prepare(int alarmType){
        if(alarmType == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE){
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if (alarmType == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE){
            configurator = Configurator.bedTimeKnownConf;
        }
    }
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_layout);
        //get an instance of this activity so it ca be destroyed from notification
        Alarm.Notification.setAlarmActivityInstance(this);

        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        alarmType = bundle.getInt(Alarm.REQUEST_CODE_KEY);
        prepare(alarmType);
        fromSnoozeNotification = bundle.getBoolean(Alarm.IS_SNOOZED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            km.requestDismissKeyguard(this, null);
        }

        TextView textView = findViewById(R.id.text_Wake_Up);
        if(fromSnoozeNotification) {
            textView.setText(R.string.alarm_snoozed);
        } else {
            textView.setText(R.string.wakeUp);
        }

        Button dismissButton = findViewById(R.id.dismissButton);
        if(fromSnoozeNotification) {
            dismissButton.setText(R.string.cancel_alarm);
        } else {
            dismissButton.setText(R.string.dismiss);
        }
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configurator.setAlarmState(false)
                            .setConfChanged(true);
                SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                SharedPreferences.Editor editor = savedPreferences.edit();
                editor.putBoolean(configurator.getAlarmStateKey(), false);
                editor.putBoolean(configurator.getSnoozeStateKey(), false);
                editor.apply();

                Log.d("dismissListener", "Macin " + alarmType);
                int hour = savedPreferences.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, 0);
                int minutes = savedPreferences.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, 0);
                Configurator.wakeUpTimeKnownConf.buildAlarmTime(hour, minutes);
                Alarm alarm = new Alarm(Configurator.wakeUpTimeKnownConf.getAlarmTime(), AlarmActivity.this, alarmType);
                alarm.cancelAlarm();

                cancelNotifications(alarmType);
                Alarm.Notification.stopRingtone();
                finishAndRemoveTask();
            }
        });

        if(!fromSnoozeNotification) {
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
                    cancelNotifications(alarmType);

                    Calendar snoozeTime = Calendar.getInstance();
                    Alarm alarm = new Alarm(snoozeTime, AlarmActivity.this, alarmType);
                    alarm.snoozeAlarm();
                    Alarm.Notification.stopRingtone();

                    finishAndRemoveTask();
                    finish();
                }
            });
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(fromSnoozeNotification){
            finishAndRemoveTask();
            Alarm.Notification snoozeNotification = new Alarm.Notification(this, Alarm.Notification.SNOOZE, alarmType);
            snoozeNotification.startNotify();
        }
    }

    private void cancelNotifications(int alarmType){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.cancel(alarmType);
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(alarmType);

        }
    }
}
