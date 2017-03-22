package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MyProfileActivity extends BaseActivity {

    Button buttonAddFrequentTrip = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
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

        buttonAddFrequentTrip = (Button) findViewById(R.id.btnAddFrequentTrip);
        buttonAddFrequentTrip.setOnClickListener(addFrequentTripOnClickListenser);
    }

    Button.OnClickListener addFrequentTripOnClickListenser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MyProfileActivity.this, AddFrequentTripActivity.class);
            startActivity(intent);
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
    }
}
