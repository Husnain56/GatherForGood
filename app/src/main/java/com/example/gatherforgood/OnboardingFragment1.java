package com.example.gatherforgood;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OnboardingFragment1 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_onboarding1, container, false);
        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            ((OnboardingScreen) requireActivity()).navigateToNextPage(0);
        });
        return view;
    }

}