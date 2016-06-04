package com.cforlando.streetartandroid.Helpers;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.media.ExifInterface;
import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

/**
 * Created by benba on 3/29/2016.
 */
public class ExifGeoDataHelper {

    public static LatLng readGeoTagImage(String imagePath)
    {
        Location loc = new Location("");
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            GeoDegree geoDegree = new GeoDegree(exif);
            loc.setLatitude(geoDegree.getLatitude());
            loc.setLongitude(geoDegree.getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }
        LatLng position = new LatLng(loc.getLatitude(), loc.getLongitude());

        return position;
    }

    public static String getRealPath(Activity activity, String path) {
        // Split at colon, use second item in the array
        String id = path.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = activity.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();

        return filePath;
    }
}
