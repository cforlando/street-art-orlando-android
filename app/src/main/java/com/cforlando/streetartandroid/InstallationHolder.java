package com.cforlando.streetartandroid;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cforlando.streetartandroid.Models.Installation;
import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseUser;

public class InstallationHolder extends RecyclerView.ViewHolder {

    private TextView location;
    private ImageView photo;
    private ImageButton propsButton;
    private TextView propsCount;
    private ParseUser user = ParseUser.getCurrentUser();


    public InstallationHolder(View itemView) {
        super(itemView);
        location = (TextView) itemView.findViewById(R.id.art_location_tv);
        photo = (ImageView) itemView.findViewById(R.id.image);
        propsCount = (TextView) itemView.findViewById(R.id.props_count_tv);
        propsButton = (ImageButton) itemView.findViewById(R.id.props_button);
    }

    public void bind(final Installation item, final InstallationAdapter.OnItemClickListener listener) throws ParseException {
        location.setText(item.getAddress());
        propsCount.setText(String.valueOf(item.getLikesCount()));
        Glide.with(itemView.getContext())
                .load(item.getFirstPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop().crossFade()
                .into(photo);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item);
            }
        });
    }

//        propsButton = (ImageButton) itemView.findViewById(R.id.props_button);
//        propsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    if (mInstallation.isLikedByUser(user)) {
//
//                        //Toggle Like OFF
//                        mInstallation.removeLike(user);
//                        propsButton.setColorFilter(Color.parseColor("#DDDDDD"));
//                        propsCountTextView.setText(String.valueOf(mInstallation.getLikesCount()));
//                    } else {
//
//                        //Toggle Like On
//                        mInstallation.addLike(user);
//                        propsButton.setColorFilter(Color.BLUE);
//                        propsCountTextView.setText(String.valueOf(mInstallation.getLikesCount()));
//                    }
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        propsCountTextView = (TextView) itemView.findViewById(R.id.props_count_tv);
////            photoImageView.setOnClickListener(this);
////            locationTextView.setOnClickListener(this);
//        user = ParseUser.getCurrentUser();
//
//    }

}
