package com.cforlando.streetartandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cforlando.streetartandroid.Models.Installation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.Iterator;
import java.util.List;

import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;

public class InstallationDetailActivity extends AppCompatActivity implements OnMapReadyCallback, com.cforlando.streetartandroid.AddTagsDialog.AddTagsDialogListener {
    public static final String EXTRA_INSTALLATION = "Extra_Installation";


    private Installation mInstallation;
    private GoogleMap mMap;
    private MarkerOptions marker;


    private ViewPager imagePager;
    private TagView tagView;
    private TextView noTagsTv;
    private ImageButton addTagBtn;
    private CollapsingToolbarLayout collapsingToolbar;
    private LinearLayout nearbyImageLayout;
    private ImageView[] nearbyImageViews = new ImageView[3];

    public void navigate(Activity activity, String objectId) {
        Intent i = new Intent(activity, InstallationDetailActivity.class);
        i.putExtra(InstallationDetailActivity.EXTRA_INSTALLATION, objectId);
        ActivityCompat.startActivity(activity, i, null);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installation_detail);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        imagePager = (ViewPager) findViewById(R.id.image_pager);
        tagView = (TagView) findViewById(R.id.tagview);
        noTagsTv = (TextView) findViewById(R.id.no_tags_tv);
        addTagBtn = (ImageButton) findViewById(R.id.add_tag_btn);
        nearbyImageLayout = (LinearLayout) findViewById(R.id.nearby_image_layout);
        for (int i = 0; i < nearbyImageViews.length; i++) {
            nearbyImageViews[i] = (ImageView) nearbyImageLayout.getChildAt(i);
        }

        Intent i = this.getIntent();
        String objectID = i.getStringExtra(EXTRA_INSTALLATION);

        ParseQuery<Installation> query = ParseQuery.getQuery("Installation");
        query.getInBackground(objectID, new GetCallback<Installation>() {
            @Override
            public void done(final Installation object, ParseException e) {
                if (e == null) {
                    mInstallation = object;

                    //Set Title using Installation.Address
                    collapsingToolbar.setTitle(object.getAddress());

                    //Set up ImageSlider
                    final List<String> photoUrls = object.getPhotoUrls();
                    FragmentManager fm = getSupportFragmentManager();
                    imagePager.setAdapter(new FragmentStatePagerAdapter(fm) {
                        @Override
                        public Fragment getItem(int position) {
                            String photoUrl = photoUrls.get(position);
                            return ImageSliderPageFragment.newInstance(photoUrl);
                        }

                        @Override
                        public int getCount() {
                            return photoUrls.size();
                        }
                    });


                    List<String> tagStrings = object.getTags();

                    //Display "No Tags Yet" if no tags
                    if (tagStrings == null) {
                        tagView.setVisibility(View.GONE);
                        noTagsTv.setVisibility(View.VISIBLE);

                    } else {
                        noTagsTv.setVisibility(View.GONE);
                        for (String tagString : tagStrings) {
                            Tag tag = buildTag(tagString);
                            tagView.addTag(tag);
                        }
                    }

                    //Find and display nearby installations
                    ParseGeoPoint location = object.getLocation();
                    ParseQuery<Installation> query = ParseQuery.getQuery("Installation");
                    query.whereNear("location", location);
                    query.setLimit(4);
                    query.findInBackground(new FindCallback<Installation>() {
                        @Override
                        public void done(final List<Installation> objects, ParseException e) {
                            for (Iterator<Installation> iterator = objects.iterator(); iterator.hasNext(); ) {
                                Installation installation = iterator.next();
                                if (installation.getObjectId() == object.getObjectId()) {
                                    iterator.remove();
                                }

                            }
                            if (!objects.isEmpty()) {
                                for (int i = 0; i < objects.size(); i++) {
                                    final int counter = i;
                                    List<String> photoUrls = objects.get(counter).getPhotoUrls();
                                    if (photoUrls.size() > 0) {
                                        String photo = photoUrls.get(0);
                                        Glide.with(InstallationDetailActivity.this)
                                                .load(photo)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .centerCrop()
                                                .into(nearbyImageViews[i]);
                                    }

//                                    nearbyImageViews[i].setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View v) {
//                                            navigate(getParent(), objects.get(counter).getObjectId());
//                                        }
//                                    });

                                }
                            }
                        }
                    });
                    //Create Map Marker
                    marker = createMarker(object);
                    mMap.addMarker(marker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
                addTagBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment addTagsDialog = new com.cforlando.streetartandroid.AddTagsDialog();
                        addTagsDialog.show(getFragmentManager(), "addTags");
                    }
                });
            }

        });


//         Get the map and register for the ready callback
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mInstallation.addTags(tagView.getTags());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    setUpMap();
                }
            });
        }


    }

    private void setUpMap() {
        if (mMap == null) {
            return;
        }
//
//        mMap.addMarker(marker);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    private MarkerOptions createMarker(Installation installation) {
        ParseGeoPoint position = installation.getLocation();
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(position.getLatitude(), position.getLongitude())
                );
        return marker;
    }

    private Tag buildTag(String tagString) {

        Tag tag = new Tag(tagString);
        tag.tagTextColor = Color.parseColor("#FFFFFF");
        tag.layoutColor = Color.parseColor("#673AB7");
        tag.isDeletable = false;

        return tag;
    }

    @Override
    public void onReturnTags(List<Tag> tags) {
        for (Tag tag : tags) {
            tag.tagTextColor = Color.parseColor("#FFFFFF");
            tag.layoutColor = Color.parseColor("#673AB7");
            tag.isDeletable = true;
            tagView.addTag(tag);
        }

        noTagsTv.setVisibility(View.GONE);
        tagView.setVisibility(View.VISIBLE);
    }
}
