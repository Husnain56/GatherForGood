package com.example.gatherforgood;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventDetails extends AppCompatActivity {

    TextView tvEventTitle, tvOrganizerName, tvOrganizerNameDetail,
            tvEventDate, tvEventTime, tvEventLocation,
            tvSlots, tvParticipantCount, tvDescription,
            tvReadMore, tvEventType, tvStatus;

    Button         btnJoin;
    MaterialButton btnOpenMaps, btnGroupChat, btnMessageHost;
    ImageButton    btnBack;
    ProgressBar    progressBar;
    LinearLayout   layoutChatButtons;

    Event            event;
    FirebaseFirestore db;
    String           currentUid;
    boolean          isAlreadyJoined = false;
    boolean          isHost          = false;
    boolean          descExpanded    = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
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
        tvEventTitle          = findViewById(R.id.tvEventTitle);
        tvOrganizerName       = findViewById(R.id.tvOrganizerName);
        tvOrganizerNameDetail = findViewById(R.id.tvOrganizerNameDetail);
        tvEventDate           = findViewById(R.id.tvEventDate);
        tvEventTime           = findViewById(R.id.tvEventTime);
        tvEventLocation       = findViewById(R.id.tvEventLocation);
        tvSlots               = findViewById(R.id.tvSlots);
        tvParticipantCount    = findViewById(R.id.tvParticipantCount);
        tvDescription         = findViewById(R.id.tvDescription);
        tvReadMore            = findViewById(R.id.tvReadMore);
        tvEventType           = findViewById(R.id.tvEventType);
        tvStatus              = findViewById(R.id.tvStatus);

        btnJoin         = findViewById(R.id.btnJoin);
        btnOpenMaps     = findViewById(R.id.btnOpenMaps);
        btnBack         = findViewById(R.id.btnBack);
        progressBar     = findViewById(R.id.progressBar);
        layoutChatButtons = findViewById(R.id.layoutChatButtons);
        btnGroupChat    = findViewById(R.id.btnGroupChat);
        btnMessageHost  = findViewById(R.id.btnMessageHost);

        event = (Event) getIntent().getSerializableExtra("event");
    }

    private void bindData() {
        tvEventTitle.setText(event.getTitle());
        tvOrganizerName.setText(event.getHostName());
        tvOrganizerNameDetail.setText(event.getHostName());
        tvEventDate.setText(event.getDate());
        tvEventTime.setText(event.getTime());
        tvEventLocation.setText(event.getLocation());
        tvDescription.setText(event.getDescription());
        tvEventType.setText(event.getEventType().toUpperCase());
        tvStatus.setText(event.getStatus().toUpperCase());
        tvSlots.setText(event.getVolunteersJoined() + "/" + event.getVolunteersRequired());
        tvParticipantCount.setText(event.getVolunteersJoined() + " Attending");

        isHost = event.getHostUid().equals(currentUid);

        if (isHost) {
            layoutChatButtons.setVisibility(View.VISIBLE);
            setupHostControls();
        } else {
            checkIfJoined();
        }
    }

    private void setEventListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnOpenMaps.setOnClickListener(v -> openGoogleMaps());

        tvReadMore.setOnClickListener(v -> {
            if (descExpanded) {
                tvDescription.setMaxLines(4);
                tvReadMore.setText("Read more");
            } else {
                tvDescription.setMaxLines(Integer.MAX_VALUE);
                tvReadMore.setText("Read less");
            }
            descExpanded = !descExpanded;
        });

        btnGroupChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatType", "group");
            intent.putExtra("chatId",   event.getEventId());
            intent.putExtra("chatName", event.getTitle());
            startActivity(intent);
        });

        btnMessageHost.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatType",   "direct");
            intent.putExtra("chatId",     currentUid + "_" + event.getHostUid());
            intent.putExtra("chatName",   event.getHostName());
            intent.putExtra("targetUid",  event.getHostUid());
            startActivity(intent);
        });
    }

    private void setupHostControls() {
        btnJoin.setVisibility(View.VISIBLE);
        btnJoin.setEnabled(true);
        // Hide "Message Host" for the host themselves
        btnMessageHost.setVisibility(View.GONE);
        updateHostButton();
    }

    private void updateHostButton() {
        switch (event.getStatus().toLowerCase()) {
            case "upcoming":
                btnJoin.setText("Mark as Ongoing");
                break;
            case "ongoing":
                btnJoin.setText("Mark as Finished");
                break;
            case "finished":
                btnJoin.setText("Delete Event");
                break;
        }

        btnJoin.setOnClickListener(v -> {
            switch (event.getStatus().toLowerCase()) {
                case "upcoming":
                    if (isEventTimeReached()) {
                        updateStatus("ongoing");
                    } else {
                        Toast.makeText(this,
                                "Event time has not been reached yet",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "ongoing":
                    updateStatus("finished");
                    break;
                case "finished":
                    deleteEvent();
                    break;
            }
        });
    }

    private boolean isEventTimeReached() {
        try {
            String dateTimeStr = event.getDate() + " " + event.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "EEEE, dd MMM yyyy hh:mm a", Locale.getDefault());
            Date eventDateTime = sdf.parse(dateTimeStr);
            return eventDateTime != null &&
                    System.currentTimeMillis() >= eventDateTime.getTime();
        } catch (Exception e) {
            return false;
        }
    }

    private void updateStatus(String newStatus) {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("volunteerEvents")
                .document(event.getEventId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);
                    event.setStatus(newStatus);
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

    private void deleteEvent() {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference eventRef = db.collection("volunteerEvents")
                .document(event.getEventId());

        eventRef.collection("participants")
                .get()
                .addOnSuccessListener(snapshot -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc
                            : snapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                eventRef.delete()
                                        .addOnSuccessListener(unused2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(this,
                                                    "Event deleted.",
                                                    Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnJoin.setEnabled(true);
                                            Toast.makeText(this,
                                                    "Failed to delete event: " + e.getMessage(),
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

        db.collection("volunteerEvents")
                .document(event.getEventId())
                .collection("participants")
                .document(currentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    btnJoin.setEnabled(true);

                    if (doc.exists()) {
                        isAlreadyJoined = true;
                        btnJoin.setText("Leave Event");
                        layoutChatButtons.setVisibility(View.VISIBLE);
                    } else {
                        isAlreadyJoined = false;
                        btnJoin.setText("Request to Join");
                        layoutChatButtons.setVisibility(View.GONE);
                    }

                    btnJoin.setOnClickListener(v -> {
                        if (isAlreadyJoined) leaveEvent();
                        else joinEvent();
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

    private void joinEvent() {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(currentUid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String userName = userDoc.getString("name");

                    DocumentReference eventRef = db.collection("volunteerEvents")
                            .document(event.getEventId());

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("uid",      currentUid);
                    entry.put("name",     userName);
                    entry.put("role",     "participant");
                    entry.put("joinedAt", System.currentTimeMillis());

                    eventRef.collection("participants")
                            .document(currentUid)
                            .set(entry)
                            .addOnSuccessListener(unused -> {
                                eventRef.update("volunteersJoined",
                                                FieldValue.increment(1))
                                        .addOnSuccessListener(unused2 -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnJoin.setEnabled(true);
                                            isAlreadyJoined = true;
                                            btnJoin.setText("Leave Event");
                                            layoutChatButtons.setVisibility(View.VISIBLE);

                                            int newCount = event.getVolunteersJoined() + 1;
                                            event.setVolunteersJoined(newCount);
                                            tvSlots.setText(newCount + "/" + event.getVolunteersRequired());
                                            tvParticipantCount.setText(newCount + " Attending");

                                            Toast.makeText(this,
                                                    "You have joined the event!",
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

    private void leaveEvent() {
        btnJoin.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference eventRef = db.collection("volunteerEvents")
                .document(event.getEventId());

        eventRef.collection("participants")
                .document(currentUid)
                .delete()
                .addOnSuccessListener(unused -> {
                    eventRef.update("volunteersJoined",
                                    FieldValue.increment(-1))
                            .addOnSuccessListener(unused2 -> {
                                progressBar.setVisibility(View.GONE);
                                btnJoin.setEnabled(true);
                                isAlreadyJoined = false;
                                btnJoin.setText("Request to Join");
                                layoutChatButtons.setVisibility(View.GONE);

                                int newCount = Math.max(0, event.getVolunteersJoined() - 1);
                                event.setVolunteersJoined(newCount);
                                tvSlots.setText(newCount + "/" + event.getVolunteersRequired());
                                tvParticipantCount.setText(newCount + " Attending");

                                Toast.makeText(this,
                                        "You have left the event.",
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
        String uri = "geo:" + event.getLat() + "," + event.getLng() +
                "?q=" + event.getLat() + "," + event.getLng() +
                "(" + Uri.encode(event.getLocation()) + ")";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
        }
    }
}