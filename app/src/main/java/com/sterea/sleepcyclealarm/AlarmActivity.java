package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;



public class AlarmActivity extends AppCompatActivity {
    private TextView textWakeUp;
    private Button dismissButton;
    private MediaPlayer r;
    final static String TAG  = AlarmActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_layout);

        //pops out the activity even if the phone is on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences savedPreferences = getApplicationContext().getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
        Log.d(TAG, savedPreferences.getString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP, getResources().getResourceName(R.raw.ceausescu_alo)) + " Macin");
        String fileName = savedPreferences.getString(Configurator.RAW_FILE_NAME_KNOWN_WAKE_UP, getResources().getResourceName(R.raw.ceausescu_alo));
        //setting up the ringtone notification
        int songId = getResources().getIdentifier(fileName, "raw", getPackageName());

        if (r == null) {
            r = MediaPlayer.create(this, songId);
            r.setLooping(true);
            r.start();
        }

        textWakeUp = findViewById(R.id.text_Wake_Up);
        dismissButton = findViewById(R.id.DismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Configurator.knownWakeUpTimeConf.setAlarmState(false);
                Configurator.knownWakeUpTimeConf.setConfChanged(true);
                SharedPreferences savedPreferences = getSharedPreferences(Configurator.SAVED_CONFIGURATION, MODE_PRIVATE);
                SharedPreferences.Editor editor = savedPreferences.edit();
                editor.putBoolean(Configurator.IS_KNOWN_WAKE_UP_ALARM_STATE, false);
                editor.apply();
                if(r != null && r.isPlaying()) {
                    r.pause();
                    r.release();
                    r = null;
                }
                finish();
                Intent i = new Intent(AlarmActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(r != null && r.isPlaying()) {
            r.pause();
            r.release();
            r = null;
        }
    }
}
