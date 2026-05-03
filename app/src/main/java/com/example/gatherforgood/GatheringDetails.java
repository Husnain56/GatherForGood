package com.example.gatherforgood;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class GatheringDetails extends AppCompatActivity {

    TextView tvStatus, tvTitle, tvDescription, tvPrayerTime,
            tvPrayerName, tvHostName, tvAttendingCount, tvLocationDescription;

    LinearLayout            layoutParticipants;
    ProgressBar             progressParticipants;
    TextView                tvNoParticipants;
    RecyclerView rvParticipants;
    ParticipantAdapter      participantAdapter;
    ArrayList<ParticipantItem> participantList = new ArrayList<>();

    Button btnJoin;
    MaterialButton btnOpenMaps, btnGroupChat;
    ImageButton btnBack;
    ProgressBar progressBar;
    LinearLayout layoutChatButtons;

    LinearLayout llLocation;

    PrayerGathering gathering;
    FirebaseFirestore db;
    String currentUid;
    MaterialButton btnCancelGathering;
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
        setEventListeners();
    }

    private void init() {
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
        layoutChatButtons     = findViewById(R.id.layoutChatButtons);
        btnGroupChat          = findViewById(R.id.btnGroupChat);
        btnCancelGathering = findViewById(R.id.btnCancelGathering);

        layoutParticipants   = findViewById(R.id.layoutParticipants);
        progressParticipants = findViewById(R.id.progressParticipants);
        tvNoParticipants     = findViewById(R.id.tvNoParticipants);
        rvParticipants       = findViewById(R.id.rvParticipants);

        rvParticipants.setLayoutManager(new LinearLayoutManager(this));
        rvParticipants.setHasFixedSize(true);

        participantAdapter = new ParticipantAdapter(this, participantList, null);
        rvParticipants.setAdapter(participantAdapter);

        llLocation = findViewById(R.id.llLocation);

        gathering = (PrayerGathering) getIntent().getSerializableExtra("prayer_gathering");
    }

    private void bindData() {
        tvStatus.setText(gathering.getStatus().toUpperCase());
        tvTitle.setText(gathering.getPrayerType());
        tvDescription.setText(gathering.getDescription());
        tvPrayerTime.setText(gathering.getTime());
        tvPrayerName.setText(gathering.getPrayerType());
        tvHostName.setText(gathering.getHostName());
        tvAttendingCount.setText(gathering.getParticipantCount() + " Attending");
        tvLocationDescription.setText(gathering.getLocation());

        isHost = gathering.getHostUid().equals(currentUid);

        if (!isHost && "finished".equals(gathering.getStatus())) {
            btnJoin.setText("Prayer Ended");
            btnJoin.setEnabled(false);
            btnJoin.setAlpha(0.5f);
            layoutChatButtons.setVisibility(View.GONE);
            return;
        }

        if ("cancelled".equals(gathering.getStatus())) {
            btnJoin.setText("Gathering Cancelled");
            btnJoin.setEnabled(false);
            btnJoin.setAlpha(0.5f);
            tvStatus.setTextColor(getColor(android.R.color.holo_red_light));
            layoutChatButtons.setVisibility(View.GONE);
            if (isHost) btnCancelGathering.setVisibility(View.GONE);
            return;
        }

        if (isHost) {
            setupHostControls();
        } else {
            checkIfJoined();
        }
    }

    private void setEventListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnOpenMaps.setOnClickListener(v -> openGoogleMaps());
        llLocation.setOnClickListener(v -> openGoogleMaps());
        btnGroupChat.setOnClickListener(v -> openGroupChat());
    }

    private void openGroupChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatType", "group");
        intent.putExtra("chatId",   gathering.getId());
        intent.putExtra("chatName", gathering.getPrayerType() + " Gathering");
        startActivity(intent);
    }

    private void setupHostControls() {
        btnCancelGathering.setVisibility(View.VISIBLE);
        btnCancelGathering.setOnClickListener(v -> showCancelDialog());
        btnJoin.setVisibility(View.VISIBLE);
        btnJoin.setEnabled(true);
        layoutChatButtons.setVisibility(View.VISIBLE);
        updateHostButton();
        loadParticipants();
    }

    private void loadParticipants() {
        layoutParticipants.setVisibility(View.VISIBLE);
        progressParticipants.setVisibility(View.VISIBLE);
        rvParticipants.setVisibility(View.GONE);
        tvNoParticipants.setVisibility(View.GONE);

        db.collection("prayerGatherings")
                .document(gathering.getId())
                .collection("participants")
                .orderBy("joinedAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int nonHostCount = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        if (!"host".equals(doc.getString("role"))) nonHostCount++;
                    }

                    if (nonHostCount == 0) {
                        progressParticipants.setVisibility(View.GONE);
                        tvNoParticipants.setVisibility(View.VISIBLE);
                        return;
                    }

                    int[] pending = {nonHostCount};
                    participantList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String role = doc.getString("role");
                        if ("host".equals(role)) continue;

                        String uid      = doc.getString("uid");
                        String name     = doc.getString("name");
                        long   joinedAt = doc.getLong("joinedAt") != null ? doc.getLong("joinedAt") : 0L;

                        db.collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String gender = userDoc.getString("gender");
                                    if (gender == null) gender = "—";
                                    participantList.add(new ParticipantItem(uid, name, gender, role, joinedAt));

                                    pending[0]--;
                                    if (pending[0] == 0) {
                                        participantList.sort((a, b) -> Long.compare(a.joinedAt, b.joinedAt));
                                        progressParticipants.setVisibility(View.GONE);
                                        rvParticipants.setVisibility(View.VISIBLE);
                                        participantAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    participantList.add(new ParticipantItem(uid, name, "—", role, joinedAt));

                                    pending[0]--;
                                    if (pending[0] == 0) {
                                        participantList.sort((a, b) -> Long.compare(a.joinedAt, b.joinedAt));
                                        progressParticipants.setVisibility(View.GONE);
                                        rvParticipants.setVisibility(View.VISIBLE);
                                        participantAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressParticipants.setVisibility(View.GONE);
                    tvNoParticipants.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to load participants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    private void showCancelDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_cancel_gathering, null);

        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.CancelGatheringSheetTheme);
        sheet.setContentView(view);

        if (sheet.getWindow() != null) {
            sheet.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        RadioGroup rgReasons = view.findViewById(R.id.rgReasons);
        Button btnGoBack     = view.findViewById(R.id.btnDialogGoBack);
        Button btnConfirm    = view.findViewById(R.id.btnDialogConfirm);

        btnGoBack.setOnClickListener(v -> sheet.dismiss());
        btnConfirm.setOnClickListener(v -> {
            int selectedId = rgReasons.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a reason", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selected = view.findViewById(selectedId);
            sheet.dismiss();
            cancelGathering(selected.getText().toString());
        });

        sheet.show();
    }
    private void cancelGathering(String reason) {
        btnCancelGathering.setEnabled(false);
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "cancelled");
        updates.put("cancellationReason", reason);
        updates.put("cancelledAt", System.currentTimeMillis());

        db.collection("prayerGatherings")
                .document(gathering.getId())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    gathering.setStatus("cancelled");
                    tvStatus.setText("CANCELLED");
                    tvStatus.setTextColor(getColor(android.R.color.holo_red_light));

                    btnJoin.setText("Gathering Cancelled");
                    btnJoin.setEnabled(false);
                    btnJoin.setAlpha(0.5f);
                    btnCancelGathering.setVisibility(View.GONE);
                    layoutChatButtons.setVisibility(View.GONE);

                    Toast.makeText(this,
                            "Gathering cancelled: " + reason,
                            Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCancelGathering.setEnabled(true);
                    btnJoin.setEnabled(true);
                    Toast.makeText(this,
                            "Failed to cancel: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateHostButton() {
        switch (gathering.getStatus().toLowerCase()) {
            case "upcoming":
                btnJoin.setText("Mark as Ongoing");
                break;
            case "ongoing":
                btnJoin.setText("Mark as Finished");
                break;
            case "finished":
                btnJoin.setText("Delete Gathering");
                break;
        }

        btnJoin.setOnClickListener(v -> {
            switch (gathering.getStatus().toLowerCase()) {
                case "upcoming":
                    if (isPrayerTimeReached()) {
                        updateStatus("ongoing");
                    } else {
                        Toast.makeText(this, "Prayer time not reached yet", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "ongoing":
                    updateStatus("finished");
                    break;
                case "finished":
                    deleteGathering();
                    break;
            }
        });
    }

    private boolean isPrayerTimeReached() {
        try {
            String dateTimeStr = gathering.getDate() + " " + gathering.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "EEEE, dd MMM yyyy hh:mm a", Locale.getDefault());
            Date prayerDateTime = sdf.parse(dateTimeStr);
            return prayerDateTime != null &&
                    System.currentTimeMillis() >= prayerDateTime.getTime();
        } catch (Exception e) {
            return false;
        }
    }

    private void updateStatus(String newStatus) {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("prayerGatherings")
                .document(gathering.getId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    gathering.setStatus(newStatus);
                    tvStatus.setText(newStatus.toUpperCase());
                    updateHostButton();
                    Toast.makeText(this,
                            "Status updated to " + newStatus,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    Toast.makeText(this,
                            "Failed to update status: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteGathering() {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference gatheringRef = db.collection("prayerGatherings")
                .document(gathering.getId());

        gatheringRef.collection("participants")
                .get()
                .addOnSuccessListener(snapshot -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc
                            : snapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                gatheringRef.delete()
                                        .addOnSuccessListener(unused2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this,
                                                    "Gathering deleted.",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnJoin.setEnabled(true);
                                            Toast.makeText(this,
                                                    "Failed to delete gathering: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnJoin.setEnabled(true);
                                Toast.makeText(this,
                                        "Failed to delete participants: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    Toast.makeText(this,
                            "Failed to fetch participants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfJoined() {
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
                        if ("cancelled".equals(gathering.getStatus())) {
                            btnJoin.setText("Gathering Cancelled");
                            btnJoin.setEnabled(false);
                            btnJoin.setAlpha(0.5f);
                            layoutChatButtons.setVisibility(View.GONE);
                            return;
                        }
                        isAlreadyJoined = true;
                        btnJoin.setText("Leave Gathering");
                        layoutChatButtons.setVisibility(View.VISIBLE);
                    } else {
                        if ("finished".equals(gathering.getStatus()) || "cancelled".equals(gathering.getStatus())) {
                            btnJoin.setText("finished".equals(gathering.getStatus()) ? "Prayer Ended" : "Gathering Cancelled");
                            btnJoin.setEnabled(false);
                            btnJoin.setAlpha(0.5f);
                            layoutChatButtons.setVisibility(View.GONE);
                            return;
                        }
                        isAlreadyJoined = false;
                        btnJoin.setText("Join Gathering");
                        layoutChatButtons.setVisibility(View.GONE);
                    }

                    btnJoin.setOnClickListener(v -> {
                        if (isAlreadyJoined) leaveGathering();
                        else joinGathering();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    Toast.makeText(this,
                            "Failed to check join status.",
                            Toast.LENGTH_SHORT).show();
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
                                            layoutChatButtons.setVisibility(View.VISIBLE);

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
                                layoutChatButtons.setVisibility(View.GONE);

                                int newCount = Math.max(0, gathering.getParticipantCount() - 1);
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

    private void openGoogleMaps() {
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