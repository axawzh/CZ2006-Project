package com.klipspringercui.sgbusgo;

import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

    static final String BUS_ROUTES_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusRoutes";
    static final String BUS_STOPS_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusStops";
    static final String ETA_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusArrival";
    static final String FARE_URL = "https://data.gov.sg/api/action/datastore_search?resource_id=d9b3b8ec-ac41-41f1-b76f-70396125774d&limit=50";

    static final String GOOGLE_MAP_API_KEY = "AIzaSyAAtPkVPCpI8Wpy-656eOc6mNAozejLOFw";

    static final String BUS_GROUPS_FILENAME = "bus_group_";
    static final String BUS_SERVICES_FILENAME = "bus_services.ser";
    static final String BUS_STOPS_FILENAME = "bus_stops.ser";
    static final String BUS_STOPS_MAP_FILENAME = "bus_stops_map.ser";
    static final String BUS_ROUTES_FILENAME = "bus_route_group.ser";
    static final String FREQUENT_TRIP_FILENAME = "frequent_trip.ser";
    static final String ACTIVATED_FREQUENT_TRIP_FILENAME = "activated_frequent_trip.ser";

    static final String SEARCH_MODE = "BUS_STOP_SEARCH_MODE";
    static final String SEARCH_AID = "BUS_STOP_SEARCH_AID";

    static final String SHARED_PREFERENCE_NAME = "SHARED_PREFERENCE";

    // For alighting alarm
    static final String ALIGHTING_ALARM_ADDED = "ALIGHTING ALARM ADDED";
    static final String ALIGHTING_BUSSTOP = "ARRIVAL_BUSSTOP_DESCRIPTION";
    static final String AA_DESTINATION_LATITUDE = "DESTINATION_LATITUDE";
    static final String AA_DESTINATION_LONGITUDE = "DESTINATION_LONGITUDE";
    static final String AA_FROM_NOTIFICATION = "FROM_NOTIFICATION";

    // For activated frequent trip
    static final String ACTIVATED_ALARM_ADDED = "ACTIVATED_ALARM_ADDED";
    static final String STARTING_BUSSTOP_DESCRIPTION = "STARTING_BUS_STOP_DESCEIPTION";
    static final String STARTING_BUSSTOP_CODE = "STARTING_BUS_STOP_CODE";
    static final String FREQUENT_SERVICE_NO = "FREQUENT_SERVICE_NO";


    // For startActivityForResult
    static final int REQUEST_BUSSTOP = 1;
    static final int REQUEST_BUSSERVICE = 2;
    static final int REQUEST_BUSSTOP_B = 3;
    static final int REQUEST_SETTIME = 4;
    static final int REQUEST_ADDFREQUENTTRIP = 5;
    // For BusStopSelectionActivity
    static final int SEARCHMODE_WITHSN = 1;
    static final int SEARCHMODE_WITHOUTSN = 2;
    // For pending intent
    static final int REQUEST_ALIGHTING_ALARM = 1;
    static final int REQUEST_ACTIVATED_ALARM = 2;

    AlertDialog connectionDialog = null;

    ConnectivityManager cm = null;
    boolean isConnected = false;

    void activateToolBar(boolean enableHome) {
        //Log.d(TAG, "activeToolBar: starts");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                actionBar = getSupportActionBar();
            }
        }
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enableHome);
        }
    }

    void activateToolBar(boolean enableHome, int title_id) {
        //Log.d(TAG, "activeToolBar: starts");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                actionBar = getSupportActionBar();
                actionBar.setTitle(title_id);
            }
        }
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enableHome);
            actionBar.setTitle(title_id);
        }
    }
    protected void showConnectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("No internet connection");
        builder.setMessage("Please turn on your Wi-Fi or Mobile Data");
        connectionDialog = builder.create();
        connectionDialog.show();
    }

    protected void dismissConnectionDialog() {
        if (connectionDialog != null && connectionDialog.isShowing())
            connectionDialog.dismiss();
    }

}
