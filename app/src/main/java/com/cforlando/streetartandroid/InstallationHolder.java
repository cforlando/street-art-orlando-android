package com.cforlando.streetartandroid;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cforlando.streetartandroid.Models.Installation;
import com.parse.ParseException;
import com.parse.ParseUser;

public class InstallationHolder extends RecyclerView.ViewHolder {

    private final int likedColor = itemView.getResources().getColor(R.color.colorAccent);
    private TextView location;
    private ImageView photo;
    private ImageButton likeButton;
    private TextView propsCount;
    private ParseUser user = ParseUser.getCurrentUser();


    public InstallationHolder(View itemView) {
        super(itemView);
        location = (TextView) itemView.findViewById(R.id.art_location_tv);
        photo = (ImageView) itemView.findViewById(R.id.image);
        propsCount = (TextView) itemView.findViewById(R.id.props_count_tv);
        likeButton = (ImageButton) itemView.findViewById(R.id.props_button);
    }

    public void bind(final Installation item, final InstallationAdapter.OnItemClickListener listener) throws ParseException {
        //Load textViews
        location.setText(item.getAddress());
        propsCount.setText(String.valueOf(item.getLikesCount()));

        //Fill in color of likeButton if installation is liked by user
        if (item.isLikedByUser(user)) {
            likeButton.setColorFilter(likedColor);
        }

        //Load image into imageView
        Glide.with(itemView.getContext())
                .load(item.getFirstPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(photo);

        //Set onClickListener for the installation
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item);
            }
        });

        //Set onClickListener for likeButton
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (item.isLikedByUser(user)) {

                        //Toggle Like OFF
                        likeButton.setColorFilter(Color.parseColor("#DDDDDD"));
                        propsCount.setText(String.valueOf(item.getLikesCount()));
                        item.removeLike(user);
                    } else {

                        //Toggle Like On
                        likeButton.setColorFilter(likedColor);
                        propsCount.setText(String.valueOf(item.getLikesCount()));
                        item.addLike(user);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
