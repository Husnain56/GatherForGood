package com.example.gatherforgood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VerifyEmailScreen extends AppCompatActivity {

    FirebaseAuth mAuth;
    AppCompatButton btnVerified;
    TextView tvResend;
    TextView tvEmail;
    ProgressBar progressBar;
    String name, email, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_email_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setEventListeners();
        resendEmail();
    }

    public void init() {
        mAuth = FirebaseAuth.getInstance();
        btnVerified = findViewById(R.id.btnVerified);
        tvResend = findViewById(R.id.tvResend);
        tvEmail = findViewById(R.id.tvEmail);
        progressBar = findViewById(R.id.progressBar);

        // Retrieve data passed from Registration Screen
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        gender = getIntent().getStringExtra("gender");

        tvEmail.setText(email);
    }

    public void setEventListeners() {
        btnVerified.setOnClickListener(v -> checkVerification());

        tvResend.setOnClickListener(v -> resendEmail());
    }

    private void resendEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Verification email resent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to resend: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    public void checkVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            setInProgress(true);

            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    saveUserToFirestore();
                } else {
                    setInProgress(false);
                    Toast.makeText(this,
                            "Email not verified yet. Please check your inbox.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void saveUserToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("gender", gender);

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    navigateToLoginScreen();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(this, "Failed to save data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setInProgress(boolean isProcessing) {
        if (isProcessing) {
            progressBar.setVisibility(View.VISIBLE);
            btnVerified.setVisibility(View.INVISIBLE);
            btnVerified.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnVerified.setVisibility(View.VISIBLE);
            btnVerified.setEnabled(true);
        }
    }

    public void navigateToLoginScreen() {
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
    }
}