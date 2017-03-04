package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Zhenghao on 4/3/17.
 */
public class AlightingAlarmActivity extends AppCompatActivity{
    static final String ALARM_SELECTED_BUSSTOP = "ALARM SELECTED BUS STOP";
    private BusStop selectedBusStop = null;
    private String selectedBusService = null;

    Button buttonSelectBusStop = null;
    TextView textSelectedBusStop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alightingalarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textSelectedBusStop = (TextView) findViewById(R.id.txtSelectedBusStop);
        buttonSelectBusStop = (Button) findViewById(R.id.btnSelectBusStop);
        buttonSelectBusStop.setOnClickListener(busStopOnClickListener);
    }

    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlightingAlarmActivity.this, BusStopSelectionActivity.class);

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
                selectedBusStop = (BusStop) bundle.getSerializable(ALARM_SELECTED_BUSSTOP);
                textSelectedBusStop.setText(selectedBusStop.getDescription());
            }
        }


    }

}
