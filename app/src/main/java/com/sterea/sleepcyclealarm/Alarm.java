package com.sterea.sleepcyclealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
    private Configurator configurator;

    Alarm (Calendar time, Context context, int requestCode){
        this.time = time;
        this.context = context;
        this.requestCode = requestCode;
        setConfigurator(requestCode);
    }

    private void setConfigurator(int confType){
        if(confType == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE){
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if (confType == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE) {
            configurator = Configurator.bedTimeKnownConf;
        } else if ( confType == Configurator.NAP_TIME_ALARM_REQ_CODE) {
            configurator = Configurator.napTimeConf;
        }
    }

    void register(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent triggerIntent = new Intent(context, AlarmReceiver.class);
        triggerIntent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent triggerPendingIntent = PendingIntent.getBroadcast(context, requestCode,
                triggerIntent, 0);

        if(time.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            time.add(Calendar.DATE,1);
        }

        configurator.setAlarmTimeTimeStamp(time.getTimeInMillis());
        configurator.setAlarmRegistrationMoment(Calendar.getInstance().getTimeInMillis());
        Intent infoIntent = new Intent(context, MainActivity.class);
        infoIntent.putExtra(MainActivity.REQUESTED_TAB, requestCode);
        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, - requestCode*requestCode, infoIntent, 0);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time.getTimeInMillis(),infoPendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, triggerPendingIntent);
        updateConfiguration(true);
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
        updateConfiguration(false);
        settingRebootReceiver(false);
    }

    void snooze(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(context).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(REQUEST_CODE_KEY, requestCode);
        PendingIntent triggerPendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);
        time.add(Calendar.MINUTE, 1);

        Intent infoIntent = new Intent(context, MainActivity.class);
        infoIntent.putExtra(MainActivity.REQUESTED_TAB, requestCode);
        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, - requestCode*requestCode, infoIntent, 0);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time.getTimeInMillis(),infoPendingIntent);
        alarmManager.setAlarmClock(alarmClockInfo, triggerPendingIntent);

        configurator.setAlarmTimeTimeStamp(time.getTimeInMillis());
        configurator.calcBedTimeTimeStamp(time.getTimeInMillis());
        SharedPreferences savedConfiguration = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedConfiguration.edit();
        if(configurator != Configurator.napTimeConf) {
            editor.putBoolean(configurator.getSnoozeStateKey(), true);
        }
        editor.putLong(configurator.getAlarmTimeTimeStampKey(), configurator.getAlarmTimeTimeStamp());
        editor.putLong(configurator.getBedTimeTimeStampKey(), configurator.getBedTimeTimeStamp());
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

    private void updateConfiguration(boolean state){
        SharedPreferences savedPreferences = context.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedPreferences.edit();
        editor.putBoolean(configurator.getAlarmStateKey(), state)
                .putBoolean(configurator.getSnoozeStateKey(), state);
        if(state)
            editor.putLong(configurator.getAlarmTimeTimeStampKey(), configurator.getAlarmTimeTimeStamp());
        if(configurator == Configurator.napTimeConf) {
            editor.putBoolean(configurator.getIsConfiguredKey(), state);
        } else {
            editor.putLong(configurator.getAlarmRegistrationMomentKey(), configurator.getAlarmRegistrationMoment());
        }
        editor.apply();
    }
}
