package com.example.myapplication;

import android.Manifest;

import java.util.Arrays;
import java.util.List;

public class Constants {
    //Constant values
    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int PICK_MULTIPLE_IMAGE_REQUEST = 2;
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final float DEFAULT_ZOOM = 18;
    public static final int PLACE_PICKER_REQUEST = 1;
    public static final List<String> PRICE_LEVEL_REPRESENTATIONS = Arrays.asList("Very cheap","Cheap","Expensive","Very expensive");
    public static final int FILTER_IN_LIST = 111;
    public static final int FILTER_IN_DB = 222;
    public static final int UPLOAD_PAUSED = 555;
    public static final int UPLOAD_CANCELED = 444;
    public static final int UPLOAD_SUCCESSFULL = 999;
    public static final int UPLOAD_FAILED = -1;
    public static final int UPLOADED = 100;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 4;
    public static final int PERMISSION_REQUEST_INTERNET = 5;
    public static final String ALERT_NOCONTITLE = "No connection";
    public static final String ALERT_NOCONMESSAGE = "No connection or connection with no internet\n" +
            "Please check your connection and try again!";
    public static final String ALERT_NOCONBTN = "Exit App";
}
