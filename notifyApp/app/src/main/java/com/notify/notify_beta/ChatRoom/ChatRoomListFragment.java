package com.notify.notify_beta.ChatRoom;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.notify.notify_beta.R;

public class ChatRoomListFragment extends Fragment {


    public ChatRoomListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        getActivity().setTitle("Chat Groups");
        return inflater.inflate(R.layout.fragment_chat_room_list, container, false);
    }

}
