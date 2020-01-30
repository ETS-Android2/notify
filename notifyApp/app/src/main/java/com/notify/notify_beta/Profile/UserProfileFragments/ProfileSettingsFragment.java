package com.notify.notify_beta.Profile.UserProfileFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.notify.notify_beta.Models.MyUser;
import com.notify.notify_beta.R;
import com.notify.notify_beta.ViewModels.AuthViewModel;
import com.notify.notify_beta.ViewModels.EventViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileSettingsFragment extends Fragment {

    private static final String TAG = "ProfileSettingsFragment: ";

    //Fields
    private TextInputEditText userNameEdit;
    private TextInputEditText userEmailEdit;
    private TextInputEditText userPassEdit;
    private TextInputEditText userPassConfirmEdit;
    private TextInputEditText userPhoneEdit;
    private TextInputEditText userTitleEdit;
    private TextInputEditText userBioEdit;
    private Button updateProfileBtn;
    private TextView logoutBtn;

    //ViewModels
    private AuthViewModel authViewModel;
    private EventViewModel eventViewModel;

    //MyUsers
    private MyUser currentUser;
    private MyUser updatedUser;

    //NavController
    private NavController navController;

    public ProfileSettingsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragments_profile_settings, container, false);

        //Init fields
        userNameEdit = v.findViewById(R.id.settings_username);
        userEmailEdit = v.findViewById(R.id.settings_email);
        userPassEdit = v.findViewById(R.id.settings_password);
        userPassConfirmEdit = v.findViewById(R.id.settings_password_confirm);
        userPhoneEdit = v.findViewById(R.id.settings_phone);
        userTitleEdit = v.findViewById(R.id.settings_title);
        userBioEdit = v.findViewById(R.id.settings_bio);
        updateProfileBtn = v.findViewById(R.id.settings_update);
        logoutBtn = v.findViewById(R.id.settings_logout);

        //Init ViewModels
        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        //Get current user, if exists
        if (authViewModel.observeCurrentUser().getValue() != null) {
            currentUser = authViewModel.observeCurrentUser().getValue();
        } else {
            Log.i(TAG, "current user null");
        }

        //Get current user
        authViewModel.observeCurrentUser().observe(getViewLifecycleOwner(), new Observer<MyUser>() {
            @Override
            public void onChanged(MyUser user) {
                if (user != null) {
                    //Set user info to ui
                    currentUser = user;
                    userNameEdit.setText(user.getName());
                    userEmailEdit.setText(user.getEmail());
                    userPhoneEdit.setText(user.getPhoneNumber());
                    if (user.getTitel() != null && !user.getTitel().isEmpty()) {
                        userTitleEdit.setText(user.getTitel());
                    }
                    if (user.getCompany() != null && !user.getCompany().isEmpty()) {
                        userBioEdit.setText(user.getCompany());
                    }
                }
            }
        });


        updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()) {
                    //Check if form valid and start update
                    Log.i(TAG, "Form is valid");
                    updateProfileInfo();
                }
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logout from firebaseAuth
                //Destroy(set null) all in app saved shared variables
                authViewModel.logout();
                destroyAllAuthShared();
                //Clean backstack
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                //Navigate to login page
                navController.navigate(UserProfileFragmentDirections.actionUserProfileFragmentToLoginNav());
            }
        });

        return v;


    }

    private void destroyAllAuthShared(){
        //Destroy(set null) all in app saved shared variables
        authViewModel.destroyAll();
        eventViewModel.destroyAll();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Init navController
        navController = Navigation.findNavController(view);
    }

    private boolean validateForm() {
        //Check if form is valid and set errors
        if (!userPassEdit.getText().toString().isEmpty() || !userPassConfirmEdit.getText().toString().isEmpty()) {
            if (!userPassEdit.getText().toString().equals(userPassConfirmEdit.getText().toString())) {
                userPassConfirmEdit.setError("Passwords must match");
                userPassEdit.setError("Passwords must match");
            } else {
                userPassEdit.setError(null);
                userPassConfirmEdit.setError(null);
            }
        } else {
            userPassEdit.setError(null);
            userPassConfirmEdit.setError(null);
        }
        if (!userNameEdit.getText().toString().isEmpty() &&
                !userEmailEdit.getText().toString().isEmpty() &&
                !userPhoneEdit.getText().toString().isEmpty() &&
                userPhoneEdit.getText().toString().length() == 10 &&
                !userEmailEdit.getText().toString().isEmpty() &&
                userEmailEdit.getText().toString().contains("@") &&
                userPassEdit.getError() == null &&
                userPassConfirmEdit.getError() == null) {
            return true;
        } else {
            return false;
        }
    }

    private void updateProfileInfo() {
        //Get all fields
        String email = userEmailEdit.getText().toString();
        String name = userNameEdit.getText().toString();
        String phone = userPhoneEdit.getText().toString();
        String title = userTitleEdit.getText().toString();
        String bio = userBioEdit.getText().toString();
        String password = userPassEdit.getText().toString();
        //Create new user from current user
        //Set all info as they have changed
        updatedUser = currentUser;
        updatedUser.setEmail(email);
        updatedUser.setName(name);
        updatedUser.setPhoneNumber(phone);
        updatedUser.setTitel(title);
        updatedUser.setCompany(bio);
        //Check if password will change
        if (!password.isEmpty()) {
            Log.i(TAG, "Updating user with password");
            authViewModel.updatePassword(password).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (aBoolean) {
                        //Update user after password changed
                        Toast.makeText(getContext(), "Password changed", Toast.LENGTH_SHORT).show();
                        authViewModel.updateUser(updatedUser).observe(getViewLifecycleOwner(), new Observer<MyUser>() {
                            @Override
                            public void onChanged(MyUser user) {
                                authViewModel.updateCurrentUserInfo(user);
                                Toast.makeText(getContext(), "User updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Cant change password", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            //Password will not be chnaged, update user only
            Log.i(TAG, "Updating user without password");
            authViewModel.updateUser(updatedUser).observe(getViewLifecycleOwner(), new Observer<MyUser>() {
                @Override
                public void onChanged(MyUser user) {
                    //Replace updated user with current user
                    authViewModel.updateCurrentUserInfo(user);
                    Toast.makeText(getContext(), "User updated", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

}
