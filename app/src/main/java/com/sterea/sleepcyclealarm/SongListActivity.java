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
    private boolean isPlaying;
    private MediaPlayer mediaPlayer = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.song_list_activity_layout);

        RadioGroup radioGroup = findViewById(R.id.group_radio_songs);
        getSong(radioGroup.getCheckedRadioButtonId());

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPlaying = false;
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
                finish();
            }
        });
    }

    private void getSong(int checkedId){
        RadioButton radioButton =  findViewById(checkedId);

        switch (checkedId){
            case R.id.horn_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.air_horn_in_close_hall_series);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.all_that_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.allthat);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.a_new_beginning_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.anewbeginning);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.ceausescu_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.ceausescu_alo);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.cig_swaag_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.cig_swaag);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.creative_minds_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.creativeminds);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.dubstep_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.dubstep);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.funny_song_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.funnysong);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.hey_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.hey);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.skull_fire_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.skull_fire);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.spaceship_alarm_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.spaceship_alarm);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
            case R.id.summer_radio:
                mediaPlayer = MediaPlayer.create(this,R.raw.summer);
                AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(radioButton.getText().toString());
                break;
        }

        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    mediaPlayer.pause();
                    isPlaying = false;
                } else {
                    mediaPlayer.start();
                    isPlaying = true;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isPlaying){
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null){
            mediaPlayer.pause();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null){
            mediaPlayer.pause();
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
