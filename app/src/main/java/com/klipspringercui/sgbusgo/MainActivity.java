package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class MainActivity extends BaseActivity implements GetJSONBusRouteData.BusRoutesDataAvailableCallable,
                                                        GetJSONBusStopData.BusStopDataAvailableCallable,
                                                        LoadLocalData.DataLoadCallable {

    private static final String TAG = "MainActivity";
    private static final String BUS_STOPS_MAP = "BUT STOPS MAP";
    private static final String BUS_STOP_LIST = "BUT STOPS LIST";
    private static final String BUS_SERVICES_SET = "BUS SERVICES SET";
    private static final String BUS_SERVICES_LIST = "BUS SERVICES LIST";


    private HashMap<String, BusStop> busStopsMap = null;
    private ArrayList<BusStop> busStopsList = null;
    private HashSet<String> busServicesSet = null;
    private ArrayList<String> busServicesList = null;

    private boolean force = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activateToolBar(false);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button ETA_button = (Button) findViewById(R.id.btnETA);
        ETA_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ETAActivity.class);
                startActivity(intent);
            }
        });

        Button AlightingAlarm_button = (Button) findViewById(R.id.btnAlightingAlarm);
        AlightingAlarm_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AlightingAlarmActivity.class);
                startActivity(intent);
            }
        });

        Button FareCalculator_button = (Button) findViewById(R.id.btnFareCalculator);
        FareCalculator_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FareCalculatorActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (force) {
            Toast.makeText(this, "Downloading Bus Stop Data", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onResume: Downloading Bus Stop Data");
            GetJSONBusStopData getJSONBusStopData = new GetJSONBusStopData(this, BUS_STOPS_URL);
            getJSONBusStopData.execute();
            Toast.makeText(this, "Downloading Bus Routes Data", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onResume: Downloading Bus Route Data");
            GetJSONBusRouteData getJSONData = new GetJSONBusRouteData(this, BUS_ROUTES_URL);
            getJSONData.execute();
            force = false;
            return;
        }
        
        this.busServicesList = BusServicesListHolder.getInstance().getData();
        this.busStopsList = BusStopsListHolder.getInstance().getData();

        if (this.busServicesList.size() != 0 && this.busStopsList.size() != 0)  {
            Log.d(TAG, "onResume: data restored with size " + this.busStopsList.size() + " and " + this.busServicesList.size());
            return;
        }

        Toast.makeText(this, "Loading data", Toast.LENGTH_SHORT).show();

        LoadLocalData loadLocalData = new LoadLocalData(getApplicationContext(), this);
        loadLocalData.execute();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBusRouteDataAvailable(List<BusRoute> data, DownloadStatus status) {
        Log.d(TAG, "onBusRouteDataAvailable: data - " + data);
        ArrayList<BusGroup> busGroups = new ArrayList<BusGroup>();
        this.busServicesList = new ArrayList<String>();
        String currentServiceNo = null;
        ArrayList<BusStop> currentBusStops = null;

        if (this.busStopsMap == null || this.busStopsMap.size() == 0) {
            try {
                Log.d(TAG, "onBusRouteDataAvailable: recovering stored data");
                FileInputStream fis = getApplicationContext().openFileInput(BUS_STOPS_MAP_FILENAME);
                ObjectInputStream ois = new ObjectInputStream(fis);
                this.busStopsMap = (HashMap) ois.readObject();
                Log.d(TAG, "onBusRouteDataAvailable: successfully recovered stored map data");
                ois.close();
                fis.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onBusRouteDataAvailable: File not found -- loading");
                GetJSONBusStopData getJSONData = new GetJSONBusStopData(this, BUS_STOPS_URL);
                getJSONData.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        for (int i = 0; i < data.size(); i++) {
            BusRoute currentRoute= data.get(i);
            if (currentServiceNo == null) {
                currentServiceNo = currentRoute.getServiceNo();
                currentBusStops = new ArrayList<BusStop>();
            }
            if (!currentRoute.getServiceNo().equalsIgnoreCase(currentServiceNo)) {
                BusGroup busServiceGroup = new BusGroup(currentServiceNo, currentBusStops);
                this.busServicesList.add(currentServiceNo);
                busGroups.add(busServiceGroup);
                currentServiceNo = currentRoute.getServiceNo();
                currentBusStops = new ArrayList<BusStop>();
            }
            if (currentRoute.getServiceNo().equalsIgnoreCase(currentServiceNo)) {
                BusStop currentBusStop = this.busStopsMap.get(currentRoute.getBusStopCode());
                if (currentBusStop == null)
                    continue;
                try {
                    BusStop busStop = new BusStop(currentBusStop, currentRoute.getDistance());
                    currentBusStops.add(busStop);
                } catch (NullPointerException e ) {
                    Log.e(TAG, "onBusRouteDataAvailable: null pointer ");
                }
            }
        }


        /**
         * Writing grouped bus data
         */
        Log.d(TAG, "onBusRouteDataAvailable: writing group data");
        for (int i = 0; i < busGroups.size(); i++) {
            BusGroup busServiceGroup = busGroups.get(i);
            String filename = BUS_GROUPS_FILENAME + busServiceGroup.getServiceNo() + ".ser";
            try {

                FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(busServiceGroup);
                oos.close();
                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "onBusRouteDataAvailable: writing group data finished: service groups size: " + busGroups.size());


        /**
         * Writing services as list
         */
        try {
            Log.d(TAG, "onBusRouteDataAvailable: writing all service no data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_SERVICES_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busServicesList);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusRouteDataAvailable: writing all service data finished service nos size: " + busServicesList.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BusServicesListHolder.getInstance().setData(busServicesList);
        BusStopsListHolder.getInstance().setData(busStopsList);

    }

    @Override
    public void onDataLoaded(int flag) {
        if (flag == LoadLocalData.LOAD_OK) {
            Log.d(TAG, "onDataLoaded: Local Cached Data Successful Loaded");
            Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "onDataLoaded: Local Cached Data Load Failure");
            Toast.makeText(this, "Downloading Bus Stop Data", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onResume: Downloading Bus Stop Data");
            GetJSONBusStopData getJSONBusStopData = new GetJSONBusStopData(this, BUS_STOPS_URL);
            getJSONBusStopData.execute();
            Toast.makeText(this, "Downloading Bus Routes Data", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onResume: Downloading Bus Route Data");
            GetJSONBusRouteData getJSONData = new GetJSONBusRouteData(this, BUS_ROUTES_URL);
            getJSONData.execute();
        }
    }

    @Override
    public void onBusStopDataAvailable(List<BusStop> data, DownloadStatus status) {
        Log.d(TAG, "onDataAvailable: " + data);
        this.busStopsList = (ArrayList) data;
        try {
            Log.d(TAG, "onBusStopDataAvailable: writing data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_STOPS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusStopDataAvailable: writing data finished");
            Log.d(TAG, "onBusStopDataAvailable: data size: " + data.size());
        } catch (FileNotFoundException e) {
            Log.e(TAG, "onBusStopDataAvailable: File Not Found - BusStopsList");
        } catch (IOException e) {
            e.printStackTrace();
        }

        busStopsMap = new HashMap<String, BusStop>();
        for (int i = 0; i < data.size(); i++) {
            BusStop busStop = data.get(i);
            busStopsMap.put(busStop.getBusStopCode(), busStop);
        }
        try {
            Log.d(TAG, "onBusStopDataAvailable: map writing data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_STOPS_MAP_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busStopsMap);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusStopDataAvailable: map writing data finished");
            Log.d(TAG, "onBusStopDataAvailable: Map size: " + busStopsMap.size());
        } catch (FileNotFoundException e) {
            Log.d(TAG, "onBusStopDataAvailable: File Not Found - BusStopsMap ");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
