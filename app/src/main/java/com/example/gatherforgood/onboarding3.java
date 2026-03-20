package com.example.gatherforgood;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

public class onboarding3 extends Fragment {

    ProgressBar progressBar;
    Button btnNext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_onboarding3, container, false);
        btnNext = view.findViewById(R.id.btnNext);
        progressBar = view.findViewById(R.id.progressBar);
        btnNext.setOnClickListener(v -> {
            setInProgress();
            ((OnboardingScreen) requireActivity()).navigateToNextPage(2);
        });

        return view;
    }
    private void setInProgress() {
            progressBar.setVisibility(android.view.View.VISIBLE);
            btnNext.setVisibility(android.view.View.INVISIBLE);
            btnNext.setEnabled(false);
    }
}