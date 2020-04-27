package com.sterea.sleepcyclealarm;

import java.util.Calendar;

final class AlarmConfiguration {
    private int sleepCycles;
    private int minutesFallingAsleep;
    private int rawSongId;
    private String ringtoneName;
    private int hour, minutes;
    private Calendar time;

    static AlarmConfiguration calcBedTimeAlarmConf = new AlarmConfiguration(7);
    static AlarmConfiguration calcWakeUpTimeAlarmConf = new AlarmConfiguration(7);

    private AlarmConfiguration(int defSleepCycles){
        this.sleepCycles = defSleepCycles;
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

    int getHour() {
        return hour;
    }

    int getMinutes() {
        return minutes;
    }

    Calendar getTime() {
        return time;
    }
}
