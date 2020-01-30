package com.notify.notify_beta.Repositories;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

public abstract class Repository {

    private static final String TAG = "Repository: ";

    //Repository variables
    protected FirebaseFirestore db;
    protected StorageReference storageReference;


}
