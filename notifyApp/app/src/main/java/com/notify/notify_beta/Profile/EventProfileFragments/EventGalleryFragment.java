package com.notify.notify_beta.Profile.EventProfileFragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.notify.notify_beta.Adapters.AdapterEventGallery;
import com.notify.notify_beta.Adapters.AdapterSelectedImages;
import com.notify.notify_beta.Constants;
import com.notify.notify_beta.Listeners.OnRecycleItemClickedListener;
import com.notify.notify_beta.Models.MyEvent;
import com.notify.notify_beta.Models.MyImage;
import com.notify.notify_beta.R;
import com.notify.notify_beta.ViewModels.AuthViewModel;
import com.notify.notify_beta.ViewModels.EventViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.notify.notify_beta.Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE;
import static com.notify.notify_beta.Constants.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE;
import static com.notify.notify_beta.Constants.PICK_MULTIPLE_IMAGE_REQUEST;
import static com.notify.notify_beta.Constants.UPLOADED;
import static com.notify.notify_beta.Constants.UPLOAD_CANCELED;
import static com.notify.notify_beta.Constants.UPLOAD_PAUSED;
import static com.notify.notify_beta.Constants.UPLOAD_SUCCESSFULL;

public class EventGalleryFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "EventGalleryFragment: ";

    //RecyclerView for gridLayout and layout manager
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AdapterEventGallery adapterGallery;

    //Floating action button
    private FloatingActionButton addImageBtn;

    //ViewModels
    private EventViewModel eventViewModel;
    private AuthViewModel authViewModel;

    //Gallery owner event
    private MyEvent event;

    //States
    private boolean uploading;
    private boolean downloading;
    private boolean deleting;

    //BottomSheet
    private LinearLayout bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    //Upload image to gallery button
    private Button uploadBtn;

    //In bottomSheet recyclerView & its layout manager
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView selectedImgRecycler;

    //Images selected from android gallery list
    private ArrayList<MyImage> selectedUriList;

    //Images selected from bottomSheetList for next versions optional
    private ArrayList<MyImage> selectedFromBottomSheet;

    //BottomSheet's recyclerView Adapter
    private AdapterSelectedImages adapterSelectedImages;

    //Images selected from event gallery list
    private ArrayList<MyImage> selectedFromInAppGallery;

    //Encoded image and list of them (Retrieving image from android gallery)
    private String imageEncoded;
    private List<String> imagesEncodedList;

    //Images of event gallery
    private ArrayList<MyImage> thumbUriList;
    private ArrayList<MyImage> originalUriList;

    //Toolbar and main activity
    private AppCompatActivity activityForBar;
    private Toolbar toolbar;

    //NavController
    private NavController navController;

    //BottomNavigation to hide
    private BottomNavigationView bottomNav;

    //CustomListeners
    private OnRecycleItemClickedListener listenerOfBottomSheet;
    private OnRecycleItemClickedListener listenerOfGallery;


    public EventGalleryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_gallery, container, false);

        //Init toolBar
        if (toolbar == null) {
            Log.i(TAG, "TOOLBAR NULL");
            setHasOptionsMenu(true);
            toolbar = v.findViewById(R.id.gallery_toolbar);
            activityForBar = (AppCompatActivity) getActivity();
            activityForBar.setSupportActionBar(toolbar);
        }

        //Set states
        uploading = false;
        downloading = false;

        //Hide bottomNavigationView
        bottomNav = getActivity().findViewById(R.id.bottom_nav);
        bottomNav.setVisibility(View.GONE);

        //Init bottomSheet
        bottomSheet = (LinearLayout) v.findViewById(R.id.selected_images_bottomsheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(100);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Init upload button
        uploadBtn = bottomSheet.findViewById(R.id.upload_btn);
        uploadBtn.setOnClickListener(this);

        //Set toolbar back icon and its action
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_32dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNav.setVisibility(View.VISIBLE);
                navController.popBackStack();
            }
        });

        //Get event object from sent from event profile
        event = EventGalleryFragmentArgs.fromBundle(getArguments()).getEvent();

        //Init lists, if they exist clear; if they don't, create
        if (thumbUriList == null) {
            thumbUriList = new ArrayList<>();
        } else {
            thumbUriList.clear();
        }

        if (selectedUriList == null) {
            selectedUriList = new ArrayList<>();
        } else {
            selectedUriList.clear();
        }

        if (selectedFromInAppGallery == null) {
            selectedFromInAppGallery = new ArrayList<>();
        } else {
            selectedFromInAppGallery.clear();
        }

        if (originalUriList == null) {
            originalUriList = new ArrayList<>();
        } else {
            originalUriList.clear();
        }


        //Set listener functions
        listenerOfBottomSheet = new OnRecycleItemClickedListener() {
            @Override
            public void onItemOfListClicked(Object o) {
                if (!uploading && !downloading && !deleting) {
                    if (selectedFromBottomSheet == null) {
                        selectedFromBottomSheet = new ArrayList<>();
                    }
                    if (selectedFromBottomSheet.contains((MyImage) o)) {
                        selectedFromBottomSheet.remove(o);
                    } else {
                        selectedFromBottomSheet.add((MyImage) o);
                        Log.i(TAG, "added uri: " + o.toString());
                    }
                }
            }

            @Override
            public void onItemLongClicked(Object o) {

            }
        };

        //Set listener functions
        listenerOfGallery = new OnRecycleItemClickedListener() {
            @Override
            public void onItemOfListClicked(Object o) {
                MyImage image = (MyImage) o;
                if (image.isSelected()) {
                    selectedFromInAppGallery.add(image);
                } else {
                    selectedFromInAppGallery.remove(image);
                }
                if (selectedFromInAppGallery.isEmpty()) {
                    //If no image selected, set delete and download invisible
                    toolbar.getMenu().findItem(R.id.app_bar_download).setVisible(false);
                    toolbar.getMenu().findItem(R.id.app_bar_delete).setVisible(false);
                } else {
                    //If one or more image selected, set delete and download visible
                    toolbar.getMenu().findItem(R.id.app_bar_download).setVisible(true);
                    toolbar.getMenu().findItem(R.id.app_bar_delete).setVisible(true);
                }
            }

            @Override
            public void onItemLongClicked(Object o) {
                //Open big image when, on an image long clicked
                openFullSizeImage((MyImage) o);
            }
        };

        //Init choose image button
        addImageBtn = v.findViewById(R.id.add_image_gallery);
        addImageBtn.setOnClickListener(this);

        //Init recyclerView & its adapter for gallery
        recyclerView = v.findViewById(R.id.gallery_recyclerview);
        layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        adapterGallery = new AdapterEventGallery(getContext(), thumbUriList, listenerOfGallery);
        recyclerView.setAdapter(adapterGallery);
        recyclerView.setHasFixedSize(true);

        //Init recyclerView & its adapter for bottomSheet
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        selectedImgRecycler = bottomSheet.findViewById(R.id.selected_image_list);
        selectedImgRecycler.setLayoutManager(linearLayoutManager);
        adapterSelectedImages = new AdapterSelectedImages(selectedUriList, getContext(), listenerOfBottomSheet);
        selectedImgRecycler.setAdapter(adapterSelectedImages);

        //Init viewModels
        eventViewModel = ViewModelProviders.of(requireActivity()).get(EventViewModel.class);
        eventViewModel.EventViewModel();

        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();

        eventViewModel.getGalleryImgUriList(eventViewModel.observeRefList().getValue()).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyImage>>() {
            @Override
            public void onChanged(ArrayList<MyImage> uris) {
                //Get thumbUriList
                for (MyImage thumbUri : uris) {
                    if (thumbUri.getName().startsWith("thumb_") && !thumbUriList.contains(thumbUri)) {
                        thumbUriList.add(thumbUri);
                    } else {
                        originalUriList.add(thumbUri);
                    }

                }
                updateAdapter(thumbUriList);
            }
        });

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (!uploading) {
                    //Delete image from bottomSheet list if image is swiped
                    deleteSelectedImage(selectedUriList.get(viewHolder.getAdapterPosition()));
                }
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(selectedImgRecycler);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init navController
        navController = Navigation.findNavController(view);

        //Handle on backbutton pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navController.popBackStack();
                    }
                });
    }

    private void updateAdapter(ArrayList<MyImage> uriList) {
        //Update gallery list
        for (MyImage uri : uriList) {
            Log.i(TAG, "Received uri for adapter: " + uri.getImgUri().toString());
        }
        adapterGallery.setImgUriList(uriList);
        adapterGallery.notifyDataSetChanged();
        Log.i(TAG, "AdapterGallery updated");
    }

    private void addToGallery(MyImage uploaded) {
        //Add uploaded image to gallery with 50% quality (as thumb)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        uploaded.getBitmap().compress(Bitmap.CompressFormat.JPEG, 50, baos);
        Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()));
        uploaded.setBitmap(decoded);
        thumbUriList.add(uploaded);
        adapterGallery.notifyDataSetChanged();
    }

    private void updateSelectedImgAdapter(ArrayList<MyImage> uriList) {
        //Update bottomSheet list
        adapterSelectedImages.setSelectedImageList(uriList);
        adapterSelectedImages.notifyDataSetChanged();
        Log.i(TAG, "AdapterSelectedImgs updated");
    }

    private void deleteSelectedImage(MyImage uri) {
        //Remove image from bottomSheet
        selectedUriList.remove(uri);
        adapterSelectedImages.notifyDataSetChanged();
        if (selectedUriList.isEmpty()) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void deleteSelectedFromGallery(MyImage image) {
        //Delete image from gallery
        Log.i(TAG, "DELETING FROM GALLERY");
        thumbUriList.remove(image);
        originalUriList.remove(image);
        adapterGallery.notifyDataSetChanged();
        Log.i(TAG, "ADAPTER UPDATED AFTER DELETE");
    }

    private void chooseImage() {
        //Choose image
        checkReadPermission();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_MULTIPLE_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            //Thanks to laith-mihyar@StackOverflow
            // When an Image is picked
            if (requestCode == PICK_MULTIPLE_IMAGE_REQUEST && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                imagesEncodedList = new ArrayList<String>();
                if (data.getData() != null) {
                    //If one image selected

                    Uri mImageUri = data.getData();

                    // Get the cursor
                    Cursor cursor = getActivity().getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded = cursor.getString(columnIndex);
                    cursor.close();
                    //Create image instance with received uri
                    MyImage myUri = new MyImage(mImageUri);
                    //Set image
                    InputStream is = getActivity().getContentResolver().openInputStream(myUri.getImgUri());
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    myUri.setBitmap(bitmap);
                    //Create timestamp for image name
                    Date date = new Date();
                    Timestamp timestamp = new Timestamp(date.getTime());
                    String fileName = timestamp.toString() + "." + getFileExtention(mImageUri);
                    myUri.setName(fileName);
                    String mime = getActivity().getContentResolver().getType(mImageUri);
                    //Check file type (expected image)
                    //Update ui accordingly
                    if (!mime.startsWith("image")) {
                        Toast.makeText(getContext(), "Please select an image and try again", Toast.LENGTH_SHORT).show();
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        return;
                    } else {
                        Log.e(TAG, "Selected document type: " + mime);
                        selectedUriList.add(myUri);
                        updateSelectedImgAdapter(selectedUriList);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        Toast.makeText(getContext(), "Swipe up or down to remove", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //If multiple image is selected
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<>();
                        int nonImageCount = 0;
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            if (i < 10) {
                                //Max 10 images are allowed for one round
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                String mime = getActivity().getContentResolver().getType(uri);
                                //Check file type (expected image)
                                //Update ui accordingly
                                if (!mime.startsWith("image")) {
                                    Log.i(TAG, "Non-Image Count: " + nonImageCount);
                                    nonImageCount++;
                                    Log.i(TAG,mime);
                                    Toast.makeText(getContext(), "Non-image selections are not added", Toast.LENGTH_SHORT).show();
                                    if (nonImageCount == mClipData.getItemCount() || nonImageCount == 10) {
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                    }
                                } else {
                                    //Create image instance with received uri
                                    MyImage myUri = new MyImage(uri);
                                    InputStream is = getActivity().getContentResolver().openInputStream(myUri.getImgUri());
                                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                                    //Create timestamp for image name
                                    Date date = new Date();
                                    Timestamp timestamp = new Timestamp(date.getTime());
                                    String fileName = timestamp.toString() + "." + getFileExtention(uri);
                                    //Set image
                                    myUri.setBitmap(bitmap);
                                    myUri.setName(fileName);
                                    selectedUriList.add(myUri);
                                }
                                // Get the cursor
                                Cursor cursor = getActivity().getContentResolver().query(uri, filePathColumn, null, null, null);
                                // Move to first row
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                imageEncoded = cursor.getString(columnIndex);
                                imagesEncodedList.add(imageEncoded);
                                cursor.close();
                            } else {
                                Toast.makeText(getContext(), "Max. 10 Images", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                        //Update list in bottomSheet and handle state
                        updateSelectedImgAdapter(selectedUriList);
                        if (nonImageCount != mClipData.getItemCount()) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            Toast.makeText(getContext(), "Swipe up or down to remove", Toast.LENGTH_SHORT).show();
                        } else {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                    }
                }
            } else {
                Toast.makeText(getContext(), "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getFileExtention(Uri uri) {
        ContentResolver cR = getContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onClick(View v) {
        if (!downloading && !uploading && !deleting) {
            //If no operation on progress
            if (v.getId() == addImageBtn.getId()) {
                //Choose image from gallery
                chooseImage();
            } else if (v.getId() == uploadBtn.getId()) {
                //Image upload started
                uploadStarted();
            }
        } else {
            //An operation is on progress
            Toast.makeText(getContext(), "Wait for ongoing operation", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSelected() {
        Log.i(TAG, "DELETE SELECTED CALLED");
        if (!selectedFromInAppGallery.isEmpty()) {
            MyImage imageToDelete = selectedFromInAppGallery.get(0);
            eventViewModel.deleteImage(event.getEventID(), imageToDelete).observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean isDeleted) {
                    Log.i(TAG, "LIVE DATA RETURNED: " + isDeleted);
                    //Controll if image is deleted, isDeleted returned from DB callback
                    //Notify user
                    if (!isDeleted) {
                        Toast.makeText(getContext(), "Can't delete image", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!selectedFromInAppGallery.isEmpty()) {
                            deleteSelectedFromGallery(selectedFromInAppGallery.get(0));
                            selectedFromInAppGallery.remove(0);
                            deleteSelected();
                        } else {
                            //Deleting ended
                            deleting = false;
                            Toast.makeText(getContext(), "Images deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private void uploadStarted() {
        //Upload progress start
        uploading = true;
        for (int i = 0; i < selectedUriList.size(); i++) {
            selectedUriList.get(i).setSelected(true);
        }
        //Set ui
        adapterSelectedImages.notifyDataSetChanged();
        upload();
    }

    private void upload() {
        //Upload images, that are in bottomSheet
        if (!selectedUriList.isEmpty()) {
            MyImage uri = selectedUriList.get(0);
            eventViewModel.uploadImgToGallery(event.getEventID(), uri, getFileExtention(uri.getImgUri())).observe(getViewLifecycleOwner(), new Observer<Integer>() {
                @Override
                public void onChanged(Integer ınteger) {
                    Log.e(TAG, "RETURNED: " + ınteger);
                    //Show upload progress and delete uploaded image from bottomSheeet
                    //add uploaded image to gallery
                    if (ınteger == UPLOAD_SUCCESSFULL && ınteger != UPLOADED) {
                        if (selectedUriList.isEmpty()) {
                            uploading = false;
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            Toast.makeText(getContext(), "Upload completed", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.i(TAG, "item removed from bottomsheet");
                            MyImage tempOrgImage = selectedUriList.get(0);
                            MyImage tempThumbImage = selectedUriList.get(0);
                            Bitmap bitmap = tempThumbImage.getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            tempThumbImage.setBitmap(BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray())));
                            tempThumbImage.setName("thumb_" + tempOrgImage.getName());
                            addToGallery(tempThumbImage);
                            originalUriList.add(tempOrgImage);
                            deleteSelectedImage(selectedUriList.get(0));
                            Log.i(TAG, "Recursive called");
                            upload();

                        }
                    } else if (ınteger != Constants.UPLOAD_FAILED && ınteger != UPLOAD_CANCELED && ınteger != UPLOAD_PAUSED && ınteger != 0) {
                        if (!selectedUriList.isEmpty()) {
                            //Progress 0 means currently no upload
                            Log.i(TAG, "Progress: " + ınteger);
                            selectedUriList.get(0).setProgress(ınteger);
                            adapterSelectedImages.notifyItemChanged(0);
                        }
                    }
                }
            });
        } else {
            uploading = false;
        }
    }

    private void downloadStarted() {
        //Download images from gallery (Download original image to gallery)

        for (MyImage downImg : selectedFromInAppGallery) {
            String originalName = downImg.getName();
            downImg.setName(originalName.replace("thumb_", ""));
            int pos = originalUriList.indexOf(downImg);
            final MyImage original = originalUriList.get(pos);
            Picasso.get().load(original.getImgUri()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    original.setBitmap(bitmap);
                    saveSelectedImages(original);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.e(TAG,"Error: " + e.getMessage());
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
    }

    private void saveSelectedImages(MyImage imageToSave) {
        //An album with the name of the event will be created
        //and images will be downloaded to that album
        //Create Path to save Image
        int savedCount = 0;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Notify/"); //Creates app specific folder
        path.mkdirs();
        File eventPath = new File(path, event.getEventName());
        eventPath.mkdirs();
        File imageFile = new File(eventPath, imageToSave.getName() + ".jpg"); // Imagename.jpg
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            imageToSave.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
            out.flush();
            out.close();
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(getContext(), new String[]{imageFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
            savedCount++;
            if (savedCount == selectedFromInAppGallery.size()) {
                downloading = false;
                Toast.makeText(getContext(), "Download completed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //Inflate toolbar menu for delete and download action
        activityForBar.getMenuInflater().inflate(R.menu.download_file, menu);
        menu.findItem(R.id.app_bar_download).setVisible(false);
        //Delete is only enabled for event's organisator
        if (event.getOrganisatorID().equals(authViewModel.isLoggedIn())) {
            menu.findItem(R.id.app_bar_delete).setVisible(false).setEnabled(true);
            Log.i(TAG, "DELETE MENU ITEM SET");
        } else {
            menu.findItem(R.id.app_bar_delete).setVisible(false).setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_download:
                checkWritePermission();
                downloading = true;
                downloadStarted();
            case R.id.app_bar_delete:
                if (authViewModel.isLoggedIn().equals(event.getOrganisatorID())) {
                    Log.i(TAG, "DELETE CLICKED");
                    deleteSelected();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkReadPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }
        return;
    }

    private void checkWritePermission() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage();
                } else {
                    return;
                }
            }
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadStarted();
                } else {
                    return;
                }
            }
        }
    }

    private void openFullSizeImage(MyImage myImage) {
        //Open big images in a dialog
        MyImage tempImage = myImage;
        final Dialog nagDialog = new Dialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        nagDialog.setCancelable(false);
        nagDialog.setContentView(R.layout.gallery_dialog);
        ImageButton btnClose = nagDialog.findViewById(R.id.btnIvClose);
        ImageView ivPreview = (ImageView) nagDialog.findViewById(R.id.iv_preview_image);
        tempImage.setName(myImage.getName().replace("thumb_", ""));

        Uri originalUri = originalUriList.get(originalUriList.indexOf(tempImage)).getImgUri();
        Picasso.get().load(originalUri).into(ivPreview);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                nagDialog.dismiss();
            }
        });
        nagDialog.show();
    }

}
