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
import com.example.myapplication.R;
import com.example.myapplication.ViewModels.EventViewModel;
import com.example.myapplication.ViewModels.UsersViewModel;

import java.util.ArrayList;

public class ParticipatedEventsList extends Fragment {

    private static final String TAG = "PartEventsList: ";

    //RecyclerView & its adapter
    private RecyclerView recyclerView;
    private AdapderEventCard adapderEventCard;

    //Lists
    private ArrayList<MyEvent> participatedEvents;
    private ArrayList<String> organisatorIDList;

    //State
    private String userID;
    private boolean forCurrent;

    //ViewModels
    private EventViewModel eventViewModel;
    private UsersViewModel usersViewModel;

    //CustomListeners
    private OnRecycleItemClickedListener listenerForEvent;
    private OnRecycleItemClickedListener listenerForOrganisator;

    //NavController
    private NavController navController;

    public ParticipatedEventsList() {
        // Required empty public constructor
    }

    public ParticipatedEventsList(String userID, boolean forCurrent) {
        //Init fragment variables
        this.userID = userID;
        this.forCurrent = forCurrent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragments_events_part, container, false);

        //Init viewModels
        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();
        usersViewModel = ViewModelProviders.of(requireActivity()).get(UsersViewModel.class);
        usersViewModel.UsersViewModel();

        //Init list
        participatedEvents = new ArrayList<>();

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

        //Init recyclerView
        recyclerView = v.findViewById(R.id.part_events_recyclerview);
        adapderEventCard = new AdapderEventCard(participatedEvents, getContext(), listenerForEvent,listenerForOrganisator);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapderEventCard);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //If for current user set all organisators from current user
        if (forCurrent) {
            if (eventViewModel.observeJoinedByCurrent().getValue() != null) {
                participatedEvents.clear();
                participatedEvents = eventViewModel.observeJoinedByCurrent().getValue();
                getOrganizators(participatedEvents);
                adapderEventCard.setMyEventList(participatedEvents);
                adapderEventCard.notifyDataSetChanged();
                Log.i(TAG, "Created events list is updated");
            } else {
                eventViewModel.getJoinedEvents(userID, forCurrent).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyEvent>>() {
                    @Override
                    public void onChanged(ArrayList<MyEvent> myEvents) {
                        participatedEvents.clear();
                        participatedEvents = myEvents;
                        getOrganizators(participatedEvents);
                        adapderEventCard.setMyEventList(participatedEvents);
                        adapderEventCard.notifyDataSetChanged();
                        Log.i(TAG, "Created events list is updated");
                    }
                });
            }
        } else {
            //If not for current user, get organisator
            eventViewModel.getJoinedEvents(userID, forCurrent).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyEvent>>() {
                @Override
                public void onChanged(ArrayList<MyEvent> myEvents) {
                    participatedEvents.clear();
                    participatedEvents = myEvents;
                    getOrganizators(participatedEvents);
                    adapderEventCard.setMyEventList(participatedEvents);
                    adapderEventCard.notifyDataSetChanged();
                    Log.i(TAG, "Created events list is updated");
                }
            });
        }
        navController = Navigation.findNavController(view);
    }

    private void getOrganizators(ArrayList<MyEvent> myEvents) {
        if (organisatorIDList == null) {
            organisatorIDList = new ArrayList<>();
        }
        for (MyEvent event : myEvents) {
            if (usersViewModel.observeOrganisatorListForFeed().getValue() != null) {
                //If Some organisators are already fetched, set organisators to their events
                Log.i(TAG,"Some organisators are already fetched");
                MyUser tempUser = new MyUser();
                tempUser.setId(event.getOrganisatorID());
                if (usersViewModel.observeOrganisatorListForFeed().getValue().contains(tempUser)) {
                    //No need to get the same organisator for every user, he created
                    //If organsiator once added to the list, dont add again
                    int pos = usersViewModel.observeOrganisatorListForFeed().getValue().indexOf(tempUser);
                    MyUser organisator = usersViewModel.observeOrganisatorListForFeed().getValue().get(pos);
                    Log.i(TAG,"Organisator with id: " + organisator.getId() + "is set for event: " + event.getEventName());
                    participatedEvents.get(participatedEvents.indexOf(event)).setOrganisator(organisator);
                    adapderEventCard.notifyDataSetChanged();
                    Log.i(TAG, "Item changed on organisator adding");
                }
            }
            //No need to get the same organisator for every user, he created
            //If organsiator once added to the list, dont add again
            if (!organisatorIDList.contains(event.getOrganisatorID())) {
                Log.i(TAG,"Organisator with id: " + event.getOrganisatorID() + "is added to list");
                organisatorIDList.add(event.getOrganisatorID());
            }

        }
        //Get organisators via list of organisatorIDs
        usersViewModel.getOrganisatorListForFeed(organisatorIDList).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyUser>>() {
            @Override
            public void onChanged(ArrayList<MyUser> myUsers) {
                for (MyUser user : myUsers) {
                    //Set organisators to the events, they had created
                    Log.i(TAG, "User received with username: " + user.getName());
                    Log.i(TAG,"EventList size: "+ participatedEvents.size());
                    for (MyEvent event : participatedEvents) {
                        Log.i(TAG,"Organisator ID: " + event.getOrganisatorID() + "\nUser ID: " + user.getId());
                        if (event.getOrganisatorID().equals(user.getId())) {
                            //Add if they match
                            int pos = participatedEvents.indexOf(event);
                            participatedEvents.get(pos).setOrganisator(user);
                            Log.i(TAG,"User with name: " + user.getName() + "is added to event with name " + event.getEventName());
                            adapderEventCard.notifyDataSetChanged();
                            Log.i(TAG, "Item changed on organisator adding");
                        }
                    }
                }
            }
        });

    }

    public ArrayList<MyEvent> getParticipatedEvents(){
        return participatedEvents;
    }
}
