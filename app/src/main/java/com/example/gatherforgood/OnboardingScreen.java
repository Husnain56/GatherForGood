package com.example.gatherforgood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingScreen extends AppCompatActivity {

    ViewPager2 vpOnboarding;
    OnboardingViewPagerAdapter adapter;
    SharedPreferences sPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    public void init(){
        vpOnboarding = findViewById(R.id.vpOnboarding);
        adapter = new OnboardingViewPagerAdapter(this);
        vpOnboarding.setAdapter(adapter);
        sPref = getSharedPreferences("user",MODE_PRIVATE);
    }
    public void navigateToNextPage(int currentPage) {
        if (currentPage < 2) {
            vpOnboarding.setCurrentItem(currentPage + 1);
        } else {
            sPref.edit().putBoolean("hasSeenOnboarding", true).apply();
            startActivity(new Intent(this, LoginScreen.class));
            finish();
        }
    }
}