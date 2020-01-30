package com.notify.myapplication.Repositories;

import android.net.Uri;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.notify.myapplication.Models.MyUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository extends Repository {

    private static final String TAG = "AuthRepository: ";
    private static AuthRepository instance;

    //FirebaseAuth
    private FirebaseAuth mAuth;

    //Observable LiveDatas
    private MutableLiveData<FirebaseUser> authenticatedUser;
    private MutableLiveData<FirebaseUser> newUser;
    private MutableLiveData<MyUser> currentUser;
    private MutableLiveData<MyUser> receivedUser;
    private MutableLiveData<Integer> followerNum;
    private MutableLiveData<Boolean> passwordUpdated;
    private MutableLiveData<MyUser> updatedUser;

    //Singleton Pattern
    public static AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }

    //Destroy all shared instances
    public void destroyAll() {
        instance = null;
        authenticatedUser = null;
        newUser = null;
        currentUser = null;
        receivedUser = null;
        followerNum = null;
        passwordUpdated = null;
        updatedUser = null;
    }

    //Login with email and password
    public LiveData<FirebaseUser> loginWithEmailAndPassword(String emailInput, String passwordInput) {
        authenticatedUser = new MutableLiveData<>();
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            authenticatedUser.postValue(mAuth.getCurrentUser());
                            Log.e(TAG, "Giriş başarılı");
                        } else {
                            //Sign in failed
                            authenticatedUser.postValue(null);
                            Log.e(TAG, "Giriş başarısız");
                            Log.e(TAG, task.getException().getMessage());
                        }
                    }
                });
        return authenticatedUser;
    }

    //Log out from firebase
    public void logout() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
    }

    public LiveData<Boolean> updatePassword(String password) {
        if (passwordUpdated == null) {
            passwordUpdated = new MutableLiveData<>();
        }
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser().updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //If password update successfull
                    passwordUpdated.postValue(Boolean.TRUE);
                } else {
                    //If password update failed
                    passwordUpdated.postValue(Boolean.FALSE);
                }
            }
        });
        return passwordUpdated;
    }

    //Update user info
    public LiveData<MyUser> updateUser(final MyUser user) {
        if (updatedUser == null) {
            updatedUser = new MutableLiveData<>();
        }
        mAuth = FirebaseAuth.getInstance();
        //Update email first,
        //Email is important for login
        mAuth.getCurrentUser().updateEmail(user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //When email update is successfull, update user info
                    db = FirebaseFirestore.getInstance();
                    db.collection("users").document(user.getId()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //On success return updated user
                                updatedUser.postValue(user);
                            } else {
                                //On fail, notify user by returning null
                                Log.e(TAG, "Error on user upload: " + task.getException().getMessage());
                                updatedUser.postValue(null);
                            }
                        }
                    });
                } else {
                    //On fail, notify user by returning null
                    Log.e(TAG, "Error on email upload: " + task.getException().getMessage());
                    updatedUser.postValue(null);
                }
            }
        });
        return updatedUser;
    }

    public String isLoggedIn() {
        //Return userID if user is already logged in
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getUid();
        } else {
            return null;
        }
    }


    //Get current user profile
    public MutableLiveData<MyUser> getCurrentUserProfile() {
        if (currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        String userUid = mAuth.getUid();

        //Get user info from db
        db.collection("users").document(userUid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Error onEvent: " + e.getLocalizedMessage());
                    return;
                }
                DocumentSnapshot user = documentSnapshot;
                MyUser tempUser = user.toObject(MyUser.class);
                tempUser.setId(user.getId());
                currentUser.postValue(tempUser);
            }
        });
        return currentUser;
    }

    //Custom signup
    public LiveData<FirebaseUser> signUpWithInfo(final EditText email,
                                                 final EditText password,
                                                 final EditText company,
                                                 final EditText userName,
                                                 final EditText phoneNum,
                                                 final EditText titel) {

        newUser = new MutableLiveData<>();
        //Signup with email and password
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            newUser.setValue(user);
                            Map<String, Object> userProfileData = new HashMap<>();
                            userProfileData.put("email", email.getText().toString());
                            userProfileData.put("company", company.getText().toString());
                            userProfileData.put("name", userName.getText().toString());
                            userProfileData.put("phoneNumber", phoneNum.getText().toString());
                            userProfileData.put("titel", titel.getText().toString());
                            db = FirebaseFirestore.getInstance();
                            // Sign in success, add new user to DB
                            db.collection("users").document(user.getUid()).set(userProfileData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                            //Follow self
                            followFriend(user.getUid(), user.getUid());
                        } else {

                            try {
                                throw task.getException();
                                //Invalid E-mail
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                email.setError("Geçerli bir mail adresi girin");
                                email.requestFocus();
                                //Email is already in use
                            } catch (FirebaseAuthUserCollisionException e) {
                                email.setError("Bu mail zaten kullanımda");
                                email.requestFocus();
                                //Other exceptions are handled
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
        return newUser;
    }

    //Get profile info for a selected user
    public LiveData<MyUser> getUserProfileInfo(String userID) {
        if (receivedUser == null) {
            receivedUser = new MutableLiveData<>();
        }
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Error onEvent: " + e.getLocalizedMessage());
                    return;
                }
                DocumentSnapshot user = documentSnapshot;
                MyUser tempUser = user.toObject(MyUser.class);
                tempUser.setId(user.getId());
                receivedUser.postValue(tempUser);

            }
        });
        return receivedUser;
    }

    //Follow a user, add user to friendsIDs list in DB
    public void followFriend(String friendID, String currentUserID) {
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUserID).update("friendsIDs", FieldValue.arrayUnion(friendID));
    }

    //Unfollow a user, remove user from friendsIDs list in DB
    public void unfollowFriend(String friendID, String currenUserID) {
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(currenUserID).update("friendsIDs", FieldValue.arrayRemove(friendID));
    }

    //Upload profile image
    public void uploadProfileImg(Uri imageUri, final MyUser currentUser, String fileExtention) {
        storageReference = FirebaseStorage.getInstance().getReference("UserProfileImages");
        final String fbStoragePath = currentUser.getId() + "." + fileExtention;
        final StorageReference fileReference = storageReference.child(fbStoragePath);
        fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "Upload successful");
                //If upload successfull, add profile image url to DB
                fileReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri imgDownloadUri = task.getResult();
                        db.collection("users").document(currentUser.getId()).update("profileImage", imgDownloadUri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i(TAG, "Upload saved");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failure on upload", e);
                            }
                        });
                        //After profile image url is added to DB, update current user's profile image info
                        currentUser.setProfileImage(imgDownloadUri.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //TODO showProgress;
                //Just like gallery bottomsheet list upload
            }
        });
    }

    //Get followers number
    public LiveData<Integer> getFollowerNum(String userID) {
        if (followerNum == null) {
            followerNum = new MutableLiveData<>();
        }
        //How many user has userID in their friendsIDs list
        db = FirebaseFirestore.getInstance();
        db.collection("users").whereArrayContains("friendsIDs", userID).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                int numFollowers = queryDocumentSnapshots.size();
                followerNum.setValue(numFollowers);
            }
        });
        return followerNum;
    }
}
