package com.klipspringercui.sgbusgo;

import android.app.ProgressDialog;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class FareCalculatorActivity extends BaseActivity implements DataLoaderFactory.FareRateDataAvailableCallable {

    private static final String TAG = "FareCalculatorActivity";
    static final String FC_SELECTED_BUSSTOP = "CALCULATOR SELECTED BUS STOP";
    static final String FC_SELECTED_BUSSERVICENO = "CALCULATOR SELECTED BUS SERVICE NO";

    private BusStop selectedStartingBusStop = null;
    private BusStop selectedAlightingBusStop = null;
    private String selectedBusService = null;


    Button btnFCSelectStartingBusStop = null;
    Button btnFCSelectAlightingBusStop = null;
    Button btnFCSelectBusService = null;

    Button btnCalculate = null;

    private ArrayList<FareRate> rates;
    boolean btnBusStopEnabled = false;
    boolean btnCalculateEnabled = false;

    ProgressDialog loadingDialog;
    private PaymentMethod paymentMethod = PaymentMethod.ADULT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_calculator);
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
                    Toast.makeText(FareCalculatorActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(FareCalculatorActivity.this, CurrentTripActivity.class);
                    startActivity(intent);
                }
            }
        });
        
        //this.txtFCSelectedStartingBusStop = (TextView) findViewById(R.id.txtFCSelectedStartingBusStop);
        //this.txtFCSelectedAlightingBusStop = (TextView) findViewById(R.id.txtFCSelectedAlightingBusStop);
        //this.txtFCSelectedBusService = (TextView) findViewById(R.id.txtFCSelectedBusService);
        //this.txtFareResult = (TextView) findViewById(R.id.txtFareResult);

        this.btnFCSelectStartingBusStop = (Button) findViewById(R.id.btnFTSelectStartingBusStop);
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
            btnCalculate.setText(R.string.btn_calculate_text);
            int id = v.getId();
            Intent intent = new Intent(FareCalculatorActivity.this, BusStopSelectionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(SEARCH_MODE, SEARCHMODE_WITHSN);
            bundle.putString(SEARCH_AID, FareCalculatorActivity.this.selectedBusService);
            intent.putExtras(bundle);
            if (id == R.id.btnFTSelectStartingBusStop) {
                startActivityForResult(intent, REQUEST_BUSSTOP);
            } else {
                startActivityForResult(intent, REQUEST_BUSSTOP_B);
            }
        }
    };

    Button.OnClickListener busServiceOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            btnCalculate.setText(R.string.btn_calculate_text);
            Intent intent = new Intent(FareCalculatorActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_BUSSERVICE);
        }
    };

    Button.OnClickListener calculateOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (!btnCalculateEnabled || selectedBusService == null || selectedStartingBusStop == null || selectedAlightingBusStop == null)
                return;
            FareCalculator calculator = FareCalculatorFactory.getFareCalculator(paymentMethod);
            double totalFare = -1;
            if (calculator != null && calculator.dataAvailable()) {
                totalFare = calculator.calculate(
                        selectedStartingBusStop.getDistance(), selectedAlightingBusStop.getDistance());
            } else {
                Toast.makeText(FareCalculatorActivity.this, "Fare rates not available yet", Toast.LENGTH_SHORT).show();
            }
            if (totalFare == -1) {
                Toast.makeText(FareCalculatorActivity.this, "Error: data damaged", Toast.LENGTH_SHORT).show();
            }
            String result;
            if (totalFare == 0)
                result = "Total Fare: $0.00";
            else
                result = "Total Fare: $" + totalFare;
            btnCalculate.setText(result);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        this.rates = LocalDB.getInstance().getFareRatesData();
        if (rates == null || this.rates.size() == 0) {
            Log.d(TAG, "onCreate: loading json data");
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
            if (!isConnected) {
                showConnectionDialog();
                return;
            }
            showFareRateLoadingDialog();

            DataLoaderFactory.DataLoader fareDataLoader = DataLoaderFactory.getFareRateDataLoader(this);
            fareDataLoader.run();
//            GetJSONFareRateData getJSONFareRateData = new GetJSONFareRateData(this, FARE_URL);
//            getJSONFareRateData.execute();
        } else {
            Log.d(TAG, "onResume: successfully recovered fare rate data");
            this.btnCalculateEnabled = true;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissFareRateLoadingDialog();
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
                        btnFCSelectStartingBusStop.setText(selectedStartingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSTOP_B:
                        selectedAlightingBusStop = (BusStop) bundle.getSerializable(FC_SELECTED_BUSSTOP);
                        btnFCSelectAlightingBusStop.setText(selectedAlightingBusStop.getDescription());
                        break;
                    case REQUEST_BUSSERVICE:
                        selectedBusService = bundle.getString(FC_SELECTED_BUSSERVICENO);
                        btnFCSelectBusService.setText(selectedBusService);
                        selectedStartingBusStop = null;
                        btnFCSelectStartingBusStop.setText(R.string.start_bus_stop_txt);
                        selectedAlightingBusStop = null;
                        btnFCSelectAlightingBusStop.setText(R.string.alight_bus_stop_txt);
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
        dismissFareRateLoadingDialog();
        Log.d(TAG, "onFareRateDataAvailable: data received -" + data);
        Log.d(TAG, "doInBackground: data received with size " + data.size());
        LocalDB.getInstance().setFareRatesData((ArrayList) data);
        this.btnCalculateEnabled = true;
    }

    private void showFareRateLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing())
            return;
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle("Loading Latest Fare Rates");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    private void dismissFareRateLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
    }
}
