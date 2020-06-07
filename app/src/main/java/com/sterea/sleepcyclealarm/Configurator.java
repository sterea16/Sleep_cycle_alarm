package com.sterea.sleepcyclealarm;

import java.util.Calendar;

final class Configurator {
    private int sleepCycles;
    private int minutesFallingAsleep;
    private int rawSongId;
    private String ringtoneName;
    private int indexPosition; //save the checked radio button
    private int itemPositionSpinnerCycles, itemPositionSpinnerMinutesAsleep;
    private int hour, minutes;
    private Calendar time;
    private Boolean isConfigured;

    /*Sate for the shared preferences file
    * Below are listed al the keys of the shared preferences file
    * SAVED_CONFIGURATION is the file name
    * FIRST_TIME_SET_UP is the key to check if the set up dialog activity was opened for the first time*/
    static final String SAVED_CONFIGURATION = "com.sterea.sleepcyclealarm";
    static final String HOUR = "wakeUpMinutes";
    static final String MINUTES = "minutesWakeUp";
    static final String IS_KNOWN_WAKE_UP_CONFIGURED = "isConfiguredKnownWakeUp";
    static final String CYCLES_INT_VALUE = "cyclesIntValue";
    static final String ASLEEP_INT_VALUE = "asleepIntValue";
    /*These 2 keys are used in SetUpAlarmActivity to display the configuration already done*/
    static final String CYCLES_POSITION_SPINNER = "cyclePositionSpinner";
    static final String ASLEEP_POSITION_SPINNER = "asleepPositionSpinner";
    static final String FIRST_TIME_SET_UP = "firstTimeSetUp";



    static Configurator knownBedTimeConf = new Configurator(7, 14, 6, 9);
    static Configurator knownWakeUpTimeConf = new Configurator(7, 14, 6, 9);

    private Configurator(int defSleepCycles, int defFallingAsleep, int itemPositionSpinnerCycles, int itemPositionSpinnerMinutesAsleep){
        sleepCycles = defSleepCycles;
        minutesFallingAsleep = defFallingAsleep;
        this.itemPositionSpinnerCycles = itemPositionSpinnerCycles;
        this.itemPositionSpinnerMinutesAsleep = itemPositionSpinnerMinutesAsleep;

    }

    void setSleepCycles(int sleepCycles) {
        this.sleepCycles = sleepCycles;
    }

    void setMinutesFallingAsleep(int minutesFallingAsleep) {
        this.minutesFallingAsleep = minutesFallingAsleep;
    }

    void setRawSongId(int rawSongId) {
        this.rawSongId = rawSongId;
    }

    void setRingtoneName(String ringtoneName) {
        this.ringtoneName = ringtoneName;
    }

    public int getIndexPosition() {
        return indexPosition;
    }

    void setHour(int hour) {
        this.hour = hour;
    }

    void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    void setTime(Calendar time) {
        this.time = time;
    }

    int getSleepCycles() {
        return sleepCycles;
    }

    int getMinutesFallingAsleep() {
        return minutesFallingAsleep;
    }

    int getRawSongId() {
        return rawSongId;
    }

    String getRingtoneName() {
        return ringtoneName;
    }

    public void setIndexPosition(int indexPosition) {
        this.indexPosition = indexPosition;
    }

    int getHour() {
        return hour;
    }

    int getMinutes() {
        return minutes;
    }

    Calendar getTime() {
        return time;
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
}
