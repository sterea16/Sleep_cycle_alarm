package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongListActivity extends AppCompatActivity {
    private boolean isPaused;
    private MediaPlayer mediaPlayer = null;
    private String rawFileSongName;
    private int radioCheckedId;
    private String songName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.song_list_activity_layout);

        RadioGroup radioGroup = findViewById(R.id.group_radio_songs);

        if(Configurator.knownWakeUpTimeConf.getSongIndexPosition() != 0 ){
            RadioButton radioButton = findViewById(Configurator.knownWakeUpTimeConf.getSongIndexPosition());
            radioButton.setChecked(true);
            getSong(Configurator.knownWakeUpTimeConf.getSongIndexPosition());
        } else {
            getSong(radioGroup.getCheckedRadioButtonId());
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                getSong(checkedId);
            }
        });

        FloatingActionButton confirmFAB = findViewById(R.id.confirm_song);
        confirmFAB.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        confirmFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Configurator.knownWakeUpTimeConf.setRawFileSongName(rawFileSongName);
                Configurator.knownWakeUpTimeConf.setSongIndexPosition(radioCheckedId);
                Configurator.knownWakeUpTimeConf.setRingtoneName(songName);
                finish();
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

        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
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
