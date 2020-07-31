package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    /*TODO Create a Preference hierarchy (a.k.a settings fragment) for notifications and sleep cycle value
    *  https://developer.android.com/guide/topics/ui/settings
    * TODO Swipe to refresh https://developer.android.com/training/swipe*/

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);//removes the title of the toolbar (this is the main activity and its label it's required in order to give a name to the app launcher)

        // Create and register notifications channels.
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            AlarmNotification.ChannelBuilder.createNotificationChannel(this, AlarmNotification.ChannelBuilder.ALARM_CHANNEL_ID,
                    getResources().getString(R.string.channel_name_alarm),
                    getResources().getString(R.string.channel_description_alarm), NotificationManager.IMPORTANCE_HIGH);

            AlarmNotification.ChannelBuilder.createNotificationChannel(this, AlarmNotification.ChannelBuilder.REMINDER_CHANNEL_ID,
                    getResources().getString(R.string.channel_name_reminder), getResources().getString(R.string.channel_description_reminder),
                    NotificationManagerCompat.IMPORTANCE_DEFAULT);

            AlarmNotification.ChannelBuilder.createNotificationChannel(this, AlarmNotification.ChannelBuilder.SNOOZE_CHANNEL_ID,
                    getResources().getString(R.string.channel_name_snooze), getResources().getString(R.string.channel_description_snooze),
                    NotificationManagerCompat.IMPORTANCE_LOW);
        }

    }

}
