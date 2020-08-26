package com.sterea.sleepcyclealarm;
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
public class WakeUpTimeFragment extends Fragment {

    private SwitchMaterial knowWakeUpTime_switch;
    public WakeUpTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wake_up_time_known, container, false);
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
                    editor.putBoolean(Configurator.ALARM_STATE_WAKE_UP_KNOWN_KEY, false);
                    editor.apply();
                    Configurator.wakeUpTimeKnownConf.setAlarmState(false);
                    updateKnownUpTimeCardView(savedPreferences);
                    Alarm alarm = new Alarm(getContext());
                    alarm.cancelAlarm();
                    Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.alarmOff), Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    if(savedPreferences.getBoolean(Configurator.IS_WAKE_UP_KNOWN_CONFIGURED, false)){
                        /* If there has been already a configuration, then just set up the alarm with that one. */
                        SharedPreferences.Editor editor = savedPreferences.edit();
                        editor.putBoolean(Configurator.ALARM_STATE_WAKE_UP_KNOWN_KEY, true);
                        editor.apply();
                        Configurator.wakeUpTimeKnownConf.setAlarmState(true);
                        updateKnownUpTimeCardView(savedPreferences);
                        Alarm alarm = new Alarm(Configurator.wakeUpTimeKnownConf.getAlarmTime(), getContext(), Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE);
                        alarm.registerAlarm();
                        Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.alarmOn), Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        /* Else start SetUpAlarmActivity to begin a new configuration. */
                        Intent i = new Intent(getContext(), SetUpAlarmActivity.class);
                        i.putExtra(SetUpAlarmActivity.CHECK_ALARM_TYPE, Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE);
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
                i.putExtra(SetUpAlarmActivity.CHECK_ALARM_TYPE, Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE);
                startActivity(i);
            }
        });
        knownWakeUpCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                alertDialog.setMessage(R.string.removeDialog)
                        .setPositiveButton(R.string.positiveDialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences savedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                                Configurator.wakeUpTimeKnownConf.setSleepCycles(6)
                                            .setItemPositionSpinnerCycles(5)
                                            .setMinutesFallingAsleep(14)
                                            .setItemPositionSpinnerMinutesAsleep(9)
                                            .setConfigured(false)
                                            .setAlarmState(false)
                                            .updateSavedConfiguration(savedPreferences, Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE);
                                knowWakeUpTime_switch.setChecked(false);
                                SpannableStringBuilder ss = new SpannableStringBuilder(getResources().getString(R.string.knownWakeUpTimeText));
                                ss.setSpan(new StyleSpan(Typeface.ITALIC), 41, 49, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                TextView textView = getActivity().findViewById(R.id.knownBedTime_textView);
                                textView.setText(ss);
                            }
                        })
                        .setNegativeButton(R.string.negativeDialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Nothing to do here.
                            }
                        });
                alertDialog.show();
                return false;
            }
        });

        if(Configurator.wakeUpTimeKnownConf.getAlarmTime() == null){
            SharedPreferences  savedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            updateKnownUpTimeCardView(savedPreferences);
        }
    }

    private void updateKnownUpTimeCardView(SharedPreferences savedPreferences){
        boolean configured = savedPreferences.getBoolean(Configurator.IS_WAKE_UP_KNOWN_CONFIGURED, false);
        if(configured) {
            boolean alarmState = savedPreferences.getBoolean(Configurator.ALARM_STATE_WAKE_UP_KNOWN_KEY, false);
            if (alarmState) {
                if (!knowWakeUpTime_switch.isChecked()) {
                    knowWakeUpTime_switch.setChecked(true);
                    /* If the alarm configuration was changed when the alarm state was false.
                     * The alarm state will then be changed to true and the switch view must be update to checked.
                     * The listener will trigger a new call of this method and the UI update will be done
                     * therefore, after this new call finishes, the first call one must be stopped right from where it was left.*/
                    return;
                }

                if (Configurator.wakeUpTimeKnownConf.getAlarmTime() == null) {
                    int hour = savedPreferences.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, 0);
                    int minutes = savedPreferences.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, 0);
                    Configurator.wakeUpTimeKnownConf.buildAlarmTime(hour, minutes);
                }
                Calendar wakeUpTime = Configurator.wakeUpTimeKnownConf.getAlarmTime();
                String wakeUpTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(wakeUpTime.getTime());

                if (Configurator.wakeUpTimeKnownConf.getBedTime() == null) {
                    int cycles = savedPreferences.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY, 0);
                    int asleep = savedPreferences.getInt(Configurator.ASLEEP_INT_VALUE_KNOWN_WAKE_UP_KEY, 0);
                    Configurator.wakeUpTimeKnownConf.calcBedTime(wakeUpTime, cycles, asleep);
                }
                Calendar bedTime = Configurator.wakeUpTimeKnownConf.getBedTime();
                String bedTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(bedTime.getTime());
                int[] hourMinutes = calcHourMinuteSleepingTime(savedPreferences.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY, 0));
                int hours = hourMinutes[0];
                int minutes = hourMinutes[1];

                StringBuilder text = new StringBuilder(getResources().getString(R.string.bed_time_text_1) + " " + wakeUpTimeText + "\n"
                        + getResources().getString(R.string.bed_time_text_2) + " " + bedTimeText + "\n"
                        + getResources().getString(R.string.bed_time_text_3) + " " + savedPreferences.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY, 0));
                if(minutes == 0 ){
                    text.append(" (").append(hours).append("h)\n").append(getResources().getString(R.string.tips));
                } else {
                    text.append(" (").append(hours).append("h ").append(minutes).append("min)\n").append(getResources().getString(R.string.tips));
                }
                /* Format the text.
                 * Styling position depends on date and time format (am/pm or 24 hour). */
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
                end = start + Integer.toString(savedPreferences.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY, 0)).length();
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

                TextView textView = Objects.requireNonNull(getView()).findViewById(R.id.knownBedTime_textView);
                textView.setText(ss);

                Alarm alarm = new Alarm (Configurator.wakeUpTimeKnownConf.getAlarmTime(), getContext(), Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE);
                alarm.registerAlarm();
            } else {
                if (knowWakeUpTime_switch.isChecked()) {
                    knowWakeUpTime_switch.setChecked(false);
                    /* If the switch was changed from checked to unchecked, the switch's listener will trigger this method
                     * so a new call of this method will begin and the first one needs to be stopped where it was left off. */
                    return;
                }
                if (Configurator.wakeUpTimeKnownConf.getAlarmTime() == null) {
                    int hour = savedPreferences.getInt(Configurator.ALARM_HOUR_KNOWN_WAKE_UP, 0);
                    int minutes = savedPreferences.getInt(Configurator.ALARM_MINUTES_KNOWN_WAKE_UP, 0);
                    Configurator.wakeUpTimeKnownConf.buildAlarmTime(hour, minutes);
                }
                Calendar wakeUpTime = Configurator.wakeUpTimeKnownConf.getAlarmTime();
                String wakeUpTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(wakeUpTime.getTime());

                if (Configurator.wakeUpTimeKnownConf.getBedTime() == null) {
                    int cycles = savedPreferences.getInt(Configurator.CYCLES_INT_VALUE_KNOWN_WAKE_UP_KEY, 0);
                    int asleep = savedPreferences.getInt(Configurator.ASLEEP_INT_VALUE_KNOWN_WAKE_UP_KEY, 0);
                    Configurator.wakeUpTimeKnownConf.calcBedTime(wakeUpTime, cycles, asleep);
                }
                Calendar bedTime = Configurator.wakeUpTimeKnownConf.getBedTime();
                String bedTimeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(bedTime.getTime());

                String text = getResources().getString(R.string.knownWakeUpTime_alarm_off_text_1) + " " + wakeUpTimeText +"\n"
                        + getResources().getString(R.string.knownWakeUpTime_alarm_off_text_2) + " " + bedTimeText + "\n"
                        + getResources().getString(R.string.alarmOff) + "\n"
                        + getResources().getString(R.string.tips);
                /* format the text
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

                TextView textView = Objects.requireNonNull(getView()).findViewById(R.id.knownBedTime_textView);
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
        /* Run the updateKnowUpTimeCardView() method from onResume() only if there are changes on the configuration.
         * If so, this will run after SetUpActivity's onDestroy() method has finished. */
        if(Configurator.wakeUpTimeKnownConf.getConfChanged() != null){
            SharedPreferences savedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            if(Configurator.wakeUpTimeKnownConf.getConfChanged()) {
                updateKnownUpTimeCardView(savedPreferences);
                Configurator.wakeUpTimeKnownConf.setConfChanged(false);
            } else if (!savedPreferences.getBoolean(Configurator.IS_WAKE_UP_KNOWN_CONFIGURED, false)) {
                knowWakeUpTime_switch.setChecked(false);
            }
        }

        if(Configurator.wakeUpTimeKnownConf.getRawFileSongName() == null) {
            SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            Configurator.wakeUpTimeKnownConf.setRawFileSongName(sharedPreferences.getString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP_KEY, getResources().getResourceName(R.raw.summer)));
        }
    }
}