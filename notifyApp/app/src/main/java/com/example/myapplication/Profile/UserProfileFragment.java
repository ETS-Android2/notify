package com.example.myapplication.Profile;


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

import com.example.myapplication.Adapters.AdapderEventCard;
import com.example.myapplication.ImageOperations.PicassoCircleTransformation;
import com.example.myapplication.Models.MyEvent;
import com.example.myapplication.Models.MyUser;
import com.example.myapplication.R;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class UserProfileFragment extends Fragment implements View.OnClickListener {

    FirebaseFirestore db;
    private Uri imageUri;
    private ImageView userProfileImg;
    private TextView usernameProfile;
    private TextView titelProfile;
    private TextView companyProfile;
    private static final int PICK_IMAGE_REQUEST = 1;

    private SimpleDateFormat formatter;
    private MyUser currentUser;

    private ViewPager viewPager;
    private AdapderEventCard adapderEventCard;
    private List<MyEvent> eventList;

    private AuthViewModel viewModel;


    private static final String TAG = "UserProfile Fragment: ";

    public UserProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Inflate fragment layout
        getActivity().setTitle("Profile");

        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        userProfileImg = view.findViewById(R.id.user_profile_img);
        usernameProfile = view.findViewById(R.id.profile_username);
        titelProfile = view.findViewById(R.id.profile_titel);
        companyProfile = view.findViewById(R.id.profile_company);
        userProfileImg.setOnClickListener(this);

        //Init list & adapter
        eventList = new ArrayList<>();
        //TODO Get events from DB
        adapderEventCard = new AdapderEventCard(eventList, getActivity());
        viewPager = view.findViewById(R.id.event_cards_on_profile);
        viewPager.setAdapter(adapderEventCard);
        viewPager.setPadding(130, 0, 130, 0);

        viewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        viewModel.AuthViewModel();

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
        if (viewModel.isLoggedIn()) {
            setUserDataToUI();
            Log.e(TAG, "setUserDataUI called");
        } else {

            Navigation.findNavController(view).navigate(R.id.to_loginNav);
        }
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
            Picasso.get().load(imageUri.toString()).transform(new PicassoCircleTransformation()).into(userProfileImg);
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
            viewModel.uploadProfileImage(imageUri,currentUser,getFileExtention(imageUri));
        } else {
            Log.w(TAG, "No file selected");
        }

    }

    private void getEvents() {

        //TODO checkHERE!!!!!
        //Get all users from db
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                eventList.clear();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot user : task.getResult()) {
                        //Pars collected data(users) into instances
                        Map<String, Object> eventDataOnUserProfile = user.getData();
                        MyEvent singleEvent = new MyEvent();
                        singleEvent.setEventName(eventDataOnUserProfile.get("eventName").toString());
                        //singleEvent.setEventDate((Date) eventDataOnUserProfile.get("eventDate").toString());
                        //TODO splitAndSetEventDate
                        singleEvent.setThumbnail(eventDataOnUserProfile.get("thumbnail").toString());
                        //Add user to the list
                        eventList.add(singleEvent);

                    }
                } else {
                    //If task has failed
                    Log.w(TAG, "Database problem on loading event grid layout");
                }
            }

        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.user_profile_img) {
            chooseImage();
        }
    }

    //Get authenticated user data from the viewModel and update UI according to that data
    private void setUserDataToUI() {
        viewModel.getAuthenticatedUser().observe(this, new Observer<MyUser>() {
            @Override
            public void onChanged(MyUser myUser) {
                if (myUser != null) {
                    currentUser = myUser;
                    usernameProfile.setText(myUser.getName());
                    titelProfile.setText(myUser.getTitel());
                    companyProfile.setText(myUser.getCompany());
                    try {
                        Picasso.get().load(myUser.getProfileImageUri()).transform(new PicassoCircleTransformation()).placeholder(R.mipmap.ic_launcher)
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
