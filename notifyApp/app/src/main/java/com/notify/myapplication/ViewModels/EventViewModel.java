package com.notify.myapplication.ViewModels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.notify.myapplication.Models.MyEvent;
import com.notify.myapplication.Models.MyImage;
import com.notify.myapplication.Repositories.EventRepository;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class EventViewModel extends ViewModel {

    private static final String TAG = "EventViewModel: ";

    //Repository
    private EventRepository eventRepo;

    //Obervable LiveDatas
    private LiveData<MyEvent> newEvent;
    private MutableLiveData<MyEvent> sharedEventInstance;
    private LiveData<MyEvent> singleEvent;
    private LiveData<ArrayList<MyEvent>> eventsForFeed;
    private LiveData<ArrayList<MyEvent>> myEvents;
    private LiveData<ArrayList<MyEvent>> createdEventsOfUser;
    private LiveData<ArrayList<MyEvent>> joinedByCurrentEvents;
    private LiveData<ArrayList<MyEvent>> joinedEvents;
    private LiveData<ArrayList<MyImage>> galleryImgUriList;
    private LiveData<ArrayList<StorageReference>> galleryStorageRefs;
    private LiveData<Integer> uploadStatus;
    private LiveData<Boolean> deletedReturn;

    //Constructor
    public void EventViewModel() {
        eventRepo = EventRepository.getInstance();
    }

    //Update shared instance from newEventFragments
    public LiveData<MyEvent> updateSharedInstance(MyEvent sharedEvent) {
        if (sharedEventInstance == null) {
            sharedEventInstance = new MutableLiveData<>();
        }
        sharedEventInstance.setValue(sharedEvent);
        return sharedEventInstance;
    }

    //Get shared instance
    public LiveData<MyEvent> getSharedInstance() {
        if (sharedEventInstance == null) {
            sharedEventInstance = new MutableLiveData<>();
        }
        return sharedEventInstance;
    }

    //Clean instace by setting value null
    public void cleanShareInstance() {
        sharedEventInstance = null;
    }

    //Create new event
    public LiveData<MyEvent> createEvent(MyEvent newEvent, Uri imageUri, String fileExtention) {
        if (this.newEvent == null) {
            this.newEvent = new MutableLiveData<>();
        }
        addEventToDB(newEvent, imageUri, fileExtention);
        return this.newEvent;
    }

    //Add event to DB
    private void addEventToDB(MyEvent newEvent, Uri imageUri, String fileExtention) {
        this.newEvent = eventRepo.setEventToDB(newEvent, imageUri, fileExtention);
    }

    //Get single event info to set selected event profile
    public LiveData<MyEvent> fetchSingleEvent(String eventID) {
        if (singleEvent == null) {
            singleEvent = new MutableLiveData<>();
        }
        getSingleEvent(eventID);
        return singleEvent;
    }

    //Get events for listing on feed page
    public LiveData<ArrayList<MyEvent>> fetchEventsForFeed(ArrayList<String> friendsIDs) {
        if (eventsForFeed == null) {
            eventsForFeed = new MutableLiveData<>();
        }
        getEventsForFeed(friendsIDs);
        return eventsForFeed;
    }

    //Retrieve events for feed page from repository
    private void getEventsForFeed(ArrayList<String> friendsIDs) {
        eventsForFeed = eventRepo.retrieveEventsForFeed(friendsIDs);
    }

    //Get observable feedList
    public LiveData<ArrayList<MyEvent>> observeEventsForFeed() {
        if (eventsForFeed == null) {
            eventsForFeed = new MutableLiveData<>();
        }
        return eventsForFeed;
    }


    //Retrieve single event from repository for event profile
    private void getSingleEvent(String eventID) {
        singleEvent = eventRepo.retrieveSingleEvent(eventID);
    }

    //Get created events
    public LiveData<ArrayList<MyEvent>> getEventsCreated(String userID, boolean forCurrentUser) {
        if (forCurrentUser) {
            if (myEvents == null) {
                myEvents = new MutableLiveData<>();
            }
            getCreatedEvents(userID);
            return myEvents;
        } else {
            if (createdEventsOfUser == null) {
                createdEventsOfUser = new MutableLiveData<>();
            }
            getCreatedEventsOfUser(userID);
            return createdEventsOfUser;
        }
    }

    //Get observable events created by current user
    public LiveData<ArrayList<MyEvent>> observeMyEvents() {
        if (myEvents == null) {
            myEvents = new MutableLiveData<>();
        }
        return myEvents;
    }

    //Get participated events
    public LiveData<ArrayList<MyEvent>> getJoinedEvents(String userID, boolean forCurrentUser) {
        if (forCurrentUser) {
            if (joinedByCurrentEvents == null) {
                joinedByCurrentEvents = new MutableLiveData<>();
            }
            getJoinedByCurrentEventList(userID);
            return joinedByCurrentEvents;
        } else {
            if (joinedEvents == null) {
                joinedEvents = new MutableLiveData<>();
            }
            getJoinedEventList(userID);
            return joinedEvents;
        }
    }

    //Get observable events list participated by current user
    public LiveData<ArrayList<MyEvent>> observeJoinedByCurrent() {
        if (joinedByCurrentEvents == null) {
            joinedByCurrentEvents = new MutableLiveData<>();
        }
        return joinedByCurrentEvents;
    }

    //Get participated events for current user
    private void getJoinedByCurrentEventList(String userID) {
        joinedByCurrentEvents = eventRepo.retrieveJoinedByCurrent(userID);
    }

    //Get participated events
    private void getJoinedEventList(String userID) {
        joinedEvents = eventRepo.retrieveJoinedEvents(userID);
    }

    //Get created events
    private void getCreatedEvents(String userID) {
        myEvents = eventRepo.retrieveCreatedEvents(userID);
    }

    //Get created events for a selected user
    private void getCreatedEventsOfUser(String userID) {
        createdEventsOfUser = eventRepo.retrieveCreatedEventsOfUser(userID);
    }

    //Join event
    public void joinEvent(String eventID, String userID) {
        eventRepo.joinEvent(eventID, userID);
    }

    //Disjoin event
    public void disjoinEvent(String eventID, String userID) {
        eventRepo.disjoinEvent(eventID, userID);
    }

    //Get image uri list for gallery
    public LiveData<ArrayList<MyImage>> getGalleryImgUriList(ArrayList<StorageReference> refList) {
        if (galleryImgUriList == null) {
            galleryImgUriList = new MutableLiveData<>();
        } else {
            if (galleryImgUriList.getValue() != null) {
                galleryImgUriList.getValue().clear();
            }
        }
        getEventImageUris(refList);
        return galleryImgUriList;
    }

    private void getEventImageUris(ArrayList<StorageReference> refList) {
        galleryImgUriList = eventRepo.getGalleryImages(refList);
    }

    //Get gallery image storageReferences
    public LiveData<ArrayList<StorageReference>> getGalleryStorageRefs(String eventID) {
        if (galleryStorageRefs == null) {
            galleryStorageRefs = new MutableLiveData<>();
        } else {
            if (galleryStorageRefs.getValue() != null) {
                galleryStorageRefs.getValue().clear();
            }
        }
        getStorageRefList(eventID);
        return galleryStorageRefs;
    }

    //Get observable storageReference List
    public LiveData<ArrayList<StorageReference>> observeRefList() {
        return galleryStorageRefs;
    }

    private void getStorageRefList(String eventID) {
        galleryStorageRefs = eventRepo.getGalleryStorageRefs(eventID);
    }

    //Upload image to gallery
    public LiveData<Integer> uploadImgToGallery(String eventID, MyImage img, String fileExtension) {
        if (uploadStatus == null) {
            uploadStatus = new MutableLiveData<>();
        }
        uploadImg(eventID, img, fileExtension);
        return uploadStatus;
    }

    private void uploadImg(String eventID, MyImage img, String fileExtension) {
        uploadStatus = eventRepo.uploadImgToCloud(eventID, img, fileExtension);
    }

    //Delete image from gallery
    public LiveData<Boolean> deleteImage(String eventID, MyImage deletedImage) {
        if (deletedReturn == null) {
            deletedReturn = new MutableLiveData<>();
        }
        deleteImageFromDB(eventID, deletedImage);
        return deletedReturn;
    }

    private void deleteImageFromDB(String eventID, MyImage deletedImage) {
        deletedReturn = eventRepo.deleteImage(eventID, deletedImage);
    }

    //Destroy all observables and repository
    public void destroyAll() {
        eventRepo.destroyAll();
        eventRepo = null;
        newEvent = null;
        sharedEventInstance = null;
        singleEvent = null;
        eventsForFeed = null;
        myEvents = null;
        createdEventsOfUser = null;
        joinedByCurrentEvents = null;
        joinedEvents = null;
        galleryImgUriList = null;
        galleryStorageRefs = null;
        uploadStatus = null;
        deletedReturn = null;
    }
}
