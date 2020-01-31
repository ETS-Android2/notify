package com.notify.myapplication.CreateNewEvent;

import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.GeoPoint;
import com.notify.myapplication.ImageOperations.PicassoCircleTransformation;
import com.notify.myapplication.Models.MyEvent;
import com.notify.myapplication.R;
import com.notify.myapplication.ViewModels.EventViewModel;

import java.util.Arrays;
import java.util.List;

import static com.notify.myapplication.Constants.DEFAULT_ZOOM;
import static com.notify.myapplication.Constants.PRICE_LEVEL_REPRESENTATIONS;

public class NewEventMapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "NewEventMapFragment: ";

    private static View view;

    //Map & Places API related variables
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    //Location related variables
    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    //Layout variables
    private View searchBar;
    private FrameLayout searchCard;
    private AutocompleteSupportFragment autocompleteFragment;

    //Expandible bottom sheet variables
    private LinearLayout bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    //MapView Variables
    private View mapView;
    private Button confirmLocationButton;
    private SupportMapFragment mapFragment;
    private Toolbar toolbar;

    //BottomSheet components
    private ImageView selectedPlaceImg;
    private TextView selectedPlaceName;
    private TextView selectedPlacePriceLevel;
    private TextView selectedPlaceAddress;
    private TextView selectedPlaceRating;

    //Selected place
    private Place finalPlace;

    //Fragment variables
    private EventViewModel eventViewModel;
    private MyEvent newEvent;

    public NewEventMapFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_new_event_place, container, false);
        } catch (
                InflateException e) {
            /* map is already there, just return view as it is */
        }

        newEvent = new MyEvent();
        //Get view models
        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        //Get shared event, that is updated from newEventFragment earlier
        eventViewModel.getSharedInstance().observe(getViewLifecycleOwner(), new Observer<MyEvent>() {
            @Override
            public void onChanged(MyEvent myEvent) {
                newEvent = myEvent;
            }
        });

        //Init BottomSheet Layout
        bottomSheet = (LinearLayout) view.findViewById(R.id.selected_place_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(100);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        //Init BottomSheet Fields
        selectedPlaceImg = bottomSheet.findViewById(R.id.place_picker_img);
        selectedPlaceName = bottomSheet.findViewById(R.id.selected_place_name);
        selectedPlaceAddress = bottomSheet.findViewById(R.id.selected_place_address);
        selectedPlacePriceLevel = bottomSheet.findViewById(R.id.selected_price_level);
        selectedPlaceRating = bottomSheet.findViewById(R.id.selected_rating);
        view.findViewById(R.id.confirm_loc_btn).setOnClickListener(this);

        //Init childFragment(MapFragment)
        mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map_view);


        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Inflate SearchBar
        if (autocompleteFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            autocompleteFragment = AutocompleteSupportFragment.newInstance();
            ft.replace(R.id.autocomplete_fragment, autocompleteFragment).commit();
            Log.i(TAG, "SearchBar is open.");
        }

        setSearchBar();

        //Inflate Map
        if (mapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map_view, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        connectToAPI();


        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Handle back button pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(view).popBackStack();
                    }
                });
    }

    private void connectToAPI() {
        //API connection
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        Places.initialize(getActivity(), getString(R.string.google_api_key));
        placesClient = Places.createClient(getActivity());
    }

    private void setSearchBar() {

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PRICE_LEVEL,
                Place.Field.RATING,
                Place.Field.PHOTO_METADATAS,
                Place.Field.LAT_LNG));

        autocompleteFragment.setCountry("tr");
        autocompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                initBottomSheetFields(place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    private void initBottomSheetFields(Place place) {
        finalPlace = place;
        // Retrieve photo of the selected place.
        if (place.getPhotoMetadatas() != null) {
            fecthAndSetSelectedPlaceImage(place);
        }
        // Set selected place address.
        if (place.getAddress() != null) {
            selectedPlaceAddress.setText(place.getAddress());
        }
        // Set selected place name.
        if (place.getName() != null) {
            selectedPlaceName.setText(place.getName());
        }
        // Set price level representation of the selected place.
        if (place.getPriceLevel() != null) {
            String priceLevelField = "Fiyat seviyesi: " + PRICE_LEVEL_REPRESENTATIONS.get(place.getPriceLevel().intValue() - 1);
            selectedPlacePriceLevel.setText(priceLevelField);
        } else {
            selectedPlacePriceLevel.setText("");
        }
        // Set selected place rating
        if (place.getRating() != null) {
            selectedPlaceRating.setText(place.getRating().toString());
        } else {
            selectedPlaceRating.setText("");
        }
        //Expand bottomsheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


    private void fecthAndSetSelectedPlaceImage(Place place) {
        PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
        // Create a FetchPhotoRequest.
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(100) // Optional.
                .setMaxHeight(100) // Optional.
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
            @Override
            public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                //Make fecthed image rounded
                PicassoCircleTransformation transformation = new PicassoCircleTransformation();
                transformation.setBORDER_RADIUS(0);
                bitmap = transformation.transform(bitmap);
                //Set selected place image.
                selectedPlaceImg.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + e.getMessage());
                }
            }
        });
    }

    private void getLastKnownLocation() {
        //Get last location
        Log.d(TAG, "getLastKnownLocation: called.");
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mLastKnownLocation = task.getResult();
                    if (mLastKnownLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        getLocationRequest();
                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult == null) {
                                    return;
                                }
                                mLastKnownLocation = locationResult.getLastLocation();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                    GeoPoint geoPoint = new GeoPoint(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    Log.d(TAG, "onComplete: latitude" + mLastKnownLocation.getLatitude());
                    Log.d(TAG, "onComplete: longitute" + mLastKnownLocation.getLongitude());
                } else {
                    Toast.makeText(getActivity(), "Unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                Log.i(TAG, "Clicked: " +
                        poi.name + "\nPlace ID:" + poi.placeId +
                        "\nLatitude:" + poi.latLng.latitude +
                        " Longitude:" + poi.latLng.longitude);
                String place_id = poi.placeId;
                List<Place.Field> placeFields = Arrays.asList(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.ADDRESS,
                        Place.Field.PRICE_LEVEL,
                        Place.Field.RATING,
                        Place.Field.PHOTO_METADATAS);
                FetchPlaceRequest request = FetchPlaceRequest.newInstance(place_id, placeFields);
                placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place tempPlace = fetchPlaceResponse.getPlace();
                        Log.i(TAG, "Place found: " + tempPlace.getName());
                        initBottomSheetFields(tempPlace);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException exception = (ApiException) e;
                            int statusCode = exception.getStatusCode();
                            // Handle error with given status code.
                            Log.e(TAG, "Place not found: " + exception.getMessage());
                        }
                    }
                });

            }
        });

        //Set map layout programmatically
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = (mapView.findViewById(Integer.parseInt("1")).getRootView().findViewById(Integer.parseInt("2")));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
        getLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        //ClientExceptionHandler
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getLastKnownLocation();
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(getActivity(), 51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

    }

    private void getLocationRequest() {
        //Ask for location callback
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_loc_btn:
                //Update shared instance to retrieve it on newEventFragment
                newEvent.setEventPlaceName(finalPlace.getName());
                newEvent.setEventPlaceID(finalPlace.getId());
                eventViewModel.updateSharedInstance(newEvent);
                //Navigate back to newEventFragment
                Navigation.findNavController(v).navigate(NewEventMapFragmentDirections.actionNewEventMapFragmentToNewEventFragment());
                break;

        }
    }


}


