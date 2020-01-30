package com.notify.myapplication.Models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class MyUser implements Parcelable {

    //My custom parcelable object that defines a user

    String id,name, email, phoneNumber, titel, company, profileImage;

    ArrayList<String> friendsIDs;

    Bitmap userImage;

    public MyUser() {
    }

    //Constructor for one user
    public MyUser(String id,String profileImageUri, String name, String email, String phoneNumber, String titel, String company) {
        this.id = id;
        this.profileImage = profileImageUri;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.titel = titel;
        this.company = company;
    }

    protected MyUser(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        titel = in.readString();
        company = in.readString();
        profileImage = in.readString();
        friendsIDs = in.createStringArrayList();
        userImage = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<MyUser> CREATOR = new Creator<MyUser>() {
        @Override
        public MyUser createFromParcel(Parcel in) {
            return new MyUser(in);
        }

        @Override
        public MyUser[] newArray(int size) {
            return new MyUser[size];
        }
    };

    //Getter and setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getFriendsIDs() {
        return friendsIDs;
    }

    public void setFriendsIDs(ArrayList<String> friendsIDs) {
        this.friendsIDs = friendsIDs;
    }

    @Exclude
    public Bitmap getUserImage() {
        return userImage;
    }

    @Exclude
    public void setUserImage(Bitmap userImage) {
        this.userImage = userImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(titel);
        dest.writeString(company);
        dest.writeString(profileImage);
        dest.writeStringList(friendsIDs);
        dest.writeParcelable(userImage,flags);
    }
}
