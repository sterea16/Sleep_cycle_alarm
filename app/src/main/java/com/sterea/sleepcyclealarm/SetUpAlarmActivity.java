package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

public class SetUpAlarmActivity extends AppCompatActivity {
    private int cyclesValue = 7;
    private int cyclesPosition_spinner = 6;
    private int asleepMinutesValue = 14;
    private int asleepMinutesPosition_spinner = 9;
    private int alarmType;
    private Configurator configurator;

    static final String CHECK_ALARM_TYPE= SetUpAlarmActivity.class.getSimpleName();

    private void setConfigurator(int alarmType){
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
        setContentView(R.layout.activity_set_up_alarm_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        alarmType = getIntent().getExtras().getInt(CHECK_ALARM_TYPE);
        setConfigurator(alarmType);
        /*create arrays to populate the spinners*/
        ArrayList<Integer> sleepCyclesArray = new ArrayList<>();
        for(int i = 1; i <=9 ; i++){
            sleepCyclesArray.add(i);
        }
        ArrayList<Integer> minutesAsleepArray = new ArrayList<>();
        for(int i = 5; i <= 50; i++){
            minutesAsleepArray.add(i);
            if(i>=15)
                i+=4;
        }

        /*setting up the adaptors and the spinners */
        Spinner sleepCyclesSpinner = findViewById(R.id.spinner_numberSleepCycles);
        ArrayAdapter<Integer> sleepCyclesAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, sleepCyclesArray);
        sleepCyclesSpinner.setAdapter(sleepCyclesAdapter);
        setUpSpinners(sleepCyclesSpinner, sleepCyclesArray, R.id.spinner_numberSleepCycles);

        Spinner minutesAsleepSpinner = findViewById(R.id.spinner_minutesOfFallingAsleep);
        ArrayAdapter<Integer> minutesAsleepAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, minutesAsleepArray);
        minutesAsleepSpinner.setAdapter(minutesAsleepAdapter);
        setUpSpinners(minutesAsleepSpinner, minutesAsleepArray, R.id.spinner_minutesOfFallingAsleep);

        Button browse = findViewById(R.id.browse);
        browse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SetUpAlarmActivity.this, SongListActivity.class);
                i.putExtra(SongListActivity.CHECK_ALARM_TYPE, alarmType);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        final TimePicker timePicker = findViewById(R.id.spinner_time_picker);
        if(DateFormat.is24HourFormat(this)){
            timePicker.setIs24HourView(true);
        } else {
            timePicker.setIs24HourView(false);
        }

        if(alarmType == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE)
            timePicker.setVisibility(View.GONE);

        /*Saving the user alarm configuration by storing the data in a SharedPreferences object*/
        //TODO on first time use this button is CHANGE instead of CREATE so need to be fixed
        Button create = findViewById(R.id.create_alarm_wake_up_time);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO create warnings for few sleep cycles during night time
                //take the time from time picker
                if(alarmType == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        configurator.setAlarmHour(timePicker.getCurrentHour())
                                    .setAlarmMinutes(timePicker.getCurrentMinute());
                    } else {
                        configurator.setAlarmHour(timePicker.getHour())
                                    .setAlarmMinutes(timePicker.getMinute());
                    }

                    configurator.buildAlarmTime(configurator.getAlarmHour(), Configurator.wakeUpTimeKnownConf.getAlarmMinutes())
                                .calcBedTime(configurator.getAlarmTime(), cyclesValue, asleepMinutesValue)
                                .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                                .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));

                } else if (alarmType == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE){
                    Calendar currentTime = Calendar.getInstance();
                    configurator.setBedTime(currentTime)
                                .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                                .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));

                    configurator.calcAlarmTime(configurator.getBedTime(), cyclesValue, asleepMinutesValue)
                                .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                                .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));
                }

                SharedPreferences savedConfiguration = getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);

                configurator.setSleepCycles(cyclesValue)
                            .setItemPositionSpinnerCycles(cyclesPosition_spinner)
                            .setMinutesFallingAsleep(asleepMinutesValue)
                            .setItemPositionSpinnerMinutesAsleep(asleepMinutesPosition_spinner)
                            .setConfChanged(true)
                            .setConfigured(true)
                            .setAlarmState(true)
                            .updateSavedConfiguration(savedConfiguration, configurator.getRequestCode());

                Alarm alarm = new Alarm(configurator.getAlarmTime(), SetUpAlarmActivity.this, configurator.getRequestCode());
                alarm.registerAlarm();
                finish();
            }
        });

        Button cancel = findViewById(R.id.cancel_alarm_set_up_time);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(configurator.getRingtoneName() != null)
                    configurator.setRingtoneName(null);
                configurator.setConfChanged(false);
                finish();
            }
        });

        SharedPreferences savedConfiguration = getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean isConfigured = savedConfiguration.getBoolean(configurator.getIsConfiguredKey(), false);
        TextView title = findViewById(R.id.WakeUpTimeInfo_textView);
        title.setVisibility(View.GONE);

        if (isConfigured) {
            create.setText(getResources().getString(R.string.change));
            sleepCyclesSpinner.setSelection(savedConfiguration.getInt(configurator.getItemPositionSpinnerCyclesKey(), 0));
            minutesAsleepSpinner.setSelection(savedConfiguration.getInt(configurator.getItemPositionSpinnerMinutesAsleepKey(), 0));

            if(alarmType == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE) {
                title.setVisibility(View.VISIBLE);
                title.setText(getResources().getString(R.string.wakingUpTime));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    timePicker.setCurrentHour(savedConfiguration.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, 0));
                    timePicker.setCurrentMinute(savedConfiguration.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, 0));
                } else {
                    timePicker.setHour(savedConfiguration.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, 0));
                    timePicker.setMinute(savedConfiguration.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, 0));
                }
            }
            if(configurator.getRingtoneName() == null)
                configurator.setRingtoneName(savedConfiguration.getString(configurator.getRingtoneNameKey(), getResources().getString(R.string.noRingtoneSelected)));

            updateSongView();
            return;
        }

        create.setText(getResources().getString(R.string.createAlarm));
        title = findViewById(R.id.WakeUpTimeInfo_textView);
        title.setText(getResources().getString(R.string.selectWakeUpTime));

        updateSongView();

        // checking for the first time usage of the app
        final boolean firstUse = savedConfiguration.getBoolean(Configurator.FIRST_TIME_SET_UP,true);
        if(firstUse){
            showTips();
        }

    }

    private void updateSongView(){
        TextView chosenSongName = findViewById(R.id.songNameChosen_textView);
        if(configurator.getRingtoneName() != null) {
            chosenSongName.setText(configurator.getRingtoneName());
        } else {
            chosenSongName.setText(R.string.noRingtoneSelected);
        }
    }

    private void setUpSpinners(final Spinner spinner, final ArrayList<Integer> listValues, final int spinnerId){
        switch (spinnerId){
            case R.id.spinner_numberSleepCycles:
                spinner.setSelection(configurator.getItemPositionSpinnerCycles());
                break;
            case R.id.spinner_minutesOfFallingAsleep:
                spinner.setSelection(configurator.getItemPositionSpinnerMinutesAsleep());
                break;
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Integer numericValue = Integer.parseInt(spinner.getSelectedItem().toString());
                for(Integer i : listValues){
                    if(i.equals(numericValue)){
                        switch (spinnerId){
                            case R.id.spinner_numberSleepCycles:
                                cyclesValue = i;
                                cyclesPosition_spinner = position;
                                break;
                            case R.id.spinner_minutesOfFallingAsleep:
                                asleepMinutesValue = i;
                                asleepMinutesPosition_spinner = position;
                                break;
                        }
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                switch (spinnerId){
                    case R.id.spinner_numberSleepCycles:
                        spinner.setSelection(configurator.getItemPositionSpinnerCycles());
                        break;
                    case R.id.spinner_minutesOfFallingAsleep:
                        spinner.setSelection(configurator.getItemPositionSpinnerMinutesAsleep());
                        break;
                }
            }
        });
    }

    //if the app is used for the first time this method runs
    //TODO create a new layout for the dialog explaining how to properly set up the alarm
    private void showTips(){
        AlertDialog.Builder tips = new AlertDialog.Builder(this);
        tips.setTitle(R.string.alert_dialog_getting_started_title).setMessage(R.string.alert_dialog_getting_started_message)
                .setPositiveButton(R.string.alert_dialog_getting_started_positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        tips.show();
        SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedConfiguration.edit();
        editor.putBoolean(Configurator.FIRST_TIME_SET_UP, false);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSongView();
    }

}
