package com.example.gatherforgood;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomeViewPagerAdapter extends FragmentStateAdapter {
    public HomeViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch(position){
            case 0:
                return new HomeFragment();
            case 1:
                return new PrayerFragment();
            case 3:
                return new EventsFragment();
            case 4:
                return new UserProfileFragment();

        }
        return new EmptyFragment();
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
