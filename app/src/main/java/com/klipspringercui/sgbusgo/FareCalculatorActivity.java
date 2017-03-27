package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class FareCalculatorActivity extends BaseActivity implements GetJSONFareRateData.FareRateDataAvailableCallable {

    private static final String TAG = "FareCalculatorActivity";
    static final String FC_SELECTED_BUSSTOP = "CALCULATOR SELECTED BUS STOP";
    static final String FC_SELECTED_BUSSERVICENO = "CALCULATOR SELECTED BUS SERVICE NO";

    private BusStop selectedStartingBusStop = null;
    private BusStop selectedAlightingBusStop = null;
    private String selectedBusService = null;

    TextView txtFCSelectedStartingBusStop = null;
    Button btnFCSelectStartingBusStop = null;
    TextView txtFCSelectedAlightingBusStop = null;
    Button btnFCSelectAlightingBusStop = null;
    TextView txtFCSelectedBusService = null;
    Button btnFCSelectBusService = null;

    Button btnCalculate = null;

    TextView txtFareResult = null;
    
    private ArrayList<FareRate> rates;
    boolean btnBusStopEnabled = false;
    boolean btnCalculateEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_calculator);
        activateToolBar(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        
        this.txtFCSelectedStartingBusStop = (TextView) findViewById(R.id.txtFCSelectedStartingBusStop);
        this.txtFCSelectedAlightingBusStop = (TextView) findViewById(R.id.txtFCSelectedAlightingBusStop);
        this.txtFCSelectedBusService = (TextView) findViewById(R.id.txtFCSelectedBusService);
        this.txtFareResult = (TextView) findViewById(R.id.txtFareResult);

        this.btnFCSelectStartingBusStop = (Button) findViewById(R.id.btnFCSelectStartingBusStop);
        this.btnFCSelectAlightingBusStop = (Button) findViewById(R.id.btnFCSelectAlightingBusStop);
        this.btnFCSelectBusService = (Button) findViewById(R.id.btnFCSelectBusService);
        this.btnCalculate = (Button) findViewById(R.id.btnCalculate);

        this.btnFCSelectAlightingBusStop.setOnClickListener(busStopOnClickListener);
        this.btnFCSelectStartingBusStop.setOnClickListener(busStopOnClickListener);
        this.btnFCSelectBusService.setOnClickListener(busServiceOnClickListener);
        this.btnCalculate.setOnClickListener(calculateOnClickListener);

        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (selectedBusService == null || !btnBusStopEnabled) {
                Toast.makeText(FareCalculatorActivity.this, "Please Select a Bus Service First", Toast.LENGTH_SHORT).show();
                return;
            }
            int id = v.getId();
            Intent intent = new Intent(FareCalculatorActivity.this, BusStopSelectionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(SEARCH_MODE, SEARCHMODE_WITHSN);
            bundle.putString(SEARCH_AID, FareCalculatorActivity.this.selectedBusService);
            intent.putExtras(bundle);
            if (id == R.id.btnFCSelectStartingBusStop) {
                startActivityForResult(intent, REQUEST_BUSSTOP);
            } else {
                startActivityForResult(intent, REQUEST_BUSSTOP_B);
            }
        }
    };

    Button.OnClickListener busServiceOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(FareCalculatorActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSERVICE);
        }
    };

    Button.OnClickListener calculateOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (!btnCalculateEnabled || selectedBusService == null || selectedStartingBusStop == null || selectedAlightingBusStop == null)
                return;
            double distance = selectedStartingBusStop.getDistance() - selectedAlightingBusStop.getDistance();
            if (distance < 0)
                distance = 0 - distance;
            Log.d(TAG, "onClick: distance = " + distance);
            for (int i = 0; i < rates.size(); i++) {
                if (rates.get(i).getDistanceUp() > distance) {
                    Log.d(TAG, "onClick: fare " + rates.get(i).getRateAdult());
                    String result = "Total Fare: $" + (double) Math.round(rates.get(i).getRateAdult()) / 100;
                    txtFareResult.setText(result);
                    break;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        this.rates = FareRatesListHolder.getInstance().getData();
        if (rates == null || this.rates.size() == 0) {
            Log.d(TAG, "onCreate: loading json data");
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                showConnectionDialog();
                return;
            }
            GetJSONFareRateData getJSONFareRateData = new GetJSONFareRateData(this, FARE_URL);
            getJSONFareRateData.execute();
        } else {
            Log.d(TAG, "onResume: successfully recovered fare rate data");
            this.btnCalculateEnabled = true;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                switch(requestCode) {
                    case REQUEST_BUSSTOP:
                        selectedStartingBusStop = (BusStop) bundle.getSerializable(FC_SELECTED_BUSSTOP);
                        txtFCSelectedStartingBusStop.setText(selectedStartingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSTOP_B:
                        selectedAlightingBusStop = (BusStop) bundle.getSerializable(FC_SELECTED_BUSSTOP);
                        txtFCSelectedAlightingBusStop.setText(selectedAlightingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSERVICE:
                        selectedBusService = bundle.getString(FC_SELECTED_BUSSERVICENO);
                        txtFCSelectedBusService.setText(selectedBusService);
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

    @Override
    public void onFareRateDataAvailable(List<FareRate> data) {
        Log.d(TAG, "onFareRateDataAvailable: data received -" + data);
        Log.d(TAG, "doInBackground: data received with size " + data.size());
        FareRatesListHolder.getInstance().setData((ArrayList) data);
        this.btnCalculateEnabled = true;
    }
}
