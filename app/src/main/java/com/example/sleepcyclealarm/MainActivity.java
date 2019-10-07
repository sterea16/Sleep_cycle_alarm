package com.example.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sleepcyclealarm.Model.Alarm.AlarmReceiver;
import com.example.sleepcyclealarm.Model.Alarm.TimePickerFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private Button alarmButtonON, alarmButtonOFF, sleepingNOW;
    private TextView dateView, alarm_status, goToSleepTime_text1;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;
    private final int oneSleepCycle = 90; //minutes

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // initialise the date display
        dateView = findViewById(R.id.dateView);
        calendar = Calendar.getInstance(); // take the current date
        dateFormat = new SimpleDateFormat("EEE, MMM d");
        date = dateFormat.format(calendar.getTime());
        dateView.setText(date);

        goToSleepTime_text1 = findViewById(R.id.goTosleepTime_text1);
        alarm_status = findViewById(R.id.alarm_status);
        alarm_status.setText("No alarm set");
        alarmButtonON = findViewById(R.id.alarmButtonOn);
        alarmButtonON.setText("Set alarm");
        alarmButtonON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(),"time picker");
            }
        });
        alarmButtonOFF = findViewById(R.id.alarmButtonOFF);
        alarmButtonOFF.setText("Cancel alarm");
        alarmButtonOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CancelAlarm();
            }
        });

        sleepingNOW = findViewById(R.id.sleepNow);
        sleepingNOW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWakeUpTime();
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
        c.add(Calendar.MINUTE,-(5*oneSleepCycle));
        String text ="To get 5 full sleep cycles you should fall asleep at <i> <font color = red>" + DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime())
                + "</font> </i>.<br /> Note that the average time to fall asleep is <i>14 minutes</i>.";
        updateGoToSleepText(text);//this will tell the user when is the right time to fall asleep
    }

    private void updateAlarmStatus(Calendar c){
        String alarmStatusText = "Alarm is set for: ";
        alarmStatusText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());
        alarm_status.setText(alarmStatusText);
        alarmButtonON.setText("Alarm is set");
    }

    private void updateGoToSleepText(String text){
        goToSleepTime_text1.setText(Html.fromHtml(text));
    }

    private void getWakeUpTime(){ // gets the right time for wake up
        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND,0);
        c.add(Calendar.MINUTE,1);// for the sake of testing, the alarm will ring after 1 minute starting from the moment the user presses the button; normally it must by at least 450 minutes.
        startAlarm(c);
        updateAlarmStatus(c);
        String text = "If you are going to sleep right now you will get 5 full sleep cycle at <i><font color = red>" + DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime())
                    +"</i></font>.";
        updateGoToSleepText(text);
    }

    private void startAlarm (Calendar c){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);

        if(c.before(Calendar.getInstance())){ //add 1 day to the input time if the user picks a time which is before the current time
            c.add(Calendar.DATE,1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this,"Alarm set", duration);
        toast.show();
    }

    private void CancelAlarm(){
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1,intent,0);
        alarmManager.cancel(pendingIntent);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this,"Alarm canceled", duration);
        toast.show();
        alarm_status.setText("No alarm set");
        alarmButtonON.setText("Set alarm");
        String text ="";
        updateGoToSleepText(text);
    }

}
