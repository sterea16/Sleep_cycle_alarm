package com.example.sleepcyclealarm.Model.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

import com.example.sleepcyclealarm.AlarmActivity;


public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context,"Wake up!", duration);
        toast.show();
        intent = new Intent(context,AlarmActivity.class);
        context.startActivity(intent);
    }

}
