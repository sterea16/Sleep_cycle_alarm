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
    final static String RINGTONE_LISTENER = DialogActivity.class.getSimpleName() + " RINGTONE LISTENER ";
    Configurator configurator;
    private int alarmType;

    private void setConfigurator(int alarmType) {
        if (alarmType == 1){
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if(alarmType == 2){
            configurator = Configurator.bedTimeKnownConf;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        alarmType = getIntent().getExtras().getInt(ALARM_TYPE);
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
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setTimePicker(){
        if(configurator.getRequestCode() == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE){
            setTitle(getResources().getString(R.string.choose_waking_time));
        } else if (configurator.getRequestCode() == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE) {
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
            if (alarmType == 1){
                //get the time from time picker
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    configurator.setAlarmHour(timePicker.getCurrentHour())
                                .setAlarmMinutes(timePicker.getCurrentMinute());
                } else {
                    configurator.setAlarmHour(timePicker.getHour())
                                .setAlarmMinutes(timePicker.getMinute());
                }
                //create a new waking hour
                configurator.buildAlarmTime(Configurator.wakeUpTimeKnownConf.getAlarmHour(), Configurator.wakeUpTimeKnownConf.getAlarmMinutes());

                //create a new bed time hour based on waking hour
                int cyclesValue = savedConfiguration.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY, 6);
                int asleepMinutesValue = savedConfiguration.getInt(Configurator.ASLEEP_INT_VALUE_KNOWN_WAKE_UP_KEY, 14);
                configurator.calcBedTime(Configurator.wakeUpTimeKnownConf.getAlarmTime(), cyclesValue, asleepMinutesValue)
                        .setConfChanged(true);

                //update saved configuration file
                editor.putInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, configurator.getAlarmHour())
                        .putInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, configurator.getAlarmMinutes());

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

        SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);

        NumberPicker numberPicker = findViewById(R.id.number_picker_dialog);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(displayedValues.length - 1);
        numberPicker.setDisplayedValues(displayedValues);
        numberPicker.setVisibility(View.VISIBLE);

        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(numberPicker.getDisplayedValues()[numberPicker.getValue()]);
                configurator.setSleepCycles(currentValue);
                if(configurator.getRequestCode() == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE){
                    //TODO code for known wake up scenario here; requires constructor update, getter, setter for alarmHour and alarmMinutes

                } else if (configurator.getRequestCode() == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE) {
                    //build the saved bed time
                    configurator.setSleepCycles(currentValue)
                                .calcAlarmTime(configurator.getBedTime(), currentValue, configurator.getMinutesFallingAsleep())
                                .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                                .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));

                    SharedPreferences.Editor editor = savedConfiguration.edit();
                    editor.putInt(configurator.getSleepCyclesKey(), configurator.getSleepCycles())
                            .putInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME, configurator.getAlarmHour())
                            .putInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME, configurator.getAlarmMinutes())
                            .apply();
                }
                finish();
            }
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

        SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);

        Button change = findViewById(R.id.change_dialog_button);
        change.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int currentValue = Integer.parseInt(numberPicker.getDisplayedValues()[numberPicker.getValue()]);
                configurator.setMinutesFallingAsleep(currentValue);
                if(configurator.getRequestCode() == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE){
                    //TODO code for known wake up scenario here; requires constructor update, getter, setter for alarmHour and alarmMinutes

                } else if (configurator.getRequestCode() == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE) {
                    //build the saved bed time
                    configurator.setMinutesFallingAsleep(currentValue)
                                .calcAlarmTime(configurator.getBedTime(), configurator.getSleepCycles(), currentValue)
                                .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                                .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));

                    SharedPreferences.Editor editor = savedConfiguration.edit();
                    editor.putInt(configurator.getMinutesFallingAsleepKey(), configurator.getMinutesFallingAsleep())
                            .putInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME, configurator.getAlarmHour())
                            .putInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME, configurator.getAlarmMinutes())
                            .apply();
                }
                finish();
            }
        });
    }

    private void setTitle(String title){
        TextView textView = findViewById(R.id.title_dialog);
        textView.setText(title);
    }
}