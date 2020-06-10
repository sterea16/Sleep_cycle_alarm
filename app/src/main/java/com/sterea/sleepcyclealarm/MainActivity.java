package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sterea.sleepcyclealarm.model.alarm.AlarmReceiver;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private SwitchMaterial knowWakeUpTime_switch;
    //TODO Create a new Class for all the methods that use the alarm

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // initialise the date display
        TextView dateView = findViewById(R.id.dateView);
        Calendar calendar = Calendar.getInstance(); // take the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
        String date = dateFormat.format(calendar.getTime());
        dateView.setText(date);
        /*TextClock textClock = findViewById(R.id.textClock);
        textClock.is24HourModeEnabled();*/

        /*if the app is reopened after a while the configurator needs to get the wake up hour form
        * shared preferences file*/
        if(Configurator.knownWakeUpTimeConf.getWakeUpTime() == null){
            SharedPreferences  savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            /*if there is already a saved configuration, the knownWakeUp configurator object will set its wake up time*/
            if(savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false)){
                int hour = savedPreferences.getInt(Configurator.HOUR, 0);
                int minutes = savedPreferences.getInt(Configurator.MINUTES, 0);
                Configurator.knownWakeUpTimeConf.setWakeUpTime(hour, minutes);
            }
        }
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
                    cancelAlarm();
                } else {
                    /*if there has been already a configuration, then just set up the alarm with that configuration*/
                    if(savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false)){
                        SharedPreferences.Editor editor = savedPreferences.edit();
                        editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, true);
                        editor.apply();
                        Configurator.knownWakeUpTimeConf.setAlarmState(true);
                        startAlarm(Configurator.knownWakeUpTimeConf.getWakeUpTime());
                    } else {
                        /*else start a SetUpAlarmActivity to begin a new configuration*/
                        Intent i = new Intent(MainActivity.this, SetUpAlarmActivity.class);
                        startActivity(i);
                    }

                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);//removes the title of the toolbar (this is the main activity and its label it's required in order to give a name to the app launcher)

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
                                //TODO Delete the alarm
                                SharedPreferences sharedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                                Configurator.knownWakeUpTimeConf.setSleepCycles(7);
                                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerCycles(6);
                                Configurator.knownWakeUpTimeConf.setMinutesFallingAsleep(14);
                                Configurator.knownWakeUpTimeConf.setItemPositionSpinnerMinutesAsleep(9);
                                Configurator.knownWakeUpTimeConf.setConfigured(false);
                                Configurator.knownWakeUpTimeConf.setAlarmState(false);
                                Configurator.knownWakeUpTimeConf.updateSharedConfiguration(sharedPreferences);
                                updateKnownUpTimeCardView();
                            }
                        })
                        .setNegativeButton(R.string.negativeDialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.show();
                return false;
            }
        });

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
        updateKnownUpTimeCardView();
    }

    private void updateKnownUpTimeCardView(){
        SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean checked = savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
        if(checked){
            knowWakeUpTime_switch.setChecked(true);
            TextView textView = findViewById(R.id.knownWakeUp_textView);
            startAlarm(Configurator.knownWakeUpTimeConf.getWakeUpTime());
        } else {
            knowWakeUpTime_switch.setChecked(false);
        }
    }
}
