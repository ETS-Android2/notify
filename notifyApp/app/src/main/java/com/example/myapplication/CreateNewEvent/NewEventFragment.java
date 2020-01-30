package com.example.myapplication.CreateNewEvent;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.myapplication.myapplication.notify_beta.R;
import com.example.myapplication.ImageOperations.PicassoCircleTransformation;
import com.example.myapplication.Models.MyEvent;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.example.myapplication.ViewModels.EventViewModel;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static com.example.myapplication.Constants.PICK_IMAGE_REQUEST;

public class NewEventFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private static final String TAG = "NewEventFragment: ";

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

    //Form validator
    private boolean isNameFilled = false;
    private boolean isDescFilled = false;
    private boolean isLocSelected = false;
    private boolean isDateSelected = false;
    private boolean isTimeSelected = false;
    private boolean isImgSelected = false;

    //ViewModels for data binding
    private AuthViewModel authViewModel;
    private EventViewModel eventViewModel;

    private NavController navController;

    public NewEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_event, container, false);

        //Init viewModels
        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        //Init empty new event
        newEvent = new MyEvent();

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
                    isNameFilled = false;
                } else {
                    event_name.setError(null);
                    isNameFilled = true;
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
                    isDescFilled = false;
                } else {
                    event_desc.setError(null);
                    isDescFilled = true;
                }
            }
        });

        //Init buttons
        nextEventCreateButton = view.findViewById(R.id.submit_event_btn);
        nextEventCreateButton.setOnClickListener(this);

        event_img = view.findViewById(R.id.new_event_img);
        event_img.setOnClickListener(this);

        event_date = view.findViewById(R.id.new_event_date_picker);
        event_date.setOnClickListener(this);

        event_time = view.findViewById(R.id.new_event_time_picker);
        event_time.setOnClickListener(this);

        event_loc = view.findViewById(R.id.new_event_loc_picker);
        event_loc.setOnClickListener(this);

        event_hide = view.findViewById(R.id.new_event_is_hidden);

        //If date is not selected
        if (event_date.getText().toString().equals("DD-MM-YYYY")) {
            isDateSelected = false;
        }
        //If date is not selected
        if (!event_time.getText().toString().equals("HH:MM")) {
            isTimeSelected = false;
        }

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        //Share an event to update it from mapFragment
        eventViewModel.getSharedInstance().observe(getViewLifecycleOwner(), new Observer<MyEvent>() {
            @Override
            public void onChanged(MyEvent myEvent) {
                //Get event on update
                newEvent = myEvent;
                setEventDataToUI(myEvent);
            }
        });
    }

    //Get selected date
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month++;
        String tempMonth;
        String tempDay;
        if (month < 10) {
            tempMonth = "0" + month;
        } else {
            tempMonth = String.valueOf(month);
        }
        if (dayOfMonth < 10) {
            tempDay = "0" + dayOfMonth;
        } else {
            tempDay = String.valueOf(dayOfMonth);
        }
        dateText = tempDay + "-" + tempMonth + "-" + year;
        Date today = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date selectedDate = sdf.parse(dateText);
            //Check if selected date is earlier than today
            if (selectedDate.before(today)) {
                Toast.makeText(getActivity(), "Event date can't be in the past", Toast.LENGTH_SHORT).show();
                isDateSelected = false;
            } else {
                event_date.setText(dateText);
                isDateSelected = true;
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //Get selected time
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
        isTimeSelected = true;
    }


    //Show time picker
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

    //Show date picker
    private void showDatePickerDialog() {
        DatePickerDialog datePicker = new DatePickerDialog(
                getActivity(),
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    //Choose image from gallery
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override   //Get gallery result (Only one image)
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Image size is not controlled!
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            //TODO controlImgSize();
            Picasso.get().load(imageUri.toString()).transform(new PicassoCircleTransformation()).into(event_img);
            isImgSelected = true;
            Log.w(TAG, "Event main image is selected");
        } else {
            Log.w(TAG, "Event main image is not selected");
            isImgSelected = false;
        }
    }

    //Get file extention (expected jpg)
    private String getFileExtention(Uri uri) {
        if (uri != null) {
            ContentResolver cR = getContext().getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cR.getType(uri));
        } else {
            return null;
        }
    }

    private MyEvent createEventInstance() {
        //Fill empty event and create an instance
        MyEvent tempEvent = new MyEvent();
        if (event_name.getText() != null || event_name.getText().length() != 0) {
            tempEvent.setEventName(event_name.getText().toString());
        }
        if (event_desc.getText() != null || event_desc.getText().length() != 0) {
            tempEvent.setEventDescription(event_desc.getText().toString());
        }
        tempEvent.setHidden(event_hide.isSelected());
        if (isDateSelected || isTimeSelected) {
            if (isDateSelected) {
                dateText = event_date.getText().toString();
            }
            if (isTimeSelected) {
                timeText = event_time.getText().toString();
            }
        }
        Timestamp timestamp = createTimestamp(dateText, timeText);
        if (timestamp != null) {
            tempEvent.setEventDateAndTime(timestamp);
        }
        if (isImgSelected) {
            BitmapDrawable drawable = (BitmapDrawable) event_img.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            tempEvent.setEventImg(bitmap);
        }
        if (place_id != null) {
            tempEvent.setEventPlaceID(place_id);
            tempEvent.setEventPlaceName(event_loc.getText().toString());
        }
        return tempEvent;
    }

    //Create timestamp
    private Timestamp createTimestamp(String date, String time) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        if (isDateSelected || isTimeSelected) {
            try {
                Date selectedDate = isoFormat.parse(date + " " + time);
                Log.i(TAG, "Parsed event date: " + selectedDate.toString());
                Timestamp timestamp = new Timestamp(selectedDate);
                Log.i(TAG, "Created Timestamp for event: " + timestamp.toString());
                newEvent.setEventDateAndTime(timestamp);
                return timestamp;
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    //Set from mapFragment updated event to ui
    private void setEventDataToUI(MyEvent myEvent) {
        if (myEvent.getEventName() != null) {
            event_name.setText(myEvent.getEventName());
        }
        if (myEvent.getEventDescription() != null) {
            event_desc.setText(myEvent.getEventDescription());
        }
        if (myEvent.getEventImg() != null) {
            event_img.setImageBitmap(myEvent.getEventImg());
            isImgSelected = true;
        } else {
            isImgSelected = false;
        }
        if (myEvent.getEventPlaceName() != null) {
            event_loc.setText(myEvent.getEventPlaceName());
            place_id = myEvent.getEventPlaceID();
            isLocSelected = true;
        } else {
            isLocSelected = false;
        }
        //Parse timestamp to date and then to text(String)
        if (myEvent.getEventDateAndTime() != null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
            String dateString = dateFormatter.format(myEvent.getEventDateAndTime().toDate());
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
            String timeString = timeFormatter.format(myEvent.getEventDateAndTime().toDate());
            event_date.setText(dateString);
            event_time.setText(timeString);
            isTimeSelected = true;
            isDateSelected = true;
        } else {
            isTimeSelected = false;
            isDateSelected = false;
        }
        event_hide.setSelected(myEvent.isHidden());
    }

    private boolean isFormFilled() {
        //Check if neccessary fields are filled
        if (isNameFilled && isDescFilled && isDateSelected && isTimeSelected && isLocSelected) {
            return true;
        } else return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == nextEventCreateButton.getId()) {
            //Form check
            if (isFormFilled()) {
                //Create instance from form
                newEvent = createEventInstance();
                newEvent.setOrganisatorID(authViewModel.isLoggedIn());
                newEvent.setOrganisator(authViewModel.observeCurrentUser().getValue());
                //Create event
                eventViewModel.createEvent(newEvent, imageUri, getFileExtention(imageUri)).observe(getViewLifecycleOwner(), new Observer<MyEvent>() {
                    @Override
                    public void onChanged(MyEvent myEvent) {
                        if (myEvent != null) {
                            //Notify user on creation result
                            Toast.makeText(getActivity(), "Event created", Toast.LENGTH_SHORT).show();
                            ArrayList<String> partList = new ArrayList<>();
                            newEvent.setParticipantsID(partList);
                            newEvent.getParticipantsID().add(authViewModel.isLoggedIn());
                            NewEventFragmentDirections.ActionNewEventFragmentToEventProfileFragment action = NewEventFragmentDirections.actionNewEventFragmentToEventProfileFragment(myEvent);
                            eventViewModel.cleanShareInstance();
                            navController.navigate(action);
                        } else {
                            Toast.makeText(getActivity(), "Failed to create event", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        if (v.getId() == event_img.getId()) {
            //Choose image
            chooseImage();
        }
        if (v.getId() == event_date.getId()) {
            //Choose date
            showDatePickerDialog();
        }
        if (v.getId() == event_time.getId()) {
            //Choose time
            showTimePickerDialog();
        }
        if (v.getId() == event_loc.getId()) {
            //Before navigating to mapFragment an event instance has to be created
            //To generate a timestamp, both date and time must be selected
            //Check if they both are selected or both are not selected
            if ((isDateSelected && isTimeSelected) || (!isDateSelected && !isTimeSelected)) {
                //If both are/are not selected navigate to mapFragment
                eventViewModel.updateSharedInstance(createEventInstance()).observe(getViewLifecycleOwner(), new Observer<MyEvent>() {
                    @Override
                    public void onChanged(MyEvent myEvent) {
                        navController.navigate(NewEventFragmentDirections.toNewEventMapFragment());
                    }
                });
            } else {
                //If only one of them is selected ask user to select the other one
                if (!isTimeSelected) {
                    event_time.setError("Please select time");
                }
                if (!isDateSelected) {
                    event_date.setError("Please select date");
                }
            }
        }
    }
}
