package com.example.gatherforgood;

import static com.bumptech.glide.Glide.init;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EventsFragment extends Fragment {

    FloatingActionButton btnCreateEvent;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        init(view);
        setListeners();
    }

    public void init(View view){
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
    }
    public void setListeners(){
        btnCreateEvent.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), CreateEvent.class);
            startActivity(intent);
        });
    }
}