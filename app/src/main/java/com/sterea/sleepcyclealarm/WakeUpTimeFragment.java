package com.sterea.sleepcyclealarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

public class WakeUpTimeFragment extends CustomFragment {
    WakeUpTimeFragment(Configurator configurator){
        super(configurator);
    }

    public WakeUpTimeFragment() {
        // Required empty public constructor
    }

    String getTotalSleepingTime(int sleepCycles){
        int totalMinutes = sleepCycles * 90;
        int totalHours = totalMinutes/60;
        int leftoverMinutes = totalMinutes % 60;
        if(leftoverMinutes != 0){
            return totalHours + " h " + leftoverMinutes + " m";
        } else {
            return totalHours + " h";
        }
    }

    @Override
    String getTitleText(boolean hasConfiguration) {
        if (hasConfiguration){
            Calendar bedTime = configurator.getBedTime();
            String bedTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(bedTime.getTime());
            return getResources().getString(R.string.calculated_bed_time) + "\n" + bedTimeText;
        } else {
            return getResources().getString(R.string.knownWakeUpTimeText);
        }
    }

    @Override
    void setButtonIcon() {
        setUpButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bed_ripple, null));
    }

    @Override
    String getTimeInputValueText() {
        Calendar wakingTime = configurator.getAlarmTime();
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(wakingTime.getTime());
    }

    @Override
    void setUpTimeInputTextViewText() {
        TextView wakingTimeTextView = getView().findViewById(R.id.time_input_text_view);
        wakingTimeTextView.setText(getResources().getString(R.string.waking_time));
    }

    @Override
    void setUpListeners() {
        alarmStateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences savedConfiguration = getContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, Context.MODE_PRIVATE);
            if(isChecked){
                setAlarm();
                configurator.setAlarmState(true);
                SharedPreferences.Editor editor = savedConfiguration.edit();
                editor.putBoolean(configurator.getAlarmStateKey(), true)
                      .apply();
            } else {
                cancelAlarm();
            }
            updateUI(true);
        });

        timeInputValue.setClickable(true);
        TypedValue outValue = new TypedValue();
        Objects.requireNonNull(getContext()).getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        timeInputValue.setBackgroundResource(outValue.resourceId);
        timeInputValue.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), DialogActivity.class);
            i.putExtra(DialogActivity.ALARM_TYPE, configurator.getRequestCode());
            i.putExtra(DialogActivity.LISTENER_TYPE, DialogActivity.TIME_LISTENER);
            startActivity(i);
        });
        super.setUpListeners();
    }

    void setAlarm(){
        /*alarmStatus.setTextColor(getResources().getColor(R.color.colorPrimaryDark));*/
        Alarm alarm = new Alarm(configurator.getAlarmTime(), getContext(), configurator.getRequestCode());
        alarm.register();
    }

}