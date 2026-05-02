package com.example.gatherforgood;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class UserProfileFragment extends Fragment {

    TextView tvUserName, tvInitialsAvatar, tvEmail, tvGender, tvCity;
    LinearLayout layoutVerified;
    AppCompatButton btnLogout, btnDeleteAccount;
    ImageButton btnEdit;
    TextView tvPrayersJoined, tvPrayersHosted, tvEventsJoined, tvEventsHosted;

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
        tvEventsJoined   = view.findViewById(R.id.tvEventsJoined);
        tvEventsHosted   = view.findViewById(R.id.tvEventsHosted);
        btnLogout        = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnEdit          = view.findViewById(R.id.btnEdit);
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
                        String name      = doc.getString("name");
                        String email     = doc.getString("email");
                        String gender    = doc.getString("gender");
                        String city      = doc.getString("city");
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
                    int prayerCount = 0;
                    int eventCount  = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        String path = doc.getReference().getPath();
                        if (path.startsWith("volunteerEvents/")) {
                            eventCount++;
                        } else if (path.startsWith("prayerGatherings/")) {
                            prayerCount++;
                        }
                    }
                    tvPrayersJoined.setText(String.valueOf(prayerCount));
                    tvEventsJoined.setText(String.valueOf(eventCount));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show());

        db.collection("volunteerEvents")
                .whereEqualTo("hostUid", uid)
                .get()
                .addOnSuccessListener(snap ->
                        tvEventsHosted.setText(String.valueOf(snap.size())));
    }

    private void showEditNameDialog() {
        String currentName = tvUserName.getText().toString();

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_name);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.6f);
        }

        EditText etNewName        = dialog.findViewById(R.id.etNewName);
        AppCompatButton btnSave   = dialog.findViewById(R.id.btnSave);
        AppCompatButton btnCancel = dialog.findViewById(R.id.btnCancel);

        etNewName.setText(currentName);
        etNewName.setSelection(currentName.length());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName = etNewName.getText().toString().trim();
            if (newName.isEmpty()) {
                etNewName.setError("Name cannot be empty");
                return;
            }
            if (newName.equals(currentName)) {
                dialog.dismiss();
                return;
            }
            saveName(newName, dialog);
        });

        dialog.show();
    }

    private void saveName(String newName, Dialog dialog) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("name", newName)
                .addOnSuccessListener(unused -> {
                    tvUserName.setText(newName);
                    setInitials(newName);
                    dialog.dismiss();
                    Toast.makeText(getContext(),
                            "Name updated successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to update name: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
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
        btnEdit.setOnClickListener(v -> showEditNameDialog());

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