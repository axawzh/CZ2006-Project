/** 
  * Created by Zhenghao on 4/3/17. 
  */

package com.klipspringercui.sgbusgo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.maps.model.LatLng;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;

import static com.klipspringercui.sgbusgo.FareCalculatorActivity.FC_SELECTED_BUSSTOP;

public class AlightingAlarmActivity extends BaseActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>{

    private static final String TAG = "AlightingAlarmActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    static final String AA_SELECTED_BUSSTOP = "ALARM SELECTED BUS STOP";
    static final String AA_SELECTED_BUSSERVICENO = "ALARM SELECTED BUS SERVICE NO";
    static final String ACTION_PROXIMITY_ALERT = "com.klipspringercui.sgbusgo.ACTION_PROXIMITY_ALERT";

    private static final float RADIUS = 150.0f;
    private static final long EXPIRATION = 3600000;


    private BusStop selectedBusStop = null;

    Button btnAASelectBusStop;
    Button btnSetAlightingAlarm;


    protected GoogleApiClient mGoogleApiClient;
    private boolean alightingAlertAdded;
    private Geofence alightingAlertGeofence;
    private SharedPreferences mSharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alighting_alarm);

        activateToolBar(false, R.string.title_activity_alighting_alarm);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrentTrip current = LocalDB.getInstance().getCurrentTrip();
                if (current == null) {
                    Snackbar.make(view, "You haven't start a trip yet.\n Set an alighting alarm or activate a frequent trip to start one!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Toast.makeText(AlightingAlarmActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AlightingAlarmActivity.this, CurrentTripActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnAASelectBusStop = (Button) findViewById(R.id.btnAASelectBusStop);
        btnAASelectBusStop.setOnClickListener(busStopOnClickListener);

        btnSetAlightingAlarm = (Button) findViewById(R.id.btnSetAlightingAlarm);
        btnSetAlightingAlarm.setOnClickListener(setAlarmOnClickListener);

        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE);
        alightingAlertAdded = mSharedPreferences.getBoolean(ALIGHTING_ALARM_ADDED, false);
        CurrentTrip currentTrip = LocalDB.getInstance().getCurrentTrip();
        if (currentTrip == null && alightingAlertAdded) {
            double latitude = (double) mSharedPreferences.getFloat(AA_DESTINATION_LATITUDE, 0);
            double longitude = (double) mSharedPreferences.getFloat(AA_DESTINATION_LONGITUDE, 0);
            String description = mSharedPreferences.getString(ALIGHTING_BUSSTOP, null);
            if (latitude != 0 && longitude != 0 && description != null) {
                LocalDB.getInstance().setCurrentTrip(new CurrentTrip(
                        new BusStop(null, null, description, latitude, longitude)));
                LocalDB.getInstance().setAlightingAlarmPendingIntent(getGeofencePendingIntent(description));
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            btnAASelectBusStop.setText(R.string.select_bus_stop_btntxt);
            selectedBusStop = null;
            Intent intent = new Intent(AlightingAlarmActivity.this, BusStopSelectionActivity.class);
            startActivityForResult(intent, 0);
        }
    };

    Button.OnClickListener setAlarmOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (selectedBusStop == null) {
                Toast.makeText(AlightingAlarmActivity.this, "Please select a bus stop", Toast.LENGTH_SHORT).show();
                return;
            }
            PendingIntent currentPendingIntent = LocalDB.getInstance().getAlightingAlarmPendingIntent();
            Toast.makeText(AlightingAlarmActivity.this, R.string.alighting_alarm_success, Toast.LENGTH_SHORT).show();
            if (currentPendingIntent != null)
                removeAlightingAlarm();
            setAlightingAlarm();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                selectedBusStop = (BusStop) bundle.getSerializable(AA_SELECTED_BUSSTOP);
                btnAASelectBusStop.setText(selectedBusStop.getDescription());
            }
        }
    }

    private void setAlarm() {
        Log.d(TAG, "setAlarm: starts");
        if (selectedBusStop == null)
            return;

        //Intent intent = new Intent(getResources().getString(R.string.action_proximity));
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(ALIGHTING_BUSSTOP, selectedBusStop.getDescription());
        intent.putExtras(bundle);
        intent.setAction(ACTION_PROXIMITY_ALERT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //For testing
        Location location = LocationHandler.getInstance().getUserLocation(getApplicationContext());
        if (location == null) {
            Log.d(TAG, "setAlarm: location null ");
            Toast.makeText(this, "Please ensure that this app has internet and location access", Toast.LENGTH_SHORT).show();
            return;
        }
        double userLatitude = location.getLatitude();
        double userLongitude = location.getLongitude();

        double longitude = selectedBusStop.getLongitude();
        double latitude = selectedBusStop.getLatitude();
        Location busStopLocation = new Location("busStop");
        busStopLocation.setLatitude(latitude);
        busStopLocation.setLongitude(longitude);
        if (location != null) {
            double distance = busStopLocation.distanceTo(location);
            Log.d(TAG, "setAlarm: distance to bus stop: " + distance);
        }
        Log.d(TAG, "setAlarm: <user> latitude " + userLatitude + "  longitude " + userLongitude);
        Log.d(TAG, "setAlarm: <target> latitude " + latitude + "  longitude " + longitude);
        //getApplicationContext().sendBroadcast(intent);
        LocationHandler.getInstance().setAlightingAlarm(getApplicationContext(), latitude, longitude, pendingIntent);
        LocalDB.getInstance().setCurrentTrip(new CurrentTrip(selectedBusStop));
    }

    private void setAlightingAlarm() {
        Log.d(TAG, "setAlightingAlarm: starts");
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "setAlightingAlarm: client not connected");
            return;
        }
        try {
            Geofence.Builder builder = new Geofence.Builder()
                    .setCircularRegion(selectedBusStop.getLatitude(),
                            selectedBusStop.getLongitude(), RADIUS)
                    .setExpirationDuration(EXPIRATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setRequestId(selectedBusStop.getDescription());

            PendingIntent currentPendingIntent = getGeofencePendingIntent();
            LocalDB.getInstance().setAlightingAlarmPendingIntent(currentPendingIntent);
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                    getGeofencingRequest(builder.build()),currentPendingIntent).setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, "setAlightingAlarm: Geofencing security exception");
            e.printStackTrace();
        }
    }

    private void removeAlightingAlarm() {
        Log.d(TAG, "removeAlightingAlarm:");
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "setAlightingAlarm: client not connected");
            return;
        }
        try {
            PendingIntent currentPendingIntent = LocalDB.getInstance().getAlightingAlarmPendingIntent();
            if (currentPendingIntent != null) {
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, currentPendingIntent);
                LocalDB.getInstance().setAlightingAlarmPendingIntent(null);
            }
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(ALIGHTING_ALARM_ADDED, alightingAlertAdded);
            editor.apply();
        } catch (SecurityException e) {
            Log.e(TAG, "setAlightingAlarm: Geofencing security exception");
            e.printStackTrace();
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.

        Intent intent = new Intent(this, ProximityIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putString(ALIGHTING_BUSSTOP, selectedBusStop.getDescription());
        intent.putExtras(bundle);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, REQUEST_ALIGHTING_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getGeofencePendingIntent(String description) {
        // Reuse the PendingIntent if we already have it.

        Intent intent = new Intent(this, ProximityIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putString(ALIGHTING_BUSSTOP, description);
        intent.putExtras(bundle);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, REQUEST_ALIGHTING_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofence(geofence);

        // Return a GeofencingRequest.
        return builder.build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: ");
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            alightingAlertAdded = true;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(ALIGHTING_ALARM_ADDED, alightingAlertAdded);
            editor.putString(ALIGHTING_BUSSTOP, selectedBusStop.getDescription());
            editor.putFloat(AA_DESTINATION_LATITUDE, (float) selectedBusStop.getLatitude());
            editor.putFloat(AA_DESTINATION_LONGITUDE, (float) selectedBusStop.getLongitude());
            editor.apply();
            LocalDB.getInstance().setCurrentTrip(new CurrentTrip(selectedBusStop));
            Intent intent = new Intent(this, CurrentTripActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.alighting_alarm_failure, Toast.LENGTH_SHORT).show();
            LocalDB.getInstance().setAlightingAlarmPendingIntent(null);
            LocalDB.getInstance().setCurrentTrip(null);
        }
    }
}
