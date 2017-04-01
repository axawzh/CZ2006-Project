package com.klipspringercui.sgbusgo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Kevin on 29/3/17.
 */


import com.google.android.gms.common.ConnectionResult;

import java.util.List;

class LocationHandler {

    private static final String TAG = "LocationHandler";
    private LocationManager locationManager;
    //private static final int MIN_UPDATE_INT = 100;
    //private static final int MIN_UPDATE_CHANGE = 100;
    private static final float RADIUS = 10.0f;
    private static final long EXPIRATION = 3600000;

    private static LocationHandler handler = new LocationHandler();
    private PendingIntent currentPending = null;





    public static LocationHandler getInstance() {
        return handler;
    }

    public boolean setLocationManager(Context context) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (this.locationManager == null)
            return false;
        else
            return true;
    }

    public boolean setAlightingAlarm(Context context, double latitude, double longitude, PendingIntent pendingIntent) {

        if (locationManager == null) {
            Log.e(TAG, "setAlightingAlarm: location manager is null");
            return false;
        }
        //Remove the previous alighting alarm
        if (currentPending != null) {
            cancelAlightingAlarm(context, currentPending);
            currentPending = null;
        }
        //check permission for API level 23+
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "This function needs location permission to work properly", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "setAlightingAlarm: permission not granted");
            return false;
        } else {
            Log.d(TAG, "setAlightingAlarm: setting alarm");
            locationManager.addProximityAlert(latitude,longitude,RADIUS,EXPIRATION,pendingIntent);
            currentPending = pendingIntent;
            return true;
        }

    }

    public Location getUserLocation(Context context) {

        if (locationManager == null) {
            Log.e(TAG, "getUserLocation: location manager is null");
            return null;
        }

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "This function needs location permission to work properly", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getUserLocation: permission not granted");
            return null;
        }

        Location location = null;
        if (isGPSEnabled) {
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_INT, MIN_UPDATE_CHANGE, );
            Log.d(TAG, "getUserLocation: gps return");
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location == null && isNetworkEnabled) {
            Log.d(TAG, "getUserLocation: network return");
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;

    }

    public boolean cancelAlightingAlarm(Context context, PendingIntent pendingIntent) {
        Log.d(TAG, "cancelAlightingAlarm: starts");
        if (locationManager == null) {
            Log.e(TAG, "setAlightingAlarm: location manager is null");
            return false;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "This function needs location permission to work properly", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getUserLocation: permission not granted");
            return false;
        }
        pendingIntent.cancel();
        locationManager.removeProximityAlert(pendingIntent);
        return true;
    }

    public boolean cancelAlightingAlarm(Context context) {
        Log.d(TAG, "cancelAlightingAlarm: starts");
        if (locationManager == null || currentPending == null) {
            Log.d(TAG, "setAlightingAlarm: null");
            return false;
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "This function needs location permission to work properly", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "getUserLocation: permission not granted");
                return false;
            }
            locationManager.removeProximityAlert(currentPending);
            return true;
        }
    }


}
