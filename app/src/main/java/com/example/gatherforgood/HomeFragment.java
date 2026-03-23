package com.example.gatherforgood;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class HomeFragment extends Fragment {

    FrameLayout cardPrayerGatherings;
    FrameLayout cardVolunteerEvents;
    TextView tvUserName;
    SharedPreferences sPref;
    ImageView ivProfilePic;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        init(view);
        setEventListeners();

    }
    public void init(View view){

        cardPrayerGatherings = view.findViewById(R.id.cardPrayerGatherings);
        cardVolunteerEvents = view.findViewById(R.id.cardVolunteerEvents);
        tvUserName = view.findViewById(R.id.tvUserName);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);

        sPref = requireContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE);
        String name = sPref.getString("user_name", "");
        tvUserName.setText(name);

    }
    public void setEventListeners(){
        cardPrayerGatherings.setOnClickListener(view -> ((HomeScreen) requireActivity()).navigateToTab(1));
        ivProfilePic.setOnClickListener(view -> ((HomeScreen) requireActivity()).navigateToTab(4));
    }
}