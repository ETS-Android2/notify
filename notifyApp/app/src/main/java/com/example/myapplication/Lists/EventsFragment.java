package com.example.myapplication.Lists;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapters.AdapterEventFeed;
import com.example.myapplication.MyObjects.MyEvent;
import com.example.myapplication.R;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.example.myapplication.ViewModels.EventViewModel;

import java.util.ArrayList;


public class EventsFragment extends Fragment {

    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        String uid = authViewModel.getAuthUser().getValue().getUid();

        ArrayList<MyEvent> eventsList = eventViewModel.fetchEventsForFeed(uid).getValue();

        RecyclerView recyclerView = view.findViewById(R.id.events_feed);
        AdapterEventFeed eventFeedAdapter = new AdapterEventFeed(getActivity(), eventsList);

        recyclerView.setAdapter(eventFeedAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

}
