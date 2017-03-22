package com.klipspringercui.sgbusgo;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.widget.Toast;
import android.widget.TimePicker;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;

public class AddFrequentTripActivity extends BaseActivity {

    //TODO: add dialog to prompt user to complete form if he/she did not do so
    //before clicking SaveFrequentTrip button

    private static final String TAG = "AddFrequentTripActivity";

    static final String FT_SELECTED_STARTINGBUSSTOP = "FT SELECTED STARTING BUS STOP";
    static final String FT_SELECTED_ALIGHTINGBUSSTOP = "FT SELECTED ALIGHTING BUS STOP";
    static final String FT_SELECTED_BUSSERVICENO = "FT SELECTED BUS SERVICE NO";
    static final String FT_SELECTED_TIME = "FT SELECTED TIME";

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

    private BusStop selectedStartingBusStop = null;
    private BusStop selectedAlightingBusStop = null;
    private String selectedBusService = null;

    boolean btnBusStopEnabled = false;
    boolean btnSaveTripEnabled = false;

    private int pickerHour = 0;
    private int pickerMin = 0;

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
        buttonSelectStartingBusStop.setOnClickListener(busStopOnClickListener);

        buttonSelectAlightingBusStop = (Button) findViewById(R.id.btnFTSelectAlightingBusStop);
        textSelectAlightingBusStop = (TextView) findViewById(R.id.txtFTAlightingSelectedBusStop);
        buttonSelectAlightingBusStop.setOnClickListener(busStopOnClickListener);

        buttonSelectAlightingBusStop = (Button) findViewById(R.id.btnFTSelectBusService);
        textSelectBusService = (TextView) findViewById(R.id.txtFTSelectedBusService);
        buttonSelectBusService.setOnClickListener(busServiceOnClickListener);

        buttonSetTime = (Button) findViewById(R.id.btnFTSetTime);
        textSetTime = (TextView) findViewById(R.id.txtFTSetTime);
        buttonSetTime.setOnClickListener(SetTimeOnClickListener);

        buttonSaveFrequentTrip = (Button) findViewById(R.id.btnSaveTrip);
        buttonSaveFrequentTrip.setOnClickListener(SaveTripOnClickListener);
    }

    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedBusService == null || !btnBusStopEnabled) {
                Toast.makeText(AddFrequentTripActivity.this, "Please Select a Bus Service First", Toast.LENGTH_SHORT).show();
                return;
            }
            int id = v.getId();
            Intent intent = new Intent(AddFrequentTripActivity.this, BusStopSelectionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(SEARCH_MODE, SEARCHMODE_WITHSN);
            bundle.putString(SEARCH_AID, AddFrequentTripActivity.this.selectedBusService);
            intent.putExtras(bundle);
            if (id == R.id.btnFTSelectStartingBusStop) {
                startActivityForResult(intent, REQUEST_BUSSTOP);
            } else {
                startActivityForResult(intent, REQUEST_BUSSTOP_B);
            }
        }
    };


    Button.OnClickListener busServiceOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddFrequentTripActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSERVICE);
        }
    };

    Button.OnClickListener SetTimeOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog tp = new TimePickerDialog(AddFrequentTripActivity.this, new TimePickerDialog.OnTimeSetListener(){
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    pickerHour = hourOfDay;
                    pickerMin = minute;
                }
            }, hour, minute, false);

            textSetTime.setText("Start Trip at " + pickerHour + ":" + pickerMin);

            btnSaveTripEnabled = true;
        }
    };

    Button.OnClickListener SaveTripOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedBusService == null || !btnSaveTripEnabled ) {
                Toast.makeText(AddFrequentTripActivity.this, "Please Set Time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStartingBusStop == null || selectedAlightingBusStop == null) {
                Toast.makeText(AddFrequentTripActivity.this, "Please Select Starting/Alighting Bus Stop", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Log.d(TAG, "AddFrequentTrip: writing data");
                FileOutputStream fos = getApplicationContext().openFileOutput(FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(data);
                oos.close();
                fos.close();
                Log.d(TAG, "onBusStopDataAvailable: writing data finished");
                Log.d(TAG, "onBusStopDataAvailable: data size: " + data.size());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(AddFrequentTripActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSERVICE);
        }
    };



}
