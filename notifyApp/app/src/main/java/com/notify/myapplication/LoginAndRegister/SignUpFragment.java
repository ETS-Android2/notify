package com.notify.myapplication.LoginAndRegister;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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

import com.google.firebase.auth.FirebaseUser;
import com.notify.myapplication.LoginNavDirections;
import com.notify.myapplication.R;
import com.notify.myapplication.ViewModels.AuthViewModel;

import static androidx.navigation.Navigation.findNavController;

public class SignUpFragment extends Fragment implements View.OnClickListener {


    private static final String TAG = "Sign Up Fragment: ";

    //Fields
    private EditText userName;
    private EditText email;
    private EditText phoneNum;
    private EditText titel;
    private EditText company;
    private EditText password;
    private EditText passwordCont;

    //ViewModel
    private AuthViewModel viewModel;

    //NavController
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        //Init fields
        userName = view.findViewById(R.id.signUpName);
        email = view.findViewById(R.id.signUpEmail);
        phoneNum = view.findViewById(R.id.signUpPhoneNumber);
        titel = view.findViewById(R.id.signUpTitel);
        company = view.findViewById(R.id.signUpCompany);
        password = view.findViewById(R.id.signUpPassword);
        passwordCont = view.findViewById(R.id.signUpCheckPassword);
        view.findViewById(R.id.signUpButton).setOnClickListener(this);
        view.findViewById(R.id.signUpBackArrow).setOnClickListener(this);


        //Check username field
        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (userName.getText().toString().length() <= 0) {
                    userName.setError("Bu alan doldurulmalı");
                } else {
                    userName.setError(null);
                }
            }
        });

        //Check email field
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (email.getText().toString().length() <= 0) {
                    email.setError("Bu alan doldurulmalı");
                } else {
                    email.setError(null);
                }
            }
        });

        //Check phone field
        phoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (phoneNum.getText().toString().length() <= 0) {
                    phoneNum.setError("Bu alan doldurulmalı");
                } else {
                    phoneNum.setError(null);
                }
            }
        });

        //Check password field
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (password.getText().toString().length() <= 0) {
                    password.setError("Bu alan doldurulmalı");
                } else {
                    password.setError(null);
                }
            }
        });

        //Check password confirmation field
        passwordCont.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordCont.getText().toString().length() <= 0) {
                    passwordCont.setError("Bu alan doldurulmalı");
                } else {
                    passwordCont.setError(null);
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init viewModel
        viewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        viewModel.AuthViewModel();

        //Init navController
        navController = findNavController(view);

        //Handle on backbutton pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navController.popBackStack();
                    }
                });

    }

    //Check if form ok
    private boolean validateForm() {
        boolean validForm = false;
        if (TextUtils.equals(password.toString(), passwordCont.toString())) {
            validForm = false;
            passwordCont.setError("Parolalar eşleşmiyor");
        } else if (TextUtils.isEmpty(userName.toString()) || TextUtils.isEmpty(email.toString()) || TextUtils.isEmpty(phoneNum.toString())
                || TextUtils.isEmpty(titel.toString()) || TextUtils.isEmpty(company.toString()) || TextUtils.isEmpty(password.toString())) {
            validForm = false;
            Toast.makeText(getActivity(), "Gerekli tüm alanlar doldurulmalı.", Toast.LENGTH_LONG).show();
        } else {
            validForm = true;
        }
        return validForm;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpButton:
                if (!validateForm()) {
                    return;
                } else {
                    //If form ok, sign up
                    viewModel.signUpWithInfo(email, password, company, userName, phoneNum, titel).observe(this, new Observer<FirebaseUser>() {
                        @Override
                        public void onChanged(FirebaseUser firebaseUser) {
                            if (firebaseUser != null) {
                                //Get returned signed up user and go to profile
                                LoginNavDirections.ActionGlobalUserProfileFragment action = LoginNavDirections.actionGlobalUserProfileFragment();
                                action.setUserID(firebaseUser.getUid());
                                Navigation.findNavController(getView()).navigate(action);
                            } else {
                                Log.e(TAG,"Can't find user");
                                return;
                            }
                        }
                    });
                }
                break;
            case R.id.signUpBackArrow:
                //Go back to login page
                Navigation.findNavController(getView()).popBackStack();
                break;
        }
    }

}
