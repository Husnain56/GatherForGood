package com.example.gatherforgood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserProfileFragment extends Fragment {

    SharedPreferences sPref;
    TextView tvUserName;

    AppCompatButton btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        init(view);
        setEventListeners();

    }

    public void init(View view){
        tvUserName = view.findViewById(R.id.tvUserName);
        btnLogout = view.findViewById(R.id.btnLogout);

        sPref = requireContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        String name = sPref.getString("user_name", "");

        tvUserName.setText(name);


    }
    public void setEventListeners(){
        btnLogout.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            sPref.edit().clear().apply();
            navigateTOLoginScreen();

        });
    }
    public void navigateTOLoginScreen(){
        Intent intent = new Intent(requireActivity(), LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}