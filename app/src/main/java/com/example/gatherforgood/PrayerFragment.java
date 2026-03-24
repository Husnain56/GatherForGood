package com.example.gatherforgood;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class PrayerFragment extends Fragment {

    RecyclerView rvPrayerGatherings;
    PrayerGatheringAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_prayer, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        init(view);
    }

    private ArrayList<PrayerGathering> getHardcodedGatherings() {
        return MockDataHelper.getHardcodedGatherings();
    }

    public void init(View view){
        rvPrayerGatherings = view.findViewById(R.id.rvPrayerGatherings);
        rvPrayerGatherings.setHasFixedSize(true);
        adapter = new PrayerGatheringAdapter(getContext(),getHardcodedGatherings());
        rvPrayerGatherings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPrayerGatherings.setAdapter(adapter);


    }
}