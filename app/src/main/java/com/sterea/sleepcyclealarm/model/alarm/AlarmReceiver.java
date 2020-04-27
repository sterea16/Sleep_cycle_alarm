package com.sterea.sleepcyclealarm.model.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

import com.sterea.sleepcyclealarm.AlarmActivity;


public class AlarmReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context,"Wake up!", duration);
        toast.show();
        intent = new Intent(context, AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
