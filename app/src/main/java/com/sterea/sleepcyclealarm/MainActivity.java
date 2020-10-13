package com.sterea.sleepcyclealarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    /*TODO Create a Preference hierarchy (a.k.a settings fragment) for notifications and sleep cycle value
    *  https://developer.android.com/guide/topics/ui/settings*/
    private static final int NUM_PAGES = 3;
    private ViewPager2 viewPager2;
    static final String REQUESTED_TAB = MainActivity.class.getName() + " REQUESTED_TAB ";
    CountDownTimer countDownTimer;
    private TextView header;
    private TextView progressTextView;
    private TextClock textClock;
    private TextView belowTextClock;
    private ProgressBar progressBar;
    private Controller controller;

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        progressBar = findViewById(R.id.progress_bar);
        progressTextView = findViewById(R.id.text_view_progress);
        header = findViewById(R.id.header_textView);
        textClock = findViewById(R.id.textClock);
        belowTextClock = findViewById(R.id.belowClockText_textView);
        controller = new Controller(this);
        controller.registerSharedPrefListener();
        controller.initialiseConfigs();
        createNotificationChannels();
        setPagesAndTabs();
        getLastTab();
    }

    private void createNotificationChannels(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            Notification.ChannelBuilder.createNotificationChannel(this, Notification.ChannelBuilder.ALARM_CHANNEL_ID,
                    getResources().getString(R.string.channel_name_alarm),
                    getResources().getString(R.string.channel_description_alarm), NotificationManager.IMPORTANCE_HIGH);

            Notification.ChannelBuilder.createNotificationChannel(this, Notification.ChannelBuilder.REMINDER_CHANNEL_ID,
                    getResources().getString(R.string.channel_name_reminder), getResources().getString(R.string.channel_description_reminder),
                    NotificationManagerCompat.IMPORTANCE_DEFAULT);

            Notification.ChannelBuilder.createNotificationChannel(this, Notification.ChannelBuilder.SNOOZE_CHANNEL_ID,
                    getResources().getString(R.string.channel_name_snooze), getResources().getString(R.string.channel_description_snooze),
                    NotificationManagerCompat.IMPORTANCE_LOW);
        }
    }

    private void setPagesAndTabs(){
        // The pager adapter, which provides the pages to the view pager widget.
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager2 = findViewById(R.id.viewPager2);
        viewPager2.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabDots);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    //setting up the tabs
                    switch(position){
                        case 0:
                            tab.setText("bed time");
                            tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_bed_time, getTheme()));
                            break;
                        case 1:
                            tab.setText("waking time");
                            tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_wake_up_time, null));
                            break;
                        case 2:
                            tab.setText("nap time");
                            tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_nap_time, getTheme()));
                            break;
                    }
                });
        tabLayoutMediator.attach();
        viewPager2.setOffscreenPageLimit(1);
    }

    private void getLastTab(){
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        int tab = 0;
        if (bundle != null){
            tab = bundle.getInt(REQUESTED_TAB);
        }
        viewPager2.setCurrentItem(tab - 1);
    }

    @Override
    public void onBackPressed() {
        if (viewPager2.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
        }
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter{

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 0:
                    return new WakeUpTimeFragment(Configurator.wakeUpTimeKnownConf);
                case 1:
                    return new BedTimeFragment(Configurator.bedTimeKnownConf);
                case 2:
                    return new NapTimeFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    private void countDownAlarms(long startTime, long endTime, String countDownTitle) {
        textClock.setVisibility(View.GONE);
        belowTextClock.setVisibility(View.GONE);
        header.setVisibility(View.VISIBLE);
        header.setText(countDownTitle);
        progressBar.setVisibility(View.VISIBLE);
        progressTextView.setVisibility(View.VISIBLE);
        long totalAmountOfTime = endTime - startTime;
        long millisInFuture = endTime - Calendar.getInstance().getTimeInMillis();

        if(countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long progress = ((millisUntilFinished) * 100) / totalAmountOfTime;
                progressBar.setProgress((int) progress);
                long secondsLeft = millisUntilFinished / 1000;
                long hours = secondsLeft / (3600);
                secondsLeft = secondsLeft - (hours * 3600);
                long minutes = secondsLeft / (60);
                secondsLeft = secondsLeft - (minutes * 60);
                long seconds = secondsLeft;
                progressTextView.post(() -> {
                    String remainingTime;
                    String sHours = "";
                    String sMinutes, sSeconds;
                    SpannableString spannableString;
                    if(hours >= 10) {
                        sHours = hours + ":";
                    } else if(hours > 0) {
                        sHours = "0" + hours + ":";
                    }

                    if(minutes >= 10){
                        sMinutes = minutes + ":";
                    } else {
                        sMinutes = "0" + minutes + ":";
                    }

                    if(seconds >= 10) {
                        sSeconds = String.valueOf(seconds);
                    } else {
                        sSeconds = "0" + seconds;
                    }
                    remainingTime = sHours + sMinutes + sSeconds;
                    spannableString = new SpannableString(remainingTime);
                    spannableString.setSpan(new AbsoluteSizeSpan(12, true),remainingTime.length() - 3, remainingTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    progressTextView.setText(spannableString);
                });
            }

            @Override
            public void onFinish() {
                if(countDownTitle.equals(getResources().getString(R.string.next_bed_time))){
                    updateHeader();
                }
            }
        };
        countDownTimer.start();
    }

    public void updateHeader(){
        long startTime, endTime;
        String title;
        controller.makeConfigsList();
        if(controller.isNapTime()) {
            startTime = Configurator.napTimeConf.getBedTimeTimeStamp();
            endTime = Configurator.napTimeConf.getAlarmTimeTimeStamp();
            title = getResources().getString(R.string.nap_ends_in);
            countDownAlarms(startTime, endTime, title);
            Log.d("headerStatus","start = " + startTime + " end = " + endTime);
        } else if(controller.isAnyAlarmOn()){
            if(controller.getSoonestBedTime() > (Calendar.getInstance().getTimeInMillis())) {
                startTime = controller.getAlarmRegistrationMoment();
                endTime = controller.getSoonestBedTime();
                title = getResources().getString(R.string.next_bed_time);
            } else {
                startTime = controller.getSoonestBedTime();
                endTime = controller.getSoonestAlarmTime();
                title = getResources().getString(R.string.next_alarm_time);
            }
            Log.d("headerStatus","start = " + startTime + " end = " + endTime);
            countDownAlarms(startTime, endTime, title);
        } else {
            if(countDownTimer != null) {
                countDownTimer.cancel();
                progressBar.setVisibility(View.GONE);
                progressTextView.setVisibility(View.GONE);
                header.setVisibility(View.GONE);
            }
            textClock.setVisibility(View.VISIBLE);
            belowTextClock.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        updateHeader();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.unregisterSharedPrefListener();
    }
}
