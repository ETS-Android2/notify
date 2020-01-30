package com.notify.notify_beta.Repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.notify.notify_beta.Models.MyUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UsersRepository extends Repository {

    private static final String TAG = "UsersRepository: ";
    private static UsersRepository instance;

    //Observable LiveDatas
    private MutableLiveData<ArrayList<MyUser>> receivedUsers = new MutableLiveData<>();
    private MutableLiveData<ArrayList<MyUser>> organisatorListForFeed;
    private MutableLiveData<MyUser> organisator;

    //Organisator
    private ArrayList<MyUser> organisatorHolder;

    //List
    private ArrayList<MyUser> userArrayList = new ArrayList<>();


    //Singleton Pattern
    public static UsersRepository getInstance() {
        if (instance == null) {
            instance = new UsersRepository();
        }
        return instance;
    }

    //Get organisatorList for feed events with their IDs
    public LiveData<ArrayList<MyUser>> getOrganisatorListForFeed(ArrayList<String> organisatorIDList) {
        if (organisatorListForFeed == null) {
            organisatorListForFeed = new MutableLiveData<>();
        }
        if(organisatorHolder == null){
            organisatorHolder = new ArrayList<>();
        }
        db = FirebaseFirestore.getInstance();
        for (String organisatorID : organisatorIDList) {
            db.collection("users").document(organisatorID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            DocumentSnapshot doc = task.getResult();
                            MyUser organisator = doc.toObject(MyUser.class);
                            organisator.setId(doc.getId());
                            organisatorHolder.add(organisator);
                            organisatorListForFeed.postValue(organisatorHolder);
                        }
                    } else {
                        Log.i(TAG, "Error: " + task.getException().getMessage());
                    }
                }
            });
        }
        return organisatorListForFeed;
    }

    //Search user in DB
    public LiveData<ArrayList<MyUser>> getSearchedUsers(String searchQuery) {
        db = FirebaseFirestore.getInstance();
        receivedUsers.setValue(new ArrayList<MyUser>());
//        The character \uf8ff used in the query is a very high code point in the Unicode range
//        (it is a Private Usage Area [PUA] code).
//        Because it is after most regular characters in Unicode,
//        the query matches all values that start with queryText.**
//        **From stackOverFlow
//        Query returns 12 users matching with searched string -- starting with input text, like autocomplete
        db.collection("users").orderBy("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff").limit(12).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Error onEvent: " + e.getLocalizedMessage());
                }
                userArrayList.clear();
                for (QueryDocumentSnapshot user : queryDocumentSnapshots) {
                    userArrayList.add(setUserInfo(user));
                }
                receivedUsers.setValue(userArrayList);
            }
        });
        return receivedUsers;
    }

    //Create user instance from DB
    private MyUser setUserInfo(QueryDocumentSnapshot user) {
        MyUser tempUser = user.toObject(MyUser.class);
        tempUser.setId(user.getId());
        return tempUser;
    }

    //Get participans of an event via participantsList(containing their IDs)
    public LiveData<ArrayList<MyUser>> getParticipants(ArrayList<String> partList) {
        if (receivedUsers == null) {
            receivedUsers = new MutableLiveData<>();
        }
        receivedUsers.setValue(new ArrayList<MyUser>());
        db = FirebaseFirestore.getInstance();
        db.collection("users").whereIn(FieldPath.documentId(), partList).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Error onEvent: " + e.getLocalizedMessage());
                    return;
                }
                ArrayList<MyUser> partList = new ArrayList<>();
                for (QueryDocumentSnapshot user : queryDocumentSnapshots) {
                    MyUser tempPart = user.toObject(MyUser.class);
                    tempPart.setId(user.getId());
                    partList.add(tempPart);
                }
                receivedUsers.postValue(partList);
            }
        });
        return receivedUsers;
    }

    public LiveData<MyUser> getOrganisator(String userID){
        if(organisator == null){
            organisator = new MutableLiveData<>();
        }
        db.collection("users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    MyUser tempUser = task.getResult().toObject(MyUser.class);
                    tempUser.setId(task.getResult().getId());
                    organisator.postValue(tempUser);
                }else{
                    Log.e(TAG,"Error: " + task.getException().getMessage());
                }
            }
        });
        return organisator;
    }
}
