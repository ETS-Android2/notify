package com.example.myapplication.CreateNewEvent;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;

public class NewEventDateFragment extends Fragment {

    private Button nextEventCreateButton;

    public NewEventDateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event_date, container, false);
        nextEventCreateButton = view.findViewById(R.id.next_event_create_button);
        nextEventCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, new NewEventMapFragment()).commit();
            }
        });
        getActivity().setTitle("Create event");

        return view;
    }

    //TODO doTheWork();
}
