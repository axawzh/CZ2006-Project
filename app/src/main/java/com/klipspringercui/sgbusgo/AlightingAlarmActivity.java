/** 
  * Created by Zhenghao on 4/3/17. 
  */

package com.klipspringercui.sgbusgo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static com.klipspringercui.sgbusgo.FareCalculatorActivity.FC_SELECTED_BUSSTOP;

public class AlightingAlarmActivity extends BaseActivity {

    private static final String TAG = "AlightingAlarmActivity";

    static final String AA_SELECTED_BUSSTOP = "ALARM SELECTED BUS STOP";
    static final String AA_SELECTED_BUSSERVICENO = "ALARM SELECTED BUS SERVICE NO";
    static final String ACTION_PROXIMITY_ALERT = "com.klipspringercui.sgbusgo.ACTION_PROXIMITY_ALERT";
    static final String ALIGHTING_BUSSTOP = "ARRIVAL_BUSSTOP_DESCRIPTION";

    private BusStop selectedBusStop = null;
    private String selectedBusService = null;

    Button btnAASelectBusStop;
    Button btnSetAlightingAlarm;
    TextView textAASelectedBusStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alighting_alarm);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                actionBar = getSupportActionBar();
                actionBar.setTitle(R.string.title_activity_alighting_alarm);
            }
        } else {
            actionBar.setTitle(R.string.title_activity_alighting_alarm);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        textAASelectedBusStop = (TextView) findViewById(R.id.txtAASelectedBusStop);
        btnAASelectBusStop = (Button) findViewById(R.id.btnAASelectBusStop);
        btnAASelectBusStop.setOnClickListener(busStopOnClickListener);

        btnSetAlightingAlarm = (Button) findViewById(R.id.btnSetAlightingAlarm);
        btnSetAlightingAlarm.setOnClickListener(setAlarmOnClickListener);
    }

    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlightingAlarmActivity.this, BusStopSelectionActivity.class);
            startActivityForResult(intent, 0);
        }
    };

    Button.OnClickListener setAlarmOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (selectedBusStop == null) {
                Toast.makeText(AlightingAlarmActivity.this, "Please select a bus stop", Toast.LENGTH_SHORT).show();
                return;
            }
            setAlarm();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                selectedBusStop = (BusStop) bundle.getSerializable(AA_SELECTED_BUSSTOP);
                textAASelectedBusStop.setText(selectedBusStop.getDescription());
            }
        }
    }

    private void setAlarm() {
        Log.d(TAG, "setAlarm: starts");
        if (selectedBusStop == null)
            return;

        //Intent intent = new Intent(getResources().getString(R.string.action_proximity));
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(ALIGHTING_BUSSTOP, selectedBusStop.getDescription());
        intent.putExtras(bundle);
        intent.setAction(ACTION_PROXIMITY_ALERT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //For testing
        Location location = LocationHandler.getInstance().getUserLocation(getApplicationContext());
        if (location == null) {
            Log.d(TAG, "setAlarm: location null ");
            Toast.makeText(this, "Please ensure that this app has internet and location access", Toast.LENGTH_SHORT).show();
            return;
        }
        double userLatitude = location.getLatitude();
        double userLongitude = location.getLongitude();

        double longitude = selectedBusStop.getLongitude();
        double latitude = selectedBusStop.getLatitude();
        Log.d(TAG, "setAlarm: <user> latitude " + userLatitude + "  longitude " + userLongitude);
        Log.d(TAG, "setAlarm: <target> latitude " + latitude + "  longitude " + longitude);
        //getApplicationContext().sendBroadcast(intent);
        LocationHandler.getInstance().setAlightingAlarm(getApplicationContext(), latitude, longitude, pendingIntent);

    }
}
