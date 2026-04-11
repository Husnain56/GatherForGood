package com.example.gatherforgood;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GatheringDetails extends AppCompatActivity {

    TextView tvStatus;
    TextView tvTitle;
    TextView tvDescription;
    TextView tvPrayerTime;
    TextView tvPrayerName;
    TextView tvHostName;
    TextView tvAttendingCount;
    TextView tvLocationDescription;

    Button btnJoin;
    MaterialButton btnOpenMaps;
    ImageButton btnBack;
    ProgressBar progressBar;

    PrayerGathering gathering;
    FirebaseFirestore db;
    String currentUid;
    boolean isAlreadyJoined = false;
    boolean isHost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gathering_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db         = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        init();
        bindData();
        checkIfJoined();
        setEventListeners();
    }

    public void init() {
        tvStatus              = findViewById(R.id.tvStatus);
        tvTitle               = findViewById(R.id.tvTitle);
        tvDescription         = findViewById(R.id.tvDescription);
        tvPrayerTime          = findViewById(R.id.tvPrayerTime);
        tvPrayerName          = findViewById(R.id.tvPrayerName);
        tvHostName            = findViewById(R.id.tvHostName);
        tvAttendingCount      = findViewById(R.id.tvAttendingCount);
        tvLocationDescription = findViewById(R.id.tvLocationDescription);
        btnJoin               = findViewById(R.id.btnJoin);
        btnOpenMaps           = findViewById(R.id.btnOpenMaps);
        btnBack               = findViewById(R.id.btnBack);
        progressBar           = findViewById(R.id.progressBar);
        gathering = (PrayerGathering) getIntent().getSerializableExtra("prayer_gathering");
    }

    public void bindData() {
        tvStatus.setText(gathering.getStatus().toUpperCase());
        tvTitle.setText(gathering.getPrayerType());
        tvDescription.setText(gathering.getDescription());
        tvPrayerTime.setText(gathering.getTime());
        tvPrayerName.setText(gathering.getPrayerType());
        tvHostName.setText(gathering.getHostName());
        tvAttendingCount.setText(gathering.getParticipantCount() + " Attending");
        tvLocationDescription.setText(gathering.getLocation());

        // Hide join button if user is the host
        isHost = gathering.getHostUid().equals(currentUid);
        if (isHost) {
            btnJoin.setVisibility(View.GONE);
        }
    }

    private void checkIfJoined() {
        if (isHost) return;

        progressBar.setVisibility(View.VISIBLE);
        btnJoin.setEnabled(false);

        db.collection("prayerGatherings")
                .document(gathering.getId())
                .collection("participants")
                .document(currentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);

                    if (doc.exists()) {
                        isAlreadyJoined = true;
                        btnJoin.setText("Leave Gathering");
                    } else {
                        isAlreadyJoined = false;
                        btnJoin.setText("Join Gathering");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                });
    }

    public void setEventListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnOpenMaps.setOnClickListener(v -> openGoogleMaps());
        btnJoin.setOnClickListener(v -> {
            if (isAlreadyJoined) {
                leaveGathering();
            } else {
                joinGathering();
            }
        });
    }

    private void joinGathering() {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userDoc.getString("name");

                    DocumentReference gatheringRef = db.collection("prayerGatherings")
                            .document(gathering.getId());

                    Map<String, Object> participantEntry = new HashMap<>();
                    participantEntry.put("uid",      currentUid);
                    participantEntry.put("name",     userName);
                    participantEntry.put("role",     "participant");
                    participantEntry.put("joinedAt", System.currentTimeMillis());

                    gatheringRef.collection("participants")
                            .document(currentUid)
                            .set(participantEntry)
                            .addOnSuccessListener(unused -> {
                                gatheringRef.update("participantCount",
                                                FieldValue.increment(1))
                                        .addOnSuccessListener(unused2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnJoin.setEnabled(true);
                                            isAlreadyJoined = true;
                                            btnJoin.setText("Leave Gathering");

                                            int newCount = gathering.getParticipantCount() + 1;
                                            gathering.setParticipantCount(newCount);
                                            tvAttendingCount.setText(newCount + " Attending");

                                            Toast.makeText(this,
                                                    "You have joined the gathering!",
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnJoin.setEnabled(true);
                                Toast.makeText(this,
                                        "Failed to join: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    Toast.makeText(this,
                            "Failed to fetch user info.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void leaveGathering() {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference gatheringRef = db.collection("prayerGatherings")
                .document(gathering.getId());

        gatheringRef.collection("participants")
                .document(currentUid)
                .delete()
                .addOnSuccessListener(unused -> {
                    gatheringRef.update("participantCount",
                                    FieldValue.increment(-1))
                            .addOnSuccessListener(unused2 -> {
                                progressBar.setVisibility(View.GONE);
                                btnJoin.setEnabled(true);
                                isAlreadyJoined = false;
                                btnJoin.setText("Join Gathering");

                                int newCount = Math.max(0,
                                        gathering.getParticipantCount() - 1);
                                gathering.setParticipantCount(newCount);
                                tvAttendingCount.setText(newCount + " Attending");

                                Toast.makeText(this,
                                        "You have left the gathering.",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    Toast.makeText(this,
                            "Failed to leave: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    public void openGoogleMaps() {
        String uri = "geo:" + gathering.getLatitude() + "," + gathering.getLongitude() +
                "?q=" + gathering.getLatitude() + "," + gathering.getLongitude() +
                "(" + Uri.encode(gathering.getLocation()) + ")";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        }
    }
}