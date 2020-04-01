package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;



public class AlarmActivity extends AppCompatActivity {
    public TextView textWakeUp;
    public Ringtone r ;
    public Button dismissButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        //pops out the activity even if the phone is on lock screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //setting up the ringtone notification
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(this, notification);
        r.play();

        textWakeUp = findViewById(R.id.text_Wake_Up);
        dismissButton = findViewById(R.id.DismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r.stop();
                Intent intent = new Intent(AlarmActivity.this,MainActivity.class);
                AlarmActivity.this.startActivity(intent); //move back to the MainActivity of the app
            }
        });
    }


}
