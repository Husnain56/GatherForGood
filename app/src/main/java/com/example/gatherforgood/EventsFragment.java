package com.example.gatherforgood;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsFragment extends Fragment {

    private static final String TAG              = "EventsFragment";
    private static final float  NEARBY_RADIUS_KM = 10f;

    FloatingActionButton btnCreateEvent;
    RecyclerView         rvEvents;
    ProgressBar          progressBar;
    LinearLayout         layoutEmpty;
    TextView             tvSectionLabel;
    EditText             etSearch;
    TextView             chipNearMe;

    Button btnAllEvents, btnMyEvents, btnJoinedEvents;

    TextView chipAll, chipFoodDrive, chipStreetCleaning, chipClothingDrive,
            chipBloodDonation, chipTreePlanting, chipMosqueCleaning, chipFundraising;

    String  activeTab    = "all";
    String  activeFilter = null;
    boolean isNearMe     = false;

    double  userLat          = 0, userLng = 0;
    String  userCity         = "";
    boolean locationResolved = false;

    ArrayList<Event> allEventsList      = new ArrayList<>();
    ArrayList<Event> filteredEventsList = new ArrayList<>();

    EventsAdapter               adapter;
    FusedLocationProviderClient fusedClient;

    private ListenerRegistration activeListener;
    private String               currentSearchQuery = "";

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            fetchUserLocationThenEvents();
                        } else {
                            locationResolved = true;
                            fetchEvents(0, 0);
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
        setListeners();
        checkPermissionAndLoad();
    }

    @Override
    public void onResume() {
        super.onResume();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListener();
    }

    private void detachListener() {
        if (activeListener != null) {
            activeListener.remove();
            activeListener = null;
        }
    }

    public void init(View view) {
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        rvEvents       = view.findViewById(R.id.rvEvents);
        progressBar    = view.findViewById(R.id.progressBar);
        layoutEmpty    = view.findViewById(R.id.layoutEmpty);
        tvSectionLabel = view.findViewById(R.id.tvSectionLabel);
        etSearch       = view.findViewById(R.id.etSearch);
        chipNearMe     = view.findViewById(R.id.chipNearMe);

        btnAllEvents    = view.findViewById(R.id.btnAllEvents);
        btnMyEvents     = view.findViewById(R.id.btnMyEvents);
        btnJoinedEvents = view.findViewById(R.id.btnJoinedEvents);

        chipAll            = view.findViewById(R.id.chipAll);
        chipFoodDrive      = view.findViewById(R.id.chipFoodDrive);
        chipStreetCleaning = view.findViewById(R.id.chipStreetCleaning);
        chipClothingDrive  = view.findViewById(R.id.chipClothingDrive);
        chipBloodDonation  = view.findViewById(R.id.chipBloodDonation);
        chipTreePlanting   = view.findViewById(R.id.chipTreePlanting);
        chipMosqueCleaning = view.findViewById(R.id.chipMosqueCleaning);
        chipFundraising    = view.findViewById(R.id.Fundraising);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        rvEvents.setHasFixedSize(true);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventsAdapter(getContext(), filteredEventsList);
        rvEvents.setAdapter(adapter);

        setActiveTab(btnAllEvents);
        updateNearMeChipStyle();
    }

    public void setListeners() {

        btnCreateEvent.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateEvent.class)));

        btnAllEvents.setOnClickListener(v -> {
            activeTab = "all";
            setActiveTab(btnAllEvents);
            chipNearMe.setVisibility(View.VISIBLE);
            reload();
        });

        btnMyEvents.setOnClickListener(v -> {
            activeTab = "my";
            setActiveTab(btnMyEvents);
            chipNearMe.setVisibility(View.GONE);
            reload();
        });

        btnJoinedEvents.setOnClickListener(v -> {
            activeTab = "joined";
            setActiveTab(btnJoinedEvents);
            chipNearMe.setVisibility(View.GONE);
            reload();
        });

        chipAll.setOnClickListener(v            -> selectTypeFilter(null,                  chipAll));
        chipFoodDrive.setOnClickListener(v      -> selectTypeFilter("Food Drive",          chipFoodDrive));
        chipStreetCleaning.setOnClickListener(v -> selectTypeFilter("Street Cleaning",     chipStreetCleaning));
        chipClothingDrive.setOnClickListener(v  -> selectTypeFilter("Clothing Drive",      chipClothingDrive));
        chipBloodDonation.setOnClickListener(v  -> selectTypeFilter("Blood Donation Camp", chipBloodDonation));
        chipTreePlanting.setOnClickListener(v   -> selectTypeFilter("Tree Planting",       chipTreePlanting));
        chipMosqueCleaning.setOnClickListener(v -> selectTypeFilter("Mosque Cleaning",     chipMosqueCleaning));
        chipFundraising.setOnClickListener(v    -> selectTypeFilter("Fundraising",         chipFundraising));

        // Near Me toggle
        chipNearMe.setOnClickListener(v -> {
            isNearMe = !isNearMe;
            updateNearMeChipStyle();
            if (!locationResolved) {
                checkPermissionAndLoad();
            } else {
                applyFiltersAndSearch();
            }
        });

        // Client-side search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                applyFiltersAndSearch();
            }
        });
    }

    private void checkPermissionAndLoad() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fetchUserLocationThenEvents();
        }
    }

    private void fetchUserLocationThenEvents() {
        if (!hasLocationPermission()) {
            locationResolved = true;
            fetchEvents(0, 0);
            return;
        }
        try {
            fusedClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            userLat = location.getLatitude();
                            userLng = location.getLongitude();
                            detectCityFromLocation(location);
                        } else {
                            locationResolved = true;
                            fetchEvents(0, 0);
                        }
                    })
                    .addOnFailureListener(e -> {
                        locationResolved = true;
                        fetchEvents(0, 0);
                    });
        } catch (SecurityException e) {
            locationResolved = true;
            fetchEvents(0, 0);
        }
    }

    private void detectCityFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                if (city == null) city = address.getSubAdminArea();
                if (city == null) city = "";
                userCity = city.toLowerCase();
            }
        } catch (IOException e) {
            userCity = "";
        }
        locationResolved = true;
        fetchEvents(userLat, userLng);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void reload() {
        updateSectionLabel();
        fetchEvents(userLat, userLng);
    }

    private void fetchEvents(double lat, double lng) {
        if (progressBar == null) return;

        if ("joined".equals(activeTab)) {
            detachListener();
            progressBar.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            rvEvents.setVisibility(View.GONE);
            fetchJoinedEvents(FirebaseAuth.getInstance().getUid(), lat, lng);
            return;
        }

        detachListener();

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvEvents.setVisibility(View.GONE);

        String currentUid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query;
        if ("my".equals(activeTab)) {
            if (activeFilter != null) {
                query = db.collection("volunteerEvents")
                        .whereEqualTo("hostUid", currentUid)
                        .whereEqualTo("eventType", activeFilter)
                        .orderBy("createdAt", Query.Direction.DESCENDING);
            } else {
                query = db.collection("volunteerEvents")
                        .whereEqualTo("hostUid", currentUid)
                        .orderBy("createdAt", Query.Direction.DESCENDING);
            }
        } else {
            if (activeFilter != null) {
                query = db.collection("volunteerEvents")
                        .whereEqualTo("eventType", activeFilter)
                        .orderBy("createdAt", Query.Direction.DESCENDING);
            } else {
                query = db.collection("volunteerEvents")
                        .orderBy("createdAt", Query.Direction.DESCENDING);
            }
        }

        activeListener = query.addSnapshotListener((querySnapshot, error) -> {
            if (error != null || querySnapshot == null) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),
                        "Failed to load events: " + (error != null ? error.getMessage() : ""),
                        Toast.LENGTH_LONG).show();
                return;
            }

            allEventsList.clear();
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Event event = doc.toObject(Event.class);
                event.setEventId(doc.getId());

                if (event.getEventEndTimeMillis() > 0 &&
                        System.currentTimeMillis() > event.getEventEndTimeMillis()) continue;

                if ("all".equals(activeTab)) {
                    if (!userCity.isEmpty()) {
                        String loc = event.getLocation() != null
                                ? event.getLocation().toLowerCase() : "";
                        if (!loc.contains(userCity)) continue;
                    }
                }

                allEventsList.add(event);
            }

            applyFiltersAndSearch();
        });
    }

    private void applyFiltersAndSearch() {
        filteredEventsList.clear();

        for (Event event : allEventsList) {
            if ("all".equals(activeTab) && isNearMe) {
                if (userLat != 0 && userLng != 0) {
                    float dist = distanceBetween(userLat, userLng, event.getLat(), event.getLng());
                    if (dist > NEARBY_RADIUS_KM) continue;
                }
            }

            if (!currentSearchQuery.isEmpty()) {
                String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
                String type  = event.getEventType() != null ? event.getEventType().toLowerCase() : "";
                if (!title.contains(currentSearchQuery) && !type.contains(currentSearchQuery)) continue;
            }

            filteredEventsList.add(event);
        }

        updateUI();
    }

    private void fetchJoinedEvents(String currentUid, double lat, double lng) {
        FirebaseFirestore.getInstance()
                .collectionGroup("participants")
                .whereEqualTo("uid", currentUid)
                .whereEqualTo("role", "participant")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        allEventsList.clear();
                        filteredEventsList.clear();
                        updateUI();
                        return;
                    }

                    ArrayList<String> joinedEventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String eventId = doc.getReference()
                                .getParent().getParent().getId();
                        if (eventId != null) joinedEventIds.add(eventId);
                    }

                    ArrayList<String> batch = new ArrayList<>(
                            joinedEventIds.subList(0, Math.min(10, joinedEventIds.size())));

                    FirebaseFirestore.getInstance()
                            .collection("volunteerEvents")
                            .whereIn("eventId", batch)
                            .get()
                            .addOnSuccessListener(eventSnapshots -> {
                                allEventsList.clear();
                                for (QueryDocumentSnapshot doc : eventSnapshots) {
                                    Event event = doc.toObject(Event.class);
                                    event.setEventId(doc.getId());
                                    if (event.getEventEndTimeMillis() > 0 &&
                                            System.currentTimeMillis() > event.getEventEndTimeMillis()) continue;
                                    allEventsList.add(event);
                                }
                                applyFiltersAndSearch();
                            })
                            .addOnFailureListener(e -> {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                Toast.makeText(getContext(),
                                        "Failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        if (filteredEventsList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
        }
    }

    private void selectTypeFilter(String type, TextView selectedChip) {
        if (activeFilter == null && type == null) return;
        if (type != null && type.equals(activeFilter)) return;
        activeFilter = type;
        updateTypeChipStyles(selectedChip);
        updateSectionLabel();
        reload();
    }

    private void updateTypeChipStyles(TextView activeChip) {
        TextView[] chips = {
                chipAll, chipFoodDrive, chipStreetCleaning, chipClothingDrive,
                chipBloodDonation, chipTreePlanting, chipMosqueCleaning, chipFundraising
        };
        for (TextView chip : chips) {
            if (chip == activeChip) {
                chip.setBackgroundResource(R.drawable.prayer_chip_active);
                chip.setTextColor(requireContext().getColor(android.R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.prayer_chip_inactive);
                chip.setTextColor(android.graphics.Color.parseColor("#80E5C76B"));
            }
        }
    }

    private void updateNearMeChipStyle() {
        if (chipNearMe == null) return;
        if (isNearMe) {
            chipNearMe.setBackgroundResource(R.drawable.prayer_chip_active);
            chipNearMe.setTextColor(requireContext().getColor(android.R.color.white));
        } else {
            chipNearMe.setBackgroundResource(R.drawable.prayer_chip_inactive);
            chipNearMe.setTextColor(android.graphics.Color.parseColor("#80E5C76B"));
        }
    }

    private void setActiveTab(Button activeBtn) {
        Button[] buttons = {btnAllEvents, btnMyEvents, btnJoinedEvents};
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                btn.setBackgroundResource(R.drawable.register_bg_gender_active);
                btn.setTextColor(android.graphics.Color.WHITE);
            } else {
                btn.setBackgroundResource(android.R.color.transparent);
                btn.setTextColor(android.graphics.Color.parseColor("#D4AF37"));
            }
        }
    }

    private void updateSectionLabel() {
        if (tvSectionLabel == null) return;
        String typeLabel = activeFilter != null ? activeFilter.toUpperCase() + " " : "";
        switch (activeTab) {
            case "my":
                tvSectionLabel.setText("MY " + typeLabel + "EVENTS");
                break;
            case "joined":
                tvSectionLabel.setText("JOINED " + typeLabel + "EVENTS");
                break;
            default:
                String locLabel = isNearMe ? "NEARBY " :
                        (userCity.isEmpty() ? "" : userCity.toUpperCase() + " ");
                tvSectionLabel.setText(locLabel + typeLabel + "EVENTS");
                break;
        }
    }

    private float distanceBetween(double lat1, double lng1,
                                  double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000f;
    }
}