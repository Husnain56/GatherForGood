package com.example.gatherforgood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeScreen extends AppCompatActivity {

    ViewPager2 homeViewPager;
    HomeViewPagerAdapter adapter;
    TabLayout homeTL;
    TabLayoutMediator mediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }
    public void init(){
        String[] titles = {"Home", "Prayers", "", "Events", "Profile"};

        homeTL = findViewById(R.id.homeTL);
        homeViewPager = findViewById(R.id.homeViewPager);
        adapter = new HomeViewPagerAdapter(this);
        homeViewPager.setAdapter(adapter);
        mediator = new TabLayoutMediator(homeTL, homeViewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int i) {
                switch(i){
                    case 0:
                        tab.setText(titles[0]);
                        break;
                    case 1:
                        tab.setText(titles[1]);
                        break;
                    case 2:
                        tab.setText(titles[2]);
                        break;
                    case 3:
                        tab.setText(titles[3]);
                        break;
                    default:
                        tab.setText(titles[0]);
                }
            }
        });
        mediator.attach();

        int[] icons = {R.drawable.ic_home, R.drawable.ic_mosque, 0, R.drawable.ic_calendar, R.drawable.ic_profile};

        for (int i = 0; i < homeTL.getTabCount(); i++) {
            View tabView = LayoutInflater.from(this).inflate(R.layout.home_custom_tab, null);
            ImageView icon = tabView.findViewById(R.id.tabIcon);
            TextView text = tabView.findViewById(R.id.tabText);
            icon.setImageResource(icons[i]);
            text.setText(titles[i]);
            homeTL.getTabAt(i).setCustomView(tabView);
        }
    }
}