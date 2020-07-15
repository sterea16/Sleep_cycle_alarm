package com.sterea.sleepcyclealarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    //TODO Create a new Class for all the methods that use the alarm
    /*TODO Create a Preference hierarchy (a.k.a settings fragment) for notifications and sleep cycle value
    *  https://developer.android.com/guide/topics/ui/settings*/

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);//removes the title of the toolbar (this is the main activity and its label it's required in order to give a name to the app launcher)
    }
}
