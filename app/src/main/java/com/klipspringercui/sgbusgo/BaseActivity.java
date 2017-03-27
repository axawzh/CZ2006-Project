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

    static final String BUS_GROUPS_FILENAME = "bus_group_";
    static final String BUS_SERVICES_FILENAME = "bus_services.ser";
    static final String BUS_STOPS_FILENAME = "bus_stops.ser";
    static final String BUS_STOPS_MAP_FILENAME = "bus_stops_map.ser";
    static final String BUS_ROUTES_FILENAME = "bus_route_group.ser";
    static final String FREQUENT_TRIP_FILENAME = "frequent_trip.ser";
    static final String ACTIVATED_FREQUENT_TRIP_FILENAME = "activated_frequent_trip.ser";

    static final String SEARCH_MODE = "BUS_STOP_SEARCH_MODE";
    static final String SEARCH_AID = "BUS_STOP_SEARCH_AID";
    static final int REQUEST_BUSSTOP = 1;
    static final int REQUEST_BUSSERVICE = 2;
    static final int REQUEST_BUSSTOP_B = 3;
    static final int REQUEST_SETTIME = 4;
    static final int REQUEST_ADDFREQUENTTRIP = 5;
    static final int SEARCHMODE_WITHSN = 1;
    static final int SEARCHMODE_WITHOUTSN = 2;

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
