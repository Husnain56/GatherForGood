package com.example.gatherforgood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class HomeScreen extends AppCompatActivity {

    ViewPager2 homeViewPager;
    HomeViewPagerAdapter adapter;
    TabLayout homeTL;
    TabLayoutMediator mediator;

    int[] fill_icons;
    int[] no_fill_icons;
    String[] titles;

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

    public void init() {
        initData();
        initViews();
        setupViewPager();
        setupTabLayout();
        setupTabCustomViews();
        setupListeners();
    }

    public void initData() {
        titles = new String[]{"Home", "Prayers", "Events", "Profile"};
        fill_icons   = new int[]{R.drawable.ic_home_nofill, R.drawable.ic_mosque_nofill, R.drawable.ic_event_nofill, R.drawable.ic_person_nofill};
        no_fill_icons = new int[]{R.drawable.ic_home_fill,   R.drawable.ic_mosque_fill,   R.drawable.ic_event_fill,   R.drawable.ic_person_fill};
    }

    public void initViews() {
        homeTL        = findViewById(R.id.homeTL);
        homeViewPager = findViewById(R.id.homeViewPager);
    }

    public void setupViewPager() {
        adapter = new HomeViewPagerAdapter(this);
        homeViewPager.setAdapter(adapter);
        homeViewPager.setOffscreenPageLimit(1);
        homeViewPager.setUserInputEnabled(false);
    }

    public void setupTabLayout() {
        mediator = new TabLayoutMediator(homeTL, homeViewPager, (tab, i) -> {
            if (i < titles.length) tab.setText(titles[i]);
        });
        mediator.attach();

        TabLayout.Tab eventsTab = homeTL.getTabAt(2);
        if (eventsTab != null) {
            eventsTab.view.setClickable(false);
            eventsTab.view.setAlpha(0.4f);
        }
    }

    public void setupTabCustomViews() {
        for (int i = 0; i < homeTL.getTabCount(); i++) {
            View tabView = LayoutInflater.from(this).inflate(R.layout.home_custom_tab, null);
            TextView text = tabView.findViewById(R.id.tabText);
            ImageView icon = tabView.findViewById(R.id.tabIcon);
            icon.setImageResource(fill_icons[i]);
            text.setText(titles[i]);
            Objects.requireNonNull(homeTL.getTabAt(i)).setCustomView(tabView);
        }
    }

    public void setupListeners() {
        homeTL.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) return;
                homeViewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        homeViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabIcons(position);
            }
        });
    }

    public void updateTabIcons(int selectedPosition) {
        for (int i = 0; i < homeTL.getTabCount(); i++) {
            View tabView = Objects.requireNonNull(homeTL.getTabAt(i)).getCustomView();
            if (tabView == null) continue;
            ImageView icon = tabView.findViewById(R.id.tabIcon);
            if (i == selectedPosition) {
                icon.setImageResource(no_fill_icons[i]);
                icon.setColorFilter(android.graphics.Color.parseColor("#0fbd66"));
            } else {
                icon.setImageResource(fill_icons[i]);
                icon.clearColorFilter();
            }
        }
    }

    public void navigateToTab(int position) {
        homeViewPager.setCurrentItem(position, false);
    }
}