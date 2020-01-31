package com.notify.myapplication.Profile.EventProfileFragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.StorageReference;
import com.notify.myapplication.ImageOperations.PicassoCircleTransformation;
import com.notify.myapplication.Models.MyEvent;
import com.notify.myapplication.Models.MyUser;
import com.notify.myapplication.R;
import com.notify.myapplication.ViewModels.AuthViewModel;
import com.notify.myapplication.ViewModels.EventViewModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class EventProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "EventProfileFragment: ";

    //MyEvent
    private MyEvent event;
    //Fields & Layouts
    private ImageView eventImg;
    private TextView eventName;
    private TextView eventDesc;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventPlace;
    private TextView partNum;
    private TextView friendsNum;
    private TextView galleryNum;
    private ImageButton eventSettings;
    private Button eventJoin;
    private ImageView organisatorImg;
    private TextView organisatorName;
    private LinearLayout organisatorLayout;

    //Toolbar & MainActivity
    private Toolbar toolbar;
    private AppCompatActivity activityForBar;

    //BottomNav View
    private BottomNavigationView bottomNav;

    //NavController
    private NavController navController;

    //ViewModels
    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;

    //CurrentUser
    private MyUser currentUser;
    private String userID;

    public EventProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_profile, container, false);

        //Get received event from list on event click
        event = EventProfileFragmentArgs.fromBundle(getArguments()).getSelectedEvent();

        //Init fields
        eventImg = view.findViewById(R.id.event_profile_img);
        eventName = view.findViewById(R.id.profile_eventname);
        eventDesc = view.findViewById(R.id.event_profile_desc);
        eventDate = view.findViewById(R.id.event_profile_date);
        eventTime = view.findViewById(R.id.event_profile_time);
        eventPlace = view.findViewById(R.id.event_profile_placename);
        partNum = view.findViewById(R.id.event_profile_partnum);
        friendsNum = view.findViewById(R.id.event_profile_friendsnum);
        galleryNum = view.findViewById(R.id.event_profile_gallery);
        eventSettings = view.findViewById(R.id.event_profile_settings);
        eventJoin = view.findViewById(R.id.event_profile_join);
        organisatorImg = view.findViewById(R.id.event_profile_organisator_img);
        organisatorName = view.findViewById(R.id.event_profile_organisator_name);
        organisatorLayout = view.findViewById(R.id.event_profile_organisator_layout);

        //Init toolBar
        if (toolbar == null) {
            Log.i(TAG, "TOOLBAR NULL");
            setHasOptionsMenu(true);
            toolbar = view.findViewById(R.id.event_profile_toolbar);
            activityForBar = (AppCompatActivity) getActivity();
            activityForBar.setSupportActionBar(toolbar);
            activityForBar.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_32dp);
        activityForBar.getSupportActionBar().show();

        getActivity().setTitle("Event Profile");

        //Init bottomNavigationView
        bottomNav = getActivity().findViewById(R.id.bottom_nav);
        bottomNav.setVisibility(View.GONE);

        //Init viewModels
        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        //Get shared current user
        currentUser = authViewModel.observeCurrentUser().getValue();
        Log.i(TAG, currentUser.getName());
        Log.i(TAG, currentUser.getId());
        userID = currentUser.getId();

        //Set event data to ui
        setEventToUI();

        partNum.setOnClickListener(this);
        friendsNum.setOnClickListener(this);
        eventJoin.setOnClickListener(this);
        galleryNum.setOnClickListener(this);
        galleryNum.setClickable(false);
        eventPlace.setOnClickListener(this);

        //Get event organisator, if already received
        if(event.getOrganisator() != null){
            Picasso.get().load(event.getOrganisator().getProfileImage()).transform(new PicassoCircleTransformation()).into(organisatorImg);
            organisatorName.setText(event.getOrganisator().getName());
            organisatorLayout.setOnClickListener(this);
        }

        //Get event gallery image references to calculate gallery image count
        eventViewModel.getGalleryStorageRefs(event.getEventID()).observe(getViewLifecycleOwner(), new Observer<ArrayList<StorageReference>>() {
            @Override
            public void onChanged(ArrayList<StorageReference> storageReferences) {
                galleryNum.setText(storageReferences.size() / 2 + " photos, see all");
                galleryNum.setClickable(true);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Navigate back on back icon pressed
                bottomNav.setVisibility(View.VISIBLE);
                navController.popBackStack();
            }
        });
    }

    //Set event data to ui
    private void setEventToUI() {
        eventJoin.setText("Join");
        eventJoin.setBackgroundColor(0x21C064);
        if (event.getEventImg() != null) {
            eventImg.setImageBitmap(event.getEventImg());
        } else {
            try {
                Picasso.get().load(event.getEventImageUri()).into(eventImg);
                BitmapDrawable drawable = (BitmapDrawable) eventImg.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                event.setEventImg(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        }
        eventName.setText(event.getEventName());
        eventDesc.setText(event.getEventDescription());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateAndTime = dateFormat.format(event.getEventDateAndTime().toDate());
        Log.i(TAG,"EVENT DATE: " + dateAndTime);
        String[] dateTimeArray = dateAndTime.split(" ");
        eventDate.setText(dateTimeArray[0]);
        eventTime.setText(dateTimeArray[1]);
        Log.i(TAG, "DATE: " + event.getEventDateAndTime().toDate());
        eventPlace.setText(event.getEventPlaceName());

        if (event.getParticipantsID() == null || event.getParticipantsID().isEmpty()) {
            partNum.setText("0 Participant");
            friendsNum.setText("See all");
            friendsNum.setClickable(true);
            partNum.setClickable(true);
        } else {
            partNum.setText(event.getParticipantsID().size() + " Participants");
            friendsNum.setText(calcFriendsNum(event.getParticipantsID(), currentUser.getFriendsIDs()) + " friends in there, see all");
            friendsNum.setClickable(false);
            partNum.setClickable(false);
        }

        if (event.getOrganisatorID().equals(userID)) {
            eventJoin.setVisibility(View.INVISIBLE);
        } else if (event.getParticipantsID().contains(userID)) {
            //Set color pink
            eventJoin.setBackgroundColor(0xffb6c1);
            eventJoin.setText("Joined");
        }
        eventSettings.setVisibility(View.GONE);

    }

    //Calculate how many friends joined to the event
    private int calcFriendsNum(ArrayList<String> partList, ArrayList<String> friendList) {
        int friendsNum = 0;
        for (String friend : friendList) {
            if (partList.contains(friend)) {
                friendsNum++;
            }
        }
        return friendsNum;
    }

    private void mapIntent() {
        //Open event place in googleMaps
        if (event.getEventPlaceName() != null && event.getEventPlaceID() != null && !event.getEventPlaceID().isEmpty() && !event.getEventPlaceName().isEmpty()) {
            String placeNameForMap = event.getEventPlaceName().replace(" ", "%20");
            String placeIDForMap = event.getEventPlaceID();

            Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + placeNameForMap + "&query_place_id=" + placeIDForMap);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }else{
                Toast.makeText(getContext(),"No useful app found for this service", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getContext(),"Event has no place",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == partNum.getId() || v.getId() == friendsNum.getId()) {
            //Go to participants list
            String[] partList = new String[event.getParticipantsID().size()];
            if (partList.length == 0 || partList == null) {
                Toast.makeText(getContext(), "No participants", Toast.LENGTH_SHORT).show();
            } else {
                for (int i = 0; i < event.getParticipantsID().size(); i++) {
                    partList[i] = event.getParticipantsID().get(i);
                }
                EventProfileFragmentDirections.ActionEventProfileFragmentToUsersFragment action =
                        EventProfileFragmentDirections.actionEventProfileFragmentToUsersFragment(partList);
                navController.navigate(action);
            }
        } else if (v.getId() == eventJoin.getId()) {
            //Dis/join event & set ui accordingly
            if (isJoinedAlready()) {
                eventViewModel.disjoinEvent(event.getEventID(), userID);
                //Set color green
                eventJoin.setBackground(getResources().getDrawable(R.drawable.rounded_button, getActivity().getTheme()));
                eventJoin.setText("Join");
            }
            eventViewModel.joinEvent(event.getEventID(), userID);
            //Set color pink
            eventJoin.setBackground(getResources().getDrawable(R.drawable.rounded_button_pink, getActivity().getTheme()));
            eventJoin.setText("Joined");
        } else if (v.getId() == galleryNum.getId()) {
            //Go to gallery
            EventProfileFragmentDirections.ActionEventProfileFragmentToEventGalleryFragment action = EventProfileFragmentDirections.actionEventProfileFragmentToEventGalleryFragment(event);
            navController.navigate(action);
        } else if (v.getId() == eventPlace.getId()) {
            mapIntent();
        }else if(v.getId() == organisatorLayout.getId()){
            //Go to organisator profile
            EventProfileFragmentDirections.ActionEventProfileFragmentToUserProfileFragment action = EventProfileFragmentDirections.actionEventProfileFragmentToUserProfileFragment();
            action.setUser(event.getOrganisator());
            navController.navigate(action);
        }
    }

    private boolean isJoinedAlready() {
        //If already joined set ui
        if (event.getParticipantsID().contains(userID)) {
            return true;
        } else return false;
    }
}
