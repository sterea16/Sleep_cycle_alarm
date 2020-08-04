package com.sterea.sleepcyclealarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    /*TODO Create a Preference hierarchy (a.k.a settings fragment) for notifications and sleep cycle value
    *  https://developer.android.com/guide/topics/ui/settings
    * TODO Swipe to refresh https://developer.android.com/training/swipe*/

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager2 viewPager;

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

        /**
         * The pager adapter, which provides the pages to the view pager widget.
         */
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabDots);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        //nothing to do here
                    }
                });
        tabLayoutMediator.attach();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter{

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 0:
                    return new KnownWakeUpTimeFragment();
                case 1:
                    return new KnownBedTimeFragment();
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
}
