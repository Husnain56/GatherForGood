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
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateEvent extends AppCompatActivity {

    private static final String TAG = "CreateEvent";

    TextView      tvSelectedDate, tvMapHint, tvTime;
    ImageView     ivPickDate;
    RelativeLayout rlDate, rlTime;
    Long          selectedDateMillis;
    Spinner       spinnerEventType, spinnerGenderSetting;
    Double        selectedLat, selectedLng;
    MaterialButton btnFetchLocation;
    EditText      etEventTitle, etVolunteersRequired, etDescription, etRequirements;
    TextView      etLocationDescription;
    Button        btnCreateEvent;
    ImageButton   btnBack;
    FirebaseFirestore db;
    FirebaseAuth  mAuth;

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
        setContentView(R.layout.activity_create_event);
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
        tvSelectedDate       = findViewById(R.id.tvSelectedDate);
        ivPickDate           = findViewById(R.id.ivPickDate);
        rlDate               = findViewById(R.id.rlDate);
        rlTime               = findViewById(R.id.rlTime);
        tvTime               = findViewById(R.id.tvTime);
        spinnerEventType     = findViewById(R.id.spinnerEventType);
        spinnerGenderSetting = findViewById(R.id.spinnerGenderSetting);
        btnFetchLocation     = findViewById(R.id.btnFetchLocation);
        etEventTitle         = findViewById(R.id.etEventTitle);
        etVolunteersRequired = findViewById(R.id.etVolunteersRequired);
        etLocationDescription= findViewById(R.id.etLocationDescription);
        etDescription        = findViewById(R.id.etDescription);
        etRequirements       = findViewById(R.id.etRequirements);
        tvMapHint            = findViewById(R.id.tvMapHint);
        btnCreateEvent       = findViewById(R.id.btnCreateEvent);
        btnBack              = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        setupSpinners();
    }


    private void setupSpinners() {
        spinnerEventType.setAdapter(buildSpinnerAdapter(
                "Select Event Type",
                new String[]{
                        "Food Drive", "Street Cleaning", "Clothing Drive",
                        "Blood Donation Camp", "Tree Planting", "Mosque Cleaning",
                        "Fundraising", "Disaster Relief", "Other"
                }
        ));

        spinnerGenderSetting.setAdapter(buildSpinnerAdapter(
                "Gender Setting",
                new String[]{ "Brothers Only", "Sisters Only", "Both" }
        ));
    }


    private BaseAdapter buildSpinnerAdapter(String hint, String[] realItems) {
        final String[] items = new String[realItems.length + 1];
        items[0] = hint;
        System.arraycopy(realItems, 0, items, 1, realItems.length);

        return new BaseAdapter() {
            @Override public int    getCount()         { return items.length; }
            @Override public Object getItem(int pos)   { return items[pos]; }
            @Override public long   getItemId(int pos) { return pos; }
            @Override public boolean isEnabled(int pos){ return pos != 0; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = buildTv(parent.getContext());
                tv.setHeight(dp(parent.getContext(), 52));
                tv.setPaddingRelative(dp(parent.getContext(), 16), 0,
                        dp(parent.getContext(), 40), 0);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                if (position == 0) {
                    tv.setText(items[0]);
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
                    TextView hidden = buildTv(parent.getContext());
                    hidden.setHeight(0);
                    hidden.setVisibility(View.GONE);
                    return hidden;
                }
                TextView tv = buildTv(parent.getContext());
                tv.setText(items[position]);
                tv.setTextColor(0xFFF9F1D7);
                tv.setBackgroundColor(0xFF1B2E25);
                int pad = dp(parent.getContext(), 14);
                tv.setPadding(pad, pad, pad, pad);
                return tv;
            }

            private TextView buildTv(Context ctx) {
                TextView tv = new TextView(ctx);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tv.setSingleLine(true);
                return tv;
            }

            private int dp(Context ctx, int dp) {
                return Math.round(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, dp,
                        ctx.getResources().getDisplayMetrics()));
            }
        };
    }


    private void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        ivPickDate.setOnClickListener(v -> showDatePicker());
        rlDate.setOnClickListener(v -> showDatePicker());
        rlTime.setOnClickListener(v -> showTimePicker());
        btnFetchLocation.setOnClickListener(v -> launchPlacesPicker());
        btnCreateEvent.setOnClickListener(v -> validateAndSubmit());
    }


    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Event Date")
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

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(7)
                .setMinute(0)
                .setTitleText("Select Event Time")
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
                                    String display = (address != null && !address.isEmpty()) ? address : name;
                                    etLocationDescription.setText(display);

                                    if (selectedLat != null && selectedLng != null) {
                                        tvMapHint.setText(String.format(Locale.getDefault(),
                                                "📍 %.6f, %.6f", selectedLat, selectedLng));
                                        tvMapHint.setTextColor(Color.parseColor("#F9F1D7"));
                                    }
                                    Toast.makeText(this, "Location selected: " + name, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "fetchPlace failed: " + e.getMessage());
                                    Toast.makeText(this, "Failed to get location details.", Toast.LENGTH_SHORT).show();
                                });

                    } else if (result.getResultCode() != RESULT_CANCELED) {
                        Toast.makeText(this, "Error selecting location. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchPlacesPicker() {
        placesLauncher.launch(new PlaceAutocomplete.IntentBuilder().build(this));
    }


    private void validateAndSubmit() {
        if (spinnerEventType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select an event type", Toast.LENGTH_SHORT).show();
            return;
        }
        String title = etEventTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter an event title", Toast.LENGTH_SHORT).show();
            return;
        }
        String volunteers = etVolunteersRequired.getText().toString().trim();
        if (volunteers.isEmpty()) {
            Toast.makeText(this, "Please enter number of volunteers needed", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerGenderSetting.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select who this event is open to", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Please select a future time for today", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, "Please pin a location first", Toast.LENGTH_SHORT).show();
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

        saveEvent();
    }

    private void saveEvent() {
        btnCreateEvent.setEnabled(false);
        btnCreateEvent.setText("Saving...");

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        saveToFirestore(uid, doc.getString("name"));
                    } else {
                        resetButton();
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    resetButton();
                    Toast.makeText(this, "Failed to fetch user info.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String uid, String hostName) {
        String docId     = db.collection("volunteerEvents").document().getId();
        String fullTime  = tvTime.getText().toString().trim();
        String genderSel = spinnerGenderSetting.getSelectedItem().toString();

        String genderSetting;
        switch (genderSel) {
            case "Brothers Only": genderSetting = "brothersOnly"; break;
            case "Sisters Only":  genderSetting = "sistersOnly";  break;
            default:              genderSetting = "all";          break;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("eventId",            docId);
        event.put("hostUid",            uid);
        event.put("hostName",           hostName);
        event.put("eventType",          spinnerEventType.getSelectedItem().toString());
        event.put("title",              etEventTitle.getText().toString().trim());
        event.put("volunteersRequired", Integer.parseInt(etVolunteersRequired.getText().toString().trim()));
        event.put("volunteersJoined",   1);
        event.put("genderSetting",      genderSetting);
        event.put("date",               tvSelectedDate.getText().toString().trim());
        event.put("time",               fullTime);
        event.put("eventTimeMillis",    buildEventTimeMillis());
        event.put("location",           etLocationDescription.getText().toString().trim());
        event.put("lat",                selectedLat);
        event.put("lng",                selectedLng);
        event.put("description",        etDescription.getText().toString().trim());
        event.put("requirements",       etRequirements.getText().toString().trim());
        event.put("status",             "upcoming");
        event.put("createdAt",          System.currentTimeMillis());

        db.collection("volunteerEvents").document(docId).set(event)
                .addOnSuccessListener(unused -> {
                    Map<String, Object> participant = new HashMap<>();
                    participant.put("uid",      uid);
                    participant.put("name",     hostName);
                    participant.put("role",     "host");
                    participant.put("joinedAt", System.currentTimeMillis());

                    db.collection("volunteerEvents").document(docId)
                            .collection("participants").document(uid)
                            .set(participant)
                            .addOnSuccessListener(u2 -> {
                                resetButton();
                                Toast.makeText(this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                resetButton();
                                Toast.makeText(this, "Event saved!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    resetButton();
                    Toast.makeText(this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resetButton() {
        btnCreateEvent.setEnabled(true);
        btnCreateEvent.setText("Create Event");
    }

    private long buildEventTimeMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(selectedDateMillis);
        cal.set(java.util.Calendar.HOUR_OF_DAY, getSelectedHour());
        cal.set(java.util.Calendar.MINUTE,      getSelectedMinute());
        cal.set(java.util.Calendar.SECOND,      0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
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
}