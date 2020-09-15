package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
/**This activity is called only if the device screen is off.
 * */
public class AlarmActivity extends AppCompatActivity {
    private Configurator configurator;
    static final String CALLER = AlarmActivity.class.getName() + " CALLER ";
    private boolean fromSnoozeNotification;
    private boolean fromAlarmNotification;
    private boolean fromFullScreenIntent;

    private void getConfigurator(int alarmType){
        if(alarmType == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE){
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if (alarmType == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE){
            configurator = Configurator.bedTimeKnownConf;
        } else {
            configurator = Configurator.bedTimeKnownConf;
        }
    }

    private void getCaller(int caller){
        if(caller == Notification.ALARM_CALLER){
            fromAlarmNotification = true;
            fromSnoozeNotification = false;
            fromFullScreenIntent = false;
        } else if (caller == Notification.SNOOZE_CALLER){
            fromSnoozeNotification = true;
            fromAlarmNotification = false;
            fromFullScreenIntent = false;
        } else if(caller == Notification.FULL_SCREEN_CALLER){
            fromFullScreenIntent = true;
            fromSnoozeNotification = false;
            fromAlarmNotification = false;
        }
    }

    private void registerKeyGuardReceiver(){
        IntentFilter keyguardReceiver = new IntentFilter(Intent.ACTION_USER_PRESENT);
        if(configurator == Configurator.wakeUpTimeKnownConf) {
            this.getApplicationContext().registerReceiver(new KeyGuardReceiver.WakeUp(), keyguardReceiver);
        } else if (configurator == Configurator.bedTimeKnownConf){
            this.getApplicationContext().registerReceiver(new KeyGuardReceiver.BedTime(), keyguardReceiver);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_layout);
        //get an instance of this activity so it ca be destroyed from notification
        Notification.setAlarmActivityInstance(this);
        int alarmType = getIntent().getExtras().getInt(Alarm.REQUEST_CODE_KEY);
        getConfigurator(alarmType);
        int caller = getIntent().getExtras().getInt(CALLER);
        getCaller(caller);
        //prepare the screen so this activity can be visible if it's off
        defineScreenState();
        registerKeyGuardReceiver();
        Notification.cancel(configurator.getRequestCode(), this);
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
        dismissButton.setOnClickListener(view -> dismiss());

        if(!fromSnoozeNotification) {
            //format the text of the button
            String text = getResources().getString(R.string.snooze);
            SpannableString s = new SpannableString(text);
            s.setSpan(new StyleSpan(Typeface.ITALIC), 6, 13, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new RelativeSizeSpan(0.85f), 6, 13, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            Button snoozeButton = findViewById(R.id.snooze_button);
            snoozeButton.setVisibility(View.VISIBLE);
            snoozeButton.setText(s);
            snoozeButton.setOnClickListener(v -> snooze());
        }
    }

    private void defineScreenState(){
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
    }

    private void dismiss(){
        configurator.setAlarmState(false)
                    .setConfChanged(true);
        SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        int hour = savedPreferences.getInt(configurator.getAlarmHourKey(), 0);
        int minutes = savedPreferences.getInt(configurator.getAlarmMinutesKey(), 0);
        configurator.buildAlarmTime(hour, minutes);
        Alarm alarm = new Alarm(configurator.getAlarmTime(), AlarmActivity.this, configurator.getRequestCode());
        alarm.cancel();

        Notification.unregisterScreenStateReceiver(this);
        Notification.cancel(configurator.getRequestCode(), this);
        Notification.stopRingtone();
        finishAndRemoveTask();
    }

    private void snooze(){
        Calendar snoozeTime = Calendar.getInstance();
        Alarm alarm = new Alarm(snoozeTime, AlarmActivity.this, configurator.getRequestCode());
        alarm.snooze();
        Notification.unregisterScreenStateReceiver(this);
        Notification.stopRingtone();
        setCallersToFalse();
        finishAndRemoveTask();
    }

    private void setCallersToFalse(){
        fromAlarmNotification = false;
        fromSnoozeNotification = false;
        fromFullScreenIntent = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean deviceUnlocked = savedConfiguration.getBoolean(Configurator.DEVICE_UNLOCKED, false);
        Log.d("onStop", "onStop deviceUnlocked " + deviceUnlocked);
        Log.d("onStop", "onStop counter callers:\n " +
                "from alarm " + fromAlarmNotification + "\n" +
                "from Snooze " + fromSnoozeNotification + "\n" +
                "from full screen " + fromFullScreenIntent);
        if(configurator.getAlarmState()) {
            if(fromSnoozeNotification) {
                Notification snoozeNotification = new Notification(this, Notification.SNOOZE, configurator.getRequestCode());
                snoozeNotification.trigger();
                finishAndRemoveTask();
            } else if(fromAlarmNotification) {
                Notification alarmNotification = new Notification(this, Notification.ALARM, configurator.getRequestCode());
                alarmNotification.trigger();
                finishAndRemoveTask();
            } else if (fromFullScreenIntent && deviceUnlocked) {
                /*if the device is unlocked and the activity is in onStop state a new notification will be triggered
                * if the user click that new notification, a new instance of AlarmActivity will start with fromAlarmNotification true
                * if the user locks device, the screen state receiver will be triggered and deviceUnlocked will be false
                * a snooze notification will be triggered*/
                Notification alarmNotification = new Notification(this, Notification.ALARM, configurator.getRequestCode());
                alarmNotification.trigger();
                SharedPreferences.Editor editor = savedConfiguration.edit();
                editor.putBoolean(Configurator.DEVICE_UNLOCKED, false);
                editor.apply();
                finishAndRemoveTask();
            }
        }
    }
}
