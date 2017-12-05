package com.googleplacepicker;

import android.*;
import android.Manifest;

/**
 * Created by BookMEds on 01-12-2017.
 */

public interface AppConstants {

    public static String[] PERMISSIONS_CAMERA = {android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE};

    public static String[] PERMISSIONS_GPS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
}
