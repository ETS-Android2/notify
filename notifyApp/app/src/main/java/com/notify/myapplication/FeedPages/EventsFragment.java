package com.notify.myapplication.FeedPages;


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

import com.notify.myapplication.Adapters.AdapterEventFeed;
import com.notify.myapplication.Listeners.OnRecycleItemClickedListener;
import com.notify.myapplication.Models.MyEvent;
import com.notify.myapplication.Models.MyUser;
import com.notify.myapplication.R;
import com.notify.myapplication.ViewModels.AuthViewModel;
import com.notify.myapplication.ViewModels.EventViewModel;
import com.notify.myapplication.ViewModels.UsersViewModel;

import java.util.ArrayList;


public class EventsFragment extends Fragment {

    private static final String TAG = "EventListFragment: ";

    //ViewModels
    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;
    private UsersViewModel usersViewModel;

    //Lists
    private ArrayList<MyEvent> eventsList;
    private ArrayList<String> organisatorIDList;

    //RecyclerViewAdapter
    private AdapterEventFeed eventFeedAdapter;

    //NavController
    private NavController navController;

    //CustomListener
    private OnRecycleItemClickedListener listenerForEvent;
    private OnRecycleItemClickedListener listenerForOrganisator;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        listenerForEvent = new OnRecycleItemClickedListener() {
            @Override
            public void onItemOfListClicked(Object o) {
                //On event item clicked, go to event's profile page
                //Get event info from list via listener
                FeedFragmentDirections.ActionFeedFragmentToEventProfileFragment action = FeedFragmentDirections.actionFeedFragmentToEventProfileFragment((MyEvent) o);
                navController.navigate(action);
            }

            @Override
            public void onItemLongClicked(Object o) {

            }
        };

        listenerForOrganisator = new OnRecycleItemClickedListener() {
            @Override
            public void onItemOfListClicked(Object o) {
                //On organisator layout clicked, go to organisator's profile page
                //Get user info from list via listener
                FeedFragmentDirections.ActionFeedFragmentToUserProfileFragment action = FeedFragmentDirections.actionFeedFragmentToUserProfileFragment();
                action.setUser((MyUser) o);
                navController.navigate(action);
            }

            @Override
            public void onItemLongClicked(Object o) {

            }
        };

        //Init list
        if (eventsList == null) {
            eventsList = new ArrayList<>();
        }

        //Get viewModels
        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        usersViewModel = ViewModelProviders.of(requireActivity()).get(UsersViewModel.class);
        usersViewModel.UsersViewModel();

        //Init RecyclerView and its adapter
        RecyclerView recyclerView = view.findViewById(R.id.events_feed);
        eventFeedAdapter = new AdapterEventFeed(getActivity(), eventsList, listenerForEvent, listenerForOrganisator);
        recyclerView.setAdapter(eventFeedAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        //Get events to create feed
        if (eventViewModel.observeEventsForFeed().getValue() == null) {
            eventViewModel.fetchEventsForFeed(authViewModel.observeCurrentUser().getValue().getFriendsIDs()).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyEvent>>() {
                @Override
                public void onChanged(ArrayList<MyEvent> myEvents) {
                    eventsList = myEvents;
                    //Update list with returned events
                    updateAdapter(eventsList);
                    Log.i(TAG, "Events fetched");
                    //Get event organisators
                    getOrganizators(eventsList);
                }
            });
        } else {
            //If feed exists, get it
            eventsList = eventViewModel.observeEventsForFeed().getValue();
            //Update adapter and get their organisators
            updateAdapter(eventsList);
            getOrganizators(eventsList);
        }

        return view;
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
                    eventsList.get(eventsList.indexOf(event)).setOrganisator(organisator);
                    eventFeedAdapter.notifyDataSetChanged();
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
                    Log.i(TAG,"EventList size: "+ eventsList.size());
                    for (MyEvent event : eventsList) {
                        Log.i(TAG,"Organisator ID: " + event.getOrganisatorID() + "\nUser ID: " + user.getId());
                        if (event.getOrganisatorID().equals(user.getId())) {
                            //Add if they match
                            int pos = eventsList.indexOf(event);
                            eventsList.get(pos).setOrganisator(user);
                            Log.i(TAG,"User with name: " + user.getName() + "is added to event with name " + event.getEventName());
                            eventFeedAdapter.notifyDataSetChanged();
                            Log.i(TAG, "Item changed on organisator adding");
                        }
                    }
                }
            }
        });


    }


    public void updateAdapter(ArrayList<MyEvent> newEvents) {
        //Update adapter
        eventFeedAdapter.setMyEventList(newEvents);
        eventFeedAdapter.notifyDataSetChanged();
        Log.i(TAG, "Adapter updated");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Init navController
        navController = Navigation.findNavController(view);
    }

}
