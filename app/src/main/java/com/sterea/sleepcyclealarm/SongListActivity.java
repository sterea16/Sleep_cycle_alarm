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
    private RadioGroup radioGroup;
    private int indexPosition; // used to save the checked radio button
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.song_list_activity_layout);

        radioGroup = findViewById(R.id.group_radio_songs);

        if(Configurator.knownWakeUpTimeConf.getIndexPosition() !=0 ){
            RadioButton radioButton = findViewById(Configurator.knownWakeUpTimeConf.getIndexPosition());
            radioButton.setChecked(true);
            getSong(Configurator.knownWakeUpTimeConf.getIndexPosition());
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

        FloatingActionButton confirmSong = findViewById(R.id.confirm_song);
        confirmSong.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        confirmSong.setClickable(true);
        confirmSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                finish();
            }
        });
    }

    private void getSong(int checkedId){
        RadioButton radioButton =  findViewById(checkedId);
        indexPosition = radioGroup.indexOfChild(radioButton);
        switch (checkedId){
            case R.id.horn_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.air_horn_in_close_hall_series);
                break;
            case R.id.all_that_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.allthat);
                break;
            case R.id.a_new_beginning_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.anewbeginning);
                break;
            case R.id.ceausescu_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.ceausescu_alo);
                break;
            case R.id.cig_swaag_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.cig_swaag);
                break;
            case R.id.creative_minds_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.creativeminds);
                break;
            case R.id.dubstep_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.dubstep);
                break;
            case R.id.funny_song_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.funnysong);
                break;
            case R.id.hey_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.hey);
                break;
            case R.id.skull_fire_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.skull_fire);
                break;
            case R.id.spaceship_alarm_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.spaceship_alarm);
                break;
            case R.id.summer_radio:
                mediaPlayer = MediaPlayer.create(this, R.raw.summer);
                break;
        }

        Configurator.knownWakeUpTimeConf.setRingtoneName(radioButton.getText().toString());
        Configurator.knownWakeUpTimeConf.setIndexPosition(checkedId);

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
