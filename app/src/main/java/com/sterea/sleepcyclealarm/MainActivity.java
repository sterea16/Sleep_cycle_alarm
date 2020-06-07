package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sterea.sleepcyclealarm.model.alarm.AlarmReceiver;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private TextView alarm_status;
    private TextView goToSleepTime_text1;
    private SwitchMaterial knowWakeUpTime_switch;
    private Dialog dialog_wake_up_time, dialog_choose_song;
    private MediaPlayer mediaPlayer;
    //TODO Create a new Class for all the methods that use the alarm

    @Override
    protected void onCreate(Bundle savedInstanceState){
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
        knowWakeUpTime_switch = findViewById(R.id.knownWakeUpTime_switch);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//removes the title of the toolbar (this is the main activity and its label it's required in order to give a name to the app launcher)

        goToSleepTime_text1 = findViewById(R.id.goToSleepTime_text1);
        alarm_status = findViewById(R.id.alarm_status);
        alarm_status.setText("No alarm set");

        Button sleepingNOW = findViewById(R.id.sleepNow);
        sleepingNOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWakeUpTime();
            }
        });

        CardView cardView = findViewById(R.id.knownWakeUp_cardView);
        cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SetUpAlarmActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE,minute);
        c.set(Calendar.SECOND,0);

        startAlarm(c);
        updateAlarmStatus(c);
        //minutes
        int oneSleepCycle = 90;
        c.add(Calendar.MINUTE,-(5* oneSleepCycle));
        String text ="To get 5 full sleep cycles you should fall asleep at <i> <font color = red>"
                + DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime())
                + "</font> </i>.<br /> Note that the average time to fall asleep is <i>14 minutes</i>.";
        updateGoToSleepText(text);//this will tell the user when is the right time to fall asleep
    }

    private void updateAlarmStatus(Calendar c){
        String alarmStatusText = "Alarm is set for: ";
        alarmStatusText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        alarm_status.setText(alarmStatusText);
    }

    private void updateGoToSleepText(String text){
        goToSleepTime_text1.setText(Html.fromHtml(text));
    }

    private void getWakeUpTime(){ // gets the right time for wake up
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND,0);
        c.add(Calendar.MINUTE,6*90);
        startAlarm(c);
        updateAlarmStatus(c);
        String text = "If you are going to sleep right now you will get 6 full sleep cycle at <i><font color = red>" + DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime())
                    +"</i></font>.";
        updateGoToSleepText(text);
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
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this,"Alarm set", duration);
        toast.show();
    }

    public void onClickAddAlarm(View view){
        /*DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(),"time picker");*/

        Intent i = new Intent(this,SetUpAlarmActivity.class);
        startActivity(i);
    }

    public void onCancelAlarms(View view){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);
        alarmManager.cancel(pendingIntent);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this,"Alarm canceled", duration);
        toast.show();
        alarm_status.setText("No alarm set");
        String text ="";
        updateGoToSleepText(text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean check = savedPreferences.getBoolean(Configurator.IS_KNOWN_WAKE_UP_CONFIGURED, false);
        if(check){
            knowWakeUpTime_switch.setChecked(true);
            TextView textView = findViewById(R.id.knownWakeUp_textView);
        }
    }

}
