package com.example.gatherforgood;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

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
                // registration logic will be added later
            }
        });

        tvSignIn.setOnClickListener(v -> navigateToSignInScreen());
    }

    public void navigateToSignInScreen() {
        Intent intent = new Intent(this, LoginScreen.class);
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
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
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