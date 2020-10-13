package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.ArrayList;
import java.util.Calendar;

public class DialogActivity extends AppCompatActivity {
    final static String ALARM_TYPE = DialogActivity.class.getSimpleName() + " ALARM TYPE ";
    final static String LISTENER_TYPE = DialogActivity.class.getSimpleName() + " LISTENER TYPE ";
    final static String TIME_LISTENER = DialogActivity.class.getSimpleName() + " TIME LISTENER ";
    final static String CYCLES_LISTENER = DialogActivity.class.getSimpleName() + " CYCLES LISTENER ";
    final static String ASLEEP_MINUTES_LISTENER = DialogActivity.class.getSimpleName() + " ASLEEP MINUTES LISTENER ";
    final static String NAP_DURATION_LISTENER = DialogActivity.class.getSimpleName() + " NAP_DURATION_LISTENER ";
    Configurator configurator;
    private NumberPicker numberPicker;
    private String listenerType;

    private void setConfigurator(int alarmType) {
        if (alarmType == Configurator.wakeUpTimeKnownConf.getRequestCode()){
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if(alarmType == Configurator.bedTimeKnownConf.getRequestCode()){
            configurator = Configurator.bedTimeKnownConf;
        } else if (alarmType == Configurator.napTimeConf.getRequestCode()) {
            configurator = Configurator.napTimeConf;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_layout);

        int alarmType = getIntent().getExtras().getInt(ALARM_TYPE);
        setConfigurator(alarmType);

        listenerType = getIntent().getExtras().getString(LISTENER_TYPE);
        if (listenerType.equals(TIME_LISTENER)){
            setTimePicker();
        } else if (listenerType.equals(CYCLES_LISTENER)){
            setCyclePicker();
        } else if (listenerType.equals(ASLEEP_MINUTES_LISTENER)){
            setAsleepPicker();
        } else if (listenerType.equals(NAP_DURATION_LISTENER)) {
            setNapDurationPicker();
        }

        Button cancel = findViewById(R.id.cancel_dialog_button);
        cancel.setOnClickListener(v -> finish());
    }

    private void setTimePicker(){
        if(configurator == Configurator.wakeUpTimeKnownConf){
            setTitle(getResources().getString(R.string.choose_waking_time));
        } else if (configurator == Configurator.bedTimeKnownConf) {
            setTitle(getResources().getString(R.string.choose_bed_time));
        }

        TimePicker timePicker = findViewById(R.id.time_picker_dialog);
        timePicker.setIs24HourView(DateFormat.is24HourFormat(this));
        timePicker.setVisibility(View.VISIBLE);

        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(v -> {
            SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            SharedPreferences.Editor editor = savedConfiguration.edit();
            if (configurator == Configurator.wakeUpTimeKnownConf){
                //get time from time picker
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    configurator.setAlarmHour(timePicker.getCurrentHour())
                                .setAlarmMinutes(timePicker.getCurrentMinute());
                } else {
                    configurator.setAlarmHour(timePicker.getHour())
                                .setAlarmMinutes(timePicker.getMinute());
                }
                //create a new waking hour
                configurator.buildAlarmTime(configurator.getAlarmHour(), configurator.getAlarmMinutes());

                //create a new bed time hour based on waking hour
                int cyclesValue = savedConfiguration.getInt(configurator.getSleepCyclesKey(), 6);
                int asleepMinutesValue = savedConfiguration.getInt(configurator.getMinutesFallingAsleepKey(), 14);
                configurator.calcBedTime(configurator.getAlarmTime(), cyclesValue, asleepMinutesValue)
                            .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                            .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));
                configurator.setAlarmState(true);
                editor.apply();
                //update saved configuration file
                //the new alarm must be registered in the system here for it to ring
                //because the alarm switcher might be already checked so the listener won't record changes
                Alarm alarm = new Alarm(configurator.getAlarmTime(), DialogActivity.this, configurator.getRequestCode());
                alarm.register();
                configurator.setAlarmRegistrationMoment(Calendar.getInstance().getTimeInMillis());
                configurator.calcBedTimeTimeStamp(configurator.getAlarmTimeTimeStamp());
                saveChanges(null, -1);
            }
            finish();
        });
    }

    private void setNumberPicker(String[] displayedValues, int lastValuePosition){
        numberPicker = findViewById(R.id.number_picker_dialog);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayedValues.length - 1);
        numberPicker.setDisplayedValues(displayedValues);
        numberPicker.setVisibility(View.VISIBLE);
        numberPicker.setValue(lastValuePosition);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if(listenerType.equals(CYCLES_LISTENER)) {
                if (newVal == 0) {
                    setUnit(getResources().getString(R.string.cycle));
                } else if (oldVal == 0) {
                    setUnit(getResources().getString(R.string.cycles));
                }
            }
        });
    }

    private int getLastValue(){
        if(listenerType.equals(CYCLES_LISTENER)){
            return getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE).getInt(configurator.getSleepCyclesKey(), 5);
        } else if (listenerType.equals(ASLEEP_MINUTES_LISTENER)) {
            return getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE).getInt(configurator.getMinutesFallingAsleepKey(), 14);
        } else {
            return getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE).getInt(configurator.getNapDurationKey(), 20);
        }
    }

    private void setCyclePicker(){
        setTitle(getResources().getString(R.string.sleep_cycles));
        int lastValue = getLastValue();
        if(lastValue > 1) {
            setUnit(getResources().getString(R.string.cycles));
        } else {
            setUnit(getResources().getString(R.string.cycle));
        }
        int NUMBER_OF_VALUES = 9; //num of values in the picker
        String[] displayedValues  = new String[NUMBER_OF_VALUES];
        for(int i = 1; i <= NUMBER_OF_VALUES ; i++){
            displayedValues[i-1] = String.valueOf(i);
        }
        setNumberPicker(displayedValues, lastValue - 1);

        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(v -> {
            int currentValue = Integer.parseInt(numberPicker.getDisplayedValues()[numberPicker.getValue()]);
            configurator.setSleepCycles(currentValue);
            if(configurator == Configurator.wakeUpTimeKnownConf){
                //build and set bed time
                configurator.calcBedTime(configurator.getAlarmTime(), currentValue, configurator.getMinutesFallingAsleep())
                            .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                            .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE))
                            .setAlarmRegistrationMoment(Calendar.getInstance().getTimeInMillis());
                configurator.calcBedTimeTimeStamp(configurator.getAlarmTimeTimeStamp());
            } else if (configurator.getRequestCode() == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE) {
                //build the set alarm time
                configurator.calcAlarmTime(configurator.getBedTime(), currentValue, configurator.getMinutesFallingAsleep())
                            .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                            .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE))
                            .setAlarmRegistrationMoment(Calendar.getInstance().getTimeInMillis());
                Alarm alarm = new Alarm(configurator.getAlarmTime(), this, configurator.getRequestCode());
                alarm.register();
            }
            saveChanges(configurator.getSleepCyclesKey(), currentValue);
            finishAfterTransition();
        });
    }

    private void setAsleepPicker(){
        setTitle(getResources().getString(R.string.minutes_falling_asleep));
        setUnit(getResources().getString(R.string.minutes));
        int lastValue = getLastValue();
        int lastValuePosition = 0, positionInLoop = 0;
        boolean positionFound = false;
        ArrayList<String> arrayListValues = new ArrayList<>();
        for(int i = 5; i <= 50; i++){
            arrayListValues.add(String.valueOf(i));

            if(lastValue == i){
                lastValuePosition = positionInLoop;
                positionFound = true;
            } else if (!positionFound) {
                ++positionInLoop;
            }

            if(i>=15)
                i+=4;
        }

        String[] displayValues = arrayListValues.toArray(new String[0]);
        setNumberPicker(displayValues, lastValuePosition);

        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(v -> {
            int currentValue = Integer.parseInt(numberPicker.getDisplayedValues()[numberPicker.getValue()]);
            configurator.setMinutesFallingAsleep(currentValue);
            if(configurator == Configurator.wakeUpTimeKnownConf){
                configurator.calcBedTime(configurator.getAlarmTime(), configurator.getSleepCycles(), currentValue)
                            .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                            .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));
                configurator.calcBedTimeTimeStamp(configurator.getAlarmTimeTimeStamp());
            } else if (configurator == Configurator.bedTimeKnownConf) {
                //build the saved bed time
                configurator.calcAlarmTime(configurator.getBedTime(), configurator.getSleepCycles(), currentValue)
                            .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                            .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE))
                            .setAlarmRegistrationMoment(Calendar.getInstance().getTimeInMillis());
                Alarm alarm = new Alarm(configurator.getAlarmTime(), this, configurator.getRequestCode());
                alarm.register();
            }
            saveChanges(configurator.getMinutesFallingAsleepKey(), currentValue);
            finishAfterTransition();
        });
    }

    private void setNapDurationPicker(){
        setTitle(getResources().getString(R.string.nap_time_choose_duration));
        setUnit(getResources().getString(R.string.minutes));

        int NUMBER_OF_VALUES = 40; //number of values in the picker
        int lastValue = getLastValue();
        ArrayList<String> arrayListValues = new ArrayList<>();
        for(int i = 1; i <= NUMBER_OF_VALUES ; i++)
            arrayListValues.add(String.valueOf(i));

        String[] displayedValues  = arrayListValues.toArray(new String[0]);
        setNumberPicker(displayedValues, lastValue - 1);

        Button confirm = findViewById(R.id.change_dialog_button);
        confirm.setText(getResources().getString(R.string.confirm));
        confirm.setOnClickListener(view -> {
            int currentValue = Integer.parseInt(String.valueOf(displayedValues[numberPicker.getValue()]));
            configurator.setNapDuration(currentValue);
            if(configurator.isConfigured()) {
                configurator.calcAlarmTime(Calendar.getInstance(), currentValue)
                            .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                            .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));

                Alarm napAlarm = new Alarm(configurator.getAlarmTime(), this, configurator.getRequestCode());
                napAlarm.register();
                Notification.cancel(configurator.getRequestCode(), this);
                Notification notification = new Notification(this, Notification.NAP, configurator.getRequestCode());
                notification.trigger();
            } else {
                //start SongListActivity from here only if thee is no configuration done yet
                Intent i = new Intent(this, SongListActivity.class);
                i.putExtra(SongListActivity.CHECK_ALARM_TYPE, configurator.getRequestCode());
                startActivity(i);
            }
            configurator.setBedTimeTimeStamp(Calendar.getInstance().getTimeInMillis());
            saveChanges(configurator.getNapDurationKey(), currentValue);
            finishAfterTransition();
        });
    }

    private void saveChanges(String key, int value){
        SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedConfiguration.edit();
        if(key != null && value != -1) {
            editor.putInt(key, value);
            if (configurator == Configurator.wakeUpTimeKnownConf) {
                editor.putInt(Configurator.BED_HOUR_KNOWN_WAKE_UP_KEY, configurator.getBedHour())
                      .putInt(Configurator.BED_MINUTES_KNOWN_WAKE_UP_KEY, configurator.getBedMinutes());
            } else if (configurator == Configurator.bedTimeKnownConf) {
                editor.putInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME_KEY, configurator.getAlarmHour())
                      .putInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME_KEY, configurator.getAlarmMinutes());
            } else if (configurator == Configurator.napTimeConf) {
                editor.putInt(Configurator.ALARM_HOUR_NAP_TIME_KEY, configurator.getAlarmHour())
                      .putInt(Configurator.ALARM_MINUTES_NAP_TIME_KEY, configurator.getAlarmMinutes())
                      .putInt(Configurator.NAP_DURATION_KEY, configurator.getNapDuration())
                      .putLong(Configurator.START_NAP_TIME_STAMP_KEY, configurator.getBedTimeTimeStamp())
                      .putBoolean(Configurator.ALARM_STATE_NAP_TIME_KEY, true)
                      .putBoolean(Configurator.IS_NAP_TIME_CONFIGURED_KEY, true);
            }
        } else {
            editor.putInt(configurator.getBedHourKey(), configurator.getBedHour())
                  .putInt(configurator.getBedMinuteKey(), configurator.getBedMinutes())
                  .putInt(configurator.getAlarmHourKey(), configurator.getAlarmHour())
                  .putInt(configurator.getAlarmMinutesKey(), configurator.getAlarmMinutes())
                  .putBoolean(configurator.getAlarmStateKey(), true);
        }
        editor.putLong(configurator.getAlarmRegistrationMomentKey(), configurator.getAlarmRegistrationMoment())
                .putLong(configurator.getBedTimeTimeStampKey(), configurator.getBedTimeTimeStamp());
        editor.apply();
    }

    private void setTitle(String title){
        TextView textView = findViewById(R.id.title_dialog);
        textView.setText(title);
    }

    private void setUnit(String unit){
        TextView unitView = findViewById(R.id.unit);
        unitView.setVisibility(View.VISIBLE);
        if (!unitView.getText().equals(unit))
            unitView.setText(unit);
    }
}