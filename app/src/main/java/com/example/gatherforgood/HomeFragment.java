package com.example.gatherforgood;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

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

    TextView tvNextPrayerName;
    TextView tvNextPrayerTime;
    TextView tvTimeLeft;

    FusedLocationProviderClient fusedClient;

    ArrayList<PrayerGathering> gatheringsList = new ArrayList<>();

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            fetchPrayerTimes();
                        } else {
                            fetchPrayerTimesFallback();
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
        fetchGatherings();
        fetchPrayerTimes();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchGatherings();
        fetchPrayerTimes();
    }

    public void init(View view) {
        rvPrayerGatherings   = view.findViewById(R.id.rvPrayerGatherings);
        cardPrayerGatherings = view.findViewById(R.id.cardPrayerGatherings);
        cardVolunteerEvents  = view.findViewById(R.id.cardVolunteerEvents);
        tvUserName           = view.findViewById(R.id.tvUserName);
        ivProfilePic         = view.findViewById(R.id.ivProfilePic);
        tvSeeAll             = view.findViewById(R.id.tvSeeAll);
        progressBar          = view.findViewById(R.id.progressBar);
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


    private void fetchPrayerTimes() {
        if (requireContext().checkSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            locationPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fetchPrayerTimesForLocation(
                                location.getLatitude(),
                                location.getLongitude());
                    } else {
                        fetchPrayerTimesFallback();
                    }
                })
                .addOnFailureListener(e -> fetchPrayerTimesFallback());
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
        if (getContext() == null) return;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
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
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String currentTime = sdf24.format(now.getTime());

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
                if (nextTime24 != null && nextTime24.length() > 5) {
                    nextTime24 = nextTime24.substring(0, 5);
                }
            }

            // Convert to 12h format for display
            Date prayerDate    = sdf24.parse(nextTime24);
            String displayTime = sdf12.format(prayerDate);

            // Calculate time remaining
            String[] parts   = nextTime24.split(":");
            int prayerHour   = Integer.parseInt(parts[0]);
            int prayerMinute = Integer.parseInt(parts[1]);

            Calendar prayerCal = Calendar.getInstance();
            prayerCal.set(Calendar.HOUR_OF_DAY, prayerHour);
            prayerCal.set(Calendar.MINUTE, prayerMinute);
            prayerCal.set(Calendar.SECOND, 0);
            prayerCal.set(Calendar.MILLISECOND, 0);

            // Push to tomorrow if already passed
            if (prayerCal.before(now)) {
                prayerCal.add(Calendar.DAY_OF_MONTH, 1);
            }

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
                        PrayerGathering gathering = doc.toObject(PrayerGathering.class);
                        gathering.setId(doc.getId());
                        gatheringsList.add(gathering);
                    }
                    adapter.notifyDataSetChanged();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    public void setEventListeners() {
        cardPrayerGatherings.setOnClickListener(
                view -> ((HomeScreen) requireActivity()).navigateToTab(1));
        ivProfilePic.setOnClickListener(
                view -> ((HomeScreen) requireActivity()).navigateToTab(4));
        tvSeeAll.setOnClickListener(
                view -> ((HomeScreen) requireActivity()).navigateToTab(1));
    }
}