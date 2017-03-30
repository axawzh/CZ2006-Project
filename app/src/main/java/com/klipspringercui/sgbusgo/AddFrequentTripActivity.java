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

    private TimePicker timePicker;
    private int pickerHour = 0;
    private int pickerMin = 0;
    static final int TIME_PICKER_DIALOG_ID = 1;

    private ArrayList<FrequentTrip> frequentTripArrayList;

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

        buttonSelectBusService = (Button) findViewById(R.id.btnFTSelectBusService);
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
                    textSetTime.setText(new StringBuilder().append(pad(pickerHour))
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
                Toast.makeText(AddFrequentTripActivity.this, "Please Set Time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStartingBusStop == null || selectedAlightingBusStop == null) {
                Toast.makeText(AddFrequentTripActivity.this, "Please Select Starting/Alighting Bus Stop", Toast.LENGTH_SHORT).show();
                return;
            }
            FrequentTrip ft = new FrequentTrip(selectedStartingBusStop, selectedAlightingBusStop, selectedBusService, pickerHour, pickerMin,(int) System.currentTimeMillis());

            try {
                // Save all frequent trips into one ArrayList, and save this ArrayList
                // into local file
                Log.d(TAG, "AddFrequentTrip: writing data");

                // Read from file to check if there are existing saved trips
                // If yes, append the new trip to the arraylist and write back
                // If no, create a new arraylist and save to file
                FileInputStream fis = getApplicationContext().openFileInput(FREQUENT_TRIP_FILENAME);
                ObjectInputStream ois = new ObjectInputStream(fis);
                frequentTripArrayList = (ArrayList) ois.readObject();
                ois.close();
                fis.close();
                frequentTripArrayList.add(ft);
                LocalDB.getInstance().setFrequentTripsData(frequentTripArrayList);
                FileOutputStream fos = getApplicationContext().openFileOutput(FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(frequentTripArrayList);
                oos.close();
                fos.close();
                setResult(RESULT_OK);
                finish();
            } catch (EOFException e){
                // If no frequent trip record. i.e. no saved file.
                Log.d(TAG, "AddFrequentTrip: EOF Exception, writing new entry");
                try {
                    FileOutputStream fos = getApplicationContext().openFileOutput(FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    ArrayList<FrequentTrip> temp = new ArrayList<FrequentTrip>();
                    temp.add(ft);
                    oos.writeObject(temp);
                    Log.d(TAG, "AddFrequentTrip: EOF Exception, writing ft: " + ft.getClass());
                    oos.close();
                    fos.close();
                    setResult(RESULT_OK);
                    Log.d(TAG, "AddFrequentTrip: EOF Exception -> RESULT_OK");
                    finish();
                } catch (IOException n) {
                    Log.d(TAG, "AddFrequentTrip: IO Exception");
                    n.printStackTrace();
                    setResult(RESULT_CANCELED);
                }
            } catch (FileNotFoundException e) {
                Log.d(TAG, "AddFrequentTrip: FileNotFound Exception");
                e.printStackTrace();
                setResult(RESULT_CANCELED);
            } catch (IOException e) {
                Log.d(TAG, "AddFrequentTrip: IO Exception");
                e.printStackTrace();
                setResult(RESULT_CANCELED);
            } catch (ClassNotFoundException e) {
                Log.d(TAG, "AddFrequentTrip: ClassNotFoundException");
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
                        textSelectStartingBusStop.setText(selectedStartingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSTOP_B:
                        selectedAlightingBusStop = (BusStop) bundle.getSerializable(FT_SELECTED_BUSSTOP);
                        textSelectAlightingBusStop.setText(selectedAlightingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSERVICE:
                        selectedBusService = bundle.getString(FT_SELECTED_BUSSERVICENO);
                        textSelectBusService.setText(selectedBusService);
                        selectedStartingBusStop = null;
                        selectedAlightingBusStop = null;
                        btnBusStopEnabled = true;
                        break;
                    default:
                        break;
                }
            }
        }
    }







}
