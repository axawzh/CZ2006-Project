package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class BusStopSelectionActivity extends AppCompatActivity implements GetJSONBusStopData.DataAvailableCallable{

    private static final String TAG = "BusStopSelectionActivit";
    private static final String BUS_STOPS_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusStops";
//    static final int SEARCH_ALL = 0;
//    static final int SEARCH_BUS_NO = 1;

//    private int searchMode;
//    private int searchBusServiceNo = 0;

    private BusStopsRecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_selection);

//        Intent intent = getIntent();
//        searchMode = intent.getIntExtra(BUSSTOP_SEARCH_MODE, SEARCH_ALL);
//        if (searchMode == SEARCH_BUS_NO) {
//            searchBusServiceNo = intent.getIntExtra(BUSSTOP_SEARCH_BUSSERVICENO, 0);
//        }

        RecyclerView busStopRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_busStop);
        busStopRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new BusStopsRecyclerViewAdapter(this, new ArrayList<BusStop>());
        busStopRecyclerView.setAdapter(recyclerViewAdapter);
        busStopRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider));

    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = BUS_STOPS_URL;
//        if (searchMode == SEARCH_BUS_NO && searchBusServiceNo != 0) {
//            url = Uri.parse(BUS_STOPS_URL).buildUpon().appendQueryParameter("BusServiceNo", "" + searchBusServiceNo).toString();
//        }
        GetJSONBusStopData getJSONData = new GetJSONBusStopData(this, url);
        getJSONData.execute();
    }

    @Override
    public void onDataAvailable(List<BusStop> data, DownloadStatus status) {
        Log.d(TAG, "onDataAvailable: " + data);
        recyclerViewAdapter.loadNewData(data);
    }
}
