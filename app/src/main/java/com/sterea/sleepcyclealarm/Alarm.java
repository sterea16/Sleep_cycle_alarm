package com.sterea.sleepcyclealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import java.util.Calendar;
import java.util.Objects;
import static android.content.Context.MODE_PRIVATE;

final class Alarm {

    private Calendar time;
    private Context context;

    /**Request code to register the alarm in the alarm service {@link Context#ALARM_SERVICE}.
     * Each alarm must have its own request code,
     * otherwise the system will overwrite the alarm with the same request code.<br>
     * <br>
     * <b>1</b> is for known wake up scenario<br>
     * <b>2</b> for known bed time<br>
     * <b>3</b> is for nap time<br>
     * <b>4</b> is for snooze<br>
     * <b>5</b> is for bed time reminder<br>
     **/
    private int requestCode;

    static final String REQUEST_CODE_KEY = Alarm.class.getName();
    static final String IS_SNOOZED = Alarm.class.getName() + "SNOOZED";
    static final String IS_DISMISSED = Alarm.class.getName() + "DISMISSED";
    static final String IS_SWIPED = Alarm.class.getName() + "SWIPED";
    private String TAG = Alarm.class.getSimpleName();

    Alarm (Calendar time, Context context, int requestCode){
        this.time = time;
        this.context = context;
        this.requestCode = requestCode;

    }

    void register(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);

        if(time.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            time.add(Calendar.DATE,1);
        }
        Log.d("Alarm register()","alarm registered");
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
        settingRebootReceiver(true);
    }

    void cancel(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                    intent, 0);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        Log.d("Alarm cancel()","alarm canceled");
        SharedPreferences savedPreferences = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedPreferences.edit();

        if(requestCode == Configurator.wakeUpTimeKnownConf.getRequestCode()){
            editor.putBoolean(Configurator.ALARM_STATE_WAKE_UP_KNOWN_KEY, false)
                    .putBoolean(Configurator.SNOOZE_STATE_WAKE_UP_KNOWN_KEY, false);
        } else if (requestCode == Configurator.bedTimeKnownConf.getRequestCode()){
            editor.putBoolean(Configurator.ALARM_STATE_KNOWN_BED_TIME_KEY, false)
                    .putBoolean((Configurator.SNOOZE_STATE_KNOWN_BED_TIME_KEY), false);
        }
        editor.apply();
        settingRebootReceiver(false);
    }

    void snooze(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);
        Log.d("Alarm snooze()", "alarm snoozed");
        time.add(Calendar.MINUTE, 1);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);

        SharedPreferences savedConfiguration = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedConfiguration.edit();
        if(requestCode == Configurator.wakeUpTimeKnownConf.getRequestCode()){
            editor.putBoolean(Configurator.SNOOZE_STATE_WAKE_UP_KNOWN_KEY, true);
        } else if (requestCode == Configurator.bedTimeKnownConf.getRequestCode()){
            editor.putBoolean((Configurator.SNOOZE_STATE_KNOWN_BED_TIME_KEY), true);
        }
        editor.apply();

        Notification.cancel(requestCode, context);
        Notification snoozeNotification = new Notification(context, Notification.SNOOZE, requestCode);
        snoozeNotification.trigger();
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
}
