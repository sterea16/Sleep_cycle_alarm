package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
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

public class SetUpAlarmActivity extends AppCompatActivity {
    private int cyclesValue = 7;
    private int cyclesPosition_spinner = 6;
    private int asleepMinutesValue = 14;
    private int asleepMinutesPosition_spinner = 9;
    private final String TAG = SetUpAlarmActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_up_alarm_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

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

        /*setting up the spinners and the adaptors*/
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
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        Button cancel = findViewById(R.id.cancel_alarm_set_up_time);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(Configurator.knownWakeUpTimeConf.getRingtoneName() != null)
                    Configurator.knownWakeUpTimeConf.setRingtoneName(null);
                Configurator.knownWakeUpTimeConf.setConfChanged(false);
                finish();
            }
        });

        final TimePicker timePicker = findViewById(R.id.spinner_time_picker);
        if(DateFormat.is24HourFormat(this)){
            timePicker.setIs24HourView(true);
        } else {
            timePicker.setIs24HourView(false);
        }

        /*Saving the user alarm configuration by storing the data in a SharedPreferences object*/
        Button create = findViewById(R.id.create_alarm_wake_up_time);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO create warnings for few sleep cycles during night time
                if(Build.VERSION.SDK_INT < 23){
                    Configurator.knownWakeUpTimeConf.setHour(timePicker.getCurrentHour());
                    Configurator.knownWakeUpTimeConf.setMinutes(timePicker.getCurrentMinute());
                } else {
                    Configurator.knownWakeUpTimeConf.setHour(timePicker.getHour());
                    Configurator.knownWakeUpTimeConf.setMinutes(timePicker.getMinute());
                }
                /*if the user forgets to choose a song, pick the last one he choose*/
                SharedPreferences savedPreferences = getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);

                Configurator.knownWakeUpTimeConf.setSleepCycles(cyclesValue);
                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerCycles(cyclesPosition_spinner);
                Configurator.knownWakeUpTimeConf.setMinutesFallingAsleep(asleepMinutesValue);
                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerMinutesAsleep(asleepMinutesPosition_spinner);
                Configurator.knownWakeUpTimeConf.setWakeUpTime(Configurator.knownWakeUpTimeConf.getHour(), Configurator.knownWakeUpTimeConf.getMinutes());
                Configurator.knownWakeUpTimeConf.setBedTime(Configurator.knownWakeUpTimeConf.getWakeUpTime(), cyclesValue, asleepMinutesValue);
                Configurator.knownWakeUpTimeConf.setConfChanged(true);
                Configurator.knownWakeUpTimeConf.setConfigured(true);
                Configurator.knownWakeUpTimeConf.setAlarmState(true);

                Configurator.knownWakeUpTimeConf.updateSharedConfiguration(savedPreferences);
                finish();
            }
        });

        SharedPreferences savedPreferences = getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean isConfigured = savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false);
        TextView title = findViewById(R.id.WakeUpTimeInfo_textView);

        if (isConfigured) {
            title.setText(getResources().getString(R.string.wakingUpTime));
            create.setText(getResources().getString(R.string.changeAlarm));
            sleepCyclesSpinner.setSelection(savedPreferences.getInt(Configurator.CYCLES_POSITION_SPINNER, 0));
            minutesAsleepSpinner.setSelection(savedPreferences.getInt(Configurator.ASLEEP_POSITION_SPINNER, 0));

            if(Build.VERSION.SDK_INT < 23){
                timePicker.setCurrentHour(savedPreferences.getInt(Configurator.HOUR_KNOWN_WAKE_UP, 0));
                timePicker.setCurrentMinute(savedPreferences.getInt(Configurator.MINUTES_KNOWN_WAKE_UP, 0));
            } else {
                timePicker.setHour(savedPreferences.getInt(Configurator.HOUR_KNOWN_WAKE_UP, 0));
                timePicker.setMinute(savedPreferences.getInt(Configurator.MINUTES_KNOWN_WAKE_UP, 0));
            }
            if(Configurator.knownWakeUpTimeConf.getRingtoneName() == null)
                Configurator.knownWakeUpTimeConf.setRingtoneName(savedPreferences.getString(Configurator.SONG_NAME_KNOWN_WAKE_UP, getResources().getString(R.string.noRingtoneSelected)));

            updateSongView();
            return;
        }

        create.setText(getResources().getString(R.string.createAlarm));
        title = findViewById(R.id.WakeUpTimeInfo_textView);
        title.setText(getResources().getString(R.string.selectWakeUpTime));

        updateSongView();

        // checking for the first time usage of the app
        final boolean firstUse = savedPreferences.getBoolean(Configurator.FIRST_TIME_SET_UP,true);
        if(firstUse){
            showTips();
        }

    }

    private void updateSongView(){
        TextView chosenSongName = findViewById(R.id.songNameChosen_textView);
        if(Configurator.knownWakeUpTimeConf.getRingtoneName() != null) {
            chosenSongName.setText(Configurator.knownWakeUpTimeConf.getRingtoneName());
        } else {
            chosenSongName.setText(R.string.noRingtoneSelected);
        }
    }

    private void setUpSpinners(final Spinner spinner, final ArrayList<Integer> listValues, final int spinnerId){
        switch (spinnerId){
            case R.id.spinner_numberSleepCycles:
                spinner.setSelection(Configurator.knownWakeUpTimeConf.getItemPositionSpinnerCycles());
                break;
            case R.id.spinner_minutesOfFallingAsleep:
                spinner.setSelection(Configurator.knownWakeUpTimeConf.getItemPositionSpinnerMinutesAsleep());
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
                        spinner.setSelection(Configurator.knownWakeUpTimeConf.getItemPositionSpinnerCycles());
                        break;
                    case R.id.spinner_minutesOfFallingAsleep:
                        spinner.setSelection(Configurator.knownWakeUpTimeConf.getItemPositionSpinnerMinutesAsleep());
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
        SharedPreferences sharedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Configurator.FIRST_TIME_SET_UP, false);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSongView();
    }

}
