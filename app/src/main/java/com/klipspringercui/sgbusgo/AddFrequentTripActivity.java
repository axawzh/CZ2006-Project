package com.klipspringercui.sgbusgo;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.widget.Toast;
import android.widget.TimePicker;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;


public class AddFrequentTripActivity extends BaseActivity {

    //TODO: add dialog to prompt user to complete form if he/she did not do so
    //before clicking SaveFrequentTrip button

    private static final String TAG = "AddFrequentTripActivity";

    static final String FT_SELECTED_BUSSTOP = "FT SELECTED BUS STOP";
    static final String FT_SELECTED_ALIGHTINGBUSSTOP = "FT SELECTED ALIGHTING BUS STOP";
    static final String FT_SELECTED_BUSSERVICENO = "FT SELECTED BUS SERVICE NO";
    static final String FT_SELECTED_TIME = "FT SELECTED TIME";

    Button buttonSelectStartingBusStop = null;
    Button buttonSelectAlightingBusStop = null;
    Button buttonSelectBusService = null;
    Button buttonSetTime = null;
    Button buttonSaveFrequentTrip = null;

    private long btnLastClickTime = 0;

    private BusStop selectedStartingBusStop = null;
    private BusStop selectedAlightingBusStop = null;
    private String selectedBusService = null;

    boolean btnBusStopEnabled = false;
    boolean btnSaveTripEnabled = false;

    private TimePicker timePicker;
    private int pickerHour = 0;
    private int pickerMin = 0;
    static final int TIME_PICKER_DIALOG_ID = 1;

    private ArrayList<FrequentTrip> frequentTripArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_frequent_trip);

        activateToolBar(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrentTrip current = LocalDB.getInstance().getCurrentTrip();
                if (current == null) {
                    Snackbar.make(view, "You haven't start a trip yet.\n Set an alighting alarm or activate a frequent trip to start one!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Toast.makeText(AddFrequentTripActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddFrequentTripActivity.this, CurrentTripActivity.class);
                    startActivity(intent);
                }
            }
        });

        buttonSelectStartingBusStop = (Button) findViewById(R.id.btnFTSelectStartingBusStop);
        buttonSelectStartingBusStop.setOnClickListener(busStopOnClickListener);

        buttonSelectAlightingBusStop = (Button) findViewById(R.id.btnFTSelectAlightingBusStop);
        buttonSelectAlightingBusStop.setOnClickListener(busStopOnClickListener);

        buttonSelectBusService = (Button) findViewById(R.id.btnFTSelectBusService);
        buttonSelectBusService.setOnClickListener(busServiceOnClickListener);

        buttonSetTime = (Button) findViewById(R.id.btnFTSetTime);
        buttonSetTime.setOnClickListener(SetTimeOnClickListener);

        buttonSaveFrequentTrip = (Button) findViewById(R.id.btnSaveTrip);
        buttonSaveFrequentTrip.setOnClickListener(SaveTripOnClickListener);

        frequentTripArrayList = LocalDB.getInstance().getFrequentTripsData();
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
                selectedStartingBusStop = null;
                buttonSelectStartingBusStop.setText(R.string.select_start_bus_stop);
                startActivityForResult(intent, REQUEST_BUSSTOP);
            } else {
                selectedAlightingBusStop = null;
                buttonSelectAlightingBusStop.setText(R.string.select_alight_bus_stop);
                startActivityForResult(intent, REQUEST_BUSSTOP_B);
            }
        }
    };

    Button.OnClickListener busServiceOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectedBusService = null;
            buttonSelectBusService.setText(R.string.bus_service_no);
            Intent intent = new Intent(AddFrequentTripActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSERVICE);
        }
    };

    Button.OnClickListener SetTimeOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            showDialog(TIME_PICKER_DIALOG_ID);
            Calendar c = Calendar.getInstance();
            pickerHour = c.get(Calendar.HOUR_OF_DAY);
            pickerMin = c.get(Calendar.MINUTE);

            btnSaveTripEnabled = true;
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_PICKER_DIALOG_ID:
                // set time picker as current time
                return new TimePickerDialog(this,
                        timePickerListener, pickerHour, pickerMin, false);

        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int selectedHour,
                                      int selectedMinute) {
                    pickerHour = selectedHour;
                    pickerMin = selectedMinute;

                    // set current time into textview
                    buttonSetTime.setText(new StringBuilder().append(pad(pickerHour))
                            .append(":").append(pad(pickerMin)));
                }
    };

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    };

    Button.OnClickListener SaveTripOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedBusService == null || !btnSaveTripEnabled ) {
                Toast.makeText(AddFrequentTripActivity.this, "Please Fill in All Fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStartingBusStop == null || selectedAlightingBusStop == null) {
                Toast.makeText(AddFrequentTripActivity.this, "Please Select Starting/Alighting Bus Stop", Toast.LENGTH_SHORT).show();
                return;
            }
            FrequentTrip ft = new FrequentTrip(selectedStartingBusStop, selectedAlightingBusStop, selectedBusService, pickerHour, pickerMin,(int) System.currentTimeMillis());

            frequentTripArrayList.add(ft);

            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(frequentTripArrayList);
                LocalDB.getInstance().setFrequentTripsData(frequentTripArrayList);
                oos.close();
                fos.close();
                setResult(RESULT_OK);
                finish();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "AddFrequentTrip: FileNotFound Exception");
                e.printStackTrace();
                setResult(RESULT_CANCELED);
            } catch (IOException e) {
                Log.d(TAG, "AddFrequentTrip: IO Exception");
                e.printStackTrace();
                setResult(RESULT_CANCELED);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                switch(requestCode) {
                    case REQUEST_BUSSTOP:
                        selectedStartingBusStop = (BusStop) bundle.getSerializable(FT_SELECTED_BUSSTOP);
                        buttonSelectStartingBusStop.setText(selectedStartingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSTOP_B:
                        selectedAlightingBusStop = (BusStop) bundle.getSerializable(FT_SELECTED_BUSSTOP);
                        buttonSelectAlightingBusStop.setText(selectedAlightingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSERVICE:
                        selectedBusService = bundle.getString(FT_SELECTED_BUSSERVICENO);
                        buttonSelectBusService.setText(selectedBusService);
                        selectedStartingBusStop = null;
                        buttonSelectStartingBusStop.setText(R.string.select_start_bus_stop);
                        selectedAlightingBusStop = null;
                        buttonSelectAlightingBusStop.setText(R.string.select_alight_bus_stop);
                        btnBusStopEnabled = true;
                        break;
                    default:
                        break;
                }
            }
        }
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
