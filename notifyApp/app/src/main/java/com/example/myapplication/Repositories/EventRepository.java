package com.example.myapplication.Repositories;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.Models.MyEvent;
import com.example.myapplication.Models.MyImage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.myapplication.Constants.UPLOAD_FAILED;
import static com.example.myapplication.Constants.UPLOAD_PAUSED;
import static com.example.myapplication.Constants.UPLOAD_SUCCESSFULL;

public class EventRepository extends Repository {

    private static final String TAG = "EventRepository: ";
    private static EventRepository instance;

    //Observable LiveDatas
    private MutableLiveData<MyEvent> newEvent;
    private MutableLiveData<MyEvent> singleEvent;
    private MutableLiveData<ArrayList<MyEvent>> eventsForFeed;
    private MutableLiveData<ArrayList<MyEvent>> myEvents;
    private MutableLiveData<ArrayList<MyEvent>> createdEventsOfUser;
    private MutableLiveData<ArrayList<MyEvent>> joinedByCurrent;
    private MutableLiveData<ArrayList<MyEvent>> joinedEvents;
    private MutableLiveData<ArrayList<MyImage>> galleryImgUriList;
    private MutableLiveData<ArrayList<StorageReference>> galleryStorageRefs;
    private MutableLiveData<Integer> uploadStatus;
    private MutableLiveData<Boolean> isDeleted;

    //Lists
    private ArrayList<MyImage> uriList;
    private ArrayList<MyEvent> eventsForFeedList;

    //Singleton Pattern
    public static EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    //Set event data to DB
    public LiveData<MyEvent> setEventToDB(final MyEvent myEvent, final Uri imageUri, final String fileExt) {
        if (newEvent == null) {
            newEvent = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        db.collection("events").add(myEvent).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if (imageUri != null && fileExt != null) {
                    //If event is successfully written to DB, upload event image;
                    uploadEventImg(imageUri, fileExt, documentReference);
                    Log.i(TAG, "Event is successfully written to DB with image");

                } else {
                    Log.i(TAG, "Event is successfully written to DB without image");
                }
                //Add organisator as a participant
                addParticipantToEvent(documentReference, myEvent.getOrganisatorID());
                Log.i(TAG, "EventID: " + documentReference.getId());
                myEvent.setEventID(documentReference.getId());
                //Return created event
                newEvent.postValue(myEvent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Failed to write event to the DB");
                newEvent.postValue(null);
            }
        });
        return newEvent;
    }

    //Upload event image to DB
    private void uploadEventImg(Uri imageUri, String fileExtention, final DocumentReference eventRef) {
        final String newEventID = eventRef.getId();
        storageReference = FirebaseStorage.getInstance().getReference("EventMainImages");
        final String fbStoragePath = newEventID + "." + fileExtention;
        final StorageReference fileReference = storageReference.child(fbStoragePath);
        Log.i(TAG, "FileRef: " + fileReference.toString());
        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.w(TAG, "Upload successful");
                //If upload is successfull, add image url to event
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri imgDownloadUri = task.getResult();
                        db.collection("events").document(newEventID).update("eventImageUri", imgDownloadUri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.w(TAG, "Upload saved");
                                        //Upload saved
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Upload lost, DB leaking data
                                Log.w(TAG, "Failure on upload", e);
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }

    //Add participants to event
    private void addParticipantToEvent(final DocumentReference eventRef, final String participantID) {
        eventRef.update("participantsID", FieldValue.arrayUnion(participantID)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Participant with id " + participantID + " is successfully added to participants list");
                } else {
                    Log.e(TAG, "Error on participants list update: " + task.getException().getMessage());
                }
            }
        });
    }

    //Get event data with eventID
    public LiveData<MyEvent> retrieveSingleEvent(String eventID) {
        if (singleEvent == null) {
            singleEvent = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Error onEvent: " + e.getLocalizedMessage());
                    return;
                }
                DocumentSnapshot event = documentSnapshot;
                MyEvent myEvent = event.toObject(MyEvent.class);
                myEvent.setEventID(event.getId());
                singleEvent.postValue(myEvent);
            }
        });
        return singleEvent;
    }

    //Get events for feed
    public LiveData<ArrayList<MyEvent>> retrieveEventsForFeed(ArrayList<String> friendsIDs) {
        if (eventsForFeed == null) {
            eventsForFeed = new MutableLiveData<>();
            eventsForFeedList = new ArrayList<>();
        }
        //Get all events, that contain at least one of user's friends(user's friends contains also user)
        db = FirebaseFirestore.getInstance();
        db.collection("events").whereArrayContainsAny("participantsID", friendsIDs).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Error onEvent: " + e.getLocalizedMessage());
                    return;
                }
                for (QueryDocumentSnapshot event : queryDocumentSnapshots) {
                    final MyEvent tempEvent = event.toObject(MyEvent.class);
                    tempEvent.setEventID(event.getId());
                    eventsForFeedList.add(tempEvent);
                }
                eventsForFeed.postValue(eventsForFeedList);
            }
        });
        return eventsForFeed;
    }

    //Get all events created by currentUser
    public LiveData<ArrayList<MyEvent>> retrieveCreatedEvents(String userID) {
        if (myEvents == null) {
            myEvents = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        db.collection("events").whereEqualTo("organisatorID", userID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.e(TAG,e.getLocalizedMessage());
                }else{ArrayList<MyEvent> eventList = new ArrayList<>();
                    for (QueryDocumentSnapshot event : queryDocumentSnapshots) {
                        MyEvent tempEvent = event.toObject(MyEvent.class);
                        tempEvent.setEventID(event.getId());
                        eventList.add(tempEvent);
                    }
                    myEvents.postValue(eventList);}
            }
        });
        return myEvents;
    }

    //Get all events created by selected user
    public LiveData<ArrayList<MyEvent>> retrieveCreatedEventsOfUser(String userID) {
        if (createdEventsOfUser == null) {
            createdEventsOfUser = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        db.collection("events").whereEqualTo("organisatorID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<MyEvent> eventList = new ArrayList<>();
                    for (QueryDocumentSnapshot event : task.getResult()) {
                        MyEvent tempEvent = event.toObject(MyEvent.class);
                        tempEvent.setEventID(event.getId());
                        eventList.add(tempEvent);
                    }
                    createdEventsOfUser.postValue(eventList);
                } else {
                    Log.e(TAG, "Error: " + task.getException().getMessage());
                }
            }
        });
        return createdEventsOfUser;
    }

    //Join event, add current user to event's participants list
    public void joinEvent(String eventID, String userID) {
        db = FirebaseFirestore.getInstance();
        DocumentReference eventRef = db.collection("events").document(eventID);
        addParticipantToEvent(eventRef, userID);
    }

    //Disjoin event, remove current user from event's participants list
    public void disjoinEvent(String eventID, String userID) {
        db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventID).update("participantsID", FieldValue.arrayRemove(userID));
    }

    //Get events, participated by current
    public LiveData<ArrayList<MyEvent>> retrieveJoinedByCurrent(String userID) {
        if (joinedByCurrent == null) {
            joinedByCurrent = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        db.collection("events").whereArrayContains("participantsID", userID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.e(TAG,e.getLocalizedMessage());
                }else{
                    ArrayList<MyEvent> eventList = new ArrayList<>();
                    for (QueryDocumentSnapshot event : queryDocumentSnapshots) {
                        MyEvent tempEvent = event.toObject(MyEvent.class);
                        tempEvent.setEventID(event.getId());
                        eventList.add(tempEvent);
                    }
                    joinedByCurrent.postValue(eventList);
                }
            }
        });
        return joinedByCurrent;
    }

    //Get events, participated by a user with ID = userID
    public LiveData<ArrayList<MyEvent>> retrieveJoinedEvents(String userID) {
        if (joinedEvents == null) {
            joinedEvents = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        db.collection("events").whereArrayContains("participantsID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<MyEvent> eventList = new ArrayList<>();
                    for (QueryDocumentSnapshot event : task.getResult()) {
                        MyEvent tempEvent = event.toObject(MyEvent.class);
                        tempEvent.setEventID(event.getId());
                        eventList.add(tempEvent);
                    }
                    joinedEvents.postValue(eventList);
                } else {
                    Log.e(TAG, "Error: " + task.getException().getMessage());
                }
            }
        });
        return joinedEvents;
    }

    //Get gallery images with storage references
    public LiveData<ArrayList<MyImage>> getGalleryImages(ArrayList<StorageReference> storageRefs) {
        if (galleryImgUriList == null) {
            galleryImgUriList = new MutableLiveData<>();
        } else {
            if (galleryImgUriList.getValue() != null) {
                galleryImgUriList.getValue().clear();
            }
        }
        uriList = new ArrayList<>();
        for (final StorageReference ref : storageRefs) {
            ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        MyImage myImage = new MyImage(task.getResult());
                        myImage.setName(ref.getName());
                        uriList.add(myImage);
                        galleryImgUriList.postValue(uriList);
                    }
                }
            });
        }
        return galleryImgUriList;
    }

    //Get storage references for gallery images
    public LiveData<ArrayList<StorageReference>> getGalleryStorageRefs(String eventID) {
        if (galleryStorageRefs == null) {
            galleryStorageRefs = new MutableLiveData<>();
        } else {
            if (galleryStorageRefs.getValue() != null) {
                galleryStorageRefs.getValue().clear();
            }
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("EventGalleries/");
        storageRef.child(eventID).listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if (task.isSuccessful() || task.getResult() != null) {
                    List<StorageReference> refList = task.getResult().getItems();
                    ArrayList<StorageReference> storageRefList = new ArrayList<>();
                    for (StorageReference ref : refList) {
                        storageRefList.add(ref);
                    }
                    galleryStorageRefs.postValue(storageRefList);
                } else {
                    Log.e(TAG, "Error: " + task.getException().getMessage());
                }
            }
        });
        return galleryStorageRefs;
    }

    //Upload an image to storage
    public LiveData<Integer> uploadImgToCloud(String eventID, MyImage img, String fileExtension) {
        if (uploadStatus == null) {
            uploadStatus = new MutableLiveData<>();
        }
        uploadStatus.setValue(0);
        Bitmap bitmap = img.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("EventGalleries/").child(eventID);
        StorageReference fileRef = storageRef.child(img.getName());
        UploadTask imgUploadTask = fileRef.putBytes(data);
        imgUploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //Return image upload progress to show user
                Log.i(TAG, "UploadProgress: " + progress);
                uploadStatus.postValue((int) progress);
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                //Upload state = paused
                uploadStatus.postValue(UPLOAD_PAUSED);
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    //Upload successfull;
                    uploadStatus.postValue(UPLOAD_SUCCESSFULL);
                    if (task.getResult() != null) {
                        Log.i(TAG, task.getResult().getMetadata().getName());
                    }
                } else {
                    //Upload failed
                    uploadStatus.postValue(UPLOAD_FAILED);
                }
            }
        });
        return uploadStatus;
    }

    //Delete image from storage
    public LiveData<Boolean> deleteImage(String eventID, MyImage deletedImage) {
        if (isDeleted == null) {
            isDeleted = new MutableLiveData<>();
        }
        Log.w(TAG, "DELETING IMG WITH NAME: " + deletedImage.getName());
        String relativeImgThumbPath = deletedImage.getName();
        String relativeImgPath = relativeImgThumbPath.replace("thumb_", "");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("EventGalleries/").child(eventID);
        StorageReference fileRef = storageRef.child(relativeImgPath);
        //Delete original image from storage
        fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Notifying observers on delete");
                    isDeleted.postValue(true);
                } else {
                    Log.i(TAG, "Error: " + task.getException().getMessage());
                    isDeleted.postValue(false);
                }
            }
        });
        StorageReference thumbFileRef = storageRef.child(relativeImgThumbPath);
        //Delete thumbnail from storage
        thumbFileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Thumbnail is successfully removed");
                } else {
                    Log.e(TAG, "Error on deleting thumbnail: " + task.getException().getMessage());
                }
            }
        });
        return isDeleted;
    }

    //Destroy all observables
    public void destroyAll() {
        instance = null;
        newEvent = null;
        singleEvent = null;
        eventsForFeed = null;
        eventsForFeedList = null;
        myEvents = null;
        createdEventsOfUser = null;
        joinedByCurrent = null;
        joinedEvents = null;
        galleryImgUriList = null;
        galleryStorageRefs = null;
        uriList = null;
        uploadStatus = null;
        isDeleted = null;
    }
}
