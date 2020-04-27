package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class SetUpAlarmActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_up_alarm_layout);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setUpTextViews();

        ImageButton info = findViewById(R.id.infoCycles);
        info.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageButton view = (ImageButton ) v;
                        view.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:

                    //TODO add a dialog box which shows more information about the number o sleep cycles and the minutes for falling asleep;

                    case MotionEvent.ACTION_CANCEL: {
                        ImageButton view = (ImageButton) v;
                        view.getBackground().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return true;
            }
        });
        Button browse = findViewById(R.id.browse);
        browse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SetUpAlarmActivity.this, SongListActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        Button cancel = findViewById(R.id.cancel_alarm_wake_up_time);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(AlarmConfiguration.calcWakeUpTimeAlarmConf.getRingtoneName() != null)
                    AlarmConfiguration.calcWakeUpTimeAlarmConf.setRingtoneName(null);
                finish();
            }
        });
    }

    private void setUpTextViews(){
        TextView chooseASong = findViewById(R.id.chooseASong_textView);
        TextView chosenSongName = findViewById(R.id.songNameChosen_textView);
        if(AlarmConfiguration.calcWakeUpTimeAlarmConf.getRingtoneName() != null) {
            chosenSongName.setText(AlarmConfiguration.calcWakeUpTimeAlarmConf.getRingtoneName());
            chooseASong.setText(R.string.selectedRingtone);
        } else {
            chooseASong.setText(R.string.noRingtoneSelected);
            chooseASong.setText(R.string.chooseARingtone);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpTextViews();
    }
}
