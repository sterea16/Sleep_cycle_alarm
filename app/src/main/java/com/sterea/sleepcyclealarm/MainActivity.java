package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sterea.sleepcyclealarm.Model.Alarm.AlarmReceiver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private TextView alarm_status;
    private TextView goToSleepTime_text1;
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
        TextClock textClock = findViewById(R.id.textClock);
        textClock.is24HourModeEnabled();

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
        c.add(Calendar.MINUTE,6*90);// for the sake of testing, the alarm will ring after 1 minute starting from the moment the user presses the button; normally it must by at least 450 minutes.
        startAlarm(c);
        updateAlarmStatus(c);
        String text = "If you are going to sleep right now you will get 6 full sleep cycle at <i><font color = red>" + DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime())
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

    public void onClickAddAlarm(View view){
        /*DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(),"time picker");*/
        dialog_wake_up_time = new Dialog(this);
        dialog_wake_up_time.setContentView(R.layout.alarm_dialog_layout);

        Button create_alarm = (Button) dialog_wake_up_time.findViewById(R.id.create_alarm_wake_up_time);
        Button cancel_create = (Button) dialog_wake_up_time.findViewById(R.id.cancel_alarm_wake_up_time);
        ImageButton choose_song = (ImageButton) dialog_wake_up_time.findViewById(R.id.choose_song_dialog);

        dialog_wake_up_time.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_wake_up_time.show();

        create_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker t = dialog_wake_up_time.findViewById(R.id.spinner_time_picker);
                int hour, minute;
                if(Build.VERSION.SDK_INT < 23){
                    hour = t.getCurrentHour();
                    minute = t.getCurrentMinute();
                } else {
                    hour = t.getHour();
                    minute = t.getMinute();
                }
                onTimeSet(t, hour, minute);
                dialog_wake_up_time.dismiss();
            }
        });
        cancel_create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog_wake_up_time.dismiss();
            }
        });
        choose_song.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openSongListDialog();
            }
        });
    }

    public void openSongListDialog(){
        dialog_choose_song = new Dialog(MainActivity.this);
        dialog_choose_song.setContentView(R.layout.song_list_layout);

        FloatingActionButton confirm_song = dialog_choose_song.findViewById(R.id.confirm_song);
        RadioGroup radioGroup = dialog_choose_song.findViewById(R.id.group_radio_songs);
        int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        playOrPauseSelectedSong(checkedRadioButtonId);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(mediaPlayer!= null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                playOrPauseSelectedSong(checkedId);
            }
        });

        dialog_choose_song.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_choose_song.show();

        confirm_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                dialog_choose_song.dismiss();
            }
        });
    }

    public void playOrPauseSelectedSong(int checkedId){
        RadioButton checkedRadioButton = (RadioButton) dialog_choose_song.findViewById(checkedId);
        checkedRadioButton.setClickable(true);
        checkedRadioButton.setFocusable(true);
        mediaPlayer = null;

        switch (checkedId){
            case R.id.horn_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.air_horn_in_close_hall_series);
                mediaPlayer.start();
                break;
            case R.id.all_that_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.allthat);
                mediaPlayer.start();
                break;
            case R.id.a_new_beginning_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.anewbeginning);
                mediaPlayer.start();
                break;
            case R.id.ceausescu_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.ceausescu_alo);
                mediaPlayer.start();
                break;
            case R.id.cig_swaag_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.cig_swaag);
                mediaPlayer.start();
                break;
            case R.id.creative_minds_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.creativeminds);
                mediaPlayer.start();
                break;
            case R.id.dubstep_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.dubstep);
                mediaPlayer.start();
                break;
            case R.id.funny_song_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.funnysong);
                mediaPlayer.start();
                break;
            case R.id.hey_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.hey);
                mediaPlayer.start();
                break;
            case R.id.skull_fire_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.skull_fire);
                mediaPlayer.start();
                break;
            case R.id.spaceship_alarm_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.spaceship_alarm);
                mediaPlayer.start();
                break;
            case R.id.summer_radio:
                mediaPlayer = MediaPlayer.create(dialog_choose_song.getContext(), R.raw.summer);
                mediaPlayer.start();
                break;
        }

        checkedRadioButton.setOnClickListener(new View.OnClickListener() {
            boolean playing;
            @Override
            public void onClick(View v) {
                if(playing) {
                    mediaPlayer.pause();
                    playing = false;
                } else {
                    mediaPlayer.start();
                    playing = true;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

}
