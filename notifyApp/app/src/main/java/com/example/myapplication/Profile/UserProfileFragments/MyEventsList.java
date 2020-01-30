package com.example.myapplication.Profile.UserProfileFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapters.AdapderEventCard;
import com.example.myapplication.Listeners.OnRecycleItemClickedListener;
import com.example.myapplication.Models.MyEvent;
import com.example.myapplication.Models.MyUser;
import com.example.myapplication.ViewModels.EventViewModel;
import com.example.myapplication.ViewModels.UsersViewModel;
import com.example.myapplication.myapplication.notify_beta.R;

import java.util.ArrayList;
import java.util.List;

public class MyEventsList extends Fragment {

    private static final String TAG = "MyEventsList: ";

    //RecyclerView & its adapter
    private RecyclerView recyclerView;
    private AdapderEventCard adapderEventCard;

    //List
    private List<MyEvent> createdEvents;

    //State
    private String userID;
    private boolean forCurrent;

    //ViewModel
    private EventViewModel eventViewModel;
    private UsersViewModel usersViewModel;

    //CustomListeners
    private OnRecycleItemClickedListener listenerForEvent;
    private OnRecycleItemClickedListener listenerForOrganisator;

    //NavController
    private NavController navController;

    public MyEventsList() {
        // Required empty public constructor
    }

    public MyEventsList(String userID, boolean forCurrent) {
        //Init fragment variables
        this.userID = userID;
        this.forCurrent = forCurrent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_myevents, container, false);

        //Init viewModel
        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();
        usersViewModel = ViewModelProviders.of(requireActivity()).get(UsersViewModel.class);
        usersViewModel.UsersViewModel();

        //Init list
        createdEvents = new ArrayList<>();

        listenerForEvent = new OnRecycleItemClickedListener() {
            @Override
            public void onItemOfListClicked(Object o) {
                //On event clicked, go to event profile
                UserProfileFragmentDirections.ActionUserProfileFragmentToEventProfileFragment action = UserProfileFragmentDirections.actionUserProfileFragmentToEventProfileFragment((MyEvent) o);
                navController.navigate(action);
            }

            @Override
            public void onItemLongClicked(Object o) {

            }
        };

        listenerForOrganisator = new OnRecycleItemClickedListener() {
            @Override
            public void onItemOfListClicked(Object o) {
                //On organisator clicked, go to user profile
                UserProfileFragmentDirections.ActionUserProfileFragmentSelf action = UserProfileFragmentDirections.actionUserProfileFragmentSelf();
                action.setUser((MyUser) o);
                navController.navigate(action);
            }

            @Override
            public void onItemLongClicked(Object o) {

            }
        };

        //Init recyclerView & its adapter
        recyclerView = v.findViewById(R.id.my_events_recyclerview);
        adapderEventCard = new AdapderEventCard(createdEvents, getContext(), listenerForEvent, listenerForOrganisator);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapderEventCard);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //If for current user set all organisators from current user
        if (forCurrent) {
            if (eventViewModel.observeMyEvents().getValue() != null) {
                createdEvents.clear();
                createdEvents = eventViewModel.observeMyEvents().getValue();
                adapderEventCard.setMyEventList(createdEvents);
                adapderEventCard.notifyDataSetChanged();
            } else {
                eventViewModel.getEventsCreated(userID, forCurrent).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyEvent>>() {
                    @Override
                    public void onChanged(ArrayList<MyEvent> myEvents) {
                        createdEvents.clear();
                        createdEvents = myEvents;
                        adapderEventCard.setMyEventList(createdEvents);
                        adapderEventCard.notifyDataSetChanged();
                        Log.i(TAG, "Created events list is updated");
                    }
                });
            }
        } else {
            //If not for current user, get organisator
            eventViewModel.getEventsCreated(userID, forCurrent).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyEvent>>() {
                @Override
                public void onChanged(ArrayList<MyEvent> myEvents) {
                    createdEvents.clear();
                    createdEvents = myEvents;
                    adapderEventCard.setMyEventList(createdEvents);
                    adapderEventCard.notifyDataSetChanged();
                    usersViewModel.getOrganisator(userID).observe(getViewLifecycleOwner(), new Observer<MyUser>() {
                        @Override
                        public void onChanged(MyUser user) {
                            for (int i = 0; i<createdEvents.size();i++) {
                                createdEvents.get(i).setOrganisator(user);
                                adapderEventCard.notifyDataSetChanged();
                            }
                        }
                    });
                    Log.i(TAG, "Created events list is updated");
                }
            });
        }

        //Init navController
        navController = Navigation.findNavController(view);
    }

    public void setOrganisator(MyUser organisator) {
        //Set organisator to events and update ui
        if (createdEvents != null) {
            for (int i = 0; i < createdEvents.size(); i++) {
                createdEvents.get(i).setOrganisator(organisator);
            }
            adapderEventCard.notifyDataSetChanged();
        }
    }

}
