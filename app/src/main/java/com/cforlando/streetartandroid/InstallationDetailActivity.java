package com.cforlando.streetartandroid;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cforlando.streetartandroid.Models.Installation;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;

public class InstallationDetailActivity extends AppCompatActivity implements com.cforlando.streetartandroid.AddTagsDialog.AddTagsDialogListener, View.OnClickListener {
    public static final String EXTRA_INSTALLATION = "Extra_Installation";


    private Installation mInstallation;


    private ViewPager imagePager;
    private TagView tagView;
    private TextView noTagsTv;
    private CollapsingToolbarLayout collapsingToolbar;
    private LinearLayout visitActionLayout;
    private LinearLayout likeActionLayout;
    private LinearLayout tagActionLayout;
    private ImageView likeActionImage;
    private TextView likeActionText;
    private LinearLayout nearbyImageLayout;
    private ImageView[] nearbyImageViews = new ImageView[3];
    private ParseUser user = ParseUser.getCurrentUser();

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

        visitActionLayout = (LinearLayout) findViewById(R.id.layout_action_visit);
        visitActionLayout.setOnClickListener(this);

        likeActionLayout = (LinearLayout) findViewById(R.id.layout_action_like);
        likeActionLayout.setOnClickListener(this);

        tagActionLayout = (LinearLayout) findViewById(R.id.layout_action_tag);
        tagActionLayout.setOnClickListener(this);

        likeActionImage = (ImageView) findViewById(R.id.like_action_image);
        likeActionText = (TextView) findViewById(R.id.like_action_text);


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

                    loadImageViewPager(object);
                    loadLikeActionView();
                    loadTagsIntoView(object);
                    loadNearbyImageViews(object);


                }
            }

        });


    }

    private void loadLikeActionView() {
        //Fill in color of likeButton if installation is liked by user
        try {
            if (mInstallation.isLikedByUser(user)) {
                likeActionImage.setColorFilter(getResources().getColor(R.color.colorAccent));
                likeActionText.setText(R.string.unlike_prompt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadImageViewPager(Installation object) {
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
    }

    private void loadTagsIntoView(Installation object) {
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
    }

    private void loadNearbyImageViews(final Installation object) {
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
                        final Installation installation = objects.get(i);
                        List<String> photoUrls = installation.getPhotoUrls();
                        if (photoUrls.size() > 0) {
                            String photo = photoUrls.get(0);
                            Glide.with(InstallationDetailActivity.this)
                                    .load(photo)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .into(nearbyImageViews[i]);


                            nearbyImageViews[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(getBaseContext(), InstallationDetailActivity.class);
                                    i.putExtra(InstallationDetailActivity.EXTRA_INSTALLATION, installation.getObjectId());
                                    startActivity(i);
                                }
                            });
                        }


                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mInstallation.addTags(tagView.getTags());
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_action_visit:
                ParseGeoPoint location = mInstallation.getLocation();

                //Open location in Google Maps Walking Nav
                String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=w", location.getLatitude(), location.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
                break;

            case R.id.layout_action_like:
                try {
                    if (user == null) {
                        Snackbar snackbar = Snackbar
                                .make(collapsingToolbar, "Sign in to get started", Snackbar.LENGTH_LONG)
                                .setAction("Sign In", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(view.getContext(), LoginActivity.class);
                                        view.getContext().startActivity(i);
                                    }
                                });

                        snackbar.show();
                    } else if (mInstallation.isLikedByUser(user)) {

                        //Toggle Like OFF
                        likeActionImage.setColorFilter(getResources().getColor(R.color.colorPrimaryDark));
                        likeActionText.setText(R.string.like_prompt);
                        mInstallation.removeLike(user);
                    } else {
                        //Toggle Like On
                        likeActionImage.setColorFilter(getResources().getColor(R.color.colorAccent));
                        likeActionText.setText(R.string.unlike_prompt);
                        mInstallation.addLike(user);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.layout_action_tag:
                DialogFragment addTagsDialog = new AddTagsDialog();
                addTagsDialog.show(getFragmentManager(), "addTags");
                break;
            default:
                throw new IllegalArgumentException("Unhandled click for " + v);
        }
    }
}
