package com.googleplacepicker.GoogleRoute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.location.LocationListener;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;

import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.googleplacepicker.AppConstants;
import com.googleplacepicker.MediaPermission.PermissionsChecker;
import com.googleplacepicker.MediaPermission.PickMediaActivity;
import com.googleplacepicker.R;


public class GoogleRouteActivity extends FragmentActivity implements AppConstants, LocationListener, OnMapReadyCallback {

    GoogleMap googleMap;
    ArrayList<LatLng> markerPoints;

    LatLng coordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_direction);
        try {
            // Initializing
            markerPoints = new ArrayList<LatLng>();

            // Getting reference to SupportMapFragment of the activity_main
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

            // Getting Map for the SupportMapFragment
            //map = fm.getMap();
            mapFragment.getMapAsync(GoogleRouteActivity.this);


            getCurrentLocation();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            PermissionsChecker checker = new PermissionsChecker(GoogleRouteActivity.this);
           /* if (!checker.lacksPermissions(PERMISSIONS_GPS)) {
                PickMediaActivity pickMediaActivity = new PickMediaActivity();
                pickMediaActivity.SetToSharePreference(GoogleRouteActivity.this, getString(R.string.gpsNeverAskAgain), true);
            }*/

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

    public void getCurrentLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            // Location location = locationManager
            // .getLastKnownLocation(bestProvider);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                onLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            // locationManager
            // .requestLocationUpdates(bestProvider, 20000, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 0, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onMapReady(final GoogleMap gMap) {
        try {


            this.googleMap = gMap;
            LatLng sydney = new LatLng(-34, 151);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            googleMap.setMyLocationEnabled(true);

            Location location = googleMap.getMyLocation();
            if (location != null) {
                onLocationChanged(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    if (markerPoints.size() > 1) {
                        markerPoints.clear();
                        GoogleRouteActivity.this.googleMap.clear();
                    }

                    // Adding new item to the ArrayList
                    markerPoints.add(latLng);

                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker
                    options.position(latLng);

                    if (markerPoints.size() == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    } else if (markerPoints.size() == 2) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    }

                    ZoomPosition(latLng, options);
                    // Checks, whether start and end locations are captured
                    if (markerPoints.size() >= 2) {
                        try {
                            LatLng origin = (LatLng) markerPoints.get(0);
                            LatLng dest = (LatLng) markerPoints.get(1);

                            // Getting URL to the Google Directions API
                            String url = getDirectionsUrl(origin, dest);

                            DownloadTask downloadTask = new DownloadTask();

                            // Start downloading json data from Google Directions API
                            downloadTask.execute(url);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String url = "";

        try {
            // Origin of route
            String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

            // Destination of route
            String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = str_origin + "&" + str_dest + "&" + sensor;

            // Output format
            String output = "json";

            // Building the url to the web service
            url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.i("downloadUrl", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void onLocationChanged(LatLng latLng) {
        String detailAddressShowing = null;
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        coordinate = new LatLng(latitude, longitude);

        markerPoints.add(latLng);
        //markerPoints.position(coordinate);
        //markerPoints.title(detailAddressShowing);
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(latLng);

        if (markerPoints.size() == 1) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }

        ZoomPosition(coordinate, options);

    }

    private void ZoomPosition(LatLng latLng, MarkerOptions markerOptions) {
        try {
            // googleMap.addMarker(new MarkerOptions().position(coordinate));
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(5));

            googleMap.addMarker(markerOptions);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                ParserTask parserTask = new ParserTask();

                // Invokes the thread for parsing the JSON data
                parserTask.execute(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            try {
                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(15);
                    lineOptions.color(Color.CYAN);

                }

                // Drawing polyline in the Google Map for the i-th route
                googleMap.addPolyline(lineOptions);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_picker, menu);
        return true;
    }
}