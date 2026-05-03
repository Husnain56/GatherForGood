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

import androidx.activity.OnBackPressedCallback;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class PrayerFragment extends Fragment {

    RecyclerView           rvPrayerGatherings;
    PrayerGatheringAdapter adapter;
    FloatingActionButton   btnCreateGathering;
    Button                 btnMyGatherings, btnAllGatherings, btnJoinedGatherings;
    ProgressBar            progressBar;
    LinearLayout           tvEmpty;
    TextView               tvSectionLabel;

    boolean isMyGatherings     = false;
    boolean isJoinedGatherings = false;

    private ListenerRegistration activeListener;

    TextView chipAll, chipFajr, chipZuhr, chipAsr, chipMaghrib, chipIsha;

    String  activeFilter      = null;
    double  userLat           = 0, userLng = 0;
    String  currentUserGender = "";
    boolean genderResolved    = false;

    private static final float NEARBY_RADIUS_KM = 10f;

    FusedLocationProviderClient    fusedClient;
    ArrayList<PrayerGathering>     gatheringsList = new ArrayList<>();

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) fetchUserLocationThenGatherings();
                        else           fetchGatherings(0, 0);
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prayer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        init(view);
        setListeners();

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        ((HomeScreen) requireActivity()).navigateToTab(0);
                    }
                });

        fetchCurrentUserGender();
    }

    @Override
    public void onStop() {
        super.onStop();
        detachListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null
                && isAdded() && genderResolved) {
            reloadCurrentTab();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListener();
    }

    public void detachListener() {
        if (activeListener != null) {
            activeListener.remove();
            activeListener = null;
        }
    }

    private void reloadCurrentTab() {
        if (isMyGatherings)          loadMyGatherings();
        else if (isJoinedGatherings) loadJoinedGatherings();
        else                         fetchGatherings(userLat, userLng);
    }

    private void fetchCurrentUserGender() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            genderResolved = true;
            checkPermissionAndLoad();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String gender = doc.getString("gender");
                        currentUserGender = gender != null ? gender : "";
                    }
                    genderResolved = true;
                    checkPermissionAndLoad();
                })
                .addOnFailureListener(e -> {
                    genderResolved = true;
                    checkPermissionAndLoad();
                });
    }

    private boolean isGatheringAllowedForCurrentUser(PrayerGathering gathering) {
        String setting = gathering.getGenderSetting();
        if (setting == null || setting.isEmpty()) return true;
        switch (setting.toLowerCase()) {
            case "brothersonly": case "brothers only": case "male":
                return "Male".equalsIgnoreCase(currentUserGender);
            case "sistersonly": case "sisters only": case "female":
                return "Female".equalsIgnoreCase(currentUserGender);
            default: return true;
        }
    }

    public void init(View view) {
        rvPrayerGatherings  = view.findViewById(R.id.rvPrayerGatherings);
        btnCreateGathering  = view.findViewById(R.id.btnCreateGathering);
        btnAllGatherings    = view.findViewById(R.id.btnAllGatherings);
        btnMyGatherings     = view.findViewById(R.id.btnMyGatherings);
        btnJoinedGatherings = view.findViewById(R.id.btnJoinedGatherings);
        progressBar         = view.findViewById(R.id.progressBar);
        tvEmpty             = view.findViewById(R.id.tvEmpty);
        tvSectionLabel      = view.findViewById(R.id.tvSectionLabel);
        chipAll             = view.findViewById(R.id.chipAll);
        chipFajr            = view.findViewById(R.id.chipFajr);
        chipZuhr            = view.findViewById(R.id.chipZuhr);
        chipAsr             = view.findViewById(R.id.chipAsr);
        chipMaghrib         = view.findViewById(R.id.chipMaghrib);
        chipIsha            = view.findViewById(R.id.chipIsha);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        rvPrayerGatherings.setHasFixedSize(true);
        rvPrayerGatherings.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PrayerGatheringAdapter(getContext(), gatheringsList);
        rvPrayerGatherings.setAdapter(adapter);

        setActiveButton(btnAllGatherings);
    }

    public void setListeners() {
        btnAllGatherings.setOnClickListener(v -> {
            isMyGatherings = false; isJoinedGatherings = false;
            setActiveButton(btnAllGatherings);
            activeFilter = null;
            updateChipStyles(chipAll);
            updateSectionLabel();
            fetchGatherings(userLat, userLng);
        });

        btnMyGatherings.setOnClickListener(v -> {
            isMyGatherings = true; isJoinedGatherings = false;
            setActiveButton(btnMyGatherings);
            activeFilter = null;
            updateChipStyles(chipAll);
            updateSectionLabel();
            loadMyGatherings();
        });

        btnJoinedGatherings.setOnClickListener(v -> {
            isMyGatherings = false; isJoinedGatherings = true;
            setActiveButton(btnJoinedGatherings);
            activeFilter = null;
            updateChipStyles(chipAll);
            updateSectionLabel();
            loadJoinedGatherings();
        });

        btnCreateGathering.setOnClickListener(v -> navigateToCreatePrayerScreen());

        chipAll.setOnClickListener(v     -> selectFilter(null,      chipAll));
        chipFajr.setOnClickListener(v    -> selectFilter("Fajr",    chipFajr));
        chipZuhr.setOnClickListener(v    -> selectFilter("Zuhr",    chipZuhr));
        chipAsr.setOnClickListener(v     -> selectFilter("Asr",     chipAsr));
        chipMaghrib.setOnClickListener(v -> selectFilter("Maghrib", chipMaghrib));
        chipIsha.setOnClickListener(v    -> selectFilter("Isha",    chipIsha));
    }

    private void setActiveButton(Button activeBtn) {
        Button[] buttons = {btnAllGatherings, btnMyGatherings, btnJoinedGatherings};
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
        if (!hasLocationPermission()) { fetchGatherings(0, 0); return; }
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

    private void fetchGatherings(double lat, double lng) {
        if (!isAdded()) return;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        detachListener();
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPrayerGatherings.setVisibility(View.GONE);

        Query query = FirebaseFirestore.getInstance().collection("prayerGatherings");
        if (activeFilter != null) query = query.whereEqualTo("prayerType", activeFilter);
        query = query.orderBy("timeInMillis", Query.Direction.ASCENDING);

        activeListener = query.addSnapshotListener((querySnapshot, error) -> {
            if (!isAdded()) return;
            if (error != null || querySnapshot == null) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (error != null && "PERMISSION_DENIED".equals(error.getCode().name())) return;
                Toast.makeText(getContext(), "Failed to load ...", Toast.LENGTH_SHORT).show();
                return;
            }

            gatheringsList.clear();
            long currentTime   = System.currentTimeMillis();
            long twentyMinutes = 20 * 60 * 1000;

            for (QueryDocumentSnapshot doc : querySnapshot) {
                PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                gathering.setId(doc.getId());

                if ("cancelled".equals(gathering.getStatus())) continue;
                if ("finished".equals(gathering.getStatus()))   continue;
                if (currentTime > (gathering.getTimeInMillis() + twentyMinutes)) continue;
                if (!isGatheringAllowedForCurrentUser(gathering)) continue;

                if (lat != 0 && lng != 0) {
                    float distance = distanceBetween(lat, lng,
                            gathering.getLatitude(), gathering.getLongitude());
                    if (distance > NEARBY_RADIUS_KM) continue;
                }

                gatheringsList.add(gathering);
            }
            updateUI();
        });
    }

    private void loadMyGatherings() {
        if (!isAdded()) return;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        detachListener();
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPrayerGatherings.setVisibility(View.GONE);

        Query query = FirebaseFirestore.getInstance()
                .collection("prayerGatherings")
                .whereEqualTo("hostUid", FirebaseAuth.getInstance().getUid());
        if (activeFilter != null) query = query.whereEqualTo("prayerType", activeFilter);

        activeListener = query.addSnapshotListener((querySnapshot, error) -> {
            if (!isAdded()) return;
            if (error != null || querySnapshot == null) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (error != null && "PERMISSION_DENIED".equals(error.getCode().name())) return;
                Toast.makeText(getContext(), "Failed to load ...", Toast.LENGTH_SHORT).show();
                return;
            }

            gatheringsList.clear();
            long currentTime   = System.currentTimeMillis();
            long twentyMinutes = 20 * 60 * 1000;

            for (QueryDocumentSnapshot doc : querySnapshot) {
                PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                gathering.setId(doc.getId());
                if (currentTime > (gathering.getTimeInMillis() + twentyMinutes)) continue;
                gatheringsList.add(gathering);
            }
            updateUI();
        });
    }

    private void loadJoinedGatherings() {
        if (!isAdded()) return;
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        detachListener();
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvPrayerGatherings.setVisibility(View.GONE);

        String currentUid = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance()
                .collectionGroup("participants")
                .whereEqualTo("uid", currentUid)
                .whereEqualTo("role", "participant")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    if (snapshots.isEmpty()) { gatheringsList.clear(); updateUI(); return; }

                    ArrayList<String> joinedIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String id = doc.getReference().getParent().getParent().getId();
                        if (id != null) joinedIds.add(id);
                    }

                    ArrayList<String> batch = new ArrayList<>(
                            joinedIds.subList(0, Math.min(10, joinedIds.size())));

                    FirebaseFirestore.getInstance()
                            .collection("prayerGatherings")
                            .whereIn("id", batch)
                            .get()
                            .addOnSuccessListener(gatheringSnapshots -> {
                                if (!isAdded()) return;
                                gatheringsList.clear();
                                long currentTime   = System.currentTimeMillis();
                                long twentyMinutes = 20 * 60 * 1000;

                                for (QueryDocumentSnapshot doc : gatheringSnapshots) {
                                    PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                                    gathering.setId(doc.getId());
                                    if (currentTime > (gathering.getTimeInMillis() + twentyMinutes)) continue;
                                    if (activeFilter != null &&
                                            !activeFilter.equals(gathering.getPrayerType())) continue;
                                    gatheringsList.add(gathering);
                                }
                                updateUI();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (!isAdded()) return;
        progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        if (gatheringsList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvPrayerGatherings.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvPrayerGatherings.setVisibility(View.VISIBLE);
        }
    }

    private void selectFilter(String prayerType, TextView selectedChip) {
        if (activeFilter == null && prayerType == null) return;
        if (prayerType != null && prayerType.equals(activeFilter)) return;
        activeFilter = prayerType;
        updateChipStyles(selectedChip);
        updateSectionLabel();
        reloadCurrentTab();
    }

    private void updateChipStyles(TextView activeChip) {
        TextView[] allChips = {chipAll, chipFajr, chipZuhr, chipAsr, chipMaghrib, chipIsha};
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
        String typeLabel = activeFilter != null ? activeFilter.toUpperCase() + " " : "";
        if (isMyGatherings)          tvSectionLabel.setText("MY " + typeLabel + "GATHERINGS");
        else if (isJoinedGatherings) tvSectionLabel.setText("JOINED " + typeLabel + "GATHERINGS");
        else                         tvSectionLabel.setText("NEARBY " + typeLabel + "PRAYERS");
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