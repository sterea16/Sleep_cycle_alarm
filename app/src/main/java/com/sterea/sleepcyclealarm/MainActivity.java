package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sterea.sleepcyclealarm.model.alarm.AlarmReceiver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private SwitchMaterial knowWakeUpTime_switch;
    //TODO Create a new Class for all the methods that use the alarm
    /*TODO Create a Preference hierarchy (a.k.a settings fragment) for notifications and sleep cycle value
    *  https://developer.android.com/guide/topics/ui/settings*/

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);//removes the title of the toolbar (this is the main activity and its label it's required in order to give a name to the app launcher)

        // initialise the date display
        TextView dateView = findViewById(R.id.dateView);
        Calendar calendar = Calendar.getInstance(); // take the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
        String date = dateFormat.format(calendar.getTime());
        dateView.setText(date);
        /*TextClock textClock = findViewById(R.id.textClock);
        textClock.is24HourModeEnabled();*/

        knowWakeUpTime_switch = findViewById(R.id.knownWakeUpTime_switch);
        knowWakeUpTime_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                if(!isChecked){
                    SharedPreferences.Editor editor = savedPreferences.edit();
                    editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
                    editor.apply();
                    Configurator.knownWakeUpTimeConf.setAlarmState(false);
                    updateKnownUpTimeCardView(savedPreferences);
                    cancelAlarm();
                } else {
                    /*if there has been already a configuration, then just set up the alarm with that one*/
                    if(savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false)){
                        SharedPreferences.Editor editor = savedPreferences.edit();
                        editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, true);
                        editor.apply();
                        Configurator.knownWakeUpTimeConf.setAlarmState(true);
                        updateKnownUpTimeCardView(savedPreferences);
                        startAlarm(Configurator.knownWakeUpTimeConf.getWakeUpTime());
                    } else {
                        /*else start SetUpAlarmActivity to begin a new configuration*/
                        Intent i = new Intent(MainActivity.this, SetUpAlarmActivity.class);
                        startActivity(i);
                    }
                }
            }
        });

        CardView knownWakeUpCardView = findViewById(R.id.knownWakeUp_cardView);
        knownWakeUpCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SetUpAlarmActivity.class);
                startActivity(i);
            }
        });
        knownWakeUpCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setMessage(R.string.deleteDialog)
                        .setPositiveButton(R.string.positiveDialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO Reset alarm configuration
                                SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                                Configurator.knownWakeUpTimeConf.setSleepCycles(6);
                                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerCycles(5);
                                Configurator.knownWakeUpTimeConf.setMinutesFallingAsleep(14);
                                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerMinutesAsleep(9);
                                Configurator.knownWakeUpTimeConf.setConfigured(false);
                                Configurator.knownWakeUpTimeConf.setAlarmState(false);
                                Configurator.knownWakeUpTimeConf.updateSharedConfiguration(savedPreferences);
                                knowWakeUpTime_switch.setChecked(false);
                                SpannableStringBuilder ss = new SpannableStringBuilder(getResources().getString(R.string.knownWakeUpTimeText));
                                ss.setSpan(new StyleSpan(Typeface.ITALIC), 41, 49,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                TextView textView = findViewById(R.id.knownWakeUp_textView);
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
            SharedPreferences  savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            updateKnownUpTimeCardView(savedPreferences);
        }

    }

    //TODO save alarm after device reboot https://developer.android.com/training/scheduling/alarms
    private void startAlarm (Calendar c){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1, intent,0);

        if(c.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            c.add(Calendar.DATE,1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
        Toast toast = Toast.makeText(this,"Alarm set", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1, intent,0);
        alarmManager.cancel(pendingIntent);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this,"Alarm canceled", duration);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*run the updateKnowUpTimeCardView method from onResume only if there are changes on the configuration
        * if so, this will run after SetUpActivity's onDestroy() method has finished*/
        if(Configurator.knownWakeUpTimeConf.getConfChanged() != null){
            SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            if(Configurator.knownWakeUpTimeConf.getConfChanged()) {
                updateKnownUpTimeCardView(savedPreferences);
                Configurator.knownWakeUpTimeConf.setConfChanged(false);
            } else if (!savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false)) {
                knowWakeUpTime_switch.setChecked(false);
            }
        }
    }

    private void updateKnownUpTimeCardView(SharedPreferences savedPreferences){
        boolean configured = savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false);
        if(configured) {
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

            boolean alarmState = savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
            if (alarmState) {
                if (!knowWakeUpTime_switch.isChecked()) {
                    knowWakeUpTime_switch.setChecked(true);
                    /*if the alarm configuration was changed when the alarm state was false
                    * the alarm state will then be changed to true and the switch view must be update to checked
                    * the listener will trigger a new call of this method and the UI update will be done
                    * therefore, after this new call finishes, the first call must stop right from where it was left*/
                    return;
                }
                String text = getResources().getString(R.string.bed_time_text_1) + " " + wakeUpTimeText + ".\n"
                        + getResources().getString(R.string.bed_time_text_2) + " " + bedTimeText
                        + " " + getResources().getString(R.string.bed_time_text_3) + " " + savedPreferences.getInt(Configurator.CYCLES_INT_VALUE, 0)
                        + " " + getResources().getString(R.string.bed_time_text_4);
                /*format the text
                * styling position depends on format of date and time (am/pm or 24 hour) */
                SpannableStringBuilder ss = new SpannableStringBuilder(text);
                int start = getResources().getString(R.string.bed_time_text_1).length() + 1;
                int end = start + wakeUpTimeText.length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end + 2 + getResources().getString(R.string.bed_time_text_2).length() + 1;
                end = start + bedTimeText.length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = end + 1 +  getResources().getString(R.string.bed_time_text_3).length() + 1;
                end = start + Integer.toString(savedPreferences.getInt(Configurator.CYCLES_INT_VALUE, 0)).length();
                ss.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.custom_green)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                TextView textView = findViewById(R.id.knownWakeUp_textView);
                textView.setText(ss);
                startAlarm(Configurator.knownWakeUpTimeConf.getWakeUpTime());
            } else {
                String text = getResources().getString(R.string.knownWakeUpTime_alarm_off_text_1)
                        + " " + wakeUpTimeText +"\n"
                        + getResources().getString(R.string.knownWakeUpTime_alarm_off_text_2)
                        + " " + bedTimeText + "\n"
                        + getResources().getString(R.string.alarmOff) + "\n"
                        + getResources().getString(R.string.tips);
                SpannableString ss = new SpannableString(text);
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

                TextView textView = findViewById(R.id.knownWakeUp_textView);
                textView.setText(ss);
            }
        }
    }
}
