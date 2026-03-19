package com.example.gatherforgood;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Paint;
import java.util.HashMap;
import java.util.Map;

public class RegisterAccount extends AppCompatActivity {

    ImageButton btnBack;
    boolean isBrother;
    TextView btnSister;
    TextView btnBrother;
    ImageView ivPasswordVisibility;
    ImageView ivConfirmPasswordVisibility;
    EditText etPassword;
    EditText etConfirmPassword;
    EditText etFullName;
    EditText etEmail;
    boolean isPasswordVisible;
    boolean isConfirmPasswordVisible;
    AppCompatButton btnCreateAccount;
    TextView tvSignIn;
    FirebaseAuth mAuth;

    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        toggleVisButton(ivPasswordVisibility, etPassword, isPasswordVisible);
        toggleVisButton(ivConfirmPasswordVisibility, etConfirmPassword, isConfirmPasswordVisible);
        setEventListeners();
    }

    public void init() {
        isPasswordVisible = false;
        isConfirmPasswordVisible = false;
        isBrother = true;
        btnBack = findViewById(R.id.btnBack);
        btnBrother = findViewById(R.id.btnBrother);
        btnSister = findViewById(R.id.btnSister);
        ivPasswordVisibility = findViewById(R.id.ivPasswordVisibility);
        ivConfirmPasswordVisibility = findViewById(R.id.ivConfirmPasswordVisibility);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvSignIn = findViewById(R.id.tvSignIn);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
    }

    public void setEventListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnBrother.setOnClickListener(v -> {
            isBrother = true;
            toggleIdButton();
        });

        btnSister.setOnClickListener(v -> {
            isBrother = false;
            toggleIdButton();
        });

        ivPasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            toggleVisButton(ivPasswordVisibility, etPassword, isPasswordVisible);
        });

        ivConfirmPasswordVisibility.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            toggleVisButton(ivConfirmPasswordVisibility, etConfirmPassword, isConfirmPasswordVisible);
        });

        btnCreateAccount.setOnClickListener(v -> {
            if (verifyFields()) {
                registerUser();
            }
        });

        tvSignIn.setOnClickListener(v -> {
            navigateToSignInScreen();
            tvSignIn.setPaintFlags(tvSignIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        });
    }

    private void setInProgress(boolean isProcessing) {
        if (isProcessing) {
            progressBar.setVisibility(android.view.View.VISIBLE);
            btnCreateAccount.setVisibility(android.view.View.INVISIBLE);
            btnCreateAccount.setEnabled(false);
        } else {
            progressBar.setVisibility(android.view.View.GONE);
            btnCreateAccount.setVisibility(android.view.View.VISIBLE);
            btnCreateAccount.setEnabled(true);
        }
    }
    public void navigateToSignInScreen() {
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
    }

    public void registerUser() {


        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etFullName.getText().toString().trim();
        String gender = isBrother ? "Male" : "Female";

        setInProgress(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(name,email,gender);
                    } else {
                        setInProgress(false);
                        String errorMsg = task.getException().getMessage();
                        if (errorMsg != null && errorMsg.contains("email address is already in use")) {
                            etEmail.setError("This email is already registered");
                            etEmail.requestFocus();
                        } else if (errorMsg != null && errorMsg.contains("badly formatted")) {
                            etEmail.setError("Invalid email format");
                            etEmail.requestFocus();
                        } else if (errorMsg != null && errorMsg.contains("password")) {
                            etPassword.setError("Password is too weak");
                            etPassword.requestFocus();
                        } else {
                            etEmail.setError("Registration failed: " + errorMsg);
                            etEmail.requestFocus();
                        }
                    }
                });
    }

    public void saveUserToFirestore(String name, String email, String gender) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("gender", gender);
        userData.put("isVerified",false);

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(unused -> {
                    navigateToVerifyEmailScreen(email);
                    finish();
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setInProgress(false);
                    Toast.makeText(this, "Failed to save data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    public void navigateToVerifyEmailScreen(String email) {
        Intent intent = new Intent(this, VerifyEmailScreen.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
    public boolean verifyFields() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etFullName.setError("Name is required");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

    public void toggleIdButton() {
        if (isBrother) {
            btnBrother.setBackgroundResource(R.drawable.register_bg_gender_active);
            btnSister.setBackgroundResource(R.drawable.register_bg_gender_toggle);
        } else {
            btnBrother.setBackgroundResource(R.drawable.register_bg_gender_toggle);
            btnSister.setBackgroundResource(R.drawable.register_bg_gender_active);
        }
    }

    public void toggleVisButton(ImageView ivVisibility, EditText etPass, boolean isVisible) {
        if (!isVisible) {
            ivVisibility.setImageResource(R.drawable.register_ic_visibility);
            etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            ivVisibility.setImageResource(R.drawable.register_ic_visibility_off);
            etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        etPass.setSelection(etPass.getText().length());
    }
}