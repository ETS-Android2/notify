package com.example.myapplication.ViewModels;

import android.net.Uri;
import android.widget.EditText;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.Models.MyUser;
import com.example.myapplication.Repositories.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private static final String TAG = "AuthViewModel: ";

    //Observable LiveDatas
    private LiveData<FirebaseUser> firebaseLoginUser;
    private LiveData<FirebaseUser> firebaseSignupUser;
    private MutableLiveData<MyUser> currentUser;
    private LiveData<Integer> followerNum;
    private LiveData<Boolean> passwordUpdated;
    private LiveData<MyUser> userUpdated;

    //Repository
    private AuthRepository authRepo;

    //Selected user
    private LiveData<MyUser> requestedUser;

    //Constructor
    public void AuthViewModel() {
        authRepo = AuthRepository.getInstance();
    }

    //Login with email and password
    public LiveData<FirebaseUser> authenticate(String emailInput, String passwordInput) {

        firebaseLoginUser = new MutableLiveData<>();

        login(emailInput, passwordInput);
        return firebaseLoginUser;
    }

    //Login
    private void login(String email, String password) {
        firebaseLoginUser = authRepo.loginWithEmailAndPassword(email, password);
    }

    //Custom sign up
    public LiveData<FirebaseUser> signUpWithInfo(EditText email, EditText password, EditText company, EditText userName, EditText phoneNum, EditText titel) {

        firebaseSignupUser = new MutableLiveData<>();

        firebaseSignupUser = authRepo.signUpWithInfo(email, password, company, userName, phoneNum, titel);
        return firebaseSignupUser;
    }

    //Get user id if user is already logged in according to the DB
    public String isLoggedIn() {
        return authRepo.isLoggedIn();
    }

    //Get profile info of current user
    public LiveData<MyUser> getCurrentUserProfile() {
        if (currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        getCurrentUser();
        return currentUser;
    }

    //Update user
    public LiveData<MyUser> updateUser(MyUser user) {
        if (userUpdated == null) {
            userUpdated = new MutableLiveData<>();
        }
        updateUserOnFirebase(user);
        return userUpdated;
    }

    //Update user
    private void updateUserOnFirebase(MyUser user) {
        userUpdated = authRepo.updateUser(user);
    }

    //Update current user info
    public void updateCurrentUserInfo(MyUser user) {
        currentUser.postValue(user);
    }

    //Logout
    public void logout() {
        authRepo.logout();
    }

    //Update password
    public LiveData<Boolean> updatePassword(String password) {
        if (passwordUpdated == null) {
            passwordUpdated = new MutableLiveData<>();
        }
        updatePasswordOnFirebase(password);
        return passwordUpdated;
    }

    //Update password
    private void updatePasswordOnFirebase(String password) {
        passwordUpdated = authRepo.updatePassword(password);
    }

    //Observe current user without getting profile again
    public LiveData<MyUser> observeCurrentUser() {
        if (currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        return currentUser;
    }

    //Upload profile image
    public void uploadProfileImage(Uri imageUri, final MyUser currentUser, String fileExtention) {
        authRepo.uploadProfileImg(imageUri, currentUser, fileExtention);
    }


    //Retrieve profile info of a selected user
    public LiveData<MyUser> getUserProfileData(String userID) {
        if (requestedUser == null) {
            requestedUser = new MutableLiveData<>();

        }
        getUserProfileInfo(userID);
        return requestedUser;
    }

    //Retrieve profile info of a selected user from repository
    private void getUserProfileInfo(String userID) {
        requestedUser = authRepo.getUserProfileInfo(userID);
    }

    //Retrieve user profile info
    private void getCurrentUser() {
        currentUser = authRepo.getCurrentUserProfile();
    }

    //Update current user
    public void updateCurrentUser(MyUser user) {
        currentUser.setValue(user);
    }

    //Follow user
    public void followUserWithID(String friendID) {
        authRepo.followFriend(friendID, currentUser.getValue().getId());
    }

    //Unfollow user
    public void unfollowUserWithID(String friendID) {
        authRepo.unfollowFriend(friendID, currentUser.getValue().getId());
    }

    //Get follower number
    public LiveData<Integer> getFollowerNum(String userID) {
        if (followerNum == null) {
            followerNum = new MutableLiveData<>();
        }
        getFollowerNumber(userID);
        return followerNum;
    }

    //Get followers
    private void getFollowerNumber(String userID) {
        followerNum = authRepo.getFollowerNum(userID);
    }

    //Destroy all observables and repository
    public void destroyAll() {
        authRepo.destroyAll();
        firebaseLoginUser = null;
        firebaseSignupUser = null;
        currentUser = null;
        authRepo = null;
        followerNum = null;
        passwordUpdated = null;
        userUpdated = null;
        requestedUser = null;
    }
}
