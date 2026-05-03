package com.example.gatherforgood;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    FrameLayout cardPrayerGatherings;
    FrameLayout cardVolunteerEvents;
    TextView    tvUserName;
    SharedPreferences sPref;
    ImageView   ivProfilePic;
    TextView    tvSeeAll;
    RecyclerView rvPrayerGatherings;
    PrayerGatheringAdapter adapter;
    ProgressBar progressBar;
    LinearLayout emptyNearby;

    TextView tvNextPrayerName;
    TextView tvNextPrayerTime;
    TextView tvTimeLeft;

    FusedLocationProviderClient fusedClient;
    private ListenerRegistration activeListener;

    double userLat = 0;
    double userLng = 0;

    private static final float NEARBY_RADIUS_KM = 10f;

    ArrayList<PrayerGathering> gatheringsList = new ArrayList<>();

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            fetchPrayerTimes();
                            fetchUserLocationThenGatherings();
                        } else {
                            fetchPrayerTimesFallback();
                            fetchGatherings(0, 0);
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        init(view);
        setEventListeners();

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().finish();
                    }
                });

        checkPermissionAndLoad();
    }

    @Override
    public void onStop() {
        super.onStop();
        detachListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null && isAdded()) {
            fetchGatherings(userLat, userLng);
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

    public void init(View view) {
        rvPrayerGatherings   = view.findViewById(R.id.rvPrayerGatherings);
        cardPrayerGatherings = view.findViewById(R.id.cardPrayerGatherings);
        cardVolunteerEvents  = view.findViewById(R.id.cardVolunteerEvents);
        tvUserName           = view.findViewById(R.id.tvUserName);
        ivProfilePic         = view.findViewById(R.id.ivProfilePic);
        tvSeeAll             = view.findViewById(R.id.tvSeeAll);
        progressBar          = view.findViewById(R.id.progressBar);
        emptyNearby          = view.findViewById(R.id.emptyNearby);
        tvNextPrayerName     = view.findViewById(R.id.tvNextPrayerName);
        tvNextPrayerTime     = view.findViewById(R.id.tvNextPrayerTime);
        tvTimeLeft           = view.findViewById(R.id.tvTimeLeft);

        sPref = requireContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        tvUserName.setText(sPref.getString("user_name", ""));

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        rvPrayerGatherings.setHasFixedSize(true);
        adapter = new PrayerGatheringAdapter(getContext(), gatheringsList);
        rvPrayerGatherings.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPrayerGatherings.setAdapter(adapter);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissionAndLoad() {
        if (!hasLocationPermission()) {
            locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            fetchPrayerTimes();
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
                            reverseGeocodeAndCache(userLat, userLng);
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

    private void reverseGeocodeAndCache(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);

                    String city    = address.getLocality();
                    if (city == null) city = address.getSubAdminArea();
                    if (city == null) city = address.getAdminArea();
                    String country = address.getCountryName();

                    String finalCity    = city    != null ? city    : "";
                    String finalCountry = country != null ? country : "";

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                                sPref.edit()
                                        .putString("user_city",    finalCity)
                                        .putString("user_country", finalCountry)
                                        .apply());
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void fetchGatherings(double lat, double lng) {
        if (!isAdded() || getContext() == null) return;
        // Guard: don't attach a listener if user is signed out
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        detachListener();

        if (progressBar != null)          progressBar.setVisibility(View.VISIBLE);
        if (emptyNearby != null)          emptyNearby.setVisibility(View.GONE);
        if (rvPrayerGatherings != null)   rvPrayerGatherings.setVisibility(View.GONE);

        activeListener = FirebaseFirestore.getInstance()
                .collection("prayerGatherings")
                .orderBy("timeInMillis", Query.Direction.ASCENDING)
                .limit(20)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (!isAdded() || getContext() == null) return;

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

                        long prayerTime = gathering.getTimeInMillis();
                        if (currentTime > (prayerTime + twentyMinutes)) continue;

                        if (lat != 0 && lng != 0) {
                            float distance = distanceBetween(lat, lng,
                                    gathering.getLatitude(), gathering.getLongitude());
                            if (distance <= NEARBY_RADIUS_KM) gatheringsList.add(gathering);
                        } else {
                            gatheringsList.add(gathering);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (gatheringsList.isEmpty()) {
                        if (emptyNearby != null)        emptyNearby.setVisibility(View.VISIBLE);
                        if (rvPrayerGatherings != null) rvPrayerGatherings.setVisibility(View.GONE);
                    } else {
                        if (emptyNearby != null)        emptyNearby.setVisibility(View.GONE);
                        if (rvPrayerGatherings != null) rvPrayerGatherings.setVisibility(View.VISIBLE);
                    }
                });
    }

    private float distanceBetween(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000f;
    }

    private void fetchPrayerTimes() {
        if (!hasLocationPermission()) {
            fetchPrayerTimesFallback();
            return;
        }
        try {
            fusedClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            fetchPrayerTimesForLocation(
                                    location.getLatitude(), location.getLongitude());
                        } else {
                            fetchPrayerTimesFallback();
                        }
                    })
                    .addOnFailureListener(e -> fetchPrayerTimesFallback());
        } catch (SecurityException e) {
            fetchPrayerTimesFallback();
        }
    }

    private void fetchPrayerTimesForLocation(double lat, double lng) {
        String url = "https://api.aladhan.com/v1/timings"
                + "?latitude=" + lat
                + "&longitude=" + lng
                + "&method=1&school=0";
        fetchFromAladhan(url);
    }

    private void fetchPrayerTimesFallback() {
        String url = "https://api.aladhan.com/v1/timingsByCity"
                + "?city=Karachi&country=Pakistan&method=1&school=0";
        fetchFromAladhan(url);
    }

    private void fetchFromAladhan(String url) {
        if (!isAdded() || getContext() == null) return;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject timings = response
                                .getJSONObject("data")
                                .getJSONObject("timings");

                        Map<String, String> prayers = new LinkedHashMap<>();
                        prayers.put("Fajr",    timings.getString("Fajr"));
                        prayers.put("Dhuhr",   timings.getString("Dhuhr"));
                        prayers.put("Asr",     timings.getString("Asr"));
                        prayers.put("Maghrib", timings.getString("Maghrib"));
                        prayers.put("Isha",    timings.getString("Isha"));

                        showNextPrayer(prayers);
                    } catch (Exception e) {
                        showFallback();
                    }
                },
                error -> showFallback()
        );
        queue.add(request);
    }

    private void showNextPrayer(Map<String, String> prayers) {
        try {
            Calendar now           = Calendar.getInstance();
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String currentTime     = sdf24.format(now.getTime());

            String nextName   = null;
            String nextTime24 = null;

            for (Map.Entry<String, String> entry : prayers.entrySet()) {
                String prayerTime = entry.getValue().length() > 5
                        ? entry.getValue().substring(0, 5)
                        : entry.getValue();
                if (prayerTime.compareTo(currentTime) > 0) {
                    nextName   = entry.getKey();
                    nextTime24 = prayerTime;
                    break;
                }
            }

            if (nextName == null) {
                nextName   = "Fajr";
                nextTime24 = prayers.get("Fajr");
                if (nextTime24 != null && nextTime24.length() > 5)
                    nextTime24 = nextTime24.substring(0, 5);
            }

            Date   prayerDate   = sdf24.parse(nextTime24);
            String displayTime  = sdf12.format(prayerDate);

            String[] parts   = nextTime24.split(":");
            int prayerHour   = Integer.parseInt(parts[0]);
            int prayerMinute = Integer.parseInt(parts[1]);

            Calendar prayerCal = Calendar.getInstance();
            prayerCal.set(Calendar.HOUR_OF_DAY, prayerHour);
            prayerCal.set(Calendar.MINUTE,      prayerMinute);
            prayerCal.set(Calendar.SECOND,      0);
            prayerCal.set(Calendar.MILLISECOND, 0);
            if (prayerCal.before(now)) prayerCal.add(Calendar.DAY_OF_MONTH, 1);

            long diffMillis  = prayerCal.getTimeInMillis() - now.getTimeInMillis();
            long diffMinutes = diffMillis / 60000;
            long hours       = diffMinutes / 60;
            long minutes     = diffMinutes % 60;

            String timeLeftStr = hours > 0
                    ? "In " + hours + "h " + minutes + "m"
                    : "In " + minutes + " minutes";

            if (getView() == null) return;
            tvNextPrayerName.setText(nextName);
            tvNextPrayerTime.setText(displayTime);
            tvTimeLeft.setText(timeLeftStr);

        } catch (Exception e) {
            showFallback();
        }
    }

    private void showFallback() {
        if (getView() == null) return;
        tvNextPrayerName.setText("--");
        tvNextPrayerTime.setText("--:--");
        tvTimeLeft.setText("Could not load");
    }

    public void setEventListeners() {
        cardPrayerGatherings.setOnClickListener(
                v -> ((HomeScreen) requireActivity()).navigateToTab(1));
        ivProfilePic.setOnClickListener(
                v -> ((HomeScreen) requireActivity()).navigateToTab(4));
        tvSeeAll.setOnClickListener(
                v -> ((HomeScreen) requireActivity()).navigateToTab(1));
        cardVolunteerEvents.setOnClickListener(
                v -> ((HomeScreen) requireActivity()).navigateToTab(2));
    }
}