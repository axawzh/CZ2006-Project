package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends BaseActivity {

    static final int LOAD_OK = 0;
    static final int LOAD_FAIL = 1;
    private int loadFlag = LOAD_OK;

    Button buttonAddFrequentTrip = null;
    ListView listFrequentTrip = null;

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

        listFrequentTrip = (ListView) findViewById(R.id.listFrequentTrip);
    }

    Button.OnClickListener addFrequentTripOnClickListenser = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MyProfileActivity.this, AddFrequentTripActivity.class);
            startActivity(intent);
        }
    };

    private ArrayList<FrequentTrip> getSavedFrequentTripList(FileInputStream fis) {
        try {
            ObjectInputStream ois = new ObjectInputStream(fis);

        } catch (IOException e) {
            loadFlag = LOAD_FAIL;
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}
