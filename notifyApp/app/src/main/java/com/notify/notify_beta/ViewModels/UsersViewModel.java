package com.notify.notify_beta.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.notify.notify_beta.Models.MyUser;
import com.notify.notify_beta.Repositories.UsersRepository;

import java.util.ArrayList;

public class UsersViewModel extends ViewModel {

    private static final String TAG = "UsersViewModel: ";

    //Repository
    private UsersRepository usersRepo;

    //Observable LiveDatas
    private LiveData<ArrayList<MyUser>> userList;
    private LiveData<ArrayList<MyUser>> participants;
    private LiveData<ArrayList<MyUser>> organisatorListForFeed;
    private LiveData<MyUser> organisator;

    //Constructor
    public void UsersViewModel() {
        usersRepo = UsersRepository.getInstance();
    }

    //Get user list and get all* with empty string *(all.size() =< limit)
    public LiveData<ArrayList<MyUser>> getUserList() {
        if (userList == null) {
            userList = new MutableLiveData<>();
        }
        getSearchedUsers("");
        return userList;
    }

    //Observe userlist
    public LiveData<ArrayList<MyUser>> observeUserList() {
        if (userList == null) {
            userList = new MutableLiveData<>();
        }
        return userList;
    }

    //Get organisators of events in feed
    public LiveData<ArrayList<MyUser>> getOrganisatorListForFeed(ArrayList<String> organisatorIDList) {
        if (organisatorListForFeed == null) {
            organisatorListForFeed = new MutableLiveData<>();
        }
        getOrganisatorListFromDB(organisatorIDList);
        return organisatorListForFeed;
    }

    private void getOrganisatorListFromDB(ArrayList<String> orgenisatorIDList) {
        organisatorListForFeed = usersRepo.getOrganisatorListForFeed(orgenisatorIDList);
    }

    //Get organisatorList observable
    public LiveData<ArrayList<MyUser>> observeOrganisatorListForFeed() {
        if (organisatorListForFeed == null) {
            organisatorListForFeed = new MutableLiveData<>();
        }
        return organisatorListForFeed;
    }

    //Search user with a query and get returned user list
    public void getSearchedUsers(String query) {
        userList = usersRepo.getSearchedUsers(query);
    }

    public LiveData<ArrayList<MyUser>> getParticipants(ArrayList<String> partList) {
        if (participants == null) {
            participants = new MutableLiveData<>();
        }
        getParticipantsForEvent(partList);
        return participants;
    }

    //Get participants of an event via their IDs
    private void getParticipantsForEvent(ArrayList<String> partList) {
        participants = usersRepo.getParticipants(partList);
    }

    //Get single user
    public LiveData<MyUser> getOrganisator(String userID){
        if(organisator == null){
            organisator = new MutableLiveData<>();
        }
        getSingleOrgansiator(userID);
        return organisator;
    }

    private void getSingleOrgansiator(String userID){
        organisator = usersRepo.getOrganisator(userID);
    }

}
