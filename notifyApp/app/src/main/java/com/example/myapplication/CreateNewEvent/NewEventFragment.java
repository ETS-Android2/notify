package com.example.myapplication.CreateNewEvent;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.myapplication.ImageOperations.PicassoCircleTransformation;
import com.example.myapplication.MyObjects.MyEvent;
import com.example.myapplication.R;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.example.myapplication.ViewModels.EventViewModel;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;

public class NewEventFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    //Constants
    private static final String TAG = "NewEventFragment: ";
    private static final int PICK_IMAGE_REQUEST = 1;


    //Views
    private Button nextEventCreateButton;
    private EditText event_name;
    private EditText event_desc;
    private ImageView event_img;
    private TextView event_date;
    private TextView event_time;
    private TextView event_loc;
    private Switch event_hide;

    //MyObject
    private MyEvent newEvent;
    private String dateText;
    private String timeText;
    private Uri imageUri;


    //Received info from our custom location picker
    private String place_id;
    private String receivedLocationName;

    //Form validator
    private int filledFieldCount;

    //ViewModels for data binding
    private AuthViewModel authViewModel;
    private EventViewModel eventViewModel;

    public NewEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event, container, false);

        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        newEvent = new MyEvent();

        filledFieldCount = 0;
        nextEventCreateButton = view.findViewById(R.id.submit_event_btn);
        nextEventCreateButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (isFieldsFilled()) {

                    newEvent.setEventName(event_name.getText().toString());
                    newEvent.setEventDescription(event_desc.getText().toString());
                    String authUid = authViewModel.getAuthUser().getValue().getUid();
                    SimpleDateFormat isoFormat = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm");
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date selectedDate = isoFormat.parse(dateText + "T" + timeText);
                        Log.i(TAG, "Parsed event date: " + selectedDate.toString());
                        Timestamp timestamp = new Timestamp(selectedDate);
                        Log.i(TAG, "Created Timestamp for event: " + timestamp.toString());
                        newEvent.setEventDateAndTime(timestamp);
                    } catch (ParseException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    newEvent.setEventPlaceID(place_id);
                    newEvent.setEventPlaceName(receivedLocationName);
                    newEvent.setOrganisatorRef(eventViewModel.getOrganisatorReference(authUid));
                    newEvent.setHidden(event_hide.isChecked());
                    MyEvent createdEvent = eventViewModel.createEvent(newEvent, imageUri, getFileExtention(imageUri)).getValue();
                    if (createdEvent != null) {
                        Toast.makeText(getActivity(), "Event created", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to create event", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        getActivity().setTitle("New event");

        //Init
        //Get changes on name field
        event_name = view.findViewById(R.id.new_event_name);
        event_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (event_name.getText().toString().length() <= 0) {
                    event_name.setError("Bu alan doldurulmalı");
                } else {
                    event_name.setError(null);
                    filledFieldCount++;
                }
            }
        });

        //Get changes on description field
        event_desc = view.findViewById(R.id.new_event_desc);
        event_desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (event_desc.getText().toString().length() <= 0) {
                    event_desc.setError("Bu alan doldurulmalı");
                } else {
                    event_desc.setError(null);
                    filledFieldCount++;
                }
            }
        });

        event_img = view.findViewById(R.id.new_event_img);
        event_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //Select date
        event_date = view.findViewById(R.id.new_event_date_picker);
        event_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        //Select time
        event_time = view.findViewById(R.id.new_event_time_picker);
        event_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        //Select location
        event_loc = view.findViewById(R.id.new_event_loc_picker);
        event_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(NewEventFragmentDirections.toNewEventMapFragment());
            }
        });

        //Is event hidden
        event_hide = view.findViewById(R.id.new_event_is_hidden);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Event place selected
        place_id = NewEventFragmentArgs.fromBundle(getArguments()).getPlaceId();
        receivedLocationName = NewEventFragmentArgs.fromBundle(getArguments()).getPlaceName();
        if (receivedLocationName != null) {
            event_loc.setText(receivedLocationName);
            filledFieldCount++;
        }
    }

    //Date selected
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dateText = dayOfMonth + "-" + (month + 1) + "-" + year;
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
        try {
            Date selectedDate = sdf.parse(dateText);
            //Check if selected date is earlier than today
            if (selectedDate.before(today)) {
                Toast.makeText(getActivity(), "Event date can't be in the past", Toast.LENGTH_SHORT).show();
            } else {
                event_date.setText(dateText);
                filledFieldCount++;
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //Time selected
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hour;
        if (hourOfDay < 10) {
            hour = "0" + hourOfDay;
        } else {
            hour = String.valueOf(hourOfDay);
        }
        timeText = hour + ":" + minute;
        event_time.setText(timeText);
        filledFieldCount++;
    }

    private boolean isFieldsFilled() {
        if (filledFieldCount >= 5) {
            return true;
        } else {
            return false;
        }
    }
    //TODO doTheWork();


    //Show time and date picker
    private void showTimePickerDialog() {
        TimePickerDialog timePicker = new TimePickerDialog(
                getActivity(),
                this,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                //check if date format is 24 hour
                android.text.format.DateFormat.is24HourFormat(getActivity()));
        timePicker.show();
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePicker = new DatePickerDialog(
                getActivity(),
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            //TODO controlImgSize();
            Picasso.get().load(imageUri.toString()).transform(new PicassoCircleTransformation()).into(event_img);
            Log.w(TAG, "Event main image is selected");
        } else {
            Log.w(TAG, "Event main image is not selected");
        }
    }


    private String getFileExtention(Uri uri) {
        if (uri != null) {
            ContentResolver cR = getContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cR.getType(uri));
        } else {
            return null;
        }
    }
}
