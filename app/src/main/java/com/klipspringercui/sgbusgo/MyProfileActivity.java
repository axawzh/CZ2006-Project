package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends BaseActivity implements FragmentFrequentTripDetail.OnFragmentInteractionListener{

    private static final String TAG = "MyProfileActivity";
    private static final String TITLE = "My Profile";

    static final int LOAD_OK = 0;
    static final int LOAD_FAIL = 1;
    private int loadFlag = LOAD_FAIL;

    Button buttonAddFrequentTrip = null;
    ListView listFrequentTrip = null;
    TextView emptyListView = null;

    private ArrayList<FrequentTrip> frequentTripArrayList = new ArrayList<FrequentTrip>();
    FrequentTripListAdapter listViewAdapter;

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

        frequentTripArrayList = getSavedFrequentTripList(FREQUENT_TRIP_FILENAME);
        listViewAdapter = new FrequentTripListAdapter(this, frequentTripArrayList);
        listFrequentTrip.setAdapter(listViewAdapter);
        listFrequentTrip.setOnItemClickListener(listFrequentTripOnItemClickListener);

//        if (loadFlag == LOAD_FAIL) {
//            listFrequentTrip.setEmptyView(findViewById(R.id.emptyListView));
//        }
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
                    listViewAdapter.clear();
                    listViewAdapter.addAll(getSavedFrequentTripList(FREQUENT_TRIP_FILENAME));
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
            listViewAdapter.clear();
            listViewAdapter.addAll(getSavedFrequentTripList(FREQUENT_TRIP_FILENAME));
            //listViewAdapter.notifyDataSetChanged();
        }
        else {
            Log.d(TAG, "Data no change");
        }
    }

    public ArrayList<FrequentTrip> getSavedFrequentTripList(String fileName) {
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
            try {
                FileOutputStream fos = getApplicationContext().openFileOutput(FREQUENT_TRIP_FILENAME, MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.close();
                fos.close();
                Log.d(TAG, "New file created");
            } catch (FileNotFoundException n) {
                Log.d(TAG, "Test: FileNotFound Exception");
                n.printStackTrace();
            } catch (IOException n) {
                Log.d(TAG, "Test: IO Exception");
                n.printStackTrace();
            }
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
        listViewAdapter.clear();
        listViewAdapter.addAll(getSavedFrequentTripList(FREQUENT_TRIP_FILENAME));
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void onFragmentInteraction(FrequentTrip item) {
        
    }

}
