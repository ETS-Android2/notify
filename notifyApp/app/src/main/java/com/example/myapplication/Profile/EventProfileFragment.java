package com.example.myapplication.Profile;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.myapplication.Models.MyEvent;
import com.example.myapplication.R;
import com.example.myapplication.ViewModels.EventViewModel;
import com.squareup.picasso.Picasso;

public class EventProfileFragment extends Fragment {

    private static final String TAG = "EventProfileFragment: ";

    private String eventID;
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
    private EventViewModel eventViewModel;

    public EventProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_profile, container, false);

        eventID = EventProfileFragmentArgs.fromBundle(getArguments()).getEventID();

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

        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventViewModel.fetchSingleEvent(eventID).observe(getViewLifecycleOwner(), new Observer<MyEvent>() {
            @Override
            public void onChanged(MyEvent myEvent) {
                try {
                    Picasso.get().load(myEvent.getEventImageUri()).placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher).into(eventImg);
                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage());
                }
                eventName.setText(myEvent.getEventName());
                eventDesc.setText(myEvent.getEventDescription());
                Log.i(TAG, "DATE HERE: " + myEvent.getEventDateAndTime().toDate());
                eventPlace.setText(myEvent.getEventPlaceName());
                if (myEvent.getParticipantList() == null) {
                    partNum.setText("0 Participant");
                }else{
                    partNum.setText(myEvent.getParticipantList().size() + "Participants");
                }
            }
        });
    }
}
