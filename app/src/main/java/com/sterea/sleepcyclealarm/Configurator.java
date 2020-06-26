package com.sterea.sleepcyclealarm;

import android.content.SharedPreferences;

import java.util.Calendar;

final class Configurator {
    private int sleepCycles;
    private int minutesFallingAsleep;
    private int songIndexPosition; //save the checked radio button of the song list
    private int itemPositionSpinnerCycles, itemPositionSpinnerMinutesAsleep;
    private int hour, minutes;
    private String rawFileSongName;
    private String ringtoneName;
    private Calendar wakeUpTime;
    private Calendar bedTime;
    private Boolean isConfigured;
    private Boolean alarmState; //true for alarm on, false for alarm off
    private Boolean confChanged; //used in onResume method of fragments to check for needed update of textViews

    /*Sate for the shared preferences file
    * Below are listed al the keys of the shared preferences file
    * SAVED_CONFIGURATION is the file name
    * FIRST_TIME_SET_UP is the key to check if the set up dialog activity was opened for the first time ever*/
    static final String SAVED_CONFIGURATION = "com.sterea.sleepcyclealarm";
    static final String HOUR_KNOWN_WAKE_UP = "wake up hour for known wake up time configuration";
    static final String MINUTES_KNOWN_WAKE_UP = "wake up minutes for known wake up configuration";
    static final String WAKING_HOUR_KNOWN_WAKE_UP = "wakingHour";
    static final String RAW_FILE_NAME_KNOWN_WAKE_UP = "songId";
    static final String SONG_NAME_KNOWN_WAKE_UP = "songName";
    static final String IS_KNOWN_WAKE_UP_CONFIGURED = "isConfiguredKnownWakeUp";
    static final String IS_KNOWN_WAKE_UP_ALARM_STATE = "theStateOfTheAlarmForKnownWakeUpTime";
    static final String CYCLES_INT_VALUE = "cyclesIntValue";
    static final String ASLEEP_INT_VALUE = "asleepIntValue";
    /*These keys are used in SetUpAlarmActivity to display the configuration already done*/
    static final String CYCLES_POSITION_SPINNER = "cyclePositionSpinner";
    static final String ASLEEP_POSITION_SPINNER = "asleepPositionSpinner";
    /*Used to show a guide when the app is first time used*/
    static final String FIRST_TIME_SET_UP = "firstTimeSetUp";

    static int[] song = {R.raw.air_horn_in_close_hall_series, R.raw.allthat, R.raw.anewbeginning, R.raw.ceausescu_alo,
            R.raw.cig_swaag, R.raw.creativeminds, R.raw.dubstep, R.raw.funnysong,
            R.raw.hey, R.raw.skull_fire, R.raw.spaceship_alarm, R.raw.summer};

    static Configurator knownBedTimeConf = new Configurator(6, 14, 5, 9);
    static Configurator knownWakeUpTimeConf = new Configurator(6, 14, 5, 9);

    private Configurator(int defSleepCycles, int defFallingAsleep, int itemPositionSpinnerCycles, int itemPositionSpinnerMinutesAsleep){
        sleepCycles = defSleepCycles;
        minutesFallingAsleep = defFallingAsleep;
        this.itemPositionSpinnerCycles = itemPositionSpinnerCycles;
        this.itemPositionSpinnerMinutesAsleep = itemPositionSpinnerMinutesAsleep;
    }

    void updateSharedConfiguration(SharedPreferences savedPreferences){
        SharedPreferences.Editor editor = savedPreferences.edit();
        editor.putInt(Configurator.HOUR_KNOWN_WAKE_UP, Configurator.knownWakeUpTimeConf.getHour());
        editor.putInt(Configurator.MINUTES_KNOWN_WAKE_UP, Configurator.knownWakeUpTimeConf.getMinutes());
        editor.putInt(Configurator.CYCLES_INT_VALUE, Configurator.knownWakeUpTimeConf.getSleepCycles());
        editor.putInt(Configurator.ASLEEP_INT_VALUE, Configurator.knownWakeUpTimeConf.getMinutesFallingAsleep());
        editor.putInt(Configurator.CYCLES_POSITION_SPINNER, Configurator.knownWakeUpTimeConf.getItemPositionSpinnerCycles());
        editor.putInt(Configurator.ASLEEP_POSITION_SPINNER, Configurator.knownWakeUpTimeConf.getItemPositionSpinnerMinutesAsleep());
        editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, Configurator.knownWakeUpTimeConf.getConfigured());
        editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, Configurator.knownWakeUpTimeConf.getAlarmState());
        editor.putString(Configurator.SONG_NAME_KNOWN_WAKE_UP, Configurator.knownWakeUpTimeConf.getRingtoneName());
        editor.putString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP, Configurator.knownWakeUpTimeConf.getRawFileSongName());
        editor.apply();
    }

    /*create calendar objects for wake up time;
    * it can be called either from SetUpAlarmActivity on Create button listener
    * or in MainActivity using the shared preferences file*/
    void setWakeUpTime(int hour, int minutes){
        this.wakeUpTime = Calendar.getInstance();
        wakeUpTime.set(Calendar.HOUR_OF_DAY, hour);
        wakeUpTime.set(Calendar.MINUTE, minutes);
        wakeUpTime.set(Calendar.SECOND, 0);
    }

    /*create calendar objects for bed time for knownWakeUpConfig object;
     * it can be called either from SetUpAlarmActivity on Create button listener
     * or in MainActivity using the shared preferences file*/
    void setBedTime(@org.jetbrains.annotations.NotNull Calendar wakeUpTime, int cycles, int asleepMinutes){
        bedTime = (Calendar) wakeUpTime.clone();
        bedTime.add(Calendar.MINUTE, -((cycles * 90) + asleepMinutes));
    }

    /*overload method, used only for knownBedTimeConfig object*/
    void setBedTime(Calendar bedTime){
        this.bedTime = bedTime;
    }

    void setSleepCycles(int sleepCycles) {
        this.sleepCycles = sleepCycles;
    }

    void setMinutesFallingAsleep(int minutesFallingAsleep) {
        this.minutesFallingAsleep = minutesFallingAsleep;
    }

    void setRawFileSongName(String rawFileSongName) {
        this.rawFileSongName = rawFileSongName;
    }

    void setRingtoneName(String ringtoneName) {
        this.ringtoneName = ringtoneName;
    }

    public int getSongIndexPosition() {
        return songIndexPosition;
    }

    void setHour(int hour) {
        this.hour = hour;
    }

    void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setConfChanged(Boolean confChanged) {
        this.confChanged = confChanged;
    }

    int getSleepCycles() {
        return sleepCycles;
    }

    int getMinutesFallingAsleep() {
        return minutesFallingAsleep;
    }

    String getRawFileSongName() {
        return rawFileSongName;
    }

    String getRingtoneName() {
        return ringtoneName;
    }

    public void setSongIndexPosition(int songIndexPosition) {
        this.songIndexPosition = songIndexPosition;
    }

    int getHour() {
        return hour;
    }

    int getMinutes() {
        return minutes;
    }

    Calendar getWakeUpTime() {
        return wakeUpTime;
    }

    public Calendar getBedTime() {
        return bedTime;
    }

    public int getItemPositionSpinnerCycles() {
        return itemPositionSpinnerCycles;
    }

    public void setItemPositionSpinnerCycles(int itemPositionSpinnerCycles) {
        this.itemPositionSpinnerCycles = itemPositionSpinnerCycles;
    }

    public int getItemPositionSpinnerMinutesAsleep() {
        return itemPositionSpinnerMinutesAsleep;
    }

    public void setItemPositionSpinnerMinutesAsleep(int itemPositionSpinnerMinutesAsleep) {
        this.itemPositionSpinnerMinutesAsleep = itemPositionSpinnerMinutesAsleep;
    }

    public Boolean getConfigured() {
        return isConfigured;
    }

    public void setConfigured(Boolean configuration) {
        isConfigured = configuration;
    }

    public Boolean getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(Boolean alarmState) {
        this.alarmState = alarmState;
    }

    public Boolean getConfChanged() {
        return confChanged;
    }

}
