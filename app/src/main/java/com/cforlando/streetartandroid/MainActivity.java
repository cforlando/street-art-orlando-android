package com.cforlando.streetartandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cforlando.streetartandroid.Helpers.ParseRecyclerQueryAdapter;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

public class MainActivity extends AppCompatActivity  {

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler) RecyclerView recyclerView;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab_speed_dial) FabSpeedDial fabSpeedDial;


    private InstallationAdapter mInstallationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        initFab();

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

    private void initFab() {

        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.action_camera:
                        if (ParseUser.getCurrentUser() == null) {
                            showSignInPrompt();
                        } else {
                            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                EasyImage.openCamera(MainActivity.this, 0);
                            } else {
                                Nammu.askForPermission(MainActivity.this,
                                        Manifest.permission.CAMERA,
                                        new PermissionCallback() {
                                            @Override
                                            public void permissionGranted() {
                                                EasyImage.openCamera(MainActivity.this, 0);
                                            }

                                            @Override
                                            public void permissionRefused() {
                                                showRationale(R.id.action_camera);
                                            }
                                        });
                            }
                        }
                        break;

                    case R.id.action_gallery:
                        if (ParseUser.getCurrentUser() == null) {
                            showSignInPrompt();
                        } else {
                            Nammu.askForPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    new PermissionCallback() {
                                        @Override
                                        public void permissionGranted() {
                                            EasyImage.openGallery(MainActivity.this, 0);
                                        }

                                        @Override
                                        public void permissionRefused() {
                                            showRationale(R.id.action_gallery);
                                        }
                                    });
                        }
                        break;
                }
                return true;
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
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {

            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                Intent intent = new Intent(getApplicationContext(), NewInstallationActivity.class);
                intent.putExtra("image_file", imageFile);
                startActivity(intent);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(MainActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
