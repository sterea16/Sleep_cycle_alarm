package com.sterea.sleepcyclealarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class BedTimeFragment extends Fragment {
    private SwitchMaterial bedTimeSwitch;
    private Button setUpButton,
                    removeButton;
    private ConstraintLayout cs;
    private TextView title,
                    alarmStatus,
            totalSleepingTimeValue,
                    bedTimeValue,
                    sleepCyclesValue,
                    minutesAsleepValue,
                    ringtoneName,
                    ringtoneChange;
    private Configurator configurator = Configurator.bedTimeKnownConf;


    public BedTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bed_time_known, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = getView().findViewById(R.id.knownBedTime_textView);
        setUpButton = getView().findViewById(R.id.set_up_button);
        cs = getView().findViewById(R.id.constraint_configuration_layout);
        alarmStatus = cs.findViewById(R.id.alarm_status_text_view);
        bedTimeSwitch = cs.findViewById(R.id.bedTime_switch);
        totalSleepingTimeValue = cs.findViewById(R.id.total_sleeping_time_value_text_view);
        bedTimeValue = cs.findViewById(R.id.bed_time_value_text_view);
        sleepCyclesValue = cs.findViewById(R.id.sleep_cycles_value_text_view);
        minutesAsleepValue = cs.findViewById(R.id.minutes_falling_asleep_value_text_view);
        ringtoneChange = cs.findViewById(R.id.ringtone_change_text_view);
        ringtoneName = cs.findViewById(R.id.ringtone_text_view);
        removeButton = cs.findViewById(R.id.remove_bed_time_button);
        setUpListeners();

        SharedPreferences savedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean isConfigured = savedPreferences.getBoolean(Configurator.IS_BED_TIME_KNOWN_CONFIGURED, false);
        updateUI(isConfigured);
    }

    private void updateUI(boolean isConfigured){
        if(isConfigured){
            /*in this case the set up button shouldn't be visible anymore
            * so the resume appears, showing the alarm configuration*/
            setUpButton.setVisibility(View.GONE);
            cs.setVisibility(View.VISIBLE);

            //get saved configuration
            setUpConfigurator();

            //update UI based on configuration
            boolean alarmState = configurator.getAlarmState();
            if(alarmState){
                alarmStatus.setText(getResources().getString(R.string.alarmOn));
                alarmStatus.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                bedTimeSwitch.setChecked(true);
                Alarm alarm = new Alarm(configurator.getAlarmTime(), getContext(), configurator.getRequestCode());
                alarm.registerAlarm();
            } else {
                alarmStatus.setText(getResources().getString(R.string.alarmOff));
                alarmStatus.setTextColor(getDefTextColor());
                bedTimeSwitch.setChecked(false);
                Alarm alarm = new Alarm(configurator.getAlarmTime(), getContext(), configurator.getRequestCode());
                alarm.cancelAlarm();
            }

            Calendar AlarmTime = configurator.getAlarmTime();
            String alarmTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(AlarmTime.getTime());
            String titleText = getResources().getString(R.string.calcWakingTime) + "\n" + alarmTimeText;
            title.setText(titleText);

            totalSleepingTimeValue.setText(getTotalSleepingTime(configurator.getSleepCycles()));

            Calendar bedTime = configurator.getBedTime();
            String bedTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(bedTime.getTime());
            bedTimeValue.setText(bedTimeText);

            sleepCyclesValue.setText(String.valueOf(configurator.getSleepCycles()));
            minutesAsleepValue.setText(String.valueOf(configurator.getMinutesFallingAsleep()));

            SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            String ringtoneName = savedConfiguration.getString(Configurator.RINGTONE_NAME_KNOWN_BED_TIME_KEY, "Ceausescu");
            String ringtoneText = getResources().getString(R.string.ringtone) + "\n" + ringtoneName;
            this.ringtoneName.setText(ringtoneText);
        } else {
            ConstraintLayout cs = getView().findViewById(R.id.constraint_configuration_layout);
            cs.setVisibility(View.GONE);
            setUpButton = getView().findViewById(R.id.set_up_button);
            setUpButton.setVisibility(View.VISIBLE);
            TextView title = getView().findViewById(R.id.knownBedTime_textView);
            title.setText(getResources().getString(R.string.knownBedTimeText));

            setUpButton.setOnClickListener(v -> {
                Intent i = new Intent(getContext(), SetUpAlarmActivity.class);
                i.putExtra(SetUpAlarmActivity.CHECK_ALARM_TYPE, configurator.getRequestCode());
                startActivity(i);
            });
        }
    }

    private void setUpConfigurator(){
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        int sleepCycles = savedConfiguration.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_BED_TIME_KEY, 6);
        int minutesAsleep = savedConfiguration.getInt(Configurator.ASLEEP_INT_VALUE_KNOWN_BED_TIME_KEY, 14);
        int alarmHour = savedConfiguration.getInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME, 0);
        int alarmMinutes = savedConfiguration.getInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME, 0);
        int bedHour = savedConfiguration.getInt(Configurator.BED_HOUR_KNOWN_BED_TIME_KEY, 0);
        int bedMinutes = savedConfiguration.getInt(Configurator.BED_MINUTES_KNOWN_BED_TIME_KEY, 0);
        boolean alarmState = savedConfiguration.getBoolean(Configurator.ALARM_STATE_KNOWN_BED_TIME_KEY, false);
        configurator.setSleepCycles(sleepCycles)
                    .setMinutesFallingAsleep(minutesAsleep)
                    .setAlarmHour(alarmHour)
                    .setAlarmMinutes(alarmMinutes)
                    .buildAlarmTime(alarmHour, alarmMinutes)
                    .setBedHour(bedHour)
                    .setBedMinutes(bedMinutes);
        configurator.buildBedTime(bedHour, bedMinutes)
                    .setAlarmState(alarmState);
    }

    private int getDefTextColor(){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getActivity().getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        TypedArray arr = getActivity().obtainStyledAttributes(typedValue.data, new int[]{
                android.R.attr.textColorPrimary});
        arr.recycle();
        return arr.getColor(0, -1);
    }

    private String getTotalSleepingTime(int sleepCycles){
        int totalMinutes = sleepCycles * 90;
        int totalHours = totalMinutes/60;
        int leftoverMinutes = totalMinutes % 60;
        if(leftoverMinutes != 0){
            return totalHours + " h " + leftoverMinutes + " m";
        } else {
            return totalHours + " h";
        }
    }

    private void setUpListeners(){
        //TODO set up listener for text views of cs layout
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);

        bedTimeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("bedTimeSwitch", "isChecked " + isChecked);
                if (isChecked) {
                    //create a new Calendar object based on the saved cycles and minutes falling asleep values
                    Calendar currentTime = Calendar.getInstance();
                    configurator.setBedTime(currentTime)
                                .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                                .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));

                    configurator.calcAlarmTime(configurator.getBedTime(), configurator.getSleepCycles(), configurator.getMinutesFallingAsleep())
                                .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                                .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));

                    //register an alarm in the system with the new calendar object
                    configurator.setAlarmState(true);

                    //save the new alarm in the shared preferences file
                    SharedPreferences.Editor editor = savedConfiguration.edit();
                    editor.putInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME, configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                            .putInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME, configurator.getAlarmTime().get(Calendar.MINUTE))
                            .putInt(Configurator.BED_HOUR_KNOWN_BED_TIME_KEY, configurator.getBedHour())
                            .putInt(Configurator.BED_MINUTES_KNOWN_BED_TIME_KEY, configurator.getBedMinutes())
                            .putBoolean(Configurator.ALARM_STATE_KNOWN_BED_TIME_KEY, true)
                            .apply();

                } else {
                    //get the waking time or build it from saved configuration file to cancel the alarm
                    if (configurator.getAlarmTime() == null) {
                        int hour = savedConfiguration.getInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME, 0);
                        int minutes = savedConfiguration.getInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME, 0);
                        configurator.buildAlarmTime(hour, minutes);
                    }

                    //switch the alarm state to off in configuration and save it
                    configurator.setAlarmState(false);
                    SharedPreferences.Editor editor = savedConfiguration.edit();
                    editor.putBoolean(Configurator.ALARM_STATE_KNOWN_BED_TIME_KEY, false);
                    editor.apply();
                }
            //update the UI
            updateUI(true);

        });

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

        ringtoneChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SongListActivity.class);
                i.putExtra(SongListActivity.CHECK_ALARM_TYPE, configurator.getRequestCode());
                startActivity(i);
            }
        });

        removeButton.setOnClickListener(v -> {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            alertDialog.setMessage(R.string.removeDialog)
                    .setPositiveButton(R.string.positiveDialog, (dialog, which) -> {

                        //get the waking time or build it if needed from saved configuration file to cancel the alarm
                        if(configurator.getAlarmTime() == null) {
                            int hour = savedConfiguration.getInt(Configurator.ALARM_HOUR_KNOWN_BED_TIME, 0);
                            int minutes = savedConfiguration.getInt(Configurator.ALARM_MINUTES_KNOWN_BED_TIME, 0);
                            configurator.buildAlarmTime(hour, minutes);
                        }
                        Calendar wakingTime = configurator.getAlarmTime();
                        Alarm alarm = new Alarm(wakingTime, getContext(), configurator.getRequestCode());
                        alarm.cancelAlarm();

                        configurator.setSleepCycles(6)
                                    .setItemPositionSpinnerCycles(5)
                                    .setMinutesFallingAsleep(14)
                                    .setItemPositionSpinnerMinutesAsleep(9)
                                    .setConfigured(false)
                                    .setAlarmState(false)
                                    .updateSavedConfiguration(savedConfiguration, configurator.getRequestCode());
                        updateUI(false);
                    })
                    .setNegativeButton(R.string.negativeDialog, (dialog, which) -> {
                        //Nothing to do here.
                    });
            alertDialog.show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean isConfigured = savedConfiguration.getBoolean(Configurator.IS_BED_TIME_KNOWN_CONFIGURED, false);
        updateUI(isConfigured);
    }
}