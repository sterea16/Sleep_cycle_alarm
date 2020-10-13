package com.sterea.sleepcyclealarm;

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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class NapTimeFragment extends Fragment {

    private TextView titleTextView;
    private TextView endTimeTextView;
    private TextView endTimeValueTextView;
    private TextView durationTextView;
    private TextView durationValueTextView;
    private TextView ringtoneName;
    private TextView ringtoneChange;
    private Button napTimeButton, removeButton;
    private View separator0, separator1, separator2, separator3;

    ConstraintLayout cs;
    private Configurator configurator = Configurator.napTimeConf;

    public NapTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nap_time_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getConfiguration();
        cs = getView().findViewById(R.id.constraint_layout_nap_time);
        displayViews(configurator.isConfigured());
    }

    private void getConfiguration(){
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        int alarmHour = savedConfiguration.getInt(configurator.getAlarmHourKey(), 0);
        int alarmMinutes = savedConfiguration.getInt(configurator.getAlarmMinutesKey(), 0);
        int napDuration = savedConfiguration.getInt(configurator.getNapDurationKey(), 0);
        boolean alarmState = savedConfiguration.getBoolean(configurator.getAlarmStateKey(), false);
        boolean isConfigured = savedConfiguration.getBoolean(configurator.getIsConfiguredKey(), false);

        configurator.setAlarmHour(alarmHour)
                    .setAlarmMinutes(alarmMinutes)
                    .buildAlarmTime(alarmHour, alarmMinutes)
                    .setNapDuration(napDuration)
                    .setAlarmState(alarmState)
                    .setConfigured(isConfigured);
    }

    @UiThread
    private void displayViews(boolean isConfigured){
        String title;
        if(isConfigured){
            title = getContext().getString(R.string.nap_time_title_nap_set);
        } else {
            title = getContext().getString(R.string.nap_time_title_no_nap_set);
        }
        setTitle(title);
        setSeparators(isConfigured);
        setEndTimeTextViews(isConfigured);
        setDurationTimeTextViews(isConfigured);
        setRemoveButton(isConfigured);
        setRingtoneName(isConfigured);
        setRingtoneChange(isConfigured);
        setNapTimeButton(!isConfigured);
    }



    private void setTitle(String title){
        if (titleTextView == null)
            titleTextView = cs.findViewById(R.id.napTime_textView);
        titleTextView.setText(title);
    }

    private void setSeparators(boolean areVisible){
        if(separator0 == null || separator1 == null || separator2 == null){
            separator0 = cs. findViewById(R.id.horizontal_separator_0_nap);
            separator1 = cs.findViewById(R.id.horizontal_separator_1_nap);
            separator2 = cs.findViewById(R.id.horizontal_separator_2_nap);
            separator3 = cs.findViewById(R.id.horizontal_separator_3_nap);
        }

        if(areVisible){
            separator0.setVisibility(View.VISIBLE);
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.VISIBLE);
            separator3.setVisibility(View.VISIBLE);
        } else {
            separator0.setVisibility(View.GONE);
            separator1.setVisibility(View.GONE);
            separator2.setVisibility(View.GONE);
            separator3.setVisibility(View.GONE);
        }
    }

    private void setEndTimeTextViews(boolean areVisible){
        if(endTimeTextView == null || endTimeValueTextView == null) {
            endTimeTextView = cs.findViewById(R.id.end_time_nap_textView);
            endTimeValueTextView = cs.findViewById(R.id.end_time_value_nap_textView);
        }

        if(areVisible){
            endTimeTextView.setVisibility(View.VISIBLE);
            endTimeValueTextView.setVisibility(View.VISIBLE);
            printEndTimeValue();
        } else {
            endTimeTextView.setVisibility(View.GONE);
            endTimeValueTextView.setVisibility(View.GONE);
        }
    }

    private void printEndTimeValue(){
        String endTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(configurator.getAlarmTime().getTime());
        endTimeValueTextView.setText(endTime);
    }

    private void setDurationTimeTextViews(boolean areVisible){
        if(durationTextView == null || durationValueTextView == null){
            durationTextView = cs.findViewById(R.id.duration_nap_textView);
            durationValueTextView = cs.findViewById(R.id.duration_value_nap_textView);
        }

        if(areVisible){
            durationTextView.setVisibility(View.VISIBLE);
            durationValueTextView.setVisibility(View.VISIBLE);
            printNapDuration();
            durationValueTextView.setOnClickListener(view -> startDialogActivity());
        } else {
            durationTextView.setVisibility(View.GONE);
            durationValueTextView.setVisibility(View.GONE);
        }
    }

    private void printNapDuration(){
        String duration  = String.valueOf(configurator.getNapDuration());
        durationValueTextView.setText(duration);
    }

    private void setRemoveButton(boolean isVisible){
        if (removeButton == null)
            removeButton = cs.findViewById(R.id.remove_configuration_button);

        if(isVisible){
            removeButton.setVisibility(View.VISIBLE);
            removeButton.setOnClickListener(view -> {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                SharedPreferences savedConfiguration = getContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
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
                            Notification.cancel(configurator.getRequestCode(), getContext());
                            configurator.setConfigured(false)
                                        .setAlarmState(false)
                                        .setSnoozeState(false)
                                        .updateSavedConfiguration(savedConfiguration);
                            displayViews(false);
                            Notification.cancel(configurator.getRequestCode(), getContext());
                            Notification.stopRingtone();
                        })
                        .setNegativeButton(R.string.negativeDialog, (dialog, which) -> {
                            //Nothing to do here.
                        });
                alertDialog.show();
            });
        } else {
            removeButton.setVisibility(View.GONE);
        }
    }

    private void setRingtoneChange(boolean isVisible) {
        if (ringtoneChange == null)
            ringtoneChange = cs.findViewById(R.id.ringtone_change_nap_textView);

        if (isVisible){
            ringtoneChange.setVisibility(View.VISIBLE);
            ringtoneChange.setOnClickListener(view -> {
                Intent i = new Intent(getContext(), SongListActivity.class);
                i.putExtra(SongListActivity.CHECK_ALARM_TYPE, configurator.getRequestCode());
                startActivity(i);
            });
        } else {
            ringtoneChange.setVisibility(View.GONE);
        }
    }

    private void setRingtoneName(boolean isVisible) {
        if(ringtoneName == null)
            ringtoneName = cs.findViewById(R.id.ringtone_nap_textView);

        if(isVisible){
            ringtoneName.setVisibility(View.VISIBLE);
            printSongName();
        } else {
            ringtoneName.setVisibility(View.GONE);
        }
    }

    private void printSongName(){
        SharedPreferences savedConfiguration = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        String ringtoneName = savedConfiguration.getString(configurator.getRingtoneNameKey(), "Ceausescu");
        String ringtoneText = getResources().getString(R.string.ringtone) + "\n" + ringtoneName;
        this.ringtoneName.setText(ringtoneText);
    }

    private void setNapTimeButton(boolean isVisible){
        if (napTimeButton == null)
            napTimeButton = cs.findViewById(R.id.nap_time_button);

        if(isVisible){
            napTimeButton.setVisibility(View.VISIBLE);
            napTimeButton.setOnClickListener(view -> startDialogActivity());
        } else {
            napTimeButton.setVisibility(View.GONE);
        }
    }

    private void startDialogActivity(){
        Intent i = new Intent(getContext(), DialogActivity.class);
        i.putExtra(DialogActivity.LISTENER_TYPE, DialogActivity.NAP_DURATION_LISTENER)
            .putExtra(DialogActivity.ALARM_TYPE, configurator.getRequestCode());
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        getConfiguration();
        displayViews(configurator.isAlarmOn());
    }

}