package com.example.gatherforgood;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginScreen extends AppCompatActivity {

    ImageButton btnBack;
    EditText etEmail;
    EditText etPassword;
    TextView tvForgot;
    AppCompatButton btnSignIn;
    ImageView ivPasswordVisibility;
    TextView tvJoinCommunity;

    FirebaseAuth mAuth;
    ProgressBar progressBar;
    boolean isPasswordVisible;
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    FirebaseFirestore db;

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

    public void init(){
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvForgot = findViewById(R.id.tvForgot);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvJoinCommunity = findViewById(R.id.tvJoinCommunity);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        ivPasswordVisibility = findViewById(R.id.ivPasswordVisibility);
        isPasswordVisible = false;
        sPref = getSharedPreferences("user",MODE_PRIVATE);
        editor = sPref.edit();
        db = FirebaseFirestore.getInstance();
    }
    public void setEventListeners(){
        tvJoinCommunity.setOnClickListener(v->{;
            setInProgress(true);
            navigateToJoinCommunity();
            finish();
        });
        btnBack.setOnClickListener(v->{
            finish();
        });
        btnSignIn.setOnClickListener(v->{
            VerifyCredentials();
        });
        tvForgot.setOnClickListener(v-> {
            resetPassword();
        });
        ivPasswordVisibility.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            toggleVisButton(ivPasswordVisibility, etPassword, isPasswordVisible);
        });
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

    public void navigateToJoinCommunity(){
        Intent intent = new Intent(this, RegisterAccount.class);
        startActivity(intent);
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
                        Toast.makeText(LoginScreen.this,
                                "Reset link sent! Please check your inbox.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginScreen.this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public boolean verifyFields() {
        String email = etEmail.getText().toString().trim();
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
    public void VerifyCredentials(){
        if(verifyFields()){
            login();
        }
    }
    public void storePreferences(String name){
        editor.putBoolean("isLoggedIn",true);
        editor.putString("user_name", name);
        editor.apply();
    }
    public void login(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        setInProgress(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        checkUserVerification();
                    }
                    else{
                        setInProgress(false);
                        Toast.makeText(LoginScreen.this, "Invalid email or password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void startHomeNavigationFlow() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            storePreferences(name);
                            Intent intent = new Intent(LoginScreen.this, HomeScreen.class);
                            startActivity(intent);
                            setInProgress(false);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        setInProgress(false);
                        Toast.makeText(this, "Failed to sync profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void checkUserVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified()) {
                // Start the name-fetch and navigation flow
                startHomeNavigationFlow();
            } else {
                setInProgress(false);
                Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                navigateToVerifyScreen();
            }
        }
    }

    private void navigateToVerifyScreen(){
        Intent intent = new Intent(this, VerifyEmailScreen.class);
        intent.putExtra("email", etEmail.getText().toString());
        startActivity(intent);
    }

    public void setInProgress(boolean isProcessing) {
        if (isProcessing) {
            progressBar.setVisibility(android.view.View.VISIBLE);
            btnSignIn.setVisibility(android.view.View.INVISIBLE);
            btnSignIn.setEnabled(false);
        } else {
            progressBar.setVisibility(android.view.View.GONE);
            btnSignIn.setVisibility(android.view.View.VISIBLE);
            btnSignIn.setEnabled(true);
        }
    }

}