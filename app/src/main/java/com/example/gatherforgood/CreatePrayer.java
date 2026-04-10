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
    EditText etTime;
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
        etTime = findViewById(R.id.etTime);

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

        
    }

}