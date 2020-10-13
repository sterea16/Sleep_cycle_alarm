package com.sterea.sleepcyclealarm;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

public class Controller implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static ArrayList<Configurator> configsList;
    private static long alarmRegistrationMoment;
    private final MainActivity mainActivity;
    private final SharedPreferences savedConfiguration;

    public Controller(MainActivity activity){
        mainActivity = activity;
        savedConfiguration = mainActivity.getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
    }

    void initialiseConfigs(){
        setUpConfigurator(Configurator.wakeUpTimeKnownConf);
        setUpConfigurator(Configurator.bedTimeKnownConf);
        setUpConfigurator(Configurator.napTimeConf);
    }

    void setUpConfigurator(Configurator configurator){
        int sleepCycles = savedConfiguration.getInt(configurator.getSleepCyclesKey(), 6);
        int minutesAsleep = savedConfiguration.getInt(configurator.getMinutesFallingAsleepKey(), 14);
        int alarmHour = savedConfiguration.getInt(configurator.getAlarmHourKey(), 0);
        int alarmMinutes = savedConfiguration.getInt(configurator.getAlarmMinutesKey(), 0);
        int bedHour = savedConfiguration.getInt(configurator.getBedHourKey(), 0);
        int bedMinutes = savedConfiguration.getInt(configurator.getBedMinuteKey(), 0);
        boolean alarmState = savedConfiguration.getBoolean(configurator.getAlarmStateKey(), false);
        boolean isConfigured = savedConfiguration.getBoolean(configurator.getIsConfiguredKey(), false);
        long alarmTimeTimeStamp = savedConfiguration.getLong(configurator.getAlarmTimeTimeStampKey(), 0);
        long bedTimeTimeStamp = savedConfiguration.getLong(configurator.getBedTimeTimeStampKey(), 0);
        long alarmRegistrationMoment = savedConfiguration.getLong(configurator.getAlarmRegistrationMomentKey(), 0);

        configurator
                .setSleepCycles(sleepCycles)
                .setMinutesFallingAsleep(minutesAsleep);
        configurator
                .setAlarmHour(alarmHour)
                .setAlarmMinutes(alarmMinutes)
                .buildAlarmTime(alarmHour, alarmMinutes)
                .setAlarmTimeTimeStamp(alarmTimeTimeStamp)
                .setAlarmRegistrationMoment(alarmRegistrationMoment);
        configurator
                .setBedHour(bedHour)
                .setBedMinutes(bedMinutes)
                .buildBedTime(bedHour, bedMinutes)
                .setBedTimeTimeStamp(bedTimeTimeStamp);
        configurator.setAlarmState(alarmState);
        configurator.setConfigured(isConfigured);
    }

    void makeConfigsList(){
        configsList = null;
        configsList = new ArrayList<>();
        configsList.add(Configurator.wakeUpTimeKnownConf);
        configsList.add(Configurator.bedTimeKnownConf);
    }

    private void updateBedTimeTimeStamp(Configurator configurator){
        if(configurator != Configurator.napTimeConf) {
            long bedTimeTimeStamp = configurator.getAlarmTimeTimeStamp() - ((configurator.getSleepCycles() * 90 + configurator.getMinutesFallingAsleep()) * 60 * 1000);
            configurator.setBedTimeTimeStamp(bedTimeTimeStamp);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Configurator configurator = null;
        //switcher listener is called the last so the configuration must be up set from here for the progress bar correct update
        if(key.equals(Configurator.ALARM_STATE_KNOWN_BED_TIME_KEY)){
            configurator = Configurator.bedTimeKnownConf;
        } else if(key.equals(Configurator.ALARM_STATE_WAKE_UP_KNOWN_KEY)){{
            configurator = Configurator.wakeUpTimeKnownConf;
            Configurator.wakeUpTimeKnownConf.setAlarmState(sharedPreferences.getBoolean(key, false));
        }} else if(key.equals(Configurator.ALARM_STATE_NAP_TIME_KEY)){
            configurator = Configurator.napTimeConf;
            Configurator.napTimeConf.setAlarmState(sharedPreferences.getBoolean(key, false));
        }
        if(configurator != null) {
            configurator.setAlarmState(sharedPreferences.getBoolean(key, false));
            updateBedTimeTimeStamp(configurator);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(configurator.getBedTimeTimeStampKey(), configurator.getBedTimeTimeStamp());
            editor.apply();
        }
        mainActivity.updateHeader();
    }

    void registerSharedPrefListener(){
        savedConfiguration.registerOnSharedPreferenceChangeListener(this);
    }

    void unregisterSharedPrefListener(){
        savedConfiguration.unregisterOnSharedPreferenceChangeListener(this);
    }

    boolean isNapTime(){
        return Configurator.napTimeConf.isAlarmOn();
    }

    public boolean isAnyAlarmOn() {
        if(configsList != null) {
            for(int i = 0; i < configsList.size(); ++i) {
                if (!configsList.get(i).isAlarmOn()) {
                    configsList.remove(i);
                    --i;
                }
            }
            return !configsList.isEmpty();
        }
        return false;
    }

    static long sortByWakingTime(){
        if(configsList != null) {
            Collections.sort(configsList, (conf1, conf2) -> {
                if(conf1.getAlarmTimeTimeStamp() > conf2.getAlarmTimeTimeStamp()){
                    return 1;
                } else if(conf1.getAlarmTimeTimeStamp() < conf2.getAlarmTimeTimeStamp()){
                    return -1;
                }
                return 0;
            });
            return configsList.get(0).getAlarmTimeTimeStamp();
        }
        return 0;
    }

    public long getSoonestAlarmTime() {
        return sortByWakingTime();
    }

    long sortByBedTime(){
        if(configsList != null) {
            Collections.sort(configsList, (conf1, conf2) -> {
                if(conf1.getBedTimeTimeStamp() > conf2.getBedTimeTimeStamp()){
                    return 1;
                } else if(conf1.getBedTimeTimeStamp() < conf2.getBedTimeTimeStamp()){
                    return -1;
                }
                return 0;
            });
            setAlarmRegistrationMoment(configsList.get(0).getAlarmRegistrationMoment());
            return configsList.get(0).getBedTimeTimeStamp();
        }
        return 0;
    }

    public long getSoonestBedTime() {
        return sortByBedTime();
    }

    public long getAlarmRegistrationMoment() {
        return alarmRegistrationMoment;
    }

    private void setAlarmRegistrationMoment(long alarmRegistrationTime) {
        Controller.alarmRegistrationMoment = alarmRegistrationTime;
    }

}
