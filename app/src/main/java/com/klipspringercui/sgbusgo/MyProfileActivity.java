package com.klipspringercui.sgbusgo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MyProfileActivity extends BaseActivity implements FragmentFrequentTripDetail.OnFragmentInteractionListener,
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>{

    private static final String TAG = "MyProfileActivity";
    private static final String TITLE = "My Profile";

    static final int LOAD_OK = 0;
    static final int LOAD_FAIL = 1;
    private int loadFlag = LOAD_FAIL;

    private static final float RADIUS = 550.0f;
    private static final long EXPIRATION = 86400000;

    Button buttonAddFrequentTrip = null;
    ListView listFrequentTrip = null;
    TextView emptyListView = null;

    FrequentTripListAdapter listViewAdapter;
    private FrequentTrip activatedFrequentTrip;

    protected GoogleApiClient mGoogleApiClient;
    boolean activated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: starts");
        setContentView(R.layout.activity_my_profile);
        activateToolBar(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrentTrip current = LocalDB.getInstance().getCurrentTrip();
                if (current == null) {
                    Snackbar.make(view, "You haven't start a trip yet.\n Set an alighting alarm or activate a frequent trip to start one!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Toast.makeText(MyProfileActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MyProfileActivity.this, CurrentTripActivity.class);
                    startActivity(intent);
                }
            }
        });

        buttonAddFrequentTrip = (Button) findViewById(R.id.btnAddFrequentTrip);
        buttonAddFrequentTrip.setOnClickListener(addFrequentTripOnClickListenser);

        listFrequentTrip = (ListView) findViewById(R.id.listFrequentTrip);

        // initiate frequent trip file
//        Log.d(TAG, "Test: starting test");
//
//        try {
//            FileOutputStream fos = getApplicationContext().openFileOutput(FREQUENT_TRIP_FILENAME, MODE_PRIVATE);
//            ObjectOutputStream oos = new ObjectOutputStream(fos);
//            oos.close();
//            fos.close();
//            Log.d(TAG, "Test: passed");
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "Test: FileNotFound Exception");
//            e.printStackTrace();
//        } catch (IOException e) {
//            Log.d(TAG, "Test: IO Exception");
//            e.printStackTrace();
//        }

        ArrayList<FrequentTrip> frequentTripArrayList = LocalDB.getInstance().getFrequentTripsData();
        if (frequentTripArrayList == null || frequentTripArrayList.size() == 0) {
            frequentTripArrayList = getSavedFrequentTripList(FREQUENT_TRIP_FILENAME);
            LocalDB.getInstance().setFrequentTripsData(frequentTripArrayList);
        }

        listViewAdapter = new FrequentTripListAdapter(this, frequentTripArrayList);
        listFrequentTrip.setAdapter(listViewAdapter);
        listFrequentTrip.setOnItemClickListener(listFrequentTripOnItemClickListener);

//        if (loadFlag == LOAD_FAIL) {
//            listFrequentTrip.setEmptyView(findViewById(R.id.emptyListView));
//        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    Button.OnClickListener addFrequentTripOnClickListenser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MyProfileActivity.this, AddFrequentTripActivity.class);
            startActivityForResult(intent,REQUEST_ADDFREQUENTTRIP);
        }
    };

    AdapterView.OnItemClickListener listFrequentTripOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            FrequentTrip temp = (FrequentTrip) listFrequentTrip.getItemAtPosition(position);
            Log.d(TAG, "getItemAtPosition temp id: " + temp.getId());
            Log.d(TAG, "getItemAtPosition temp Alighting Bus Stop: " + temp.getAlightingBusStop().getDescription());

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            FragmentFrequentTripDetail frequentTripDetail = FragmentFrequentTripDetail.newInstance(temp);
            frequentTripDetail.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d(TAG, "dialog dismissed, update ListView");
                    listViewAdapter.loadNewData(LocalDB.getInstance().getFrequentTripsData());
                    FrequentTrip newActivated = LocalDB.getInstance().getActivatedFrequentTrip();
                    if (newActivated != null) {
                        if (activatedFrequentTrip == null) {
                            Log.d(TAG, "on dialog dismissed: adding alert");
                            activatedFrequentTrip = newActivated;
                            setETAAlarm(activatedFrequentTrip);
                        } else if (newActivated.getId() != activatedFrequentTrip.getId()) {
                            activatedFrequentTrip = newActivated;
                            setETAAlarm(activatedFrequentTrip);
                        }
                    } else if (activatedFrequentTrip !=null) {
                        removeETAAlarm();
                        activatedFrequentTrip = null;
                    }
                }
            });
            frequentTripDetail.show(ft, "dialog");
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADDFREQUENTTRIP && resultCode == RESULT_OK) {
            Log.d(TAG, "update ListView");
            listViewAdapter.loadNewData(LocalDB.getInstance().getFrequentTripsData());
            //listViewAdapter.notifyDataSetChanged();
        }
        else {
            Log.d(TAG, "Data no change");
        }
    }

    private ArrayList<FrequentTrip> getSavedFrequentTripList(String fileName) {
        ArrayList<FrequentTrip> result = new ArrayList<FrequentTrip>();
        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = openFileInput(fileName);
            ois = new ObjectInputStream(fis);
            result = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
            loadFlag = LOAD_OK;
        } catch (FileNotFoundException e) {
            // No exist file
            return new ArrayList<FrequentTrip>();
        }
        catch (EOFException e) {
            Log.d(TAG, "MyProfile: EOF Exception");
            e.printStackTrace();
        }
        catch (IOException e) {
            this.loadFlag = LOAD_FAIL;
            Log.d(TAG, "MyProfile: IO Exception");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            this.loadFlag = LOAD_FAIL;
            Log.d(TAG, "MyProfile: ClassNotFund Exception");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: starts");
        getSupportActionBar().setTitle(TITLE);
        listViewAdapter.loadNewData(LocalDB.getInstance().getFrequentTripsData());
        activatedFrequentTrip = getActivatedFrequentTrip();
        if (activatedFrequentTrip != null) {
            setETAAlarm(activatedFrequentTrip);
            activated = true;
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void onFragmentInteraction(FrequentTrip item) {
        
    }

    private void setETAAlarm(FrequentTrip activatedTrip) {
        Log.d(TAG, "setAlightingAlarm: starts");
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "setAlightingAlarm: client not connected");
            return;
        }
        try {
            BusStop startingBusStop = activatedTrip.getStartingBusStop();

            Geofence.Builder builder = new Geofence.Builder()
                    .setCircularRegion(startingBusStop.getLatitude(),
                            startingBusStop.getLongitude(), RADIUS)
                    .setExpirationDuration(EXPIRATION)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setRequestId("activated");

            PendingIntent currentPendingIntent = getGeofencePendingIntent(activatedTrip.getServiceNo(),
                    startingBusStop.getBusStopCode(), startingBusStop.getDescription());
            LocalDB.getInstance().setActivatedPendingIntent(currentPendingIntent);
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                    getGeofencingRequest(builder.build()),currentPendingIntent).setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, "setAlightingAlarm: Geofencing security exception");
            e.printStackTrace();
        }
    }

    private void removeETAAlarm() {
        Log.d(TAG, "removeAlightingAlarm:");
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "setAlightingAlarm: client not connected");
            return;
        }
        try {
            PendingIntent currentPendingIntent = LocalDB.getInstance().getActivatedPendingIntent();
            if (currentPendingIntent != null) {
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, currentPendingIntent);
                LocalDB.getInstance().setActivatedPendingIntent(null);
            }
            //SharedPreferences.Editor editor = mSharedPreferences.edit();
//            editor.putBoolean(ALIGHTING_ALARM_ADDED, alightingAlertAdded);
//            editor.apply();
        } catch (SecurityException e) {
            Log.e(TAG, "setAlightingAlarm: Geofencing security exception");
            e.printStackTrace();
        }
    }

    private PendingIntent getGeofencePendingIntent(String busServiceNo, String busStopCode, String description) {
        // Reuse the PendingIntent if we already have it.


        Intent intent = new Intent(this, ProximityIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putString(STARTING_BUSSTOP_DESCRIPTION, description);
        bundle.putString(STARTING_BUSSTOP_CODE, busStopCode);
        bundle.putString(FREQUENT_SERVICE_NO, busServiceNo);
        intent.putExtras(bundle);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, REQUEST_ACTIVATED_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        Log.d(TAG, "onConnectionFailed: ");
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Toast.makeText(this, "Successfully Activated", Toast.LENGTH_SHORT).show();
//            SharedPreferences.Editor editor = mSharedPreferences.edit();
//            editor.putBoolean(ALIGHTING_ALARM_ADDED, alightingAlertAdded);
//            editor.putString(ALIGHTING_BUSSTOP, selectedBusStop.getDescription());
//            editor.putFloat(AA_DESTINATION_LATITUDE, (float) selectedBusStop.getLatitude());
//            editor.putFloat(AA_DESTINATION_LONGITUDE, (float) selectedBusStop.getLongitude());
//            editor.apply();
            LocalDB.getInstance().setActivatedFrequentTrip(activatedFrequentTrip);
        } else {
            Toast.makeText(this, R.string.alighting_alarm_failure, Toast.LENGTH_SHORT).show();
            LocalDB.getInstance().setActivatedPendingIntent(null);
        }
    }

    private FrequentTrip getActivatedFrequentTrip() {
        FrequentTrip result = null;
        try {
            FileInputStream fis = openFileInput(ACTIVATED_FREQUENT_TRIP_FILENAME);
            ObjectInputStream ois =  new ObjectInputStream(fis);
            result = (FrequentTrip) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            Log.e(TAG, "getActivatedFT: IO Exception");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getActivatedFT: ClassNotFound Exception");
            e.printStackTrace();
        }
        return result;
    }
}
