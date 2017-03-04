package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

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

import static com.klipspringercui.sgbusgo.BusStopSelectionActivity.BUS_STOPS_FILENAME;
import static com.klipspringercui.sgbusgo.BusStopSelectionActivity.BUS_STOPS_MAP_FILENAME;

public class MainActivity extends AppCompatActivity implements GetJSONBusRouteData.BusRoutesDataAvailableCallable {

    private static final String TAG = "MainActivity";
    static final String BUS_ROUTES_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusRoutes";
    static final String BUS_STOPS_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusStops";
    static final String BUS_SERVICE_GROUPS_FILENAME = "bus_services_";
    static final String BUS_SERVICE_NOS_FILENAME = "bus_service_nos.ser";


    private HashMap<String, BusStop> busStopsMap;
    private HashSet<String> busServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        AlightingAlarm_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FareCalculatorActivity.class);
                startActivity(intent);
            }
        });

//        GetJSONBusRouteData getJSONBusRouteData = new GetJSONBusRouteData(this, BUS_ROUTES_URL);
//        getJSONBusRouteData.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = BUS_ROUTES_URL;
//        if (searchMode == SEARCH_BUS_NO && searchBusServiceNo != 0) {
//            url = Uri.parse(BUS_STOPS_URL).buildUpon().appendQueryParameter("BusServiceNo", "" + searchBusServiceNo).toString();
//        }

//        try {
//            Log.d(TAG, "onResume: recovering stored data");
//            FileInputStream fis = getApplicationContext().openFileInput(BUS_SERVICES_FILENAME);
//            ObjectInputStream ois = new ObjectInputStream(fis);
//            busServices = (HashSet) ois.readObject();
//           // recyclerViewAdapter.loadNewData(busStops);
//            Log.d(TAG, "onResume: successfully recovered stored data");
//            ois.close();
//            fis.close();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "onResume: File not found -- loading");
//            GetJSONBusRouteData getJSONData = new GetJSONBusRouteData(this, url);
//            getJSONData.execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
        ArrayList<BusServiceGroup> busServiceGroups = new ArrayList<BusServiceGroup>();
        HashSet<String> busServiceNos = new HashSet<String>();
        String currentServiceNo = null;
        ArrayList<BusStop> currentBusStops = null;
        HashMap<String, BusStop> allBusStops = null;

        try {
            Log.d(TAG, "onBusRouteDataAvailable: recovering stored data");
            FileInputStream fis = getApplicationContext().openFileInput(BUS_STOPS_MAP_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            allBusStops = (HashMap) ois.readObject();
            Log.d(TAG, "onBusRouteDataAvailable: successfully recovered stored map data");
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "onBusRouteDataAvailable: File not found -- loading");
//            GetJSONBusStopData getJSONData = new GetJSONBusStopData(this, BUS_STOPS_URL);
//            getJSONData.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (int i = 0; i < data.size(); i++) {
            BusRoute currentRoute= data.get(i);
            if (currentServiceNo == null) {
                currentServiceNo = currentRoute.getServiceNo();
                currentBusStops = new ArrayList<BusStop>();
            }
            if (!currentRoute.getServiceNo().equalsIgnoreCase(currentServiceNo)) {
                BusServiceGroup busServiceGroup = new BusServiceGroup(currentServiceNo, currentBusStops);
                busServiceNos.add(currentServiceNo);
                busServiceGroups.add(busServiceGroup);
                currentServiceNo = currentRoute.getServiceNo();
                currentBusStops = new ArrayList<BusStop>();
            }
            if (currentRoute.getServiceNo().equalsIgnoreCase(currentServiceNo)) {
                BusStop currentBusStop = allBusStops.get(currentRoute.getBusStopCode());
                currentBusStops.add(currentBusStop);
            }
        }

        for (int i = 0; i < busServiceGroups.size(); i++) {
            BusServiceGroup busServiceGroup = busServiceGroups.get(i);
            String filename = BUS_SERVICE_GROUPS_FILENAME + busServiceGroup.getServiceNo() + ".ser";
            try {
                Log.d(TAG, "onBusRouteDataAvailable: writing group data");
                FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(busServiceGroup);
                oos.close();
                fos.close();
                Log.d(TAG, "onBusRouteDataAvailable: writing group data finished");
                Log.d(TAG, "onBusRouteDataAvailable: service groups size: " + busServiceGroups.size());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Log.d(TAG, "onBusRouteDataAvailable: writing all service no data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_SERVICE_NOS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busServiceNos);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusRouteDataAvailable: writing all service data finished");
            Log.d(TAG, "onBusRouteDataAvailable: service nos size: " + busServiceNos.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
