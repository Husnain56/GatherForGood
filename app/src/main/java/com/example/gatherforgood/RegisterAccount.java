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

public class RegisterAccount extends AppCompatActivity {


    ImageButton btnBack;
    boolean isBrother;
    TextView btnSister;
    TextView btnBrother;
    ImageView ivPasswordVisibility;
    EditText etPassword;
    boolean isVisible;
    AppCompatButton btnCreateAccount;
    TextView tvSignIn;



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
        toggleVisButton();
        setEventListeners();
    }

    public void init(){
        isVisible = false;
        isBrother = true;
        btnBack = findViewById(R.id.btnBack);
        btnBrother = findViewById(R.id.btnBrother);
        btnSister = findViewById(R.id.btnSister);
        ivPasswordVisibility = findViewById(R.id.ivPasswordVisibility);
        etPassword = findViewById(R.id.etPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvSignIn = findViewById(R.id.tvSignIn);
    }
    public void setEventListeners(){
        btnBack.setOnClickListener(v->{
            finish();

        });
        btnBrother.setOnClickListener(v-> {
            isBrother = true;
            toggleIdButton();
        });
        btnSister.setOnClickListener(v-> {
            isBrother = false;
            toggleIdButton();
        });
        ivPasswordVisibility.setOnClickListener(v->{
            isVisible = !isVisible;
            toggleVisButton();
        });
        btnCreateAccount.setOnClickListener(v->{
                verifyEmail();
                navigateToSignInScreen();
                finish();
        });
        tvSignIn.setOnClickListener(v->{
                navigateToSignInScreen();
        });

    }
    public void navigateToSignInScreen(){
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
    }
    public void verifyEmail(){

    }
    public void toggleIdButton(){
        if(isBrother){
            btnBrother.setBackgroundResource(R.drawable.register_bg_gender_active);
            btnSister.setBackgroundResource(R.drawable.register_bg_gender_toggle);
        }
        else{
            btnBrother.setBackgroundResource(R.drawable.register_bg_gender_toggle);
            btnSister.setBackgroundResource(R.drawable.register_bg_gender_active);
        }
    }
    public void toggleVisButton(){
        if(!isVisible){
            ivPasswordVisibility.setImageResource(R.drawable.register_ic_visibility);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        }
        else{
            ivPasswordVisibility.setImageResource(R.drawable.register_ic_visibility_off);
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        etPassword.setSelection(etPassword.getText().length());
    }
}