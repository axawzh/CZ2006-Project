package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.FragmentTransaction;
import android.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ETAActivity extends BaseActivity implements GetJSONETAData.ETADataAvailableCallable,
                                                                DialogDisplayETA.OnFragmentInteractionListener {

    private static final String TAG = "ETAActivity";
    static final String ETA_SEARCH_MODE = "ETA SEARCH MODE";
    static final String ETA_SEARCH_BUSSERVICENO = "ETA SEARCH BUS SERVICE NO";
    static final String ETA_SEARCH_BUSSTOPNO = "ETA SEARCH BUS STOP NO";
    static final String ETA_SELECTED_BUSSTOP = "ETA SELECTED BUS STOP";
    static final String ETA_SELECTED_BUSSERVICENO = "ETA SELECTED BUS SERVICE NO";


    private BusStop selectedBusStop = null;
    private String selectedBusService = null;

    Button buttonSelectBusStop = null;
    TextView textSelectedBusStop = null;
    Button buttonSelectBusService = null;
    TextView textSelectedBusService = null;
    Button buttonGetETA = null;
    ConstraintSet mConstraintSet = null;

    RecyclerView recyclerViewFreq = null;
    ETADRecyclerViewAdapter recyclerViewAdapter;

    private ArrayList<ETAItem> frequentTripETAs = new ArrayList<ETAItem>();
    private int etaObtained;

    private long btnLastClickTime = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eta);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        textSelectedBusStop = (TextView) findViewById(R.id.txtSelectedBusStop);
        buttonSelectBusStop = (Button) findViewById(R.id.btnSelectBusStop);
        buttonSelectBusStop.setOnClickListener(busStopOnClickListener);

        textSelectedBusService = (TextView) findViewById(R.id.txtETASelectedBusService);
        buttonSelectBusService = (Button) findViewById(R.id.btnETASelectBusService);
        buttonSelectBusService.setOnClickListener(busServiceOnClickListener);

        buttonGetETA = (Button) findViewById(R.id.btnGetETA);
        buttonGetETA.setOnClickListener(getETAOnClickListener);

        recyclerViewFreq = (RecyclerView) findViewById(R.id.recyclerView_ETAFreq);
        recyclerViewFreq.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new ETADRecyclerViewAdapter(this.frequentTripETAs, null);
        recyclerViewFreq.setAdapter(recyclerViewAdapter);

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.content_eta);
        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(layout);

        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    }


    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ETAActivity.this, BusStopSelectionActivity.class);
            String serviceNo = ETAActivity.this.selectedBusService;
            Bundle bundle = new Bundle();
            if (serviceNo != null && serviceNo.length() > 0) {
                bundle.putInt(SEARCH_MODE, SEARCHMODE_WITHSN);
                bundle.putString(SEARCH_AID, serviceNo);
            } else {
                bundle.putInt(SEARCH_MODE, SEARCHMODE_WITHOUTSN);
            }
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_BUSSTOP);
        }
    };

    Button.OnClickListener busServiceOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ETAActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSERVICE);
        }
    };

    Button.OnClickListener getETAOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (selectedBusStop == null) {
                Toast.makeText(ETAActivity.this, "Please select a bus stop (and optionally a bus service no)", Toast.LENGTH_SHORT).show();
                return;
            }
            if (SystemClock.elapsedRealtime() - btnLastClickTime < 500){
                return;
            }
            btnLastClickTime = SystemClock.elapsedRealtime();
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                showConnectionDialog();
                return;
            }
            showETADialog(selectedBusStop, selectedBusService);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<FrequentTrip> frequentTrips = LocalDB.getInstance().getFrequentTripsData();
        etaObtained = frequentTrips.size();
        frequentTripETAs.clear();
//        if (frequentTrips.size() <= 1) {
//            mConstraintSet.setGuidelineBegin(R.id.guidelineETA,160);
//        } else {
//            mConstraintSet.setGuidelineBegin(R.id.guidelineETA,240);
//        }
        for (FrequentTrip trip : frequentTrips) {
            String busStopCode = trip.getStartingBusStop().getBusStopCode();
            String busServiceNo = trip.getServiceNo();
            GetJSONETAData getJSONETAData = new GetJSONETAData(this, ETA_URL);
            getJSONETAData.execute(busStopCode, busServiceNo);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request Code" + requestCode);
        if (requestCode == REQUEST_BUSSTOP && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Log.d(TAG, "onActivityResult: bus stop data received");
            if (bundle != null) {
                selectedBusStop = (BusStop) bundle.getSerializable(ETA_SELECTED_BUSSTOP);
                textSelectedBusStop.setText(selectedBusStop.getDescription());
            }
        } else if (requestCode == REQUEST_BUSSERVICE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                selectedBusService = bundle.getString(ETA_SELECTED_BUSSERVICENO);
                textSelectedBusService.setText(selectedBusService);
                selectedBusStop = null;
                textSelectedBusStop.setText("Selected Bus Stop");
            }
        }

    }

    @Override
    public void onETADataAvailable(List<ETAItem> data, String serviceNo, String busStopCode) {
        Log.d(TAG, "onETADataAvailable: called with data - " + data);
        if (data == null || data.size() == 0) {
            Log.e(TAG, "onETADataAvailable: data not available");
            Toast.makeText(this, "Data of this bus service is currently not available", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etaObtained > 0) {
            this.frequentTripETAs.addAll(data);
            etaObtained -= data.size();
        }
        if (etaObtained == 0) {
            recyclerViewAdapter.loadNewData(frequentTripETAs, null);
        }

    }

//    void showETADialog(BusStop busStop, List<ETAItem> data) {
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        DialogFragment etaDialog = DialogDisplayETA.newInstance(data, busStop);
//        etaDialog.show(ft, "dialog");
//    }

    void showETADialog(BusStop busStop, String busServiceNo) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment etaDialog = DialogDisplayETA.newInstance(busStop, busServiceNo);
        etaDialog.show(ft, "dialog");
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissConnectionDialog();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
