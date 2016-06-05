package com.cforlando.streetartandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private InstallationAdapter mInstallationAdapter;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        initRecyclerView();
        initFab();

        setRecyclerAdapter(mRecyclerView);

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

        mInstallationAdapter = new com.cforlando.streetartandroid.InstallationAdapter(factory, new com.cforlando.streetartandroid.InstallationAdapter.OnItemClickListener() {
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
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onLoading() {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }

            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mInstallationAdapter.loadObjects();
            }
        });

    }

    private void initFab() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser() == null) {
                    loadSignIn();
                } else {
                    EasyImage.openChooserWithGallery(MainActivity.this, "Add a Photo", 0);
                }
            }
        });
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
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
                Snackbar.make(mCoordinatorLayout,R.string.signed_out_message, Snackbar.LENGTH_SHORT).show();
                item.setTitle(R.string.sign_in);
            }


//            Intent intent = new Intent(MainActivity.this,
//                    LoginActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
