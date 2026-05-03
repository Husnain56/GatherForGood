package com.example.gatherforgood;

import android.content.Intent;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginScreen extends AppCompatActivity {

    ImageButton     btnBack;
    EditText        etEmail, etPassword;
    TextView        tvForgot, tvJoinCommunity;
    AppCompatButton btnSignIn;
    ImageView       ivPasswordVisibility;
    ProgressBar     progressBar;

    FirebaseAuth      mAuth;
    FirebaseFirestore db;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        setEventListeners();
    }

    private void init() {
        btnBack              = findViewById(R.id.btnBack);
        etEmail              = findViewById(R.id.etEmail);
        etPassword           = findViewById(R.id.etPassword);
        tvForgot             = findViewById(R.id.tvForgot);
        btnSignIn            = findViewById(R.id.btnSignIn);
        tvJoinCommunity      = findViewById(R.id.tvJoinCommunity);
        ivPasswordVisibility = findViewById(R.id.ivPasswordVisibility);
        progressBar          = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
    }

    private void setEventListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvJoinCommunity.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterAccount.class));
            finish();
        });

        btnSignIn.setOnClickListener(v -> verifyCredentials());

        tvForgot.setOnClickListener(v -> resetPassword());

        ivPasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility();
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            ivPasswordVisibility.setImageResource(R.drawable.register_ic_visibility_off);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            ivPasswordVisibility.setImageResource(R.drawable.register_ic_visibility);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Enter your registered email first");
            etEmail.requestFocus();
            return;
        }
        setInProgress(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Reset link sent! Check your inbox.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean verifyFields() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    private void verifyCredentials() {
        if (verifyFields()) login();
    }

    private void login() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        setInProgress(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserVerification();
                    } else {
                        setInProgress(false);
                        Toast.makeText(this,
                                "Invalid email or password. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        if (user.isEmailVerified()) {
            navigateToHome(user.getUid());
        } else {
            setInProgress(false);
            Toast.makeText(this,
                    "Please verify your email first.",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, VerifyEmailScreen.class);
            intent.putExtra("email", etEmail.getText().toString());
            startActivity(intent);
        }
    }

    private void navigateToHome(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        getSharedPreferences("user", MODE_PRIVATE)
                                .edit()
                                .putString("user_name", name)
                                .apply();
                    }
                    setInProgress(false);
                    startActivity(new Intent(this, HomeScreen.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    startActivity(new Intent(this, HomeScreen.class));
                    finish();
                });
    }

    private void setInProgress(boolean isProcessing) {
        progressBar.setVisibility(isProcessing
                ? android.view.View.VISIBLE : android.view.View.GONE);
        btnSignIn.setVisibility(isProcessing
                ? android.view.View.INVISIBLE : android.view.View.VISIBLE);
        btnSignIn.setEnabled(!isProcessing);
    }
}