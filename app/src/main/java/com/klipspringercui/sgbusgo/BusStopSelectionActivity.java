package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.klipspringercui.sgbusgo.ETAActivity.ETA_SELECTED_BUSSTOP;


public class BusStopSelectionActivity extends AppCompatActivity implements GetJSONBusStopData.BusStopDataAvailableCallable,
                                                                            RecyclerItemOnClickListener.OnRecyclerClickListener {

    private static final String TAG = "BusStopSelectionActivit";
    private static final String BUS_STOPS_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusStops";

    static final String BUS_STOPS_FILENAME = "bus_stops.ser";
//    static final int SEARCH_ALL = 0;
//    static final int SEARCH_BUS_NO = 1;

//    private int searchMode;
//    private int searchBusServiceNo = 0;

    private BusStopsRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<BusStop> busStops = null;

    interface BusStopSelectionCallable {
        void onBusStopSelected(BusStop selection);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_selection);

//        Intent intent = getIntent();
//        searchMode = intent.getIntExtra(BUSSTOP_SEARCH_MODE, SEARCH_ALL);
//        if (searchMode == SEARCH_BUS_NO) {
//            searchBusServiceNo = intent.getIntExtra(BUSSTOP_SEARCH_BUSSERVICENO, 0);
//        }

        busStops = new ArrayList<BusStop>();
        RecyclerView busStopRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_busStop);
        busStopRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new BusStopsRecyclerViewAdapter(this, busStops);
        busStopRecyclerView.setAdapter(recyclerViewAdapter);
        busStopRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider));
        busStopRecyclerView.addOnItemTouchListener(new RecyclerItemOnClickListener(this, busStopRecyclerView, this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = BUS_STOPS_URL;
//        if (searchMode == SEARCH_BUS_NO && searchBusServiceNo != 0) {
//            url = Uri.parse(BUS_STOPS_URL).buildUpon().appendQueryParameter("BusServiceNo", "" + searchBusServiceNo).toString();
//        }

        try {
            Log.d(TAG, "onResume: recovering stored data");
            FileInputStream fis = getApplicationContext().openFileInput(BUS_STOPS_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            busStops = (ArrayList) ois.readObject();
            recyclerViewAdapter.loadNewData(busStops);
            Log.d(TAG, "onResume: successfully recovered stored data");
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "onResume: File not found -- loading");
            GetJSONBusStopData getJSONData = new GetJSONBusStopData(this, url);
            getJSONData.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBusStopDataAvailable(List<BusStop> data, DownloadStatus status) {
        Log.d(TAG, "onDataAvailable: " + data);
        try {
            Log.d(TAG, "onBusStopDataAvailable: writing data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_STOPS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusStopDataAvailable: writing data finished");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recyclerViewAdapter.loadNewData(data);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ETAActivity.ETA_SELECTED_BUSSTOP, recyclerViewAdapter.getBusStop(position));
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
