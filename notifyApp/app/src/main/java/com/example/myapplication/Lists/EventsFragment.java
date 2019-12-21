package com.example.myapplication.Lists;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapters.AdapterEventFeed;
import com.example.myapplication.Models.MyEvent;
import com.example.myapplication.R;
import com.example.myapplication.Repositories.MyEventFetchListener;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.example.myapplication.ViewModels.EventViewModel;

import java.util.ArrayList;


public class EventsFragment extends Fragment {

    private static final String TAG = "EventListFragment: ";

    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;
    private ArrayList<MyEvent> eventsList = new ArrayList<>();
    private AdapterEventFeed eventFeedAdapter;

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


        new MyEventFetchListener(this).execute(uid);

//        ArrayList<MyEvent> eventsList = eventViewModel.fetchEventsForFeed(uid).getValue();

        RecyclerView recyclerView = view.findViewById(R.id.events_feed);
        eventFeedAdapter = new AdapterEventFeed(getActivity(), eventsList);

        recyclerView.setAdapter(eventFeedAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    public void updateAdapter(ArrayList<MyEvent> newEvents){
        eventFeedAdapter.setMyEventList(newEvents);
        eventFeedAdapter.notifyDataSetChanged();
        Log.i(TAG,"Adapter updated");
    }

}
