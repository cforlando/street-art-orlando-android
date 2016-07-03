package com.cforlando.streetartandroid;

import android.content.Intent;
import android.support.design.widget.Snackbar;
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

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InstallationHolder extends RecyclerView.ViewHolder {

    @BindColor(R.color.colorAccent) int likedColor;
    @BindColor(R.color.white) int unlikedColor;
    @BindView(R.id.art_location_tv) TextView location;
    @BindView(R.id.image) ImageView photo;
    @BindView(R.id.like_button) ImageButton likeButton;
    @BindView(R.id.likes_count_tv) TextView likesCount;

    private ParseUser user = ParseUser.getCurrentUser();


    public InstallationHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(final Installation item, final InstallationAdapter.OnItemClickListener listener) throws ParseException {
        //Load textViews
        location.setText(item.getAddress());

        initLikesViews(item);

        //Load image into imageView
        Glide.with(itemView.getContext())
                .load(item.getFirstPhotoUrl())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
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
                    if (user == null) {
                        Snackbar snackbar = Snackbar
                                .make(itemView, "Sign in to get started", Snackbar.LENGTH_LONG)
                                .setAction("Sign In", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(view.getContext(), LoginActivity.class);
                                        view.getContext().startActivity(i);
                                    }
                                });

                        snackbar.show();
                    } else {
                        //If item is liked by user, unlike the item
                        if (item.isLikedByUser(user)) {

                            //Toggle Like OFF
                            likeButton.setColorFilter(unlikedColor);
                            item.removeLike(user);
                            likesCount.setText(String.valueOf(item.getLikesCount()));
                        } else {
                            //Toggle Like On
                            likeButton.setColorFilter(likedColor);
                            item.addLike(user);
                            likesCount.setText(String.valueOf(item.getLikesCount()));

                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


        });

    }

    private void initLikesViews(Installation item) throws ParseException {

        //Set likesCount string
        likesCount.setText(String.valueOf(item.getLikesCount()));

        //If user is not signed in, set color to UNLIKE
        if (user == null) {
            likeButton.setColorFilter(unlikedColor);
        } else {
            //Fill in color of likeButton if installation is liked by user
            if (item.isLikedByUser(user)) {
                likeButton.setColorFilter(likedColor);
            }
        }
    }


}
