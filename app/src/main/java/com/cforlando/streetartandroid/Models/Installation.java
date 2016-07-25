package com.cforlando.streetartandroid.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.bumptech.glide.Glide;
import com.cforlando.streetartandroid.Helpers.ParseHelper;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.kaede.tagview.Tag;

/**
 * Created by benba on 2/10/2016.
 */
@ParseClassName("Installation")
public class Installation extends ParseObject implements Parcelable {

    public static final String TAG = "InstallationObject";
    public static final Creator<Installation> CREATOR = new Creator<Installation>() {
        @Override
        public Installation createFromParcel(Parcel in) {
            return new Installation(in);
        }

        @Override
        public Installation[] newArray(int size) {
            return new Installation[size];
        }
    };
    private Context mContext;

    // Public default constructor
    public Installation() {
        super();
    }

    public Installation(Context context) {
        super();
        mContext = context;
    }

    protected Installation(Parcel in) {
    }

    public static ParseQuery<Installation> getQuery() {
        return ParseQuery.getQuery(Installation.class);
    }

    public Context getContext() {
        return mContext;
    }

    // Get the user for this item
    public ParseUser getCreator() {
        return getParseUser(ParseHelper.INSTALLATION_CREATOR);
    }

    // Associate each item with a user
    public void setCreator(ParseUser user) {
        put(ParseHelper.INSTALLATION_CREATOR, user);
    }

    // Get name of creator as a string
    public String getCreatorName() {
        return getCreator().getUsername();
    }

    public ParseGeoPoint getLocation() {

        ParseGeoPoint point = getParseGeoPoint("location");
        return point;
    }

    public void setLocation(LatLng latLng) {
        ParseGeoPoint point = new ParseGeoPoint(latLng.latitude, latLng.longitude);
        put(ParseHelper.INSTALLATION_LOCATION, point);
    }

    public String getAddress() {
        return getString(ParseHelper.INSTALLATION_ADDRESS);
    }

    public void setAddress(String address) {
        put(ParseHelper.INSTALLATION_ADDRESS, address);
    }

    public void addPhoto(Bitmap bitmap) throws ParseException {
        // Convert it to byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] image = stream.toByteArray();

        // Create the ParseFile
        ParseFile file = new ParseFile("image.jpg", image);
        // Upload the image into Parse Cloud
        file.saveInBackground();

        add(ParseHelper.INSTALLATION_PHOTOS, file);
        saveInBackground();
    }

    public void addPhotos(List<Uri> uris) throws ExecutionException, InterruptedException, ParseException {
        for (Uri uri : uris) {
            Bitmap theBitmap = Glide.
                    with(mContext).
                    load(uri).
                    asBitmap().
                    into(-1, -1).
                    get();

            addPhoto(theBitmap);
        }
    }

    public List<String> getPhotoUrls() {
        List<String> urls = new ArrayList<String>();
        List<ParseFile> photos = getList(ParseHelper.INSTALLATION_PHOTOS);

        if (photos != null) {
            for (ParseFile file : photos) {
                urls.add(file.getUrl());
            }
        }
        return urls;
    }

    public String getFirstPhotoUrl() {
        List<String> urls = getPhotoUrls();
        if (urls.size() > 0) {
            return urls.get(0);
        } else {
            return "";
        }
    }

    public List<ParseFile> getPhotos() {
        List<ParseFile> photos = getList(ParseHelper.INSTALLATION_PHOTOS);
        return photos;
    }

    public List<String> getTags() {
        List<String> tags = getList(ParseHelper.INSTALLATION_TAGS);
        return tags;
    }

    public void addTags(List<Tag> tags) {
        ArrayList<String> tagStrings = new ArrayList<String>();
        for (Tag tag : tags) {
            tagStrings.add(tag.text);
        }
        addAllUnique(ParseHelper.INSTALLATION_TAGS, tagStrings);
    }

    public void addLike(ParseUser user) {
        ParseObject like = new ParseObject("Like");
        like.put(ParseHelper.LIKE_FROM_USER, user);
        like.put(ParseHelper.LIKE_TO_INSTALLATION, this);
        like.saveInBackground();
        increment("likesCount", 1);
        saveInBackground();

    }

    public void removeLike(ParseUser user) throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseHelper.LIKE_CLASS);
        query.whereEqualTo(ParseHelper.LIKE_FROM_USER, user);
        query.whereEqualTo(ParseHelper.LIKE_TO_INSTALLATION, this);

        List<ParseObject> likes = query.find();
        if (!likes.isEmpty()) {
            for (ParseObject like : likes) {
                like.delete();
                increment("likesCount", -1);
            }
        }
        saveInBackground();
    }

    public List<ParseObject> getLikes() throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseHelper.LIKE_CLASS);
        query.whereEqualTo(ParseHelper.LIKE_TO_INSTALLATION, this);
        query.include(ParseHelper.LIKE_FROM_USER);
        List<ParseObject> results = query.find();
        return results;
    }

    public int getLikesCount() throws ParseException {
        int likesCount = getInt("likesCount");
        return likesCount;
    }

    public boolean isLikedByUser(ParseUser user) throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseHelper.LIKE_CLASS);
        query.whereEqualTo(ParseHelper.LIKE_FROM_USER, user);
        query.whereEqualTo(ParseHelper.LIKE_TO_INSTALLATION, this);
        List<ParseObject> likes = query.find();
        return !likes.isEmpty();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
