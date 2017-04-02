package com.klipspringercui.sgbusgo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.vision.text.Text;

import java.util.Locale;

import static com.klipspringercui.sgbusgo.BaseActivity.AA_DESTINATION_LATITUDE;
import static com.klipspringercui.sgbusgo.BaseActivity.AA_DESTINATION_LONGITUDE;
import static com.klipspringercui.sgbusgo.BaseActivity.ALIGHTING_ALARM_ADDED;
import static com.klipspringercui.sgbusgo.BaseActivity.ALIGHTING_BUSSTOP;
import static com.klipspringercui.sgbusgo.BaseActivity.SHARED_PREFERENCE_NAME;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CurrentTripActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "CurrentTripActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };










    Button btnCancel;
    TextView txtDestination;
    TextView txtDistance;
    TextView txtMessage;

    private boolean mPermissionDenied = false;
    private boolean recover;
    private BusStop destination;
    private Location currentLocation;
    private Location destinationLocation;
    GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_current_trip);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.btn_cancel).setOnTouchListener(mDelayHideTouchListener);


        txtDistance = (TextView) findViewById(R.id.txt_distance);
        txtDestination = (TextView) findViewById(R.id.txt_destination);
        txtMessage = (TextView) findViewById(R.id.txt_message);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(CancelOnClickListener);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        recover = false;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            recover = bundle.getBoolean(BaseActivity.AA_FROM_NOTIFICATION);
        }

        SharedPreferences mSharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        boolean alightingAlertAdded = mSharedPreferences.getBoolean(ALIGHTING_ALARM_ADDED, false);
        CurrentTrip currentTrip = LocalDB.getInstance().getCurrentTrip();
        if (alightingAlertAdded && currentTrip == null)
            recover = true;

        if (recover) {
            Log.d(TAG, "onCreate: recover from notification");
            double latitude = (double) mSharedPreferences.getFloat(AA_DESTINATION_LATITUDE, 0);
            double longitude = (double) mSharedPreferences.getFloat(AA_DESTINATION_LONGITUDE, 0);
            String description = mSharedPreferences.getString(ALIGHTING_BUSSTOP, null);
            if (latitude != 0 && longitude != 0 && description != null) {
                destination = new BusStop(null, null, description, latitude, longitude);
            }
        } else {
            destination = LocalDB.getInstance().getCurrentTrip().getAlightingBusStop();
        }

        if (destination == null) {
            Toast.makeText(this, "No current trip", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Initialize destination location
        Log.d(TAG, "onCreate: current trip to " + destination.getDescription());
        destinationLocation = new Location("destination");
        destinationLocation.setLatitude(destination.getLatitude());
        destinationLocation.setLongitude(destination.getLongitude());
        String destinationText = "To: " + destination.getDescription();
        txtDestination.setText(destinationText);
        txtDistance.setText("Distance: ...");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);



    }

    Button.OnClickListener CancelOnClickListener = new Button.OnClickListener(){

        /**
         * Cancel current trip and synchronize local storage.
         * @param v
         */
        @Override
        public void onClick(View v) {
            Toast.makeText(CurrentTripActivity.this, "Trip canceled", Toast.LENGTH_SHORT).show();
            removeAlightingAlarm();
            SharedPreferences mSharedPreference = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreference.edit();
            editor.putBoolean(ALIGHTING_ALARM_ADDED, false);
            editor.apply();
            LocalDB.getInstance().setCurrentTrip(null);
            LocalDB.getInstance().setAlightingAlarmPendingIntent(null);
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (recover) {
            txtMessage.setText(R.string.alight_message);
            btnCancel.setText("FINISH THIS TRIP");
            btnCancel.setTextColor(Color.parseColor("#33e550"));
            SharedPreferences mSharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(ALIGHTING_ALARM_ADDED, false);
            editor.apply();
            LocalDB.getInstance().setCurrentTrip(null);
            LocalDB.getInstance().setAlightingAlarmPendingIntent(null);
            return;
        }
        BusStop altDestination = LocalDB.getInstance().getCurrentTrip().getAlightingBusStop();
        if (altDestination == null || altDestination.getDescription() == null)
            finish();
        if (!altDestination.getDescription().equals(destination.getDescription())) {
            destination = altDestination;
            Log.d(TAG, "onCreate: current trip to " + destination.getDescription());
            destinationLocation = new Location("destination");
            destinationLocation.setLatitude(destination.getLatitude());
            destinationLocation.setLongitude(destination.getLongitude());

            String destinationText = "To: " + destination.getDescription();
            txtDestination.setText(destinationText);
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        String displayText;
        double distance = currentLocation.distanceTo(destinationLocation);
        if (distance > 2000.0)
            displayText = String.format(Locale.getDefault(),"Distance: %.2fkm", distance/1000);
        else
            displayText = String.format(Locale.getDefault(), "Distance: %.2fm", distance);
        txtDistance.setText(displayText);
        if (distance < 150.0) {
            txtMessage.setText(R.string.alight_message);
            btnCancel.setText("FINISH THIS TRIP");
            btnCancel.setTextColor(Color.parseColor("#33e550"));
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        String displayText;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (currentLocation == null) {
            Log.e(TAG, "onConnected: user location not available");
        } else {
            double distance = destinationLocation.distanceTo(currentLocation);
            Log.d(TAG, "onResume: distance: " + distance);
            if (distance > 2000.0)
                displayText = String.format(Locale.getDefault(),"Distance: %.2fkm", distance/1000);
            else
                displayText = String.format(Locale.getDefault(), "Distance: %.2fm", distance);
            txtDistance.setText(displayText);
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void removeAlightingAlarm() {
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "setAlightingAlarm: client not connected");
            return;
        }
        try {
            PendingIntent currentPendingIntent = LocalDB.getInstance().getAlightingAlarmPendingIntent();
            if (currentPendingIntent != null) {
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, currentPendingIntent);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "setAlightingAlarm: Geofencing security exception");
            e.printStackTrace();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onConnectionFailed: called");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        LatLng busStop = new LatLng(destination.getLatitude(), destination.getLongitude());
        enableMyLocation();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busStop, 13));

        double longitude = destination.getLongitude();
        double latitude = destination.getLatitude();
        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Marker"));
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
