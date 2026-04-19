package com.example.gatherforgood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileFragment extends Fragment {

    TextView tvUserName, tvInitialsAvatar, tvEmail, tvGender, tvCity;
    LinearLayout layoutVerified;
    AppCompatButton btnLogout;
    TextView tvPrayersJoined, tvPrayersHosted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        init(view);
        setEventListeners();
        fetchUserData();
    }

    public void init(View view) {
        tvUserName       = view.findViewById(R.id.tvUserName);
        tvInitialsAvatar = view.findViewById(R.id.tvInitialsAvatar);
        tvEmail          = view.findViewById(R.id.tvEmail);
        tvGender         = view.findViewById(R.id.tvGender);
        tvCity           = view.findViewById(R.id.tvCity);
        layoutVerified   = view.findViewById(R.id.layoutVerified);
        tvPrayersJoined  = view.findViewById(R.id.tvPrayersJoined);
        tvPrayersHosted  = view.findViewById(R.id.tvPrayersHosted);
        btnLogout        = view.findViewById(R.id.btnLogout);
    }

    private void fetchUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name     = doc.getString("name");
                        String email    = doc.getString("email");
                        String gender   = doc.getString("gender");
                        String city     = doc.getString("city");
                        Boolean verified = doc.getBoolean("isVerified");

                        tvUserName.setText(name);
                        tvEmail.setText(email);
                        tvGender.setText(gender);
                        tvCity.setText(city != null ? city : "City not set");
                        setInitials(name);

                        if (Boolean.TRUE.equals(verified)) {
                            layoutVerified.setVisibility(View.VISIBLE);
                        }
                    }
                });

        fetchStats(uid);
    }

    private void fetchStats(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("prayerGatherings")
                .whereEqualTo("hostUid", uid)
                .get()
                .addOnSuccessListener(snap ->
                        tvPrayersHosted.setText(String.valueOf(snap.size())));

        db.collectionGroup("participants")
                .whereEqualTo("uid", uid)
                .whereEqualTo("role", "participant")
                .get()
                .addOnSuccessListener(snap -> {
                    tvPrayersJoined.setText(String.valueOf(snap.size()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                });
    }

    private void setInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            tvInitialsAvatar.setText("?");
            return;
        }
        String[] parts = fullName.trim().split("\\s+");
        String initials;
        if (parts.length >= 2) {
            initials = String.valueOf(parts[0].charAt(0))
                    + String.valueOf(parts[parts.length - 1].charAt(0));
        } else {
            initials = String.valueOf(parts[0].charAt(0));
        }
        tvInitialsAvatar.setText(initials.toUpperCase());
    }

    public void setEventListeners() {
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            navigateToLoginScreen();
        });
    }

    public void navigateToLoginScreen() {
        Intent intent = new Intent(requireActivity(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    @Override
    public void onResume() {
        super.onResume();
        fetchUserData();
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
}