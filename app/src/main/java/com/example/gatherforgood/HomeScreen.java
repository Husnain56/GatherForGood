package com.example.gatherforgood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
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

    int[] fill_icons;
    int[] no_fill_icons;

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
        fill_icons = new int[]{R.drawable.ic_home_nofill, R.drawable.ic_mosque_nofill, 0, R.drawable.ic_event_nofill, R.drawable.ic_person_nofill};
        no_fill_icons = new int[]{R.drawable.ic_home_fill, R.drawable.ic_mosque_fill, 0, R.drawable.ic_event_fill, R.drawable.ic_person_fill};

        homeTL = findViewById(R.id.homeTL);
        homeViewPager = findViewById(R.id.homeViewPager);
        adapter = new HomeViewPagerAdapter(this);
        homeViewPager.setAdapter(adapter);
        mediator = new TabLayoutMediator(homeTL, homeViewPager, (tab, i) -> {
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
        });
        mediator.attach();

        for (int i = 0; i < homeTL.getTabCount(); i++) {
            View tabView = LayoutInflater.from(this).inflate(R.layout.home_custom_tab, null);
            ImageView icon = tabView.findViewById(R.id.tabIcon);
            icon.setImageResource(fill_icons[i]);
            homeTL.getTabAt(i).setCustomView(tabView);
        }

        homeViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                for (int i = 0; i < homeTL.getTabCount(); i++) {
                    View tabView = homeTL.getTabAt(i).getCustomView();
                    if (tabView == null) continue;
                    ImageView icon = tabView.findViewById(R.id.tabIcon);
                    if (i == position) {
                        icon.setImageResource(no_fill_icons[i]);
                        icon.setColorFilter(android.graphics.Color.parseColor("#0fbd66"));
                    } else {
                        icon.setImageResource(fill_icons[i]);
                        icon.clearColorFilter();
                    }
                }
            }
        });
    }
}