package com.cforlando.streetartandroid;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cforlando.streetartandroid.Models.Installation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class NewInstallationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, AddTagsDialog.AddTagsDialogListener {

    public static final String TAG = "NewInstallationActivity";
    public static final String IMAGE_PATHS = "image_paths";

    private static final int REQUEST_PLACE_PICKER_CODE = 1;

    protected GoogleApiClient mGoogleApiClient;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.image_pager)
    ViewPager imagePager;
    @BindView(R.id.pageIndicator)
    CirclePageIndicator pageIndicator;
    @BindView(R.id.tagview) TagView tagView;
    @BindView(R.id.no_tags_tv)
    TextView noTagsTv;
    @BindView(R.id.layout_action_edit_location)
    LinearLayout editLocationActionLayout;
    @BindView(R.id.layout_action_tag)
    LinearLayout tagActionLayout;
    @BindView(R.id.address)
    TextView addressText;
    @BindView(R.id.fab) FloatingActionButton fab;
    private ArrayList<String> imagePaths;
    private LatLng installationCoordinate;
    private String installationAddress;
    private Installation mInstallation = new Installation(this);

    @OnClick(R.id.layout_action_edit_location)
    public void pickLocation(View v) {
        int permissionCheck = ContextCompat.checkSelfPermission(NewInstallationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            displayPlacePicker();
        } else {
            Nammu.askForPermission(NewInstallationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    displayPlacePicker();
                }

                @Override
                public void permissionRefused() {
                    Snackbar
                            .make(coordinatorLayout, R.string.rationale_fine_location, Snackbar.LENGTH_LONG)
                            .show();
                }
            });
        }
    }

    @OnClick(R.id.fab)
    public void saveInstallation() {
        Intent intent = new Intent(NewInstallationActivity.this, MainActivity.class);
        startActivity(intent);
        try {
            mInstallation.addPhotos(imagePaths);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mInstallation.addTags(tagView.getTags());
        mInstallation.setAddress(installationAddress);
        mInstallation.setLocation(installationCoordinate);
        mInstallation.saveInBackground();
    }

    @OnClick(R.id.layout_action_tag)
    public void showAddTagsDialog() {
        DialogFragment addTagsDialog = new AddTagsDialog();
        addTagsDialog.show(getFragmentManager(), "addTags");
    }

//    @OnEditorAction(R.id.edit_tag)
//    public boolean addNewTag(TextView v, int actionId, KeyEvent event) {
//        boolean handled = false;
//        if (actionId == EditorInfo.IME_ACTION_DONE) {
//            addTag();
//            handled = true;
//        }
//        return handled;
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_installation);
        ButterKnife.bind(this);

        imagePaths = this.getIntent().getStringArrayListExtra(IMAGE_PATHS);
        loadImageViewPager(imagePaths);

        mInstallation.setCreator(ParseUser.getCurrentUser());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Extract location info from first photo
        determineLocation(imagePaths.get(0));

    }

    private void determineLocation(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            if (exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null) {
                detectLocationFromExif(exif);
            } else {
                detectCurrentLocation();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void detectLocationFromExif(ExifInterface exif) throws IOException {
        float[] coords = new float[2];
        exif.getLatLong(coords);
        LatLng point = new LatLng(coords[0], coords[1]);
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> listAddresses = geocoder.getFromLocation(coords[0], coords[1], 1);
        String address = listAddresses.get(0).getAddressLine(0);
        address = cleanAddressString(address);
        addressText.setText(address);
        installationCoordinate = point;
        installationAddress = address;
    }

    private void detectCurrentLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(NewInstallationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    Place place = likelyPlaces.get(0).getPlace();
                    String address = place.getAddress().toString().split(",")[0];
                    address = cleanAddressString(address);
                    addressText.setText(address);
                    installationCoordinate = place.getLatLng();
                    installationAddress = address;
                    likelyPlaces.release();
                }
            });
        } else {
            Nammu.askForPermission(NewInstallationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    detectCurrentLocation();
                }

                @Override
                public void permissionRefused() {
                    Snackbar
                            .make(coordinatorLayout, R.string.rationale_fine_location, Snackbar.LENGTH_LONG)
                            .show();
                }
            });
        }

    }

    private String cleanAddressString(String address) {
        if (address.contains("-")) {
            address = address.replaceAll(".*-", "");
        }
        if (address.contains("Street")) {
            address = address.replaceAll("\\bStreet\\b", "St");
        }
        if (address.contains("Avenue")) {
            address = address.replaceAll("\\bAvenue\\b", "Ave");
        }
        if (address.contains("Road")) {
            address = address.replaceAll("\\bRoad\\b", "Rd");
        }
        if (address.contains("Boulevard")) {
            address = address.replaceAll("\\bBoulevard\\b", "Blvd");
        }
        return address;
    }


    private void displayPlacePicker() {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            if (installationCoordinate != null) {
                LatLngBounds bounds = LatLngBounds.builder()
                        .include(installationCoordinate)
                        .build();
                intentBuilder.setLatLngBounds(bounds);
            }

            Intent intent = intentBuilder.build(NewInstallationActivity.this);
            startActivityForResult(intent, REQUEST_PLACE_PICKER_CODE);

        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void loadImageViewPager(final List<String> imagePaths) {
        //Set up ImageSlider
        FragmentManager fm = getSupportFragmentManager();
        imagePager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                String imagePath = imagePaths.get(position);
                return ImageSliderPageFragment.newInstance(imagePath);
            }

            @Override
            public int getCount() {
                return imagePaths.size();
            }
        });
        if (imagePager.getAdapter().getCount() <= 1) {
            pageIndicator.setVisibility(View.GONE);
        } else {
            pageIndicator.setViewPager(imagePager);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == REQUEST_PLACE_PICKER_CODE && resultCode == Activity.RESULT_OK) {
            // The user has selected a place. Extract the name and address.
            Place place = PlacePicker.getPlace(resultData, this);

            String address = place.getAddress().toString().split(",")[0];
            LatLng latLng = place.getLatLng();

            String attributions = PlacePicker.getAttributions(resultData);
            if (attributions == null) {
                attributions = "";
            }
            address = cleanAddressString(address);
            addressText.setText(address);
            installationCoordinate = latLng;
            installationAddress = address;

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Snackbar.make(coordinatorLayout, "Could not connect to internet", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onReturnTags(List<Tag> tags) {
        tagView.setVisibility(View.VISIBLE);
        for (Tag tag : tags) {
            tag.isDeletable = true;
            tagView.addTag(tag);
        }
        tagView.refreshDrawableState();
    }
}

