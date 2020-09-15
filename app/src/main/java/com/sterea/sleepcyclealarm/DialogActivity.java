package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
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
    Configurator configurator;

    private void setConfigurator(int alarmType) {
        if (alarmType == Configurator.wakeUpTimeKnownConf.getRequestCode()){
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if(alarmType == Configurator.bedTimeKnownConf.getRequestCode()){
            configurator = Configurator.bedTimeKnownConf;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int alarmType = getIntent().getExtras().getInt(ALARM_TYPE);
        setConfigurator(alarmType);

        String listenerType = getIntent().getExtras().getString(LISTENER_TYPE);
        if (listenerType.equals(TIME_LISTENER)){
            setTimePicker();
        } else if (listenerType.equals(CYCLES_LISTENER)){
            setCyclePicker();
        } else if (listenerType.equals(ASLEEP_MINUTES_LISTENER)){
            setAsleepPicker();
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
        if(DateFormat.is24HourFormat(this)){
            timePicker.setIs24HourView(true);
        } else {
            timePicker.setIs24HourView(false);
        }
        timePicker.setVisibility(View.VISIBLE);
        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(v -> {
            SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            SharedPreferences.Editor editor = savedConfiguration.edit();
            if (configurator == Configurator.wakeUpTimeKnownConf){
                //get the time from time picker
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
                //update saved configuration file
                saveChanges(null, -1);
                //the new alarm must be registered in the system here for it to ring
                //because the alarm switcher might be already checked so the listener won't record changes
                Alarm alarm = new Alarm(configurator.getAlarmTime(), DialogActivity.this, configurator.getRequestCode());
                alarm.register();

            } /*else if (alarmType == 2) {
                //get the time from time picker
                int hour, minutes;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    hour = timePicker.getCurrentHour();
                    minutes = timePicker.getCurrentMinute();
                } else {
                    hour = timePicker.getHour();
                    minutes = timePicker.getMinute();
                }

                //create a new bed time and notice the changes
                configurator.buildBedTime(hour, minutes);
                configurator.setConfChanged(true);

                //create a new waking time
                int cyclesValue = savedConfiguration.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_BED_TIME, 6);
                int asleepMinutesValue = savedConfiguration.getInt(Configurator.ASLEEP_INT_VALUE_KNOWN_BED_TIME, 14);
                configurator.calculateWakingTime(configurator.getBedTime(), cyclesValue, asleepMinutesValue);
                configurator.setHour(configurator.getHour());
                configurator.setMinutes(configurator.getMinutes());

                //update saved configuration file
                editor.putInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME, configurator.getHour());
                editor.putInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME, configurator.getMinutes());
            }*/
            editor.apply();
            finish();
        });
    }

    private void setCyclePicker(){
        setTitle(getResources().getString(R.string.sleep_cycles));
        int NUMBER_OF_VALUES = 9; //num of values in the picker
        String[] displayedValues  = new String[NUMBER_OF_VALUES];
        //Populate the array
        for(int i = 1; i <= NUMBER_OF_VALUES ; i++){
            displayedValues[i-1] = String.valueOf(i);
        }

        NumberPicker numberPicker = findViewById(R.id.number_picker_dialog);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayedValues.length - 1);
        numberPicker.setDisplayedValues(displayedValues);
        numberPicker.setVisibility(View.VISIBLE);

        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(v -> {
            int currentValue = Integer.parseInt(numberPicker.getDisplayedValues()[numberPicker.getValue()]);
            configurator.setSleepCycles(currentValue);
            if(configurator == Configurator.wakeUpTimeKnownConf){
                //build and set bed time
                configurator.calcBedTime(configurator.getAlarmTime(), currentValue, configurator.getMinutesFallingAsleep())
                            .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                            .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));

            } else if (configurator.getRequestCode() == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE) {
                //build the set alarm time
                configurator.calcAlarmTime(configurator.getBedTime(), currentValue, configurator.getMinutesFallingAsleep())
                            .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                            .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));
            }
            saveChanges(configurator.getSleepCyclesKey(), currentValue);
            finish();
        });
    }

    private void setAsleepPicker(){
        setTitle(getResources().getString(R.string.minutes_falling_asleep));

        ArrayList<String> arrayListValues = new ArrayList<>();
        for(int i = 5; i <= 50; i++){
            arrayListValues.add(String.valueOf(i));
            if(i>=15)
                i+=4;
        }
        String[] displayValues = arrayListValues.toArray(new String[arrayListValues.size()]);
        NumberPicker numberPicker = findViewById(R.id.number_picker_dialog);
        numberPicker.setDisplayedValues(displayValues);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayValues.length - 1);
        numberPicker.setVisibility(View.VISIBLE);

        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(v -> {
            int currentValue = Integer.parseInt(numberPicker.getDisplayedValues()[numberPicker.getValue()]);
            configurator.setMinutesFallingAsleep(currentValue);
            if(configurator == Configurator.wakeUpTimeKnownConf){
                configurator.calcBedTime(configurator.getAlarmTime(), configurator.getSleepCycles(), currentValue)
                            .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                            .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));

            } else if (configurator == Configurator.bedTimeKnownConf) {
                //build the saved bed time
                configurator.calcAlarmTime(configurator.getBedTime(), configurator.getSleepCycles(), currentValue)
                            .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                            .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));
            }
            saveChanges(configurator.getMinutesFallingAsleepKey(), currentValue);
            finish();
        });
    }

    private void saveChanges(String key, int value){
        SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedConfiguration.edit();
        if(key != null & value != -1) {
            editor.putInt(key, value);
            if (configurator == Configurator.wakeUpTimeKnownConf) {
                editor.putInt(Configurator.BED_HOUR_KNOWN_WAKE_UP_KEY, configurator.getBedHour())
                        .putInt(Configurator.BED_MINUTES_KNOWN_WAKE_UP_KEY, configurator.getBedMinutes());
            } else if (configurator == Configurator.bedTimeKnownConf) {
                editor.putInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME_KEY, configurator.getAlarmHour())
                        .putInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME_KEY, configurator.getAlarmMinutes());
            }
        } else {
            editor.putInt(configurator.getBedHourKey(), configurator.getBedHour())
                  .putInt(configurator.getBedMinuteKey(), configurator.getBedMinutes())
                  .putInt(configurator.getAlarmHourKey(), configurator.getAlarmHour())
                  .putInt(configurator.getAlarmMinutesKey(), configurator.getAlarmMinutes())
                  .putBoolean(configurator.getAlarmStateKey(), true);
        }
        editor.apply();
    }

    private void setTitle(String title){
        TextView textView = findViewById(R.id.title_dialog);
        textView.setText(title);
    }
}