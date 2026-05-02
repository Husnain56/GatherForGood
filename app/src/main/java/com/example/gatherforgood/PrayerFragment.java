package com.example.gatherforgood;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class PrayerFragment extends Fragment {

    RecyclerView rvPrayerGatherings;
    PrayerGatheringAdapter adapter;
    FloatingActionButton btnCreateGathering;
    Button btnMyGatherings;
    Button btnAllGatherings;
    ProgressBar progressBar;
    LinearLayout tvEmpty;
    TextView tvSectionLabel;
    boolean isMyGatherings = false;

    TextView chipAll, chipFajr, chipZuhr, chipAsr, chipMaghrib, chipIsha, chipJumuah;

    String activeFilter = null;
    double userLat = 0, userLng = 0;
    private static final float NEARBY_RADIUS_KM = 10f;

    FusedLocationProviderClient fusedClient;
    ArrayList<PrayerGathering> gatheringsList = new ArrayList<>();

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            fetchUserLocationThenGatherings();
                        } else {
                            fetchGatherings(0, 0);
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prayer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        init(view);
        setListeners();
        checkPermissionAndLoad();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermissionAndLoad();
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((HomeScreen) requireActivity()).navigateToTab(0);
                    }
                }
        );
    }

    public void init(View view) {
        rvPrayerGatherings = view.findViewById(R.id.rvPrayerGatherings);
        btnCreateGathering = view.findViewById(R.id.btnCreateGathering);
        btnMyGatherings    = view.findViewById(R.id.btnMyGatherings);
        btnAllGatherings   = view.findViewById(R.id.btnAllGatherings);
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

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        rvPrayerGatherings.setHasFixedSize(true);
        rvPrayerGatherings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PrayerGatheringAdapter(getContext(), gatheringsList);
        rvPrayerGatherings.setAdapter(adapter);

        setActiveButton(btnAllGatherings);
    }

    public void setListeners() {
        btnAllGatherings.setOnClickListener(v -> {
            isMyGatherings = false;
            setActiveButton(btnAllGatherings);
            activeFilter = null;
            updateChipStyles(chipAll);
            updateSectionLabel();
            fetchGatherings(userLat, userLng);
        });

        btnMyGatherings.setOnClickListener(v -> {
            isMyGatherings = true;
            setActiveButton(btnMyGatherings);
            activeFilter = null;
            updateChipStyles(chipAll);
            updateSectionLabel();
            loadMyGatherings();
        });

        btnCreateGathering.setOnClickListener(v -> navigateToCreatePrayerScreen());

        chipAll.setOnClickListener(v     -> selectFilter(null,      chipAll));
        chipFajr.setOnClickListener(v    -> selectFilter("Fajr",    chipFajr));
        chipZuhr.setOnClickListener(v    -> selectFilter("Zuhr",    chipZuhr));
        chipAsr.setOnClickListener(v     -> selectFilter("Asr",     chipAsr));
        chipMaghrib.setOnClickListener(v -> selectFilter("Maghrib", chipMaghrib));
        chipIsha.setOnClickListener(v    -> selectFilter("Isha",    chipIsha));
        chipJumuah.setOnClickListener(v  -> selectFilter("Jumuah",  chipJumuah));
    }

    private void setActiveButton(Button activeBtn) {
        Button[] buttons = {btnAllGatherings, btnMyGatherings};
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                btn.setBackgroundResource(R.drawable.register_bg_gender_active);
                btn.setTextColor(android.graphics.Color.WHITE);
            } else {
                btn.setBackgroundResource(R.drawable.register_bg_input_field);
                btn.setTextColor(android.graphics.Color.parseColor("#D4AF37"));
            }
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissionAndLoad() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fetchUserLocationThenGatherings();
        }
    }

    private void fetchUserLocationThenGatherings() {
        if (!hasLocationPermission()) {
            fetchGatherings(0, 0);
            return;
        }
        try {
            fusedClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            userLat = location.getLatitude();
                            userLng = location.getLongitude();
                            fetchGatherings(userLat, userLng);
                        } else {
                            fetchGatherings(0, 0);
                        }
                    })
                    .addOnFailureListener(e -> fetchGatherings(0, 0));
        } catch (SecurityException e) {
            fetchGatherings(0, 0);
        }
    }

    private void loadMyGatherings() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPrayerGatherings.setVisibility(View.GONE);

        Query query = FirebaseFirestore.getInstance()
                .collection("prayerGatherings")
                .whereEqualTo("hostUid", FirebaseAuth.getInstance().getUid());

        if (activeFilter != null) {
            query = query.whereEqualTo("prayerType", activeFilter);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    gatheringsList.clear();
                    long currentTime = System.currentTimeMillis();
                    long twentyMinutes = 20 * 60 * 1000;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                        gathering.setId(doc.getId());
                        long prayerTime = gathering.getCreatedAt();
                        if (currentTime > (prayerTime + twentyMinutes)) continue;
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
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchGatherings(double lat, double lng) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPrayerGatherings.setVisibility(View.GONE);

        Query query = FirebaseFirestore.getInstance()
                .collection("prayerGatherings");

        if (activeFilter != null) {
            query = query.whereEqualTo("prayerType", activeFilter);
        }

        query = query.orderBy("timeInMillis", Query.Direction.ASCENDING);

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    gatheringsList.clear();
                    long currentTime = System.currentTimeMillis();
                    long twentyMinutes = 20 * 60 * 1000;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                        gathering.setId(doc.getId());

                        long prayerTime = gathering.getTimeInMillis();
                        if (currentTime > (prayerTime + twentyMinutes)) continue;

                        if (lat != 0 && lng != 0) {
                            float distance = distanceBetween(lat, lng,
                                    gathering.getLatitude(), gathering.getLongitude());
                            if (distance <= NEARBY_RADIUS_KM) {
                                gatheringsList.add(gathering);
                            }
                        } else {
                            gatheringsList.add(gathering);
                        }
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

    private void selectFilter(String prayerType, TextView selectedChip) {
        if (activeFilter == null && prayerType == null) return;
        if (prayerType != null && prayerType.equals(activeFilter)) return;
        activeFilter = prayerType;
        updateChipStyles(selectedChip);
        updateSectionLabel();
        if (!isMyGatherings) {
            fetchGatherings(userLat, userLng);
        } else {
            loadMyGatherings();
        }
    }

    private void updateChipStyles(TextView activeChip) {
        TextView[] allChips = {chipAll, chipFajr, chipZuhr, chipAsr, chipMaghrib, chipIsha, chipJumuah};
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
        if (isMyGatherings) {
            tvSectionLabel.setText(activeFilter == null ? "MY GATHERINGS" : "MY " + activeFilter.toUpperCase() + " GATHERINGS");
        } else {
            tvSectionLabel.setText(activeFilter == null ? "NEARBY PRAYERS" : activeFilter.toUpperCase() + " PRAYERS");
        }
    }

    private float distanceBetween(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000f;
    }

    public void navigateToCreatePrayerScreen() {
        startActivity(new Intent(getContext(), CreatePrayer.class));
    }
}