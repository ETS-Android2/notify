package com.notify.notify_beta.Profile.UserProfileFragments;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.notify.notify_beta.Adapters.AdapterViewPagerUserProfile;
import com.notify.notify_beta.Models.MyUser;
import com.notify.notify_beta.R;
import com.notify.notify_beta.ViewModels.AuthViewModel;
import com.notify.notify_beta.ViewModels.EventViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;
import static com.notify.notify_beta.Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE;
import static com.notify.notify_beta.Constants.PICK_IMAGE_REQUEST;

public class UserProfileFragment extends Fragment implements View.OnClickListener {

    //User profile image uri
    private Uri imageUri;

    //Fields
    private ImageView userProfileImg;
    private TextView usernameProfile;
    private TextView titelProfile;
    private TextView companyProfile;
    private Button followBtnProfile;
    private TextView followersNum;
    private TextView followingNum;
    private TextView eventsNum;

    //MyUser
    private MyUser user;
    private String userID;

    //States
    private boolean forCurrentUser;
    private boolean isFollowedByCurrent;

    //ViewModels
    private AuthViewModel authViewModel;
    private EventViewModel eventViewModel;

    //ViewPager & its adapter
    private TabLayout tabLayout;
    private AdapterViewPagerUserProfile adapterViewPager;
    private ViewPager viewPager;

    //Arguments
    private Bundle args;

    //NavController
    private NavController navController;

    //Toolbar & BottomNavigationView & MainActivity
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private AppCompatActivity activityForBar;

    //Child fragments
    private MyEventsList myEventsList;
    private ParticipatedEventsList participatedEventsList;
    private ProfileSettingsFragment profileSettingsFragment;

    private static final String TAG = "UserProfile Fragment: ";

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle("Profile");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Init bottomNavigationView
        bottomNav = getActivity().findViewById(R.id.bottom_nav);

        //Init toolBar
        if (toolbar == null) {
            Log.i(TAG, "TOOLBAR NULL");
            setHasOptionsMenu(true);
            toolbar = view.findViewById(R.id.profile_toolbar);
            activityForBar = (AppCompatActivity) getActivity();
            activityForBar.setSupportActionBar(toolbar);
        }

        //Hide toolbar until, forCurrent checked
        activityForBar.getSupportActionBar().hide();

        //Init fields
        userProfileImg = view.findViewById(R.id.user_profile_img);
        userProfileImg.setOnClickListener(this);
        usernameProfile = view.findViewById(R.id.profile_username);
        titelProfile = view.findViewById(R.id.profile_titel);
        companyProfile = view.findViewById(R.id.profile_company);
        followBtnProfile = view.findViewById(R.id.profile_follow_btn);
        followBtnProfile.setOnClickListener(this);
        followersNum = view.findViewById(R.id.user_profil_followerNum);
        followingNum = view.findViewById(R.id.user_profil_followingNum);
        eventsNum = view.findViewById(R.id.user_profil_eventsNum);

        //Init viewModels
        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();


        return view;
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init navController
        navController = Navigation.findNavController(view);
        navController.restoreState(savedInstanceState);

        //Check if logged in
        if (authViewModel.isLoggedIn() == null) {
            navController.navigate(UserProfileFragmentDirections.actionUserProfileFragmentToLoginNav());
        }

        if (getArguments() != null) {
            //Getting arguments from bundle
            Log.i(TAG, "Getting arguments from bundle");
            args = getArguments();
            user = UserProfileFragmentArgs.fromBundle(getArguments()).getUser();
            userID = UserProfileFragmentArgs.fromBundle(getArguments()).getUserID();
            Log.i(TAG, "Received userID: " + userID);
            //Check if user is known
            if (user != null && userID == null) {
                Log.i(TAG,"userID is null");
                //Init userID from user
                userID = user.getId();
                //If returned user is authenticated user
                if (userID.equals(authViewModel.isLoggedIn())) {
                    forCurrentUser = true;
                } else {
                    forCurrentUser = false;
                }
                //Check if user is unknown but userID is known
            } else if (user == null && userID != null) {
                Log.i(TAG,"user is null");
                //Check if userID is equal to authenticated userID
                if (userID.equals(authViewModel.isLoggedIn())) {
                    forCurrentUser = true;
                } else {
                    forCurrentUser = false;
                }
                //Nothing returned from bundle
            } else if (user == null && userID == null) {
                //Happens when navigation with bottom navigation
                userID = authViewModel.isLoggedIn();
                forCurrentUser = true;
            }
        } else {
            Log.i(TAG, "SOMETHING IS WRONG");
        }

        //Init viewPager
        tabLayout = view.findViewById(R.id.profile_tablayout);
        viewPager = view.findViewById(R.id.profile_viewpager);

        //Init adapter if its not in savedState
        if (viewPager.getAdapter() == null) {
            adapterViewPager = new AdapterViewPagerUserProfile(getChildFragmentManager(), AdapterViewPagerUserProfile.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            myEventsList = new MyEventsList(userID, forCurrentUser);
            participatedEventsList = new ParticipatedEventsList(userID, forCurrentUser);
            profileSettingsFragment = new ProfileSettingsFragment();

            adapterViewPager.addFragment(myEventsList, "My Events");
            adapterViewPager.addFragment(participatedEventsList, "Participated Events");
            if (forCurrentUser) {
                //Add settings only if profile forCurrent
                adapterViewPager.addFragment(profileSettingsFragment, "Settings");
            }
        }

        //setOffScreenPageLimit solved problem about disappearing fragment content in ViewPager
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapterViewPager);
        tabLayout.setupWithViewPager(viewPager);

        //Handle on back button pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getFragmentManager().getBackStackEntryCount() == 1) {
                            //Only login page is in stack, when backpressed finish app
                            getActivity().finish();
                        } else {
                            //Otherwise go one step back
                            navController.popBackStack();
                        }
                    }
                });

        if (forCurrentUser) {
            //Set ui for current user
            Log.i(TAG, "Setting profile for current user");
            followBtnProfile.setVisibility(View.GONE);
            bottomNav.setVisibility(View.VISIBLE);
            activityForBar.getSupportActionBar().hide();
            if (authViewModel.observeCurrentUser().getValue() == null) {
                //Get authenticated user data from the authViewModel and observe
                authViewModel.getCurrentUserProfile().observe(getViewLifecycleOwner(), new Observer<MyUser>() {
                    @Override
                    public void onChanged(MyUser myUser) {
                        if (myUser != null) {
                            user = myUser;
                            Log.i(TAG, "Current user profile data retrieved and set");
                            setUserDataToUI(user);
                        } else {
                            Log.e(TAG, "ERROR: No user found");
                        }
                    }
                });
            } else {
                //User profile data already retrieved
                Log.i(TAG, "User profile data already retrieved");
                user = authViewModel.observeCurrentUser().getValue();
                setUserDataToUI(user);
            }
        } else {
            //Setting profile for a selected user
            Log.i(TAG, "Setting profile for a selected user");
            isFollowedByCurrent = false;
            //Checking if user is followed
            if (authViewModel.observeCurrentUser().getValue().getFriendsIDs() != null) {
                setFollowBtnFollowed(authViewModel.observeCurrentUser().getValue().getFriendsIDs().contains(userID));
            }
            bottomNav.setVisibility(View.GONE);
            activityForBar.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activityForBar.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activityForBar.getSupportActionBar().show();
            //Set toolbar for back navigation
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_32dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomNav.setVisibility(View.VISIBLE);
                    navController.popBackStack();
                }
            });

            //Set user data to ui according to the state
            setUserDataToUI(user);
        }

    }




    private String getFileExtention(Uri uri) {
        //Get file extention type // should be jpg
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImg() {
        //Upload image if image successfully retrieved from internal gallery
        if (imageUri != null) {
            authViewModel.uploadProfileImage(imageUri, user, getFileExtention(imageUri));
        } else {
            Log.w(TAG, "No file selected");
        }

    }

    @Override
    public void onClick(View v) {
        if (forCurrentUser) {
            if (v.getId() == userProfileImg.getId()) {
                //Choose image if profile belongs to the current user
                chooseImage();
            }
        } else {
            if (v.getId() == followBtnProfile.getId()) {
                //Manage follow button ui and start un/follow flow
                if (!isFollowedByCurrent) {
                    authViewModel.followUserWithID(userID);
                    //Set color pink
                    followBtnProfile.setBackground(getResources().getDrawable(R.drawable.rounded_button_pink, getActivity().getTheme()));
                    followBtnProfile.setText("Followed");
                    authViewModel.observeCurrentUser().getValue().getFriendsIDs().add(userID);
                    isFollowedByCurrent = true;
                    //following number will be automatically updated, thanks to SnapshotListener
                } else {
                    authViewModel.unfollowUserWithID(userID);
                    followBtnProfile.setBackground(getResources().getDrawable(R.drawable.rounded_button, getActivity().getTheme()));
                    followBtnProfile.setText("Follow");
                    authViewModel.observeCurrentUser().getValue().getFriendsIDs().remove(userID);
                    isFollowedByCurrent = false;
                    if (followersNum != null && !followersNum.getText().toString().equals(0)) {
                        int followers = Integer.parseInt(followersNum.getText().toString());
                        followersNum.setText(String.valueOf(followers - 1));
                        //Snapshot not working, update manually
                    }else{
                        //If user has no followers
                        followersNum.setText(String.valueOf(0));
                    }
                }
            }
        }
    }

    //Update UI according to that data
    private void setUserDataToUI(final MyUser myUser) {
        if (myUser != null) {
            user = myUser;
            myEventsList.setOrganisator(myUser);
            //Set profile data to ui
            setFollowersNum(myUser.getId());
            if (myUser.getFriendsIDs() != null && !myUser.getFriendsIDs().isEmpty()) {
                int following = myUser.getFriendsIDs().size() - 1;
                Log.i(TAG, "Following: " + following);
                followingNum.setText(String.valueOf(following));//This list includes user itself
            } else {
                followingNum.setText(String.valueOf(0));
            }
            usernameProfile.setText(myUser.getName());
            titelProfile.setText(myUser.getTitel());
            companyProfile.setText(myUser.getCompany());
            if (myUser.getUserImage() == null) {
                try {
                    //Get profile image
                    Picasso.get().load(myUser.getProfileImage()).placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher).into(userProfileImg, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (forCurrentUser) {
                                BitmapDrawable drawable = (BitmapDrawable) userProfileImg.getDrawable();
                                Bitmap bitmap = drawable.getBitmap();
                                myUser.setUserImage(bitmap);
                                authViewModel.updateCurrentUser(myUser);
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });

                    Log.w(TAG, "Profile image is set from Cloud");
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            } else {
                //If user has already a saved image in savedState, use it
                userProfileImg.setImageBitmap(myUser.getUserImage());
            }
            //Set events number that user participated
            setEventsNum();
        }
    }


    private void setFollowBtnFollowed(boolean isFollowed) {
        //Manage follow button ui
        if (isFollowed) {
            followBtnProfile.setBackground(getResources().getDrawable(R.drawable.rounded_button_pink, getActivity().getTheme()));
            followBtnProfile.setText("Followed");
            isFollowedByCurrent = true;
        } else {
            followBtnProfile.setBackground(getResources().getDrawable(R.drawable.rounded_button, getActivity().getTheme()));
            followBtnProfile.setText("Follow");
            isFollowedByCurrent = false;
        }
    }

    private void setFollowersNum(String userID) {
        //Get how many users has this user in their friends list
        authViewModel.getFollowerNum(userID).observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer ınteger) {
                if (ınteger != null) {
                    followersNum.setText(String.valueOf(ınteger));
                }
            }
        });
    }

    public void setEventsNum() {
        //Get and set number of participated events
        if (participatedEventsList != null && participatedEventsList.getParticipatedEvents() != null && !participatedEventsList.getParticipatedEvents().isEmpty()) {
            eventsNum.setText(String.valueOf(participatedEventsList.getParticipatedEvents().size()));
        } else {
            eventsNum.setText(String.valueOf(0));
        }
    }

    private void chooseImage() {
        //Check external storage read permission
        //Choose profile image if current profile belongs to the current user
        checkReadPermission();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void checkReadPermission() {
        //Check external storage read permission
        //If not granted, ask
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
        return;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //If permission granted, choose image
                    chooseImage();
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            imageUri = data.getData();
            //TODO controlImgSize();
            //Saving image as fullsize and highest quality
            Picasso.get().load(imageUri.toString()).into(userProfileImg, new Callback() {
                @Override
                public void onSuccess() {
                    BitmapDrawable drawable = (BitmapDrawable) userProfileImg.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    user.setUserImage(bitmap);
                    authViewModel.updateCurrentUser(user);
                }

                @Override
                public void onError(Exception e) {

                }
            });
            Log.w(TAG, "User Profile Image is selected");
            //Upload image without ack
            uploadImg();
        } else {
            Log.w(TAG, "User Profile Image is not selected");
        }

    }

}
