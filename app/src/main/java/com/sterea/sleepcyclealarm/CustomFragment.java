package com.sterea.sleepcyclealarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Calendar;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public abstract class CustomFragment extends Fragment {
    SwitchMaterial alarmStateSwitch;
    Button setUpButton,
            removeAlarm;
    ConstraintLayout constraintLayout;
    TextView title,
            alarmStatus,
            totalSleepingTimeValue,
            timeInputValue,
            sleepCyclesValue,
            minutesAsleepValue,
            ringtoneName,
            ringtoneChange;
    Configurator configurator;


    public CustomFragment() {
        // Required empty public constructor
    }

    public CustomFragment(Configurator configurator){
        this.configurator = configurator;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.custom_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = getView().findViewById(R.id.fragment_title_textView);
        setUpButton = getView().findViewById(R.id.set_up_button);
        constraintLayout = getView().findViewById(R.id.constraint_configuration_layout);
        alarmStatus = constraintLayout.findViewById(R.id.alarm_status_text_view);
        alarmStateSwitch = constraintLayout.findViewById(R.id.alarmState_switch);
        totalSleepingTimeValue = constraintLayout.findViewById(R.id.total_sleeping_time_value_text_view);
        timeInputValue = constraintLayout.findViewById(R.id.bed_time_value_text_view);
        sleepCyclesValue = constraintLayout.findViewById(R.id.sleep_cycles_value_text_view);
        minutesAsleepValue = constraintLayout.findViewById(R.id.minutes_falling_asleep_value_text_view);
        ringtoneChange = constraintLayout.findViewById(R.id.ringtone_change_text_view);
        ringtoneName = constraintLayout.findViewById(R.id.ringtone_text_view);
        removeAlarm = constraintLayout.findViewById(R.id.remove_configuration_button);
        initTimeInputTextViewText();
        initListeners();

        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean isConfigured = savedConfiguration.getBoolean(configurator.getIsConfiguredKey(), false);
        updateUI(isConfigured);
    }

    void updateUI(boolean isConfigured){
        if(isConfigured){
            /*in this case the set up button shouldn't be visible anymore
            * so the resume appears, showing the alarm configuration*/
            setUpButton.setVisibility(View.GONE);
            constraintLayout.setVisibility(View.VISIBLE);

            //get saved configuration
            getConfiguration();

            //update UI based on configuration
            boolean alarmState = configurator.isAlarmOn();
            if(alarmState){
                alarmStateSwitch.setChecked(true);
                alarmStatus.setText(getContext().getResources().getString(R.string.alarmOn));
            } else {
                alarmStateSwitch.setChecked(false);
                alarmStatus.setText(getContext().getResources().getString(R.string.alarmOff));
            }

            title.setText(getTitleText(true));
            totalSleepingTimeValue.setText(getTotalSleepingTime(configurator.getSleepCycles()));
            timeInputValue.setText(getTimeInputValueText());
            sleepCyclesValue.setText(String.valueOf(configurator.getSleepCycles()));
            minutesAsleepValue.setText(String.valueOf(configurator.getMinutesFallingAsleep()));

            //set ringtone name text
            SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            String ringtoneName = savedConfiguration.getString(configurator.getRingtoneNameKey(), "Ceausescu");
            String ringtoneText = getResources().getString(R.string.ringtone) + "\n" + ringtoneName;
            this.ringtoneName.setText(ringtoneText);
        } else {
            ConstraintLayout cs = getView().findViewById(R.id.constraint_configuration_layout);
            cs.setVisibility(View.GONE);
            setUpButton = getView().findViewById(R.id.set_up_button);
            setUpButton.setVisibility(View.VISIBLE);
            setButtonIcon();

            TextView title = getView().findViewById(R.id.fragment_title_textView);
            title.setText(getTitleText(false));

            setUpButton.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), SetUpAlarmActivity.class);
                i.putExtra(SetUpAlarmActivity.CHECK_ALARM_TYPE, configurator.getRequestCode());
                startActivity(i);
            });
        }
    }

    void getConfiguration(){
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        int sleepCycles = savedConfiguration.getInt(configurator.getSleepCyclesKey(), 6);
        int minutesAsleep = savedConfiguration.getInt(configurator.getMinutesFallingAsleepKey(), 14);
        int alarmHour = savedConfiguration.getInt(configurator.getAlarmHourKey(), 0);
        int alarmMinutes = savedConfiguration.getInt(configurator.getAlarmMinutesKey(), 0);
        int bedHour = savedConfiguration.getInt(configurator.getBedHourKey(), 0);
        int bedMinutes = savedConfiguration.getInt(configurator.getBedMinuteKey(), 0);
        boolean alarmState = savedConfiguration.getBoolean(configurator.getAlarmStateKey(), false);

        configurator.setSleepCycles(sleepCycles)
                    .setMinutesFallingAsleep(minutesAsleep);
        configurator.setAlarmHour(alarmHour)
                    .setAlarmMinutes(alarmMinutes)
                    .buildAlarmTime(alarmHour, alarmMinutes);
        configurator.setBedHour(bedHour)
                    .setBedMinutes(bedMinutes)
                    .buildBedTime(bedHour, bedMinutes);
        configurator.setAlarmState(alarmState);
    }

    abstract void setAlarm();

    void cancelAlarm(){
        Alarm alarm = new Alarm(configurator.getAlarmTime(), getContext(), configurator.getRequestCode());
        alarm.cancel();

        SharedPreferences savedConfiguration = getContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, Context.MODE_PRIVATE);
        //get the waking time or build it from saved configuration file to cancel the alarm
        if (configurator.getAlarmTime() == null) {
            int hour = savedConfiguration.getInt(configurator.getAlarmHourKey(), 0);
            int minutes = savedConfiguration.getInt(configurator.getAlarmMinutesKey(), 0);
            configurator.buildAlarmTime(hour, minutes);
        }
        //switch the alarm state to off in configuration and save it
        configurator.setAlarmState(false);
        SharedPreferences.Editor editor = savedConfiguration.edit();
        editor.putBoolean(configurator.getAlarmStateKey(), false);
        editor.apply();
    }

    abstract void initTimeInputTextViewText();

    abstract String getTotalSleepingTime(int sleepCycles);

    abstract String getTitleText(boolean hasConfiguration);

    abstract String getTimeInputValueText();

    @UiThread
    abstract void setButtonIcon();

    void initListeners(){
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);

        sleepCyclesValue.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), DialogActivity.class);
            i.putExtra(DialogActivity.ALARM_TYPE, configurator.getRequestCode());
            i.putExtra(DialogActivity.LISTENER_TYPE, DialogActivity.CYCLES_LISTENER);
            startActivity(i);
        });

        minutesAsleepValue.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), DialogActivity.class);
            i.putExtra(DialogActivity.ALARM_TYPE, configurator.getRequestCode());
            i.putExtra(DialogActivity.LISTENER_TYPE, DialogActivity.ASLEEP_MINUTES_LISTENER);
            startActivity(i);
        });

        ringtoneChange.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), SongListActivity.class);
            i.putExtra(SongListActivity.CHECK_ALARM_TYPE, configurator.getRequestCode());
            startActivity(i);
        });

        removeAlarm.setOnClickListener(v -> {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            alertDialog.setMessage(R.string.removeDialog)
                    .setPositiveButton(R.string.positiveDialog, (dialog, which) -> {

                        //get the waking time or build it if needed from saved configuration file to cancel the alarm
                        if(configurator.getAlarmTime() == null) {
                            int hour = savedConfiguration.getInt(configurator.getAlarmHourKey(), 0);
                            int minutes = savedConfiguration.getInt(configurator.getAlarmMinutesKey(), 0);
                            configurator.buildAlarmTime(hour, minutes);
                        }
                        Calendar wakingTime = configurator.getAlarmTime();
                        Alarm alarm = new Alarm(wakingTime, getContext(), configurator.getRequestCode());
                        alarm.cancel();

                        configurator.setSleepCycles(6)
                                    .setItemPositionSpinnerCycles(5)
                                    .setMinutesFallingAsleep(14)
                                    .setItemPositionSpinnerMinutesAsleep(9)
                                    .setConfigured(false)
                                    .setAlarmState(false)
                                    .updateSavedConfiguration(savedConfiguration);
                        updateUI(false);
                        Notification.cancel(configurator.getRequestCode(), getContext());
                        Notification.stopRingtone();
                    })
                    .setNegativeButton(R.string.negativeDialog, (dialog, which) -> {
                        //Nothing to do here.
                    });
            alertDialog.show();
        });
    }

    @Override
    public void onResume() {
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean isConfigured = savedConfiguration.getBoolean(configurator.getIsConfiguredKey(), false);
        updateUI(isConfigured);
        super.onResume();
    }
}