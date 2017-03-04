package com.klipspringercui.sgbusgo;

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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.klipspringercui.sgbusgo.BusStopSelectionActivity.BUS_STOPS_FILENAME;

public class MainActivity extends AppCompatActivity implements GetJSONBusRouteData.BusRoutesDataAvailableCallable {

    private static final String TAG = "MainActivity";
    private static final String BUS_ROUTES_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusRoutes";
    static final String BUS_ROUTES_FILENAMEPRE = "bus_routes_";
    static final String BUS_SERVICES_FILENAME = "bus_services.ser";

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

        try {
            Log.d(TAG, "onResume: recovering stored data");
            FileInputStream fis = getApplicationContext().openFileInput(BUS_SERVICES_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            busServices = (HashSet) ois.readObject();
           // recyclerViewAdapter.loadNewData(busStops);
            Log.d(TAG, "onResume: successfully recovered stored data");
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "onResume: File not found -- loading");
            GetJSONBusRouteData getJSONData = new GetJSONBusRouteData(this, url);
            getJSONData.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    }
}
