package com.example.myapplication.LoginAndRegister;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.R;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

import static androidx.navigation.Navigation.findNavController;


public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = "Login Fragment: ";

    private EditText mEmailField;
    private EditText mPasswordField;
    private AuthViewModel viewModel;
    private boolean loginAllowed;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_login, container, false);


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
        viewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        viewModel.AuthViewModel();
        final NavController navController = findNavController(view);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        //Login cancelled
                        //TODO Maybe implemented
                        if (!navController.popBackStack()) {
                            getActivity().finish();
                        }
                    }
                });
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
                    viewModel.authenticate(email, password);
                    viewModel.getAuthUser().observe(this, new Observer<FirebaseUser>() {
                        @Override
                        public void onChanged(FirebaseUser firebaseUser) {
                            if (firebaseUser != null) {
                                viewModel.initUser(firebaseUser);
                                Navigation.findNavController(getView()).navigate(R.id.userProfileFragment);
                            } else {
                                Log.e(TAG,"Can't reach user");
                                return;
                            }
                        }
                    });
                }
                break;
            case R.id.googleSignInButton:
//                viewModel.authenticate(R.id.googleSignInButton, "", "");
                break;
            case R.id.facebookSignInButton:
//                viewModel.authenticate(R.id.facebookSignInButton, "", "");
                break;
            case R.id.signUpIntentButton:
                Navigation.findNavController(getView()).navigate(R.id.signUpFragment);
        }
    }

    //Update profile information for the logged in user
    //TODO

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
