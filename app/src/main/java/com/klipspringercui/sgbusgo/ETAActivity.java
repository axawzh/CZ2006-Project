package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class ETAActivity extends AppCompatActivity {

    private static final String TAG = "ETAActivity";
    static final String ETA_SEARCH_MODE = "ETA SEARCH MODE";
    static final String ETA_SEARCH_BUSSERVICENO = "ETA SEARCH BUS SERVICE NO";
    static final String ETA_SEARCH_BUSSTOPNO = "ETA SEARCH BUS STOP NO";

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

        Button btnSelectBusStop = (Button) findViewById(R.id.btnSelectBusStop);
        btnSelectBusStop.setOnClickListener(busStopOnClickListener);

    }


    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ETAActivity.this, BusStopSelectionActivity.class);
//            intent.putExtra(BUSSTOP_SEARCH_MODE, BusStopSelectionActivity.SEARCH_ALL);
//            intent.putExtra(BUSSTOP_SEARCH_BUSSERVICENO)
            startActivity(intent);
        }
    };
}
