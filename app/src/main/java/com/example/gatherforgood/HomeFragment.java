package com.example.gatherforgood;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    FrameLayout cardPrayerGatherings;
    FrameLayout cardVolunteerEvents;
    TextView tvUserName;
    SharedPreferences sPref;
    ImageView ivProfilePic;
    TextView tvSeeAll;
    RecyclerView rvPrayerGatherings;
    PrayerGatheringAdapter adapter;
    ProgressBar progressBar;

    ArrayList<PrayerGathering> gatheringsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        init(view);
        setEventListeners();
        fetchGatherings();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchGatherings();
    }

    public void init(View view) {
        rvPrayerGatherings = view.findViewById(R.id.rvPrayerGatherings);
        rvPrayerGatherings.setHasFixedSize(true);

        cardPrayerGatherings = view.findViewById(R.id.cardPrayerGatherings);
        cardVolunteerEvents  = view.findViewById(R.id.cardVolunteerEvents);
        tvUserName           = view.findViewById(R.id.tvUserName);
        ivProfilePic         = view.findViewById(R.id.ivProfilePic);
        tvSeeAll             = view.findViewById(R.id.tvSeeAll);
        progressBar          = view.findViewById(R.id.progressBar);

        sPref = requireContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        tvUserName.setText(sPref.getString("user_name", ""));

        adapter = new PrayerGatheringAdapter(getContext(), gatheringsList);
        rvPrayerGatherings.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPrayerGatherings.setAdapter(adapter);
    }

    private void fetchGatherings() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection("prayerGatherings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    gatheringsList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        gatheringsList.add(doc.toObject(PrayerGathering.class));
                    }
                    adapter.notifyDataSetChanged();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    public void setEventListeners() {
        cardPrayerGatherings.setOnClickListener(view -> ((HomeScreen) requireActivity()).navigateToTab(1));
        ivProfilePic.setOnClickListener(view -> ((HomeScreen) requireActivity()).navigateToTab(4));
        tvSeeAll.setOnClickListener(view -> ((HomeScreen) requireActivity()).navigateToTab(1));
    }
}