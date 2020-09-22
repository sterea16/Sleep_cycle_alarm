package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Calendar;

public class SongListActivity extends AppCompatActivity {
    private boolean isPaused;
    private MediaPlayer mediaPlayer = null;
    private String rawFileSongName;
    private int radioCheckedId;
    private String songName;
    private Configurator configurator;
    static final String CHECK_ALARM_TYPE = SongListActivity.class.getSimpleName();

    private void prepareConfiguration(int alarmType){
        if (alarmType == Configurator.WAKE_UP_TIME_KNOWN_ALARM_REQ_CODE) {
            configurator = Configurator.wakeUpTimeKnownConf;
        } else if (alarmType == Configurator.BED_TIME_KNOWN_ALARM_REQ_CODE) {
            configurator = Configurator.bedTimeKnownConf;
        } else if (alarmType == Configurator.NAP_TIME_ALARM_REQ_CODE) {
            configurator= Configurator.napTimeConf;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_song_list_layout);

        int alarmType = getIntent().getExtras().getInt(CHECK_ALARM_TYPE);
        prepareConfiguration(alarmType);

        SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        boolean isConfigured = savedPreferences.getBoolean(configurator.getIsConfiguredKey(), false);

        RadioGroup radioGroup = findViewById(R.id.group_radio_songs);

        if(isConfigured) {
            configurator.setRingtoneIndexPosition(savedPreferences.getInt(configurator.getRingtoneIndexPositionKey(), 0));
            RadioButton radioButton = findViewById(configurator.getRingtoneIndexPosition());
            if(radioButton != null) {
                radioButton.setChecked(true);
                getSong(configurator.getRingtoneIndexPosition());
            }
        } else {
            getSong(radioGroup.getCheckedRadioButtonId());
        }


        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            getSong(checkedId);
        });

        View confirmButton = findViewById(R.id.confirm_song);
        confirmButton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            configurator.setRawFileSongName(rawFileSongName)
                        .setRingtoneIndexPosition(radioCheckedId)
                        .setRingtoneName(songName);
            //save changes
            SharedPreferences savedConfiguration = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
            SharedPreferences.Editor editor = savedConfiguration.edit();
            editor.putInt(configurator.getRingtoneIndexPositionKey(), configurator.getRingtoneIndexPosition())
                    .putString(configurator.getRawFileSongNameKey(), configurator.getRawFileSongName())
                    .putString(configurator.getRingtoneNameKey(), configurator.getRingtoneName());
            editor.apply();
            finish();
            if(configurator == Configurator.napTimeConf && !configurator.isConfigured()){
                Calendar currentTime = Calendar.getInstance();
                configurator.calcAlarmTime(currentTime, configurator.getNapDuration())
                            .setAlarmHour(configurator.getAlarmTime().get(Calendar.HOUR_OF_DAY))
                            .setAlarmMinutes(configurator.getAlarmTime().get(Calendar.MINUTE))
                            .setAlarmState(true)
                            .setConfigured(true);

                editor.putInt(Configurator.ALARM_HOUR_NAP_TIME_KEY, configurator.getAlarmHour())
                        .putInt(Configurator.ALARM_MINUTES_NAP_TIME_KEY, configurator.getAlarmMinutes())
                        .putInt(Configurator.NAP_DURATION_KEY, configurator.getNapDuration())
                        .putBoolean(Configurator.ALARM_STATE_NAP_TIME_KEY, true)
                        .putBoolean(Configurator.IS_NAP_TIME_CONFIGURED_KEY, true);
                editor.apply();

                Alarm napAlarm = new Alarm(configurator.getAlarmTime(), this, configurator.getRequestCode());
                napAlarm.register();
                Notification notification = new Notification(this, Notification.NAP, configurator.getRequestCode());
                notification.trigger();
            }
        });
    }

    private void getSong(int checkedId){
        RadioButton radioButton =  findViewById(checkedId);

        switch (checkedId){
            case R.id.horn_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.air_horn_in_close_hall_series);
                rawFileSongName = getResources().getResourceName(R.raw.air_horn_in_close_hall_series);
                break;
            case R.id.all_that_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.allthat);
                rawFileSongName = getResources().getResourceName(R.raw.allthat);
                break;
            case R.id.a_new_beginning_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.anewbeginning);
                rawFileSongName = getResources().getResourceName(R.raw.anewbeginning);
                break;
            case R.id.ceausescu_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.ceausescu_alo);
                rawFileSongName = getResources().getResourceName(R.raw.ceausescu_alo);
                break;
            case R.id.cig_swaag_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.cig_swaag);
                rawFileSongName = getResources().getResourceName(R.raw.cig_swaag);
                break;
            case R.id.creative_minds_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.creativeminds);
                rawFileSongName = getResources().getResourceName(R.raw.creativeminds);
                break;
            case R.id.dubstep_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.dubstep);
                rawFileSongName = getResources().getResourceName(R.raw.dubstep);
                break;
            case R.id.funny_song_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.funnysong);
                rawFileSongName = getResources().getResourceName(R.raw.funnysong);
                break;
            case R.id.hey_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.hey);
                rawFileSongName = getResources().getResourceName(R.raw.hey);
                break;
            case R.id.skull_fire_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.skull_fire);
                rawFileSongName = getResources().getResourceName(R.raw.skull_fire);
                break;
            case R.id.spaceship_alarm_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.spaceship_alarm);
                rawFileSongName = getResources().getResourceName(R.raw.spaceship_alarm);
                break;
            case R.id.summer_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.summer);
                rawFileSongName = getResources().getResourceName(R.raw.summer);
                break;
        }
        songName = radioButton.getText().toString();
        radioCheckedId = checkedId;

        radioButton.setOnClickListener(v -> {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isPaused){
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null){
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
