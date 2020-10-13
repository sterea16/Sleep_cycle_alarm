package com.sterea.sleepcyclealarm;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Calendar;

public final class Configurator {

    private int sleepCycles;
    private String sleepCyclesKey;
    private int minutesFallingAsleep;
    private String minutesFallingAsleepKey;
    private int ringtoneIndexPosition; //save the checked radio button of the song list
    private final String ringtoneIndexPositionKey;
    private int itemPositionSpinnerCycles, itemPositionSpinnerMinutesAsleep;
    private String itemPositionSpinnerCyclesKey, itemPositionSpinnerMinutesAsleepKey;
    private int alarmHour, alarmMinutes;
    private final String alarmHourKey;
    private final String alarmMinutesKey;
    private long alarmTimeTimeStamp;
    private final String alarmTimeTimeStampKey;
    private long alarmRegistrationMoment;
    private final String alarmRegistrationMomentKey;
    private int bedHour, bedMinutes;
    private String bedHourKey, bedMinuteKey;
    private long bedTimeTimeStamp;
    private final String bedTimeTimeStampKey;

    /**Request code represents the type of configurator
     * which translates in the scenario type.<br>
     *     1 is for known waking time scenario<br>
     *     2 is for known bed time scenario.*/
    private final int requestCode;
    private String rawFileSongName;
    private String ringtoneName;
    private final String ringtoneNameKey;
    private final String rawFileSongNameKey;
    private Calendar alarmTime;
    private Calendar bedTime;
    private int napDuration;
    private String napDurationKey;
    private boolean isConfigured;
    private boolean alarmState; //true for alarm on, false for alarm off
    private final String alarmStateKey;
    private boolean snoozeState;
    private final String snoozeStateKey;
    private boolean confChanged; //used in onResume method of fragments to check for needed update of textViews
    private final String isConfiguredKey;

    /**Sate for the shared preferences file
    * Below are listed al the keys of the shared preferences file
    * SAVED_CONFIGURATION is the file name
    * FIRST_TIME_SET_UP is the key to check if the set up dialog activity was opened for the first time ever. */
    static final String FIRST_TIME_SET_UP = "firstTimeSetUp";

    static final String SAVED_CONFIGURATION = "com.sterea.sleepcyclealarm";//file name
    static final String DEVICE_UNLOCKED = Configurator.class.getSimpleName() + " DEVICE UNLOCKED ";
    /*known wake up time configuration keys**/

    static final String ALARM_STATE_WAKE_UP_KNOWN_KEY = Configurator.class.getName() + "ALARM_STATE_WAKE_UP_KNOWN_KEY";
    static final String SNOOZE_STATE_WAKE_UP_KNOWN_KEY = Configurator.class.getName() + "SNOOZE_STATE_WAKE_UP_KNOWN_KEY";
    static final String ALARM_HOUR_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "ALARM_HOUR_KNOWN_WAKE_UP_KEY";
    static final String ALARM_MINUTES_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "ALARM_MINUTES_KNOWN_WAKE_UP_KEY";
    static final String ALARM_TIME_TIME_STAMP_WAKE_UP_KEY = Configurator.class.getName() + "ALARM_TIME_TIME_STAMP_WAKE_UP_KEY";
    static final String ALARM_REGISTRATION_MOMENT_STAMP_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "ALARM_REGISTRATION_TIME_STAMP_KNOWN_WAKE_UP_KEY";
    static final String BED_HOUR_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "BED_HOUR_KNOWN_WAKE_UP_KEY";
    static final String BED_MINUTES_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "BED_MINUTES_KNOWN_WAKE_UP_KEY";
    static final String BED_TIME_TIME_STAMP_WAKE_UP_KEY = Configurator.class.getName() + "BED_TIME_TIME_STAMP_WAKE_UP_KEY";
    static final String RAW_FILE_NAME_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "RAW_FILE_NAME_KNOWN_WAKE_UP_KEY";
    static final String RINGTONE_NAME_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "RINGTONE_NAME_KNOWN_WAKE_UP_KEY";
    static final String RINGTONE_INDEX_POSITION_WAKE_UP_KEY = Configurator.class.getName() + "RINGTONE_INDEX_POSITION_WAKE_UP_KEY";
    static final String IS_WAKE_UP_KNOWN_CONFIGURED_KEY = Configurator.class.getName() + "IS_WAKE_UP_KNOWN_CONFIGURED_KEY";
    static final String CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY";
    static final String ASLEEP_INT_VALUE_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "ASLEEP_INT_VALUE_KNOWN_WAKE_UP_KEY";
    static final String CYCLES_POSITION_SPINNER_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "CYCLES_POSITION_SPINNER_KNOWN_WAKE_UP_KEY";
    static final String ASLEEP_POSITION_SPINNER_KNOWN_WAKE_UP_KEY = Configurator.class.getName() + "ASLEEP_POSITION_SPINNER_KNOWN_WAKE_UP_KEY";
    static final int WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE = 1;

    /*known bed time configuration keys*/
    static final String ALARM_STATE_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "ALARM_STATE_KNOWN_BED_TIME_KEY";
    static final String SNOOZE_STATE_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "SNOOZE_STATE_KNOWN_BED_TIME_KEY";
    static final String ALARM_HOUR_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "ALARM_HOUR_KNOWN_BED_TIME_KEY";
    static final String ALARM_MINUTES_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "ALARM_MINUTES_KNOWN_BED_TIME_KEY";
    static final String ALARM_TIME_TIME_STAMP_BED_TIME_KEY = Configurator.class.getName() + "ALARM_TIME_TIME_STAMP_BED_TIME_KEY";
    static final String ALARM_REGISTRATION_MOMENT_STAMP_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "ALARM_REGISTRATION_TIME_STAMP_KNOWN_WAKE_UP_KEY";
    static final String BED_HOUR_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "BED_HOUR_KNOWN_BED_TIME_KEY";
    static final String BED_MINUTES_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "BED_MINUTES_KNOWN_BED_TIME_KEY";
    static final String BED_TIME_TIME_STAMP_BED_TIME_KEY = Configurator.class.getName() + "BED_TIME_TIME_STAMP_BED_TIME_KEY";
    static final String RAW_FILE_NAME_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "RAW_FILE_NAME_KNOWN_BED_TIME_KEY";
    static final String RINGTONE_NAME_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "RINGTONE_NAME_KNOWN_BED_TIME_KEY";
    static final String RINGTONE_INDEX_POSITION_BED_TIME_KEY = Configurator.class.getName() + "RINGTONE_INDEX_POSITION_BED_TIME_KEY";
    static final String IS_BED_TIME_KNOWN_CONFIGURED_KEY = Configurator.class.getName() + "IS_BED_TIME_KNOWN_CONFIGURED_KEY";
    static final String CYCLES_INT_VALUE_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "CYCLES_INT_VALUE_KNOWN_BED_TIME_KEY";
    static final String ASLEEP_INT_VALUE_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "ASLEEP_INT_VALUE_KNOWN_BED_TIME_KEY";
    static final String CYCLES_POSITION_SPINNER_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "CYCLES_POSITION_SPINNER_KNOWN_BED_TIME_KEY";
    static final String ASLEEP_POSITION_SPINNER_KNOWN_BED_TIME_KEY = Configurator.class.getName() + "ASLEEP_POSITION_SPINNER_KNOWN_BED_TIME_KEY";
    static final int BED_TIME_KNOWN_ALARM_REQ_CODE = 2;

    /*nap time configurator keys*/
    static final String ALARM_STATE_NAP_TIME_KEY = Configurator.class.getName() + "ALARM_STATE_NAP_TIME_KEY";
    static final String SNOOZE_STATE_NAP_TIME_KEY = Configurator.class.getName() + "SNOOZE_STATE_NAP_TIME_KEY";
    static final String NAP_DURATION_KEY = Configurator.class.getName() + "NAP_DURATION_KEY";
    static final String ALARM_HOUR_NAP_TIME_KEY = Configurator.class.getName() + "ALARM_HOUR_NAP_TIME_KEY";
    static final String ALARM_MINUTES_NAP_TIME_KEY = Configurator.class.getName() + "ALARM_MINUTES_NAP_TIME_KEY";
    static final String START_NAP_TIME_STAMP_KEY = Configurator.class.getName() + "START_NAP_TIME_STAMP";
    static final String END_NAP_TIME_STAMP_KEY = Configurator.class.getName() + "END_NAP_TIME_STAMP";
    static final String IS_NAP_TIME_CONFIGURED_KEY = Configurator.class.getName() + "IS_NAP_TIME_CONFIGURED_KEY";
    static final String RAW_FILE_NAME_KNOWN_NAP_TIME_KEY = Configurator.class.getName() + "RAW_FILE_NAME_KNOWN_NAP_TIME_KEY";
    static final String RINGTONE_NAME_KNOWN_NAP_TIME_KEY = Configurator.class.getName() + "RINGTONE_NAME_KNOWN_NAP_TIME_KEY";
    static final String RINGTONE_INDEX_POSITION_NAP_TIME_KEY = Configurator.class.getName() + "RINGTONE_INDEX_POSITION_NAP_TIME_KEY";
    static final int NAP_TIME_ALARM_REQ_CODE = 3;

    /**
     *
     * The configurator for the scenario when the users is in bed ready for sleep
     * and he wants to have an alarm regardless of the waking time.
     *
     * */
    static final Configurator bedTimeKnownConf =
                                                new Configurator(6, 14, 5, 9,
                                                        BED_TIME_KNOWN_ALARM_REQ_CODE,
                                                        ALARM_STATE_KNOWN_BED_TIME_KEY,
                                                        SNOOZE_STATE_KNOWN_BED_TIME_KEY,
                                                        CYCLES_INT_VALUE_KNOWN_BED_TIME_KEY,
                                                        ASLEEP_INT_VALUE_KNOWN_BED_TIME_KEY,
                                                        ALARM_HOUR_KNOWN_BED_TIME_KEY,
                                                        ALARM_MINUTES_KNOWN_BED_TIME_KEY,
                                                        ALARM_TIME_TIME_STAMP_BED_TIME_KEY,
                                                        ALARM_REGISTRATION_MOMENT_STAMP_KNOWN_BED_TIME_KEY,
                                                        BED_HOUR_KNOWN_BED_TIME_KEY,
                                                        BED_MINUTES_KNOWN_BED_TIME_KEY,
                                                        BED_TIME_TIME_STAMP_BED_TIME_KEY,
                                                        IS_BED_TIME_KNOWN_CONFIGURED_KEY,
                                                        CYCLES_POSITION_SPINNER_KNOWN_BED_TIME_KEY,
                                                        ASLEEP_POSITION_SPINNER_KNOWN_BED_TIME_KEY,
                                                        RINGTONE_NAME_KNOWN_BED_TIME_KEY,
                                                        RAW_FILE_NAME_KNOWN_BED_TIME_KEY, RINGTONE_INDEX_POSITION_BED_TIME_KEY);

    /**
     *
     * The configurator for the scenario when the users knows when he must wake up
     * and he wants to know when is the right time to go in bed for sleep.
     *
     * */
    static final Configurator wakeUpTimeKnownConf =
                                                new Configurator(6, 14, 5, 9,
                                                        WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE,
                                                        ALARM_STATE_WAKE_UP_KNOWN_KEY,
                                                        SNOOZE_STATE_WAKE_UP_KNOWN_KEY,
                                                        CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY,
                                                        ASLEEP_INT_VALUE_KNOWN_WAKE_UP_KEY,
                                                        ALARM_HOUR_KNOWN_WAKE_UP_KEY,
                                                        ALARM_MINUTES_KNOWN_WAKE_UP_KEY,
                                                        ALARM_TIME_TIME_STAMP_WAKE_UP_KEY,
                                                        ALARM_REGISTRATION_MOMENT_STAMP_KNOWN_WAKE_UP_KEY,
                                                        BED_HOUR_KNOWN_WAKE_UP_KEY,
                                                        BED_MINUTES_KNOWN_WAKE_UP_KEY,
                                                        BED_TIME_TIME_STAMP_WAKE_UP_KEY,
                                                        IS_WAKE_UP_KNOWN_CONFIGURED_KEY,
                                                        CYCLES_POSITION_SPINNER_KNOWN_WAKE_UP_KEY,
                                                        ASLEEP_POSITION_SPINNER_KNOWN_WAKE_UP_KEY,
                                                        RINGTONE_NAME_KNOWN_WAKE_UP_KEY,
                                                        RAW_FILE_NAME_KNOWN_WAKE_UP_KEY,
                                                        RINGTONE_INDEX_POSITION_WAKE_UP_KEY);

    private Configurator(int defSleepCycles, int defFallingAsleep, int defItemPositionSpinnerCycles, int defItemPositionSpinnerMinutesAsleep,
                         int requestCode,
                         String alarmStateKey,
                         String snoozeStateKey,
                         String sleepCyclesKey,
                         String minutesFallingAsleepKey,
                         String alarmHourKey,
                         String alarmMinutesKey,
                         String alarmTimeTimeStampKey,
                         String alarmRegistrationMomentKey,
                         String bedHourKey,
                         String bedMinuteKey,
                         String bedTimeTimeStampKey,
                         String isConfiguredKey,
                         String itemPositionSpinnerCyclesKey,
                         String itemPositionSpinnerMinutesAsleepKey,
                         String ringtoneNameKey,
                         String rawFileSongNameKey,
                         String ringtoneIndexPositionKey){
        sleepCycles = defSleepCycles;
        minutesFallingAsleep = defFallingAsleep;
        this.sleepCyclesKey = sleepCyclesKey;
        this.minutesFallingAsleepKey = minutesFallingAsleepKey;
        this.alarmHourKey = alarmHourKey;
        this.alarmMinutesKey = alarmMinutesKey;
        this.alarmTimeTimeStampKey = alarmTimeTimeStampKey;
        this.alarmRegistrationMomentKey = alarmRegistrationMomentKey;
        this.bedHourKey = bedHourKey;
        this.bedMinuteKey = bedMinuteKey;
        this.bedTimeTimeStampKey = bedTimeTimeStampKey;
        this.itemPositionSpinnerCycles = defItemPositionSpinnerCycles;
        this.itemPositionSpinnerMinutesAsleep = defItemPositionSpinnerMinutesAsleep;
        this.requestCode = requestCode;
        this.isConfiguredKey = isConfiguredKey;
        this.alarmStateKey = alarmStateKey;
        this.snoozeStateKey = snoozeStateKey;
        this.itemPositionSpinnerCyclesKey = itemPositionSpinnerCyclesKey;
        this.itemPositionSpinnerMinutesAsleepKey = itemPositionSpinnerMinutesAsleepKey;
        this.ringtoneNameKey = ringtoneNameKey;
        this.rawFileSongNameKey = rawFileSongNameKey;
        this.ringtoneIndexPositionKey = ringtoneIndexPositionKey;
    }

    static final Configurator napTimeConf = new Configurator(NAP_TIME_ALARM_REQ_CODE,
                                                            IS_NAP_TIME_CONFIGURED_KEY,
                                                            ALARM_STATE_NAP_TIME_KEY,
                                                            SNOOZE_STATE_NAP_TIME_KEY,
                                                            NAP_DURATION_KEY,
                                                            ALARM_HOUR_NAP_TIME_KEY,
                                                            ALARM_MINUTES_NAP_TIME_KEY,
                                                            END_NAP_TIME_STAMP_KEY,
                                                            START_NAP_TIME_STAMP_KEY,
                                                            RINGTONE_NAME_KNOWN_NAP_TIME_KEY,
                                                            RAW_FILE_NAME_KNOWN_NAP_TIME_KEY,
                                                            RINGTONE_INDEX_POSITION_NAP_TIME_KEY);

    private Configurator(int requestCode,
                         String isConfiguredKey,
                         String alarmStateKey,
                         String snoozeStateKey,
                         String napDurationKey,
                         String alarmHourKey,
                         String alarmMinutesKey,
                         String alarmTimeTimeStampKey,
                         String bedTimeTimeStampKey,
                         String ringtoneNameKey,
                         String rawFileSongNameKey,
                         String ringtoneIndexPositionKey){
        this.requestCode = requestCode;
        this.isConfiguredKey = isConfiguredKey;
        this.alarmStateKey = alarmStateKey;
        this.snoozeStateKey = snoozeStateKey;
        this.napDurationKey = napDurationKey;
        this.alarmHourKey = alarmHourKey;
        this.alarmMinutesKey = alarmMinutesKey;
        this.alarmTimeTimeStampKey = alarmTimeTimeStampKey;
        this.bedTimeTimeStampKey = bedTimeTimeStampKey;
        alarmRegistrationMomentKey = bedTimeTimeStampKey;
        this.ringtoneNameKey = ringtoneNameKey;
        this.rawFileSongNameKey = rawFileSongNameKey;
        this.ringtoneIndexPositionKey = ringtoneIndexPositionKey;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public String getIsConfiguredKey() {
        return isConfiguredKey;
    }

    public String getRingtoneNameKey() {
        return ringtoneNameKey;
    }

    void updateSavedConfiguration(SharedPreferences savedConfiguration) {
        SharedPreferences.Editor editor = savedConfiguration.edit();
        editor.putInt(alarmHourKey, alarmHour)
                .putInt(alarmMinutesKey, alarmMinutes)
                .putLong(alarmTimeTimeStampKey, alarmTimeTimeStamp)
                .putLong(alarmRegistrationMomentKey, alarmRegistrationMoment)
                .putInt(bedHourKey, bedHour)
                .putInt(bedMinuteKey, bedMinutes)
                .putLong(bedTimeTimeStampKey, bedTimeTimeStamp)
                .putInt(sleepCyclesKey, sleepCycles)
                .putInt(minutesFallingAsleepKey, minutesFallingAsleep)
                .putBoolean(isConfiguredKey, isConfigured)
                .putBoolean(alarmStateKey, alarmState)
                .putString(ringtoneNameKey, ringtoneName)
                .putString(rawFileSongNameKey, rawFileSongName)
                .putInt(ringtoneIndexPositionKey, ringtoneIndexPosition);

        if (this == Configurator.napTimeConf) {
            editor.putInt(NAP_DURATION_KEY, napTimeConf.getNapDuration())
                    .putBoolean(SNOOZE_STATE_NAP_TIME_KEY, napTimeConf.isSnoozeState());
        }
        editor.apply();
    }

    /**
     * Used when there is already an wake up hour saved in shared preferences file,
     * so it need the hour and minutes to build it.
     * @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator buildAlarmTime(int hour, int minutes) {
        alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minutes);
        alarmTime.set(Calendar.SECOND, 0);
        return this;
    }

    /** @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator buildBedTime(int hour, int minutes){
        bedTime = Calendar.getInstance();
        bedTime.set(Calendar.HOUR_OF_DAY, hour);
        bedTime.set(Calendar.MINUTE, minutes);
        bedTime.set(Calendar.SECOND, 0);
        return this;
    }

    /**Calculates and <b>SETS</b> the alarm time.
     * @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator calcAlarmTime(Calendar bedTime, int cycles, int minutesFallingAsleep){
        alarmTime = (Calendar) bedTime.clone();
        /*commented only during tests*/
        alarmTime.add(Calendar.MINUTE, cycles * 90 + minutesFallingAsleep);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        /*this line must be commented outside tests*/
        /*alarmTime.add(Calendar.MINUTE, 1);*/

        return this;
    }

    /**Used to set nap time alarms*/
    Configurator calcAlarmTime (Calendar currentTime, int duration){
        alarmTime = currentTime;
        alarmTime.add(Calendar.MINUTE, duration);
        return this;
    }

    /**Calculates and <b>SETS</b> the bed time.
     * @return Returns a reference to the same Configurator object, so you can
     *  chain put calls together.*/
    Configurator calcBedTime(Calendar wakeUpTime, int cycles, int minutesFallingAsleep){
        bedTime = (Calendar) wakeUpTime.clone();
        /*commented only during tests*/
        bedTime.add(Calendar.MINUTE, -((cycles * 90) + minutesFallingAsleep));
        bedTime.set(Calendar.SECOND, 0);
        bedTime.set(Calendar.MILLISECOND, 0);
        /*this line must be deleted outside tests*/
        /*bedTime.add(Calendar.MINUTE, -1);*/
        return this;
    }

    /** @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setBedTime(Calendar currentTime){
        currentTime.set(Calendar.SECOND, 0);
        bedTime = currentTime;
        return this;
    }

    public String getAlarmStateKey() {
        return alarmStateKey;
    }

    public String getSnoozeStateKey() {
        return snoozeStateKey;
    }

    /** @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setSleepCycles(int sleepCycles) {
        this.sleepCycles = sleepCycles;
        return this;
    }
    /** @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setMinutesFallingAsleep(int minutesFallingAsleep) {
        this.minutesFallingAsleep = minutesFallingAsleep;
        return this;
    }

    public String getSleepCyclesKey() {
        return sleepCyclesKey;
    }

    public String getMinutesFallingAsleepKey() {
        return minutesFallingAsleepKey;
    }



    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setAlarmHour(int alarmHour) {
        this.alarmHour = alarmHour;
        return this;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setAlarmMinutes(int alarmMinutes) {
        this.alarmMinutes = alarmMinutes;
        return this;
    }

    public long getBedTimeTimeStamp() {
        return bedTimeTimeStamp;
    }

    /**@return Returns a reference to the same Configurator object, so
     * chain put calls together are possible.*/
    public Configurator calcBedTimeTimeStamp(long alarmTimeTimeStamp) {
        if(this == napTimeConf){
            this.bedTimeTimeStamp = Calendar.getInstance().getTimeInMillis();
        } else {
            long millisecondsBackwards = (sleepCycles * 90 + minutesFallingAsleep) * 60 * 1000;
            this.bedTimeTimeStamp = alarmTimeTimeStamp - millisecondsBackwards;
        }
        return this;
    }

    public Configurator setBedTimeTimeStamp(long bedTimeTimeStamp){
        this.bedTimeTimeStamp = bedTimeTimeStamp;
        return this;
    }

    public String getBedTimeTimeStampKey() {
        return bedTimeTimeStampKey;
    }

    public long getAlarmRegistrationMoment() {
        return alarmRegistrationMoment;
    }

    public long getAlarmTimeTimeStamp() {
        return alarmTimeTimeStamp;
    }

    /**@return Returns a reference to the same Configurator object, so
     * chain put calls together are possible.*/
    public Configurator setAlarmTimeTimeStamp(long alarmTimeTimeStamp) {
        this.alarmTimeTimeStamp = alarmTimeTimeStamp;
        return this;
    }

    public String getAlarmTimeTimeStampKey() {
        return alarmTimeTimeStampKey;
    }

    /**@return Returns a reference to the same Configurator object, so
     * chain put calls together are possible.*/
    public Configurator setAlarmRegistrationMoment(long alarmRegistrationTimeStamp) {
        this.alarmRegistrationMoment = alarmRegistrationTimeStamp;
        return this;
    }

    public String getAlarmRegistrationMomentKey() {
        return alarmRegistrationMomentKey;
    }


    public Boolean isConfigured() {
        return isConfigured;
    }

    int getSleepCycles() {
        return sleepCycles;
    }

    int getMinutesFallingAsleep() {
        return minutesFallingAsleep;
    }

    int getAlarmHour() {
        return alarmHour;
    }

    int getAlarmMinutes() {
        return alarmMinutes;
    }

    public int getBedHour() {
        return bedHour;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setBedHour(int bedHour) {
        this.bedHour = bedHour;
        return this;
    }



    /** @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    public String getBedHourKey() {
        return bedHourKey;
    }

    public String getAlarmHourKey() {
        return alarmHourKey;
    }

    public String getAlarmMinutesKey() {
        return alarmMinutesKey;
    }

    public String getBedMinuteKey() {
        return bedMinuteKey;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setBedMinuteKey(String bedMinuteKey) {
        this.bedMinuteKey = bedMinuteKey;
        return this;
    }

    public int getBedMinutes() {
        return bedMinutes;
    }

    public Configurator setBedMinutes(int bedMinutes) {
        this.bedMinutes = bedMinutes;
        return this;
    }

    Calendar getAlarmTime() {
        return alarmTime;
    }

    public Calendar getBedTime() {
        return bedTime;
    }

    public int getItemPositionSpinnerCycles() {
        return itemPositionSpinnerCycles;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setItemPositionSpinnerCycles(int itemPositionSpinnerCycles) {
        this.itemPositionSpinnerCycles = itemPositionSpinnerCycles;
        return this;
    }

    public int getItemPositionSpinnerMinutesAsleep() {
        return itemPositionSpinnerMinutesAsleep;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setItemPositionSpinnerMinutesAsleep(int itemPositionSpinnerMinutesAsleep) {
        this.itemPositionSpinnerMinutesAsleep = itemPositionSpinnerMinutesAsleep;
        return this;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setConfigured(boolean configuration) {
        isConfigured = configuration;
        return this;
    }

    public boolean isAlarmOn() {
        return alarmState;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setAlarmState(boolean alarmState) {
        this.alarmState = alarmState;
        return this;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setConfChanged(boolean confChanged) {
        this.confChanged = confChanged;
        return this;
    }

    public boolean isConfChanged() {
        return confChanged;
    }

    /** @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setRawFileSongName(String rawFileSongName) {
        this.rawFileSongName = rawFileSongName;
        return this;
    }

    /** @return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setRingtoneName(String ringtoneName) {
        this.ringtoneName = ringtoneName;
        return this;
    }

    public int getRingtoneIndexPosition() {
        return ringtoneIndexPosition;
    }

    public String getRingtoneIndexPositionKey() {
        return ringtoneIndexPositionKey;
    }

    String getRawFileSongName() {
        return rawFileSongName;
    }

    String getRingtoneName() {
        return ringtoneName;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    Configurator setRingtoneIndexPosition(int ringtoneIndexPosition) {
        this.ringtoneIndexPosition = ringtoneIndexPosition;
        return  this;
    }

    public String getRawFileSongNameKey() {
        return rawFileSongNameKey;
    }

    public String getNapDurationKey() {
        return napDurationKey;
    }

    public int getNapDuration() {
        return napDuration;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    public Configurator setNapDuration(int napDuration) {
        this.napDuration = napDuration;
        return this;
    }
    
    public boolean isSnoozeState() {
        return snoozeState;
    }

    /**@return Returns a reference to the same Configurator object, so 
     * chain put calls together are possible.*/
    public Configurator setSnoozeState(boolean snoozeState) {
        this.snoozeState = snoozeState;
        return  this;
    }

    @NonNull
    @Override
    public String toString() {
        if(this == wakeUpTimeKnownConf){
            return "known wake up configurator";
        } else if ( this == bedTimeKnownConf) {
            return "known bed time configurator";
        } else {
            return "nap time configurator";
        }
    }
}
