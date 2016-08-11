package com.cforlando.streetartandroid;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import com.viewpagerindicator.CirclePageIndicator;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;

public class InstallationDetailActivity extends AppCompatActivity implements AddTagsDialog.AddTagsDialogListener {
    public static final String EXTRA_INSTALLATION = "Extra_Installation";
    @BindColor(R.color.colorAccent)
    int colorAccent;
    @BindColor(R.color.colorPrimaryDark)
    int colorPrimaryDark;
    @BindColor(R.color.white)
    int colorWhite;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.image_pager)
    ViewPager imagePager;
    @BindView(R.id.pageIndicator)
    CirclePageIndicator pageIndicator;
    @BindView(R.id.tagview)
    TagView tagView;
    @BindView(R.id.no_tags_tv)
    TextView noTagsTv;
    @BindView(R.id.layout_action_visit)
    LinearLayout visitActionLayout;
    @BindView(R.id.layout_action_like)
    LinearLayout likeActionLayout;
    @BindView(R.id.layout_action_tag)
    LinearLayout tagActionLayout;
    @BindView(R.id.like_action_image)
    ImageView likeActionImage;
    @BindView(R.id.like_action_text)
    TextView likeActionText;
    @BindViews({R.id.nearby_image_1, R.id.nearby_image_2, R.id.nearby_image_3})
    List<ImageView> nearbyImageViews;
    private ParseUser user = ParseUser.getCurrentUser();
    private Installation mInstallation;
    private List<Installation> nearbyInstallations;

    @OnClick(R.id.layout_action_visit)
    public void openGoogleNav() {
        ParseGeoPoint location = mInstallation.getLocation();

        //Open location in Google Maps Walking Nav
        String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=w", location.getLatitude(), location.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    @OnClick(R.id.layout_action_like)
    public void performLikeAction() {

        try {
            if (user == null) {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Sign in to get started", Snackbar.LENGTH_LONG)
                        .setAction("Sign In", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(view.getContext(), LoginActivity.class);
                                view.getContext().startActivity(i);
                            }
                        });

                snackbar.show();
            } else {
                if (mInstallation.isLikedByUser(user)) {

                    //Toggle Like OFF
                    likeActionImage.setColorFilter(colorPrimaryDark);
                    likeActionText.setText(R.string.like_prompt);
                    mInstallation.removeLike(user);

                } else {
                    //Toggle Like On
                    likeActionImage.setColorFilter(colorAccent);
                    likeActionText.setText(R.string.unlike_prompt);
                    mInstallation.addLike(user);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.layout_action_tag)
    public void showAddTagsDialog() {
        DialogFragment addTagsDialog = new AddTagsDialog();
        addTagsDialog.show(getFragmentManager(), "addTags");
    }

    @OnClick({R.id.nearby_image_1, R.id.nearby_image_2, R.id.nearby_image_3})
    public void openInstallationDetail(ImageView imageView) {
        if (nearbyInstallations != null) {
            Installation installation = nearbyInstallations.get(nearbyImageViews.indexOf(imageView));
            Intent i = new Intent(this, InstallationDetailActivity.class);
            i.putExtra(InstallationDetailActivity.EXTRA_INSTALLATION, installation.getObjectId());
            startActivity(i);
        }
    }

    public void navigate(Activity activity, String objectId) {
        Intent i = new Intent(activity, InstallationDetailActivity.class);
        i.putExtra(InstallationDetailActivity.EXTRA_INSTALLATION, objectId);
        ActivityCompat.startActivity(activity, i, null);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installation_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Intent i = this.getIntent();
        final String objectID = i.getStringExtra(EXTRA_INSTALLATION);

        ParseQuery<Installation> query = ParseQuery.getQuery("Installation");
        query.getInBackground(objectID, new GetCallback<Installation>() {
            @Override
            public void done(final Installation object, ParseException e) {
                if (e == null) {
                    mInstallation = object;

                    //Set Title using Installation.Address
                    address.setText(object.getAddress());

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
                likeActionImage.setColorFilter(colorAccent);
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
                Fragment fragment = ImageSliderPageFragment.newInstance(photoUrl);
                return fragment;
            }

            @Override
            public int getCount() {
                return photoUrls.size();
            }
        });
        if (imagePager.getAdapter().getCount() <= 1) {
            pageIndicator.setVisibility(View.GONE);
        } else {
            pageIndicator.setViewPager(imagePager);
        }
    }

    private void loadTagsIntoView(Installation object) {
        List<String> tagStrings = object.getTags();

        //Display "No Tags Yet" if no tags
        if (tagStrings.isEmpty()) {
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
        query.whereNear("location", location).setLimit(4);
        query.findInBackground(new FindCallback<Installation>() {
            @Override
            public void done(final List<Installation> objects, ParseException e) {

                removeCurrentInstallation(objects);
                nearbyInstallations = objects;
                loadImages(objects);

            }

            private void loadImages(List<Installation> objects) {
                for (int i = 0; i < objects.size(); i++) {
                    Installation installation = nearbyInstallations.get(i);
                    String photo = installation.getFirstPhotoUrl();
                    nearbyImageViews.get(i).setVisibility(View.VISIBLE);
                    Glide.with(InstallationDetailActivity.this)
                            .load(photo)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(nearbyImageViews.get(i));
                }
            }

            private void removeCurrentInstallation(List<Installation> objects) {
                for (Iterator<Installation> iterator = objects.iterator(); iterator.hasNext(); ) {
                    Installation installation = iterator.next();
                    if (installation.getObjectId().equals(object.getObjectId())) {
                        iterator.remove();
                    }

                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mInstallation.addTags(tagView.getTags());
        mInstallation.saveInBackground();
    }

    private Tag buildTag(String tagString) {

        Tag tag = new Tag(tagString);
        tag.tagTextColor = colorWhite;
        tag.layoutColor = colorPrimaryDark;
        tag.isDeletable = false;

        return tag;
    }

    @Override
    public void onReturnTags(List<Tag> tags) {
        for (Tag tag : tags) {
            tag.isDeletable = true;
            tagView.addTag(tag);
        }

        noTagsTv.setVisibility(View.GONE);
        tagView.setVisibility(View.VISIBLE);
    }
}
