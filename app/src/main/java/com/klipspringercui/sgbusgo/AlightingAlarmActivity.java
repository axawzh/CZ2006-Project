/** 
  * Created by Zhenghao on 4/3/17. 
  */

package com.klipspringercui.sgbusgo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.klipspringercui.sgbusgo.FareCalculatorActivity.FC_SELECTED_BUSSTOP;

public class AlightingAlarmActivity extends BaseActivity {

    static final String AA_SELECTED_BUSSTOP = "ALARM SELECTED BUS STOP";
    static final String AA_SELECTED_BUSSERVICENO = "ALARM SELECTED BUS SERVICE NO";
    private BusStop selectedBusStop = null;
    private String selectedBusService = null;

    Button btnAASelectBusStop;
    TextView textAASelectedBusStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alighting_alarm);
        activateToolBar(false);

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
    }

    Button.OnClickListener busStopOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlightingAlarmActivity.this, BusStopSelectionActivity.class);
            startActivityForResult(intent, 0);
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
}
