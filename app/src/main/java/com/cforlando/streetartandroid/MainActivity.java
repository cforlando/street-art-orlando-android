package com.cforlando.streetartandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cforlando.streetartandroid.Helpers.ParseRecyclerQueryAdapter;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.tajchert.nammu.Nammu;

public class MainActivity extends AppCompatActivity  {

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler) RecyclerView recyclerView;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton addPhotoFab;
    private int REQUEST_CODE_IMAGE_PICKER = 2000;
    private InstallationAdapter mInstallationAdapter;

    @OnClick(R.id.fab)
    public void startPhotoPicker() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_MODE, ImagePickerActivity.MODE_MULTIPLE);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_LIMIT, 10);
        intent.putExtra(ImagePickerActivity.INTENT_EXTRA_SHOW_CAMERA, true);
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        setRecyclerAdapter(recyclerView);

    }

    private void setRecyclerAdapter(RecyclerView recyclerView) {

        ParseQueryAdapter.QueryFactory factory =
                new ParseQueryAdapter.QueryFactory<com.cforlando.streetartandroid.Models.Installation>() {
                    public ParseQuery<com.cforlando.streetartandroid.Models.Installation> create() {
                        ParseQuery<com.cforlando.streetartandroid.Models.Installation> query = com.cforlando.streetartandroid.Models.Installation.getQuery();
                        query.include("creator");
                        query.whereExists("photos");
                        query.orderByDescending("createdAt");

                        return query;
                    }
                };

        mInstallationAdapter = new InstallationAdapter(factory, new InstallationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(com.cforlando.streetartandroid.Models.Installation installation) {
                Intent i = new Intent(getBaseContext(), InstallationDetailActivity.class);
                i.putExtra(InstallationDetailActivity.EXTRA_INSTALLATION, installation.getObjectId());
                startActivity(i);
            }
        }, true);
        recyclerView.setAdapter(mInstallationAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        mInstallationAdapter.addOnQueryLoadListener(new ParseRecyclerQueryAdapter.OnQueryLoadListener<com.cforlando.streetartandroid.Models.Installation>() {
            @Override
            public void onLoaded(List<com.cforlando.streetartandroid.Models.Installation> objects, Exception e) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onLoading() {
                if (!swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(true);
                }

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mInstallationAdapter.loadObjects();
            }
        });

    }


    private void showRationale(int action) {
        switch (action) {
            case R.id.action_camera:
                Snackbar
                        .make(coordinatorLayout, R.string.rationale_camera, Snackbar.LENGTH_LONG)
                        .show();
                break;

            case R.id.action_gallery:
                Snackbar
                        .make(coordinatorLayout, R.string.rationale_gallery, Snackbar.LENGTH_LONG)
                        .show();
                break;
        }

    }

    private void showSignInPrompt() {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, R.string.prompt_sign_in, Snackbar.LENGTH_LONG)
                .setAction(R.string.sign_in, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadSignIn();
                    }
                });
        snackbar.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_logout);
        if (ParseUser.getCurrentUser() == null) {
            menuItem.setTitle(R.string.sign_in);
        } else {
            menuItem.setTitle(R.string.sign_out);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_licenses) {
            Intent intent = new Intent(this, LicenseActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_logout) {
            if (ParseUser.getCurrentUser() == null) {
                loadSignIn();
            } else {
                ParseUser.logOut();
                Snackbar.make(coordinatorLayout, R.string.signed_out_message, Snackbar.LENGTH_SHORT).show();
                item.setTitle(R.string.sign_in);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadSignIn() {
        Intent i = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
            ArrayList<String> imagePaths = new ArrayList<String>();

            for (int i = 0, l = images.size(); i < l; i++) {
                imagePaths.add(images.get(i).getPath());
            }
            Intent intent = new Intent(getApplicationContext(), NewInstallationActivity.class);
            intent.putStringArrayListExtra(NewInstallationActivity.IMAGE_PATHS, imagePaths);
            startActivity(intent);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
