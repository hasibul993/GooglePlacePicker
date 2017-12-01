package com.googleplacepicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import com.google.android.gms.maps.model.LatLng;
import com.googleplacepicker.MediaPermission.PermissionsChecker;
import com.googleplacepicker.MediaPermission.PickMediaActivity;
import com.googleplacepicker.Utils.Utility;

public class PlacePickerActivity extends AppCompatActivity implements AppConstants {

    Utility utility = new Utility();

    TextView locationStartTV, locationEndTV, calcuteDistanceTV, calcuteDistanceValueTV, showLocationTV;
    ImageView profileIcon;
    boolean isStartLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_picker_activity);

        locationStartTV = (TextView) findViewById(R.id.locationStartTV);
        locationEndTV = (TextView) findViewById(R.id.LocationEnd);
        calcuteDistanceTV = (TextView) findViewById(R.id.calcuteDistance);
        showLocationTV = (TextView) findViewById(R.id.showLocation);
        calcuteDistanceValueTV = (TextView) findViewById(R.id.calcuteDistanceValue);
        profileIcon = (ImageView) findViewById(R.id.icon);

        locationStartTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    isStartLocation = true;
                    PickMediaActivity pickMediaActivity = new PickMediaActivity();
                    pickMediaActivity.ShowLocationMap(PlacePickerActivity.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });


        locationEndTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    isStartLocation = false;
                    PickMediaActivity pickMediaActivity = new PickMediaActivity();
                    pickMediaActivity.ShowLocationMap(PlacePickerActivity.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });


        calcuteDistanceTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String startLocation, endLocation, latlangStart, latlangEnd;
                LatLng latLngStart, latLngEnd;
                try {
                    startLocation = locationStartTV.getText().toString();
                    endLocation = locationEndTV.getText().toString();

                    if (StringUtils.isBlank(startLocation)) {
                        utility.ShowToast(PlacePickerActivity.this, "Pick start place");
                        return;
                    } else if (StringUtils.isBlank(endLocation)) {
                        utility.ShowToast(PlacePickerActivity.this, "Pick end place");
                        return;
                    }

                    //Calcute Start LatLong
                    latlangStart = startLocation.split("#")[1];
                    latlangStart = latlangStart.split(":")[1].replace(".map", "");
                    latlangStart = latlangStart.replace("(", "");
                    latlangStart = latlangStart.replace(")", "");

                    String[] latLngStartArray = latlangStart.split(",");
                    double latitudeS = Double.parseDouble(latLngStartArray[0]);
                    double longitudeS = Double.parseDouble(latLngStartArray[1]);
                    latLngStart = new LatLng(latitudeS, longitudeS);

                    //Calcute End LatLong
                    latlangEnd = endLocation.split("#")[1];
                    latlangEnd = latlangEnd.split(":")[1].replace(".map", "");
                    latlangEnd = latlangEnd.replace("(", "");
                    latlangEnd = latlangEnd.replace(")", "");

                    String[] latLngEndArray = latlangEnd.split(",");
                    double latitudeE = Double.parseDouble(latLngEndArray[0]);
                    double longitudeE = Double.parseDouble(latLngEndArray[1]);
                    latLngEnd = new LatLng(latitudeE, longitudeE);


                    double value = utility.CalculationByDistance(PlacePickerActivity.this, latLngStart, latLngEnd);

                    calcuteDistanceValueTV.setText(value + "");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        showLocationTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    String getLocation = locationStartTV.getText().toString();
                    if (!StringUtils.isBlank(getLocation)) {
                        utility.redirectToGoogleMap(PlacePickerActivity.this, getLocation);
                    } else {
                        utility.ShowToast(PlacePickerActivity.this, "Select place first");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PickMediaActivity pickMediaActivity = new PickMediaActivity();
                    pickMediaActivity.checkPermission(PlacePickerActivity.this, PERMISSIONS_CAMERA, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

    }


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_place_picker, menu);
        MenuItem menuItem = menu.findItem(R.id.archive);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.archive:
                utility.refreshScreen(PlacePickerActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void SetActivityIcon(Bitmap bitmap, String filePath) {
        try {
            if (bitmap != null) {
                profileIcon.setImageBitmap(null);
                profileIcon.setImageBitmap(bitmap);
            } else {
                utility.ShowToast(PlacePickerActivity.this, getString(R.string.upload_fail));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    ///this for marshmallow permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            PickMediaActivity pickMediaActivity = new PickMediaActivity();
            pickMediaActivity.activityPermissionsResult(PlacePickerActivity.this, requestCode, permissions, grantResults);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /// this is for image picker as well as place picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            PickMediaActivity pickMediaActivity = new PickMediaActivity();
            if (isStartLocation)
                pickMediaActivity.activityResult(PlacePickerActivity.this, getString(R.string.placePickerActivity), locationStartTV, requestCode, resultCode, data);
            else
                pickMediaActivity.activityResult(PlacePickerActivity.this, getString(R.string.placePickerActivity), locationEndTV, requestCode, resultCode, data);
            // passing set_location tv as parameter for location set.
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            PermissionsChecker checker = new PermissionsChecker(PlacePickerActivity.this);
            if (!checker.lacksPermissions(PERMISSIONS_CAMERA)) {
                PickMediaActivity pickMediaActivity = new PickMediaActivity();
                pickMediaActivity.SetToSharePreference(PlacePickerActivity.this, getString(R.string.cameraNeverAskAgain), false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
