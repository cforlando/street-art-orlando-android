package com.cforlando.streetartandroid;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

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
    //    @BindView(R.id.art_location_tv) TextView location;
    @BindView(R.id.image) ImageView photo;
    @BindView((R.id.info_toolbar))
    Toolbar infoToolbar;


    private ParseUser user = ParseUser.getCurrentUser();


    public InstallationHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(final Installation installation, final InstallationAdapter.OnItemClickListener listener) throws ParseException {


        //Load image into imageView
        Glide.with(itemView.getContext())
                .load(installation.getFirstPhotoUrl())
                .asBitmap()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photo);

        //Load textViews
//        location.setText(installation.getAddress());

        infoToolbar.setTitle(installation.getAddress());


        //Set onClickListener for the installation
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(installation);
            }
        });

        //Set onClickListener for likeButton
    }




}
