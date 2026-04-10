package com.example.gatherforgood;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class PrayerFragment extends Fragment {

    RecyclerView rvPrayerGatherings;
    PrayerGatheringAdapter adapter;
    Button btnCreateGathering;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_prayer, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        init(view);
        setListeners();
    }

    private ArrayList<PrayerGathering> getHardcodedGatherings() {
        return MockDataHelper.getHardcodedGatherings();
    }

    public void init(View view){
        rvPrayerGatherings = view.findViewById(R.id.rvPrayerGatherings);
        btnCreateGathering = view.findViewById(R.id.btnCreateGathering);
        rvPrayerGatherings.setHasFixedSize(true);
        adapter = new PrayerGatheringAdapter(getContext(),getHardcodedGatherings());
        rvPrayerGatherings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPrayerGatherings.setAdapter(adapter);
    }

    public void setListeners(){
        btnCreateGathering.setOnClickListener(v->{
            navigateToCreatePrayerScreen();
        });
    }
    public void navigateToCreatePrayerScreen(){
        Intent intent = new Intent(getContext(), CreatePrayer.class);
        startActivity(intent);
    }
}