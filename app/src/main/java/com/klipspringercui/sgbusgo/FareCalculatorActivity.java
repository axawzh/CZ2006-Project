package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FareCalculatorActivity extends BaseActivity {

    static final String FC_SELECTED_BUSSTOP = "CALCULATOR SELECTED BUS STOP";

    private BusStop selectedBusStop = null;
    private String selectedBusService = null;

    Button btnFCSelectBusStop = null;
    TextView textFCSelectedBusStop = null;


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

        this.btnFCSelectBusStop = (Button) findViewById(R.id.btnFCSelectBusStop);
        this.textFCSelectedBusStop = (TextView) findViewById(R.id.txtFCSelectedBusStop);
        this.btnFCSelectBusStop.setOnClickListener(busStopOnClickListener);

    }
    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(FareCalculatorActivity.this, BusStopSelectionActivity.class);
            startActivityForResult(intent, 0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                selectedBusStop = (BusStop) bundle.getSerializable(FC_SELECTED_BUSSTOP);
                textFCSelectedBusStop.setText(selectedBusStop.getDescription());
            }
        }
    }
}
