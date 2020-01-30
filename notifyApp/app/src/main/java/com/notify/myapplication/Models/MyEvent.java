package com.notify.myapplication.Models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class MyEvent implements Parcelable {

    //My custom parcelable object that defines an event

    private String organisatorID;

    private String eventID;
    private String eventName;
    private String eventDescription;

    private String eventPlaceName;
    private String eventPlaceID;
    private Timestamp eventDateAndTime;
    private int eventDurationMinute;
    private boolean isHidden;
    private String eventImageUri;
    private Bitmap eventImg;

    private float eventRating;

    private ArrayList<String> participantsID;
    private String shareRoomId;
    private String chatRoomId;

    @Exclude
    private MyUser organisator;

    private String Thumbnail;


    public MyEvent() {
    }

    public MyEvent(String organisatorID,
                   String eventName,
                   String eventDescription,
                   String eventPlaceName,
                   String eventPlaceID,
                   Timestamp eventDateAndTime) {
        this.organisatorID = organisatorID;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventPlaceName = eventPlaceName;
        this.eventPlaceID = eventPlaceID;
        this.eventDateAndTime = eventDateAndTime;
    }

    protected MyEvent(Parcel in) {
        organisatorID = in.readString();
        eventID = in.readString();
        eventName = in.readString();
        eventDescription = in.readString();
        eventPlaceName = in.readString();
        eventPlaceID = in.readString();
        eventDateAndTime = in.readParcelable(Timestamp.class.getClassLoader());
        eventDurationMinute = in.readInt();
        isHidden = in.readByte() != 0;
        eventImageUri = in.readString();
        eventRating = in.readFloat();
        participantsID = in.createStringArrayList();
        shareRoomId = in.readString();
        chatRoomId = in.readString();
        Thumbnail = in.readString();
        eventImg = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(organisatorID);
        dest.writeString(eventID);
        dest.writeString(eventName);
        dest.writeString(eventDescription);
        dest.writeString(eventPlaceName);
        dest.writeString(eventPlaceID);
        dest.writeParcelable(eventDateAndTime, flags);
        dest.writeInt(eventDurationMinute);
        dest.writeByte((byte) (isHidden ? 1 : 0));
        dest.writeString(eventImageUri);
        dest.writeFloat(eventRating);
        dest.writeStringList(participantsID);
        dest.writeString(shareRoomId);
        dest.writeString(chatRoomId);
        dest.writeString(Thumbnail);
        dest.writeParcelable(eventImg,flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyEvent> CREATOR = new Creator<MyEvent>() {
        @Override
        public MyEvent createFromParcel(Parcel in) {
            return new MyEvent(in);
        }

        @Override
        public MyEvent[] newArray(int size) {
            return new MyEvent[size];
        }
    };

    @Exclude
    public MyUser getOrganisator() {
        return organisator;
    }
    @Exclude
    public void setOrganisator(MyUser organisator) {
        this.organisator = organisator;
    }

    @Exclude
    public Bitmap getEventImg() {
        return eventImg;
    }
    @Exclude
    public void setEventImg(Bitmap eventImg) {
        this.eventImg = eventImg;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String  getEventImageUri() {
        return eventImageUri;
    }

    public void setEventImageUri(String eventImageUri) {
        this.eventImageUri = eventImageUri;
    }

    public Timestamp getEventDateAndTime() {
        return eventDateAndTime;
    }

    public void setEventDateAndTime(Timestamp eventDateAndTime) {
        this.eventDateAndTime = eventDateAndTime;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public String getEventPlaceID() {
        return eventPlaceID;
    }

    public void setEventPlaceID(String eventPlaceID) {
        this.eventPlaceID = eventPlaceID;
    }

    public String getOrganisatorID() {
        return organisatorID;
    }

    public void setOrganisatorID(String organisatorID) {
        this.organisatorID = organisatorID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventPlaceName() {
        return eventPlaceName;
    }

    public void setEventPlaceName(String eventPlaceName) {
        this.eventPlaceName = eventPlaceName;
    }


    public int getEventDurationMinute() {
        return eventDurationMinute;
    }

    public void setEventDurationMinute(int eventDurationMinute) {
        this.eventDurationMinute = eventDurationMinute;
    }

    public float getEventRating() {
        return eventRating;
    }

    public void setEventRating(float eventRating) {
        this.eventRating = eventRating;
    }

    public String getShareRoomId() {
        return shareRoomId;
    }

    public void setShareRoomId(String shareRoomId) {
        this.shareRoomId = shareRoomId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getThumbnail() {
        return Thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        Thumbnail = thumbnail;
    }

    public ArrayList<String> getParticipantsID() {
        return participantsID;
    }

    public void setParticipantsID(ArrayList<String> participantList) {
        this.participantsID = participantList;
    }
    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof MyEvent){
            MyEvent toCompare = (MyEvent) obj;
            return this.eventID.equals(toCompare.eventID);
        }else{
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        String event = "Event: \nID: " + eventID +"\nName: " +eventName+"\nEventDateAndTime: " + eventDateAndTime.toDate().toString() + "\nEventPlace: " + eventPlaceName;
        return event;
    }
}
