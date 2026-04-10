package com.example.gatherforgood;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreatePrayer extends AppCompatActivity {

    TextView tvSelectedDate;
    ImageView ivPickDate;
    RelativeLayout rlDate;

    Long selectedDateMillis;
    TextView tvTime;
    Spinner spinnerAmPm;


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
        init();
        setListeners();
    }
    private void init(){
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        ivPickDate = findViewById(R.id.ivPickDate);
        rlDate = findViewById(R.id.rldate);
        tvTime = findViewById(R.id.tvTime);
        spinnerAmPm = findViewById(R.id.spinnerAmPm);

    }
    private void setListeners() {
        ivPickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Gathering Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

            datePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
                String date = sdf.format(new Date(selection));
                tvSelectedDate.setText(date);
                tvSelectedDate.setTextColor(Color.parseColor("#F9F1D7"));
                selectedDateMillis = selection;
            });
        });

        rlDate.setOnClickListener(v -> {

            ivPickDate.performClick();

        });

        tvTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(7)
                    .setMinute(0)
                    .setTitleText("Select Time")
                    .build();

            timePicker.show(getSupportFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(v2 -> {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String amPm = hour < 12 ? "AM" : "PM";
                int displayHour = hour % 12 == 0 ? 12 : hour % 12;

                tvTime.setText(String.format("%02d:%02d", displayHour, minute));

                spinnerAmPm.setSelection(amPm.equals("AM") ? 0 : 1);
            });
        });
    }

}