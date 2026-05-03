package com.example.gatherforgood;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.PlaceAutocomplete;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreatePrayer extends AppCompatActivity {

    private static final String TAG = "CreatePrayer";

    RelativeLayout rlDate;
    TextView       tvSelectedDate;

    RelativeLayout rlTime;
    TextView       tvTime;

    MaterialButton btnFetchLocation;
    TextView       etLocationDescription;
    TextView       tvMapHint;

    EditText      etDescription;
    Spinner       spinnerPrayerType;
    Button        btnCreateGathering;
    ImageButton   btnBack;

    FirebaseFirestore db;
    FirebaseAuth      mAuth;

    Long   selectedDateMillis;
    Double selectedLat, selectedLng;

    private PlacesClient placesClient;
    private ActivityResultLauncher<Intent> placesLauncher;

    private static final List<Place.Field> PLACE_FIELDS = Arrays.asList(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_prayer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                    getApplicationContext(), ApiKeys.MAPS_API_KEY);
        }
        placesClient = Places.createClient(this);

        registerPlacesLauncher();
        init();
        setListeners();
    }

    private void init() {
        rlDate               = findViewById(R.id.rldate);
        tvSelectedDate       = findViewById(R.id.tvSelectedDate);
        rlTime               = findViewById(R.id.rlTime);
        tvTime               = findViewById(R.id.tvTime);
        btnFetchLocation     = findViewById(R.id.btnFetchLocation);
        etLocationDescription= findViewById(R.id.etLocationDescription);
        tvMapHint            = findViewById(R.id.tvMapHint);
        etDescription        = findViewById(R.id.etDescription);
        spinnerPrayerType    = findViewById(R.id.spinnerPrayerType);
        btnCreateGathering   = findViewById(R.id.btnCreateGathering);
        btnBack              = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        setupPrayerTypeSpinner();
    }

    private void setupPrayerTypeSpinner() {
        final String[] rawTypes = getResources().getStringArray(R.array.prayer_types);

        final String[] items = new String[rawTypes.length + 1];
        items[0] = "Select a Prayer Type";
        System.arraycopy(rawTypes, 0, items, 1, rawTypes.length);

        spinnerPrayerType.setAdapter(new BaseAdapter() {

            @Override public int     getCount()            { return items.length; }
            @Override public Object  getItem(int pos)      { return items[pos]; }
            @Override public long    getItemId(int pos)    { return pos; }
            @Override public boolean isEnabled(int pos)    { return pos != 0; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = buildTextView(parent.getContext());
                tv.setHeight(dp(parent.getContext(), 52));
                tv.setPaddingRelative(dp(parent.getContext(), 16), 0,
                        dp(parent.getContext(), 40), 0);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                if (position == 0) {
                    tv.setText("Select a Prayer Type");
                    tv.setTextColor(0x80F9F1D7);
                } else {
                    tv.setText(items[position]);
                    tv.setTextColor(0xFFF9F1D7);
                }
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (position == 0) {
                    TextView hidden = buildTextView(parent.getContext());
                    hidden.setHeight(0);
                    hidden.setVisibility(View.GONE);
                    return hidden;
                }
                TextView tv = buildTextView(parent.getContext());
                tv.setText(items[position]);
                tv.setTextColor(0xFFF9F1D7);
                tv.setBackgroundColor(0xFF1B2E25);
                int h = dp(parent.getContext(), 14);
                tv.setPadding(h, h, h, h);
                return tv;
            }

            private TextView buildTextView(Context ctx) {
                TextView tv = new TextView(ctx);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv.setSingleLine(true);
                return tv;
            }

            private int dp(Context ctx, int dp) {
                return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dp, ctx.getResources().getDisplayMetrics()));
            }
        });
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        rlDate.setOnClickListener(v -> showDatePicker());
        rlTime.setOnClickListener(v -> showTimePicker());
        btnFetchLocation.setOnClickListener(v -> launchPlacesPicker());
        btnCreateGathering.setOnClickListener(v -> validateAndSubmit());
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(7)
                .setMinute(0)
                .setTitleText("Select Prayer Time")
                .build();

        picker.show(getSupportFragmentManager(), "TIME_PICKER");

        picker.addOnPositiveButtonClickListener(v -> {
            int hour        = picker.getHour();
            int minute      = picker.getMinute();
            String amPm     = hour < 12 ? "AM" : "PM";
            int displayHour = hour % 12 == 0 ? 12 : hour % 12;
            tvTime.setText(String.format(Locale.getDefault(),
                    "%02d:%02d %s", displayHour, minute, amPm));
            tvTime.setTextColor(Color.parseColor("#F9F1D7"));
        });
    }

    private void registerPlacesLauncher() {
        placesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        com.google.android.libraries.places.api.model.AutocompletePrediction pred =
                                PlaceAutocomplete.getPredictionFromIntent(result.getData());

                        if (pred == null) {
                            Toast.makeText(this, "Could not get place info.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        placesClient.fetchPlace(
                                        FetchPlaceRequest.newInstance(pred.getPlaceId(), PLACE_FIELDS))
                                .addOnSuccessListener(response -> {
                                    Place place   = response.getPlace();
                                    LatLng latLng = place.getLocation();
                                    if (latLng != null) {
                                        selectedLat = latLng.latitude;
                                        selectedLng = latLng.longitude;
                                    }
                                    String address = place.getFormattedAddress();
                                    String name    = place.getDisplayName();
                                    String display = (address != null && !address.isEmpty())
                                            ? address : name;

                                    etLocationDescription.setText(display);
                                    etLocationDescription.setTextColor(
                                            Color.parseColor("#F9F1D7"));

                                    if (selectedLat != null && selectedLng != null) {
                                        tvMapHint.setText(String.format(Locale.getDefault(),
                                                "📍 %.6f, %.6f", selectedLat, selectedLng));
                                        tvMapHint.setTextColor(
                                                Color.parseColor("#80F9F1D7"));
                                    }
                                    btnFetchLocation.setText("📍 Change Location");
                                    Toast.makeText(this,
                                            "Location selected: " + name,
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "fetchPlace failed: " + e.getMessage());
                                    Toast.makeText(this,
                                            "Failed to get location details.",
                                            Toast.LENGTH_SHORT).show();
                                });

                    } else if (result.getResultCode() != RESULT_CANCELED) {
                        Toast.makeText(this,
                                "Error selecting location. Try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchPlacesPicker() {
        placesLauncher.launch(new PlaceAutocomplete.IntentBuilder().build(this));
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Gathering Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(
                        new com.google.android.material.datepicker.CalendarConstraints.Builder()
                                .setValidator(
                                        com.google.android.material.datepicker.DateValidatorPointForward.now())
                                .build())
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            tvSelectedDate.setText(
                    new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                            .format(new Date(selection)));
            tvSelectedDate.setTextColor(Color.parseColor("#F9F1D7"));
            selectedDateMillis = selection;

            tvTime.setText("Tap to select time");
            tvTime.setTextColor(Color.parseColor("#80F9F1D7"));
        });
    }

    private void validateAndSubmit() {
        if (spinnerPrayerType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a Prayer Type", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDateMillis == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        String time = tvTime.getText().toString().trim();
        if (time.isEmpty() || time.equals("Tap to select time")) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isToday(selectedDateMillis)) {
            java.util.Calendar now = java.util.Calendar.getInstance();
            int sh = getSelectedHour(), sm = getSelectedMinute();
            if (sh < now.get(java.util.Calendar.HOUR_OF_DAY) ||
                    (sh == now.get(java.util.Calendar.HOUR_OF_DAY)
                            && sm <= now.get(java.util.Calendar.MINUTE))) {
                Toast.makeText(this,
                        "Please select a future time for today",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, "Please fetch your location first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etLocationDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please add location details", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etDescription.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show();
            return;
        }
        saveGathering();
    }

    private boolean isToday(long dateMillis) {
        java.util.Calendar sel   = java.util.Calendar.getInstance();
        java.util.Calendar today = java.util.Calendar.getInstance();
        sel.setTimeInMillis(dateMillis);
        return sel.get(java.util.Calendar.YEAR)         == today.get(java.util.Calendar.YEAR)  &&
                sel.get(java.util.Calendar.MONTH)        == today.get(java.util.Calendar.MONTH) &&
                sel.get(java.util.Calendar.DAY_OF_MONTH) == today.get(java.util.Calendar.DAY_OF_MONTH);
    }

    private int getSelectedHour() {
        String[] parts = tvTime.getText().toString().split(":");
        int hour = Integer.parseInt(parts[0].trim());
        String amPm = parts[1].trim().split(" ")[1];
        if (amPm.equals("AM")) { if (hour == 12) hour = 0; }
        else                   { if (hour != 12) hour += 12; }
        return hour;
    }

    private int getSelectedMinute() {
        return Integer.parseInt(
                tvTime.getText().toString().split(":")[1].trim().split(" ")[0]);
    }

    private long buildPrayerTimeMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);
        cal.set(java.util.Calendar.HOUR_OF_DAY, getSelectedHour());
        cal.set(java.util.Calendar.MINUTE,      getSelectedMinute());
        cal.set(java.util.Calendar.SECOND,      0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void saveGathering() {
        btnCreateGathering.setEnabled(false);
        btnCreateGathering.setText("Saving...");

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        saveToFirestore(uid,
                                doc.getString("name"),
                                doc.getString("gender"));
                    } else {
                        resetButton();
                        Toast.makeText(this,
                                "User profile not found.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    resetButton();
                    Toast.makeText(this,
                            "Failed to fetch user info.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String uid, String hostName, String hostGender) {
        String docId         = db.collection("prayerGatherings").document().getId();
        String fullTime      = tvTime.getText().toString().trim();
        String genderSetting = "Male".equals(hostGender) ? "brothersOnly" : "sistersOnly";
        String prayerType    = spinnerPrayerType.getSelectedItem().toString().trim();

        PrayerGathering gathering = new PrayerGathering(
                docId, uid,
                etDescription.getText().toString().trim(),
                hostName, prayerType,
                tvSelectedDate.getText().toString().trim(),
                fullTime,
                etLocationDescription.getText().toString().trim(),
                selectedLat, selectedLng,
                genderSetting, "upcoming", 1,
                System.currentTimeMillis(),
                buildPrayerTimeMillis()
        );

        db.collection("prayerGatherings").document(docId).set(gathering)
                .addOnSuccessListener(unused -> {
                    java.util.Map<String, Object> participant = new java.util.HashMap<>();
                    participant.put("uid",      uid);
                    participant.put("name",     hostName);
                    participant.put("role",     "host");
                    participant.put("joinedAt", System.currentTimeMillis());

                    db.collection("prayerGatherings").document(docId)
                            .collection("participants").document(uid)
                            .set(participant)
                            .addOnSuccessListener(u2 -> {
                                resetButton();
                                Toast.makeText(this,
                                        "Gathering created successfully!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                resetButton();
                                Toast.makeText(this,
                                        "Gathering saved but participant entry failed.",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    resetButton();
                    Toast.makeText(this,
                            "Failed to save gathering: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void resetButton() {
        btnCreateGathering.setEnabled(true);
        btnCreateGathering.setText("Create Gathering");
    }
}