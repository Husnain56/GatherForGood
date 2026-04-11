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
    TextView tvSectionLabel;

    // Filter chips
    TextView chipAll, chipFajr, chipZuhr, chipAsr, chipMaghrib, chipIsha, chipJumuah;

    // Currently active filter — null means "All"
    String activeFilter = null;

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
        fetchGatherings();
    }

    public void init(View view) {
        rvPrayerGatherings = view.findViewById(R.id.rvPrayerGatherings);
        btnCreateGathering = view.findViewById(R.id.btnCreateGathering);
        progressBar        = view.findViewById(R.id.progressBar);
        tvEmpty            = view.findViewById(R.id.tvEmpty);
        tvSectionLabel     = view.findViewById(R.id.tvSectionLabel);

        chipAll     = view.findViewById(R.id.chipAll);
        chipFajr    = view.findViewById(R.id.chipFajr);
        chipZuhr    = view.findViewById(R.id.chipZuhr);
        chipAsr     = view.findViewById(R.id.chipAsr);
        chipMaghrib = view.findViewById(R.id.chipMaghrib);
        chipIsha    = view.findViewById(R.id.chipIsha);
        chipJumuah  = view.findViewById(R.id.chipJumuah);

        rvPrayerGatherings.setHasFixedSize(true);
        rvPrayerGatherings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PrayerGatheringAdapter(getContext(), gatheringsList);
        rvPrayerGatherings.setAdapter(adapter);
    }

    public void setListeners() {
        btnCreateGathering.setOnClickListener(v -> navigateToCreatePrayerScreen());

        chipAll.setOnClickListener(v     -> selectFilter(null,       chipAll));
        chipFajr.setOnClickListener(v    -> selectFilter("Fajr",     chipFajr));
        chipZuhr.setOnClickListener(v    -> selectFilter("Zuhr",     chipZuhr));
        chipAsr.setOnClickListener(v     -> selectFilter("Asr",      chipAsr));
        chipMaghrib.setOnClickListener(v -> selectFilter("Maghrib",  chipMaghrib));
        chipIsha.setOnClickListener(v    -> selectFilter("Isha",     chipIsha));
        chipJumuah.setOnClickListener(v  -> selectFilter("Jumuah",   chipJumuah));
    }

    private void selectFilter(String prayerType, TextView selectedChip) {
        if (activeFilter == null && prayerType == null) return;
        if (prayerType != null && prayerType.equals(activeFilter)) return;

        activeFilter = prayerType;
        updateChipStyles(selectedChip);
        updateSectionLabel();
        fetchGatherings();
    }

    private void updateChipStyles(TextView activeChip) {
        TextView[] allChips = {
                chipAll, chipFajr, chipZuhr, chipAsr,
                chipMaghrib, chipIsha, chipJumuah
        };

        for (TextView chip : allChips) {
            if (chip == activeChip) {
                chip.setBackgroundResource(R.drawable.prayer_chip_active);
                chip.setTextColor(requireContext().getColor(android.R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.prayer_chip_inactive);
                chip.setTextColor(android.graphics.Color.parseColor("#80E5C76B"));
            }
        }
    }

    private void updateSectionLabel() {
        if (tvSectionLabel == null) return;
        if (activeFilter == null) {
            tvSectionLabel.setText("NEARBY PRAYERS");
        } else {
            tvSectionLabel.setText(activeFilter.toUpperCase() + " PRAYERS");
        }
    }

    private void fetchGatherings() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPrayerGatherings.setVisibility(View.GONE);

        Query query = FirebaseFirestore.getInstance()
                .collection("prayerGatherings")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // Apply prayer type filter if one is selected
        if (activeFilter != null) {
            query = query.whereEqualTo("prayerType", activeFilter);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    gatheringsList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                        gathering.setId(doc.getId());
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