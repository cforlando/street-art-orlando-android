package com.cforlando.streetartandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LicenseActivity extends AppCompatActivity {
    @BindView(R.id.legal) TextView tv;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);

        String licenseText = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);

        if (licenseText == null) {
            Toast.makeText(LicenseActivity.this,
                    "Google Play Services not available on this device",
                    Toast.LENGTH_SHORT).show();
        } else {
            tv.setText(licenseText);
        }


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
