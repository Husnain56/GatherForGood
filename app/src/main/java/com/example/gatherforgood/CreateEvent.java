package com.example.gatherforgood;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateEvent extends AppCompatActivity {

    private static final String TAG = "CreateEvent";

    TextView tvSelectedDate, tvMapHint, tvTime;
    ImageView ivPickDate;
    RelativeLayout rlDate;
    Long selectedDateMillis;
    Spinner spinnerAmPm, spinnerEventType, spinnerGenderSetting;
    Double selectedLat, selectedLng;
    MaterialButton btnFetchLocation;
    EditText etEventTitle, etVolunteersRequired, etLocationDescription, etDescription, etRequirements;
    Button btnCreateEvent;
    ImageButton btnBack;

    private PlacesClient placesClient;
    private ActivityResultLauncher<Intent> placesLauncher;

    private static final List<Place.Field> PLACE_FIELDS = Arrays.asList(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.LOCATION
    );


    private static final String[] EVENT_TYPES = {
            "Select Event Type",
            "Food Drive",
            "Street Cleaning",
            "Clothing Drive",
            "Blood Donation Camp",
            "Tree Planting",
            "Mosque Cleaning",
            "Fundraising",
            "Disaster Relief",
            "Other"
    };

    private static final String[] GENDER_OPTIONS = {
            "Select Audience",
            "Brothers Only",
            "Sisters Only",
            "Both"
    };

    private static final String[] AM_PM_OPTIONS = { "AM", "PM" };


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
                    getApplicationContext(),
                    ApiKeys.MAPS_API_KEY
            );
        }

        placesClient = Places.createClient(this);

        registerPlacesLauncher();
        init();
        setListeners();
    }

    private void init() {
        tvSelectedDate        = findViewById(R.id.tvSelectedDate);
        ivPickDate            = findViewById(R.id.ivPickDate);
        rlDate                = findViewById(R.id.rlDate);
        tvTime                = findViewById(R.id.tvTime);
        spinnerAmPm           = findViewById(R.id.spinnerAmPm);
        spinnerEventType      = findViewById(R.id.spinnerEventType);
        spinnerGenderSetting  = findViewById(R.id.spinnerGenderSetting);
        btnFetchLocation      = findViewById(R.id.btnFetchLocation);
        etEventTitle          = findViewById(R.id.etEventTitle);
        etVolunteersRequired  = findViewById(R.id.etVolunteersRequired);
        etLocationDescription = findViewById(R.id.etLocationDescription);
        etDescription         = findViewById(R.id.etDescription);
        etRequirements        = findViewById(R.id.etRequirements);
        tvMapHint             = findViewById(R.id.tvMapHint);
        btnCreateEvent        = findViewById(R.id.btnCreateEvent);
        btnBack               = findViewById(R.id.btnBack);

        setupSpinners();
    }

    private void setupSpinners() {
        ArrayAdapter<String> eventTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, EVENT_TYPES);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(eventTypeAdapter);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, GENDER_OPTIONS);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenderSetting.setAdapter(genderAdapter);

        ArrayAdapter<String> amPmAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, AM_PM_OPTIONS);
        amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAmPm.setAdapter(amPmAdapter);
    }

    private void setListeners() {
        ivPickDate.setOnClickListener(v -> showDatePicker());
        rlDate.setOnClickListener(v -> showDatePicker());

        tvTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(7)
                    .setMinute(0)
                    .setTitleText("Select Time")
                    .build();

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(v2 -> {
                int hour        = timePicker.getHour();
                int minute      = timePicker.getMinute();
                int amPmIndex   = hour < 12 ? 0 : 1;
                int displayHour = hour % 12 == 0 ? 12 : hour % 12;
                tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", displayHour, minute));
                spinnerAmPm.setSelection(amPmIndex);
            });
        });

        btnFetchLocation.setOnClickListener(v -> launchPlacesPicker());
        btnCreateEvent.setOnClickListener(v -> validateAndSubmit());
        btnBack.setOnClickListener(v -> finish());
    }

    private void registerPlacesLauncher() {
        placesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                        com.google.android.libraries.places.api.model.AutocompletePrediction prediction =
                                PlaceAutocomplete.getPredictionFromIntent(result.getData());

                        if (prediction == null) {
                            Toast.makeText(this, "Could not get place info.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String placeId = prediction.getPlaceId();
                        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, PLACE_FIELDS);

                        placesClient.fetchPlace(request)
                                .addOnSuccessListener(response -> {
                                    Place place = response.getPlace();

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
                                        tvMapHint.setText(String.format(
                                                Locale.getDefault(),
                                                "📍 %.6f, %.6f",
                                                selectedLat, selectedLng));
                                    }

                                    Toast.makeText(this, "Location selected: " + name, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "fetchPlace failed: " + e.getMessage());
                                    Toast.makeText(this, "Failed to get location details.", Toast.LENGTH_SHORT).show();
                                });

                    } else if (result.getResultCode() == RESULT_CANCELED) {
                    } else {
                        Toast.makeText(this, "Error selecting location. Try again.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void launchPlacesPicker() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .build(this);
        placesLauncher.launch(intent);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Event Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(
                        new com.google.android.material.datepicker.CalendarConstraints.Builder()
                                .setValidator(com.google.android.material.datepicker.DateValidatorPointForward.now())
                                .build()
                )
                .build();

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
            tvSelectedDate.setText(sdf.format(new Date(selection)));
            tvSelectedDate.setTextColor(Color.parseColor("#F9F1D7"));
            selectedDateMillis = selection;

            tvTime.setText("Select Time");
            spinnerAmPm.setSelection(0);
        });
    }

    private void validateAndSubmit() {
        String eventType   = spinnerEventType.getSelectedItem().toString();
        String genderSel   = spinnerGenderSetting.getSelectedItem().toString();
        String title       = etEventTitle.getText().toString().trim();
        String volunteers  = etVolunteersRequired.getText().toString().trim();
        String location    = etLocationDescription.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String time        = tvTime.getText().toString().trim();

        if (eventType.equals("Select Event Type")) {
            Toast.makeText(this, "Please select an event type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter an event title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (volunteers.isEmpty()) {
            Toast.makeText(this, "Please enter number of volunteers needed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (genderSel.equals("Select Audience")) {
            Toast.makeText(this, "Please select who this event is open to", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDateMillis == null) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (time.isEmpty() || time.equals("Select Time")) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isToday(selectedDateMillis)) {
            int selectedHour   = getSelectedHour();
            int selectedMinute = getSelectedMinute();

            java.util.Calendar now = java.util.Calendar.getInstance();
            int currentHour   = now.get(java.util.Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(java.util.Calendar.MINUTE);

            if (selectedHour < currentHour ||
                    (selectedHour == currentHour && selectedMinute <= currentMinute)) {
                Toast.makeText(this, "Please select a future time for today", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, "Please pin a location first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (location.isEmpty()) {
            Toast.makeText(this, "Please add location details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean isToday(long dateMillis) {
        java.util.Calendar selected = java.util.Calendar.getInstance();
        selected.setTimeInMillis(dateMillis);

        java.util.Calendar today = java.util.Calendar.getInstance();

        return selected.get(java.util.Calendar.YEAR)         == today.get(java.util.Calendar.YEAR) &&
                selected.get(java.util.Calendar.MONTH)        == today.get(java.util.Calendar.MONTH) &&
                selected.get(java.util.Calendar.DAY_OF_MONTH) == today.get(java.util.Calendar.DAY_OF_MONTH);
    }

    private int getSelectedHour() {
        String[] parts = tvTime.getText().toString().split(":");
        int hour = Integer.parseInt(parts[0]);
        String amPm = spinnerAmPm.getSelectedItem().toString();

        if (amPm.equals("AM")) {
            if (hour == 12) hour = 0;
        } else {
            if (hour != 12) hour += 12;
        }
        return hour;
    }

    private int getSelectedMinute() {
        String[] parts = tvTime.getText().toString().split(":");
        return Integer.parseInt(parts[1]);
    }
}