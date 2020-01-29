package com.example.myapplication.LoginAndRegister;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.LoginNavDirections;
import com.example.myapplication.R;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

import static androidx.navigation.Navigation.findNavController;


public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "Login Fragment: ";

    //Fields
    private EditText mEmailField;
    private EditText mPasswordField;

    //ViewModel
    private AuthViewModel viewModel;

    //Check if form ok
    private boolean loginAllowed;

    //Control multiple return values
    private int semaphore;

    //NavController
    private NavController navController;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);


        //Initialization of Email and Password EditTexts
        mEmailField = view.findViewById(R.id.emailEditText);
        mPasswordField = view.findViewById(R.id.passwordEditText);

        mEmailField.addTextChangedListener(emailWatcher);
        mPasswordField.addTextChangedListener(passWatcher);

        loginAllowed = false;

        //Buttons for login options
        view.findViewById(R.id.loginButton).setOnClickListener(this);
        view.findViewById(R.id.googleSignInButton).setOnClickListener(this);
        view.findViewById(R.id.facebookSignInButton).setOnClickListener(this);
        view.findViewById(R.id.signUpIntentButton).setOnClickListener(this);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init viewModel
        viewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        viewModel.AuthViewModel();

        //Set NavController
        navController = findNavController(view);

        //Handle backpressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        getActivity().finish();

                    }
                });

        String loggedInUserID = isUserLoggedIn();
        //Check if user is already logged in
        if (loggedInUserID != null) {
            Log.i(TAG, "No need to authenticate, user logged in");
            //If so, get userID and go to profile
            LoginNavDirections.ActionGlobalUserProfileFragment action = LoginNavDirections.actionGlobalUserProfileFragment();
            action.setUserID(loggedInUserID);
            navController.navigate(action);
        }
    }

    private String isUserLoggedIn() {
        return viewModel.isLoggedIn();
    }

    //Detect which login option has chosen
    //Detect if sign up function is chosen
    @Override
    public void onClick(View v) {
        int logInType = v.getId();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        switch (logInType) {
            case R.id.loginButton:
                if (isLoginAllowed()) {
                    semaphore = 1;
                    viewModel.authenticate(email, password).observe(this, new Observer<FirebaseUser>() {
                        @Override
                        public void onChanged(FirebaseUser firebaseUser) {
                            if (firebaseUser != null) {
                                if (semaphore == 1) {
                                    Log.i(TAG, navController.getCurrentDestination().getLabel().toString());
                                    Toast.makeText(getContext(), "Login success", Toast.LENGTH_SHORT).show();
                                    LoginNavDirections.ActionGlobalUserProfileFragment action = LoginNavDirections.actionGlobalUserProfileFragment();
                                    action.setUserID(firebaseUser.getUid());
                                    Log.i(TAG,"Authenticated userID: " + action.getUserID());
                                    navController.navigate(action);
                                    semaphore--;
                                }
                            } else {
                                    Log.e(TAG, "Can't find user");
                                    Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                                    semaphore--;

                            }
                        }
                    });
                }
                break;
            case R.id.googleSignInButton:
            case R.id.facebookSignInButton:
                Toast.makeText(getContext(),"This service will be avaliable soon",Toast.LENGTH_SHORT).show();
                break;
            case R.id.signUpIntentButton:
                //Start signUp flow
                Navigation.findNavController(getView()).navigate(R.id.signUpFragment);
        }
    }


    //Check and warn if input fields are empty
    TextWatcher emailWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            loginAllowed = false;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mEmailField.getText().toString().length() <= 0) {
                mEmailField.setError("Bu alan doldurulmalı");
                loginAllowed = false;
            } else {
                mEmailField.setError(null);
                loginAllowed = true;
            }
        }
    };

    //Check and warn if input fields are empty
    TextWatcher passWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            loginAllowed = false;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mPasswordField.getText().toString().length() <= 0) {
                mPasswordField.setError("Bu alan doldurulmalı");
                loginAllowed = false;
            } else {
                mPasswordField.setError(null);
                loginAllowed = true;
            }
        }
    };

    //Check if neccessary fields are filled
    private boolean isLoginAllowed() {
        if (mEmailField.getText().toString().length() <= 0) {
            mEmailField.setError("Bu alan doldurulmalı");
            loginAllowed = false;
        } else if (mPasswordField.getText().toString().length() <= 0) {
            mPasswordField.setError("Bu alan doldurulmalı");
            loginAllowed = false;
        }

        return loginAllowed;
    }

}
