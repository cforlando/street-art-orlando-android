package com.cforlando.streetartandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benba on 4/21/2016.
 */
public class ImageSliderPageFragment extends android.support.v4.app.Fragment {
    @BindView(R.id.slider_image)
    ImageView imageView;
    private String photoUrl;

    public static ImageSliderPageFragment newInstance(String photoUrl) {
        ImageSliderPageFragment fragment = new ImageSliderPageFragment();
        Bundle args = new Bundle();
        args.putString("photoUrl", photoUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoUrl = getArguments().getString("photoUrl");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_page, container, false);
        ButterKnife.bind(this, view);
        Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .into(imageView);

        return view;
    }
}
