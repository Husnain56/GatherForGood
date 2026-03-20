package com.example.gatherforgood;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingViewPagerAdapter extends FragmentStateAdapter
{
    public OnboardingViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new onboarding1();
            case 1:
                return new onboarding2();
            case 2:
                return new onboarding3();
            default:
                return new onboarding1();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
