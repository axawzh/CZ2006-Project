package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.klipspringercui.sgbusgo.R.id.btnSelectBusStop;
import static com.klipspringercui.sgbusgo.R.id.txtSelectedBusStop;

public class ETAActivity extends AppCompatActivity {

    private static final String TAG = "ETAActivity";
    static final String ETA_SEARCH_MODE = "ETA SEARCH MODE";
    static final String ETA_SEARCH_BUSSERVICENO = "ETA SEARCH BUS SERVICE NO";
    static final String ETA_SEARCH_BUSSTOPNO = "ETA SEARCH BUS STOP NO";
    static final String ETA_SELECTED_BUSSTOP = "ETA SELECTED BUS STOP";
    private BusStop selectedBusStop = null;
    private String selectedBusService = null;

    Button buttonSelectBusStop = null;
    TextView textSelectedBusStop = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eta);
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

        textSelectedBusStop = (TextView) findViewById(R.id.txtSelectedBusStop);
        buttonSelectBusStop = (Button) findViewById(R.id.btnSelectBusStop);
        buttonSelectBusStop.setOnClickListener(busStopOnClickListener);

    }


    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ETAActivity.this, BusStopSelectionActivity.class);

//            intent.putExtra(BUSSTOP_SEARCH_MODE, BusStopSelectionActivity.SEARCH_ALL);
//            intent.putExtra(BUSSTOP_SEARCH_BUSSERVICENO)
            startActivityForResult(intent, 0);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                selectedBusStop = (BusStop) bundle.getSerializable(ETA_SELECTED_BUSSTOP);
                textSelectedBusStop.setText(selectedBusStop.getDescription());
            }
        }


    }




}
