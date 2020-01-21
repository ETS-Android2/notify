package com.example.myapplication.Profile.UserProfileFragments;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.Adapters.AdapterViewPagerUserProfile;
import com.example.myapplication.Models.MyUser;
import com.example.myapplication.R;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.example.myapplication.ViewModels.EventViewModel;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class UserProfileFragment extends Fragment implements View.OnClickListener {

    private Uri imageUri;
    private ImageView userProfileImg;
    private TextView usernameProfile;
    private TextView titelProfile;
    private TextView companyProfile;
    private static final int PICK_IMAGE_REQUEST = 1;

    private MyUser currentUser;

    private AuthViewModel authViewModel;
    private EventViewModel eventViewModel;


    private TabLayout tabLayout;
    private AdapterViewPagerUserProfile adapterViewPager;
    private ViewPager viewPager;


    private static final String TAG = "UserProfile Fragment: ";

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Inflate fragment layout
        getActivity().setTitle("Profile");

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userProfileImg = view.findViewById(R.id.user_profile_img);
        usernameProfile = view.findViewById(R.id.profile_username);
        titelProfile = view.findViewById(R.id.profile_titel);
        companyProfile = view.findViewById(R.id.profile_company);


        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        if (authViewModel.isLoggedIn()) {
            setUserDataToUI();
            Log.e(TAG, "setUserDataUI called");
        } else {
            Navigation.findNavController(view).navigate(R.id.to_loginNav);
        }

        String userID = authViewModel.getAuthUser().getValue().getUid();
        tabLayout = view.findViewById(R.id.profile_tablayout);
        viewPager = view.findViewById(R.id.profile_viewpager);

        //Init adapter if its not in savedState
        if(adapterViewPager == null) {
            adapterViewPager = new AdapterViewPagerUserProfile(getChildFragmentManager(), AdapterViewPagerUserProfile.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            MyEventsList myEventsList = new MyEventsList();
            ParticipatedEventsList participatedEventsList = new ParticipatedEventsList();
            ProfileSettingsFragment profileSettingsFragment = new ProfileSettingsFragment();

            adapterViewPager.addFragment(myEventsList, "My Events");
            eventViewModel.fetchEvents(myEventsList, userID);
            Log.e(TAG, "BURASIII!!!: " + adapterViewPager.getCount());
            adapterViewPager.addFragment(participatedEventsList, "Participated Events");
            eventViewModel.fetchEvents(participatedEventsList, userID);

            adapterViewPager.addFragment(profileSettingsFragment, "Settings");
        }
            Log.e(TAG, "BURASIII!!!: "+ adapterViewPager.getCount());

        //setOffScreenPageLimit solved problem about disappearing fragment content in ViewPager
        viewPager.setOffscreenPageLimit(2);

        viewPager.setAdapter(adapterViewPager);
        tabLayout.setupWithViewPager(viewPager);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        //Login cancelled
                        //TODO Maybe implemented
                        if (!Navigation.findNavController(view).popBackStack()) {
                            getActivity().finish();
                        }
                    }
                });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            imageUri = data.getData();
            //TODO controlImgSize();
            Picasso.get().load(imageUri.toString()).into(userProfileImg);
            Log.w(TAG, "User Profile Image is selected");
            uploadImg();
        } else {
            Log.w(TAG, "User Profile Image is not selected");
        }

    }

    private String getFileExtention(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImg() {
        if (imageUri != null) {
            authViewModel.uploadProfileImage(imageUri, currentUser, getFileExtention(imageUri));
        } else {
            Log.w(TAG, "No file selected");
        }

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.user_profile_img) {
            chooseImage();
        }
    }

    //Get authenticated user data from the authViewModel and update UI according to that data
    private void setUserDataToUI() {
        authViewModel.getAuthenticatedUser().observe(this, new Observer<MyUser>() {
            @Override
            public void onChanged(MyUser myUser) {
                if (myUser != null) {
                    currentUser = myUser;
                    usernameProfile.setText(myUser.getName());
                    titelProfile.setText(myUser.getTitel());
                    companyProfile.setText(myUser.getCompany());
                    try {
                        Picasso.get().load(myUser.getProfileImage()).placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher).into(userProfileImg);
                        Log.w(TAG, "Profile image is set from Cloud");
                    } catch (Exception e) {
                        Log.w(TAG, e.getMessage());
                    }

                }
            }
        });
    }


}
