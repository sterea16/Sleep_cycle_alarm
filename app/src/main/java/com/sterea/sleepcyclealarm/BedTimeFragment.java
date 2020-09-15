package com.sterea.sleepcyclealarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import java.text.DateFormat;
import java.util.Calendar;

public class BedTimeFragment extends CustomFragment{

    public BedTimeFragment() {
        // Required empty public constructor
    }

    BedTimeFragment(Configurator configurator){
        super(configurator);
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
            Calendar alarmTime = configurator.getAlarmTime();
            String alarmTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(alarmTime.getTime());
            return getResources().getString(R.string.calcWakingTime) + "\n" + alarmTimeText;
        } else {
            return getResources().getString(R.string.knownBedTimeText);
        }
    }

    @Override
    void setButtonIcon() {
        setUpButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.waking_ripple, null));
    }

    @Override
    String getTimeInputValueText() {
        Calendar bedTime = configurator.getBedTime();
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(bedTime.getTime());
    }

    @Override
    void setUpTimeInputTextViewText() {
        TextView bedTimeTextView = getView().findViewById(R.id.time_input_text_view);
        bedTimeTextView.setText(getResources().getString(R.string.bed_time));
    }

    @Override
    void setUpListeners() {
        super.setUpListeners();
        alarmStateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setAlarm();
            } else {
               cancelAlarm();
            }
            updateUI(true);
        });
    }

    @Override
    void setAlarm() {
        SharedPreferences savedConfiguration = getContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, Context.MODE_PRIVATE);
        //create a new Calendar object based on the saved cycles and minutes falling asleep values
        Calendar currentTime = Calendar.getInstance();
        configurator.setBedTime(currentTime)
                    .setBedHour(configurator.getBedTime().get(Calendar.HOUR_OF_DAY))
                    .setBedMinutes(configurator.getBedTime().get(Calendar.MINUTE));

        configurator.calcAlarmTime(configurator.getBedTime(), configurator.getSleepCycles(), configurator.getMinutesFallingAsleep())
                    .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                    .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE));

        //register an alarm in the system with the new calendar object
        Alarm alarm = new Alarm(configurator.getAlarmTime(), getContext(), configurator.getRequestCode());
        alarm.register();
        configurator.setAlarmState(true);

        //save the new alarm in the shared preferences file
        SharedPreferences.Editor editor = savedConfiguration.edit();
        editor.putInt(configurator.getAlarmHourKey(), configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                .putInt(configurator.getAlarmMinutesKey(), configurator.getAlarmTime().get(Calendar.MINUTE))
                .putInt(configurator.getBedHourKey(), configurator.getBedHour())
                .putInt(configurator.getBedMinuteKey(), configurator.getBedMinutes())
                .putBoolean(configurator.getAlarmStateKey(), true)
                .apply();
    }
}