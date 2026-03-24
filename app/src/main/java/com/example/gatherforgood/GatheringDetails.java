package com.example.gatherforgood;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class GatheringDetails extends AppCompatActivity {


    TextView tvStatus;
    TextView tvTitle;
    TextView tvDescription;
    TextView tvPrayerTime;
    TextView tvPrayerName;
    TextView tvHostName;
    TextView tvAttendingCount;

    MaterialButton btnOpenMaps;
    ImageButton btnBack;
    PrayerGathering gathering;
    TextView tvLocationDescription;

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
        init();
        bindData();
        setEventListeners();
    }

    public void init(){
        tvStatus = findViewById(R.id.tvStatus);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvPrayerTime = findViewById(R.id.tvPrayerTime);
        tvPrayerName = findViewById(R.id.tvPrayerName);
        tvHostName = findViewById(R.id.tvHostName);
        tvAttendingCount = findViewById(R.id.tvAttendingCount);
        btnOpenMaps = findViewById(R.id.btnOpenMaps);
        btnBack = findViewById(R.id.btnBack);
        tvLocationDescription = findViewById(R.id.tvLocationDescription);
        gathering = (PrayerGathering) getIntent().getSerializableExtra("prayer_gathering");
    }

    public void bindData(){
        tvStatus.setText(gathering.getStatus());
        tvTitle.setText(gathering.getPrayerType());
        tvDescription.setText(gathering.getDescription());
        tvPrayerTime.setText(gathering.getTime());
        tvPrayerName.setText(gathering.getPrayerType());
        tvHostName.setText(gathering.getHostName());
        tvAttendingCount.setText(gathering.getParticipantCount() + " Attending");
        tvLocationDescription.setText(gathering.getLocation());
    }
    public void setEventListeners(){
        btnBack.setOnClickListener(v -> {
            finish();
        });
        btnOpenMaps.setOnClickListener(v -> {
            openGoogleMaps();
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
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(browserIntent);
        }
    }
}