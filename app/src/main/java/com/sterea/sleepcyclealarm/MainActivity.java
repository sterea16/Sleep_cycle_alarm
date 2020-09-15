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
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    /*TODO Create a Preference hierarchy (a.k.a settings fragment) for notifications and sleep cycle value
    *  https://developer.android.com/guide/topics/ui/settings
    * TODO Swipe to refresh https://developer.android.com/training/swipe*/
    private static final int NUM_PAGES = 3;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);

        // Create and register notifications channels.
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

        /*
         * The pager adapter, which provides the pages to the view pager widget.
         */
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabDots);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
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
}
