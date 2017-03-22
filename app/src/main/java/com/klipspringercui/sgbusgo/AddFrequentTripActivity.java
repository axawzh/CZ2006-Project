package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddFrequentTripActivity extends BaseActivity {

    //TODO: add dialog to prompt user to complete form if he/she did not do so
    //before clicking SaveFrequentTrip button

    Button buttonSelectStartingBusStop = null;
    Button buttonSelectAlightingBusStop = null;
    Button buttonSelectBusService = null;
    Button buttonSetTime = null;
    Button buttonSaveFrequentTrip = null;

    TextView textSelectStartingBusStop = null;
    TextView textSelectAlightingBusStop = null;
    TextView textSelectBusService = null;
    TextView textSetTime = null;

    private long btnLastClickTime = 0;

    private boolean selectedStartingBusStop = false;
    private boolean selectedAlightinggBusStop = false;
    private boolean selectedBusService = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_frequent_trip);
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

        buttonSelectStartingBusStop = (Button) findViewById(R.id.btnFTSelectStartingBusStop);
        textSelectStartingBusStop = (TextView) findViewById(R.id.txtFTStartingSelectedBusStop);
        buttonSelectStartingBusStop.setOnClickListener(startingBusStopOnClickListener);

        buttonSelectAlightingBusStop = (Button) findViewById(R.id.btnFTSelectAlightingBusStop);
        textSelectAlightingBusStop = (TextView) findViewById(R.id.txtFTAlightingSelectedBusStop);
        buttonSelectAlightingBusStop.setOnClickListener(alightingBusStopOnClickListener);

        buttonSelectAlightingBusStop = (Button) findViewById(R.id.btnFTSelectBusService);
        textSelectBusService = (TextView) findViewById(R.id.txtFTSelectedBusService);
        buttonSelectBusService.setOnClickListener(busServiceOnClickListener);

        buttonSetTime = (Button) findViewById(R.id.btnFTSetTime);
        textSetTime = (TextView) findViewById(R.id.txtFTSetTime);
        buttonSetTime.setOnClickListener(SetTimeOnClickListener);

        buttonSaveFrequentTrip = (Button) findViewById(R.id.btnSaveTrip);
        buttonSaveFrequentTrip.setOnClickListener(SaveTripOnClickListener);
    }

    Button.OnClickListener startingBusStopOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddFrequentTripActivity.this, BusStopSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSTOP);
        }
    };

    Button.OnClickListener alightingBusStopOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddFrequentTripActivity.this, BusStopSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSTOP_B);
        }
    };

    Button.OnClickListener busServiceOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddFrequentTripActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_SETTIME);
        }
    };





}
