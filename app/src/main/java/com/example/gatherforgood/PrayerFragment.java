package com.example.gatherforgood;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class PrayerFragment extends Fragment {

    RecyclerView rvPrayerGatherings;
    PrayerGatheringAdapter adapter;
    Button btnCreateGathering;
    ProgressBar progressBar;
    LinearLayout tvEmpty;

    ArrayList<PrayerGathering> gatheringsList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prayer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        init(view);
        setListeners();
        fetchGatherings();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh list when coming back from CreatePrayer
        fetchGatherings();
    }

    public void init(View view) {
        rvPrayerGatherings = view.findViewById(R.id.rvPrayerGatherings);
        btnCreateGathering = view.findViewById(R.id.btnCreateGathering);
        progressBar        = view.findViewById(R.id.progressBar);
        tvEmpty            = view.findViewById(R.id.tvEmpty);

        rvPrayerGatherings.setHasFixedSize(true);
        rvPrayerGatherings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PrayerGatheringAdapter(getContext(), gatheringsList);
        rvPrayerGatherings.setAdapter(adapter);
    }

    public void setListeners() {
        btnCreateGathering.setOnClickListener(v -> navigateToCreatePrayerScreen());
    }

    private void fetchGatherings() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPrayerGatherings.setVisibility(View.GONE);

        FirebaseFirestore.getInstance()
                .collection("prayerGatherings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    gatheringsList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                        gatheringsList.add(gathering);
                    }

                    progressBar.setVisibility(View.GONE);

                    if (gatheringsList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvPrayerGatherings.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvPrayerGatherings.setVisibility(View.VISIBLE);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    rvPrayerGatherings.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(),
                            "Failed to load gatherings: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    public void navigateToCreatePrayerScreen() {
        Intent intent = new Intent(getContext(), CreatePrayer.class);
        startActivity(intent);
    }
}