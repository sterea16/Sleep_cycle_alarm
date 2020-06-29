package com.sterea.sleepcyclealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class KnownWakeUpTimeFragment extends Fragment {

    private SwitchMaterial knowWakeUpTime_switch;
    public final static String TAG = MainActivity.class.getSimpleName();
    public KnownWakeUpTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kown_wake_up_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        knowWakeUpTime_switch = Objects.requireNonNull(getView()).findViewById(R.id.knownWakeUpTime_switch);
        knowWakeUpTime_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences savedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                if(!isChecked){
                    SharedPreferences.Editor editor = savedPreferences.edit();
                    editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
                    editor.apply();
                    Configurator.knownWakeUpTimeConf.setAlarmState(false);
                    updateKnownUpTimeCardView(savedPreferences);
                    cancelAlarm();
                } else {
                    if(savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false)){
                        //*if there has been already a configuration, then just set up the alarm with that one*//*
                        SharedPreferences.Editor editor = savedPreferences.edit();
                        editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, true);
                        editor.apply();
                        Configurator.knownWakeUpTimeConf.setAlarmState(true);
                        updateKnownUpTimeCardView(savedPreferences);
                        startAlarm(Configurator.knownWakeUpTimeConf.getWakeUpTime());
                    } else {
                        //*else start SetUpAlarmActivity to begin a new configuration*//*
                        Intent i = new Intent(getContext(), SetUpAlarmActivity.class);
                        startActivity(i);
                    }
                }
            }
        });

        CardView knownWakeUpCardView = getView().findViewById(R.id.knownWakeUp_cardView);
        knownWakeUpCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SetUpAlarmActivity.class);
                startActivity(i);
            }
        });
        knownWakeUpCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                alertDialog.setMessage(R.string.deleteDialog)
                        .setPositiveButton(R.string.positiveDialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO Reset alarm configuration
                                SharedPreferences savedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                                Configurator.knownWakeUpTimeConf.setSleepCycles(6);
                                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerCycles(5);
                                Configurator.knownWakeUpTimeConf.setMinutesFallingAsleep(14);
                                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerMinutesAsleep(9);
                                Configurator.knownWakeUpTimeConf.setConfigured(false);
                                Configurator.knownWakeUpTimeConf.setAlarmState(false);
                                Configurator.knownWakeUpTimeConf.updateSharedConfiguration(savedPreferences);
                                knowWakeUpTime_switch.setChecked(false);
                                SpannableStringBuilder ss = new SpannableStringBuilder(getResources().getString(R.string.knownWakeUpTimeText));
                                ss.setSpan(new StyleSpan(Typeface.ITALIC), 41, 49, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                TextView textView = getActivity().findViewById(R.id.knownWakeUp_textView);
                                textView.setText(ss);
                            }
                        })
                        .setNegativeButton(R.string.negativeDialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //nothing to do here
                            }
                        });
                alertDialog.show();
                return false;
            }
        });

        if(Configurator.knownWakeUpTimeConf.getWakeUpTime() == null){
            SharedPreferences  savedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            updateKnownUpTimeCardView(savedPreferences);
        }
    }

    //TODO save alarm after device reboot https://developer.android.com/training/scheduling/alarms
    private void startAlarm (Calendar c){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(getContext()).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),1, intent,0);

        if(c.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            c.add(Calendar.DATE,1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
        Toast toast = Toast.makeText(getContext(),"Alarm set", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(getContext()).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),1, intent,0);
        alarmManager.cancel(pendingIntent);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getContext(),"Alarm canceled", duration);
        toast.show();
    }

    private void updateKnownUpTimeCardView(SharedPreferences savedPreferences){
        boolean configured = savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false);
        if(configured) {
            boolean alarmState = savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
            if (alarmState) {
                if (!knowWakeUpTime_switch.isChecked()) {
                    knowWakeUpTime_switch.setChecked(true);
                    /*if the alarm configuration was changed when the alarm state was false
                     * the alarm state will then be changed to true and the switch view must be update to checked
                     * the listener will trigger a new call of this method and the UI update will be done
                     * therefore, after this new call finishes, the first call of this method must stop right from where it was left*/
                    return;
                }

                if (Configurator.knownWakeUpTimeConf.getWakeUpTime() == null) {
                    int hour = savedPreferences.getInt(Configurator.HOUR_KNOWN_WAKE_UP, 0);
                    int minutes = savedPreferences.getInt(Configurator.MINUTES_KNOWN_WAKE_UP, 0);
                    Configurator.knownWakeUpTimeConf.setWakeUpTime(hour, minutes);
                }
                Calendar wakeUpTime = Configurator.knownWakeUpTimeConf.getWakeUpTime();
                String wakeUpTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(wakeUpTime.getTime());

                if (Configurator.knownWakeUpTimeConf.getBedTime() == null) {
                    int cycles = savedPreferences.getInt(Configurator.CYCLES_INT_VALUE, 0);
                    int asleep = savedPreferences.getInt(Configurator.ASLEEP_INT_VALUE, 0);
                    Configurator.knownWakeUpTimeConf.setBedTime(wakeUpTime, cycles, asleep);
                }
                Calendar bedTime = Configurator.knownWakeUpTimeConf.getBedTime();
                String bedTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(bedTime.getTime());
                int[] hourMinutes = calcHourMinuteSleepingTime(savedPreferences.getInt(Configurator.CYCLES_INT_VALUE, 0));
                int hours = hourMinutes[0];
                int minutes = hourMinutes[1];

                StringBuilder text = new StringBuilder(getResources().getString(R.string.bed_time_text_1) + " " + wakeUpTimeText + "\n"
                        + getResources().getString(R.string.bed_time_text_2) + " " + bedTimeText + "\n"
                        + getResources().getString(R.string.bed_time_text_3) + " " + savedPreferences.getInt(Configurator.CYCLES_INT_VALUE, 0));
                if(minutes == 0 ){
                    text.append(" (" + hours + "h)\n"
                            + getResources().getString(R.string.tips));
                } else {
                    text.append(" (" + hours + "h " + minutes + "min)\n"
                            + getResources().getString(R.string.tips));
                }
                /*format the text
                 * styling position depends on date and time format (am/pm or 24 hour) */
                SpannableStringBuilder ss = new SpannableStringBuilder(text);
                int start = getResources().getString(R.string.bed_time_text_1).length() + 1;
                int end = start + wakeUpTimeText.length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end + 1 + getResources().getString(R.string.bed_time_text_2).length() + 1;
                end = start + bedTimeText.length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end + 1 +  getResources().getString(R.string.bed_time_text_3).length() + 1;
                end = start + Integer.toString(savedPreferences.getInt(Configurator.CYCLES_INT_VALUE, 0)).length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end;
                end = text.length() - getResources().getString(R.string.tips).length();
                ss.setSpan(new RelativeSizeSpan(0.85f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end;
                end = text.length();
                ss.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new RelativeSizeSpan(0.65f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_gray)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                TextView textView = Objects.requireNonNull(getView()).findViewById(R.id.knownWakeUp_textView);
                textView.setText(ss);
                startAlarm(Configurator.knownWakeUpTimeConf.getWakeUpTime());
            } else {
                if (knowWakeUpTime_switch.isChecked()) {
                    knowWakeUpTime_switch.setChecked(false);
                    /* if the switch was changed from checked to unchecked the listener of the switch view will trigger this method
                     * so a new session of this method will begin and the first one needs to be stopped where it was left of */
                    return;
                }
                if (Configurator.knownWakeUpTimeConf.getWakeUpTime() == null) {
                    int hour = savedPreferences.getInt(Configurator.HOUR_KNOWN_WAKE_UP, 0);
                    int minutes = savedPreferences.getInt(Configurator.MINUTES_KNOWN_WAKE_UP, 0);
                    Configurator.knownWakeUpTimeConf.setWakeUpTime(hour, minutes);
                }
                Calendar wakeUpTime = Configurator.knownWakeUpTimeConf.getWakeUpTime();
                String wakeUpTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(wakeUpTime.getTime());

                if (Configurator.knownWakeUpTimeConf.getBedTime() == null) {
                    int cycles = savedPreferences.getInt(Configurator.CYCLES_INT_VALUE, 0);
                    int asleep = savedPreferences.getInt(Configurator.ASLEEP_INT_VALUE, 0);
                    Configurator.knownWakeUpTimeConf.setBedTime(wakeUpTime, cycles, asleep);
                }
                Calendar bedTime = Configurator.knownWakeUpTimeConf.getBedTime();
                String bedTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(bedTime.getTime());

                String text = getResources().getString(R.string.knownWakeUpTime_alarm_off_text_1) + " " + wakeUpTimeText +"\n"
                        + getResources().getString(R.string.knownWakeUpTime_alarm_off_text_2) + " " + bedTimeText + "\n"
                        + getResources().getString(R.string.alarmOff) + "\n"
                        + getResources().getString(R.string.tips);
                /*format the text
                 * styling position depends on date and time format (am/pm or 24 hour) */
                SpannableStringBuilder ss = new SpannableStringBuilder(text);
                int start = getResources().getString(R.string.knownWakeUpTime_alarm_off_text_1).length() + 1;
                int end = start + wakeUpTimeText.length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end + 1 + getResources().getString(R.string.knownWakeUpTime_alarm_off_text_2).length() + 1;
                end = start + bedTimeText.length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end + 1 + getResources().getString(R.string.alarmOff).length() + 1;
                end = start + getResources().getString(R.string.tips).length();
                ss.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new RelativeSizeSpan(0.65f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_gray)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                TextView textView = Objects.requireNonNull(getView()).findViewById(R.id.knownWakeUp_textView);
                textView.setText(ss);
            }
        }
    }

    private int[] calcHourMinuteSleepingTime(int totalSleepingTime){
        totalSleepingTime *= 90;
        int i = 60;
        int hours = 0, minutes;
        while(totalSleepingTime >= i){
            totalSleepingTime -= i;
            ++hours;
        }
        minutes = totalSleepingTime;
        int[] hourMinutes = new int[2];
        hourMinutes[0] = hours;
        hourMinutes[1] = minutes;
        return hourMinutes;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*run the updateKnowUpTimeCardView method from onResume only if there are changes on the configuration
         * if so, this will run after SetUpActivity's onDestroy() method has finished*/
        if(Configurator.knownWakeUpTimeConf.getConfChanged() != null){
            SharedPreferences savedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            if(Configurator.knownWakeUpTimeConf.getConfChanged()) {
                updateKnownUpTimeCardView(savedPreferences);
                Configurator.knownWakeUpTimeConf.setConfChanged(false);
            } else if (!savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false)) {
                knowWakeUpTime_switch.setChecked(false);
            }
        }

        if(Configurator.knownWakeUpTimeConf.getRawFileSongName() == null) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            Configurator.knownWakeUpTimeConf.setRawFileSongName(sharedPreferences.getString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP, getResources().getResourceName(R.raw.air_horn_in_close_hall_series)));
        }
    }
}