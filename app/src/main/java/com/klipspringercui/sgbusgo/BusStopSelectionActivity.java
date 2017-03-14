package com.klipspringercui.sgbusgo;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.searchMode;
import static com.klipspringercui.sgbusgo.ETAActivity.ETA_SELECTED_BUSSTOP;



public class BusStopSelectionActivity extends BaseActivity implements GetJSONBusStopData.BusStopDataAvailableCallable,
                                                                            RecyclerItemOnClickListener.OnRecyclerClickListener {

    private static final String TAG = "BusStopSelectionActivit";
    static final String BUS_STOP_SEARCH_KEYWORD = "SearchKeyword";


//    static final int SEARCH_ALL = 0;
//    static final int SEARCH_BUS_NO = 1;

//    private int searchMode;
//    private int searchBusServiceNo = 0;

    private BusStopsRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<BusStop> busStops = null;

    private boolean force = false;
    private String searchKeyword;
    private int searchMode = SEARCHMODE_WITHOUTSN;
    private String searchAid;

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

        activateToolBar(false);
        busStops = new ArrayList<BusStop>();
        RecyclerView busStopRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_busStop);
        busStopRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new BusStopsRecyclerViewAdapter(this, busStops);
        busStopRecyclerView.setAdapter(recyclerViewAdapter);
        busStopRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider));
        busStopRecyclerView.addOnItemTouchListener(new RecyclerItemOnClickListener(this, busStopRecyclerView, this));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            this.searchMode = bundle.getInt(SEARCH_MODE);
            if (this.searchMode == SEARCHMODE_WITHSN) {
                this.searchAid = bundle.getString(SEARCH_AID);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        String url = BUS_STOPS_URL;

        if (force) {
            GetJSONBusStopData getJSONData = new GetJSONBusStopData(this, url);
            getJSONData.execute();
            return;
        }

        if (this.searchMode == SEARCHMODE_WITHSN && searchAid != null) {
            String filename = BUS_GROUPS_FILENAME + searchAid + ".ser";
            try {
                Log.d(TAG, "onResume: recovering stored data - " + filename);
                FileInputStream fis = getApplicationContext().openFileInput(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);
                BusGroup busGroup = (BusGroup) ois.readObject();
                this.busStops = busGroup.getBusStops();
                if (this.busStops == null || this.busStops.size() == 0) {
                    Log.e(TAG, "onResume: bus group data damaged");
                    this.busStops = BusStopsListHolder.getInstance().getData();
                }
                recyclerViewAdapter.loadNewData(busStops);
                Toast.makeText(this, "Displaying Bus Stops of Service " + searchAid, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onResume: successfully recovered stored data");
                ois.close();
                fis.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onResume: File not found -- loading all");
                this.busStops = BusStopsListHolder.getInstance().getData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.busStops = BusStopsListHolder.getInstance().getData();
        }

        if (this.busStops.size() == 0) {
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

        if (this.searchKeyword != null && this.searchKeyword.length() > 0) {
            Toast.makeText(this, "Search Finished", Toast.LENGTH_SHORT).show();
            ArrayList<BusStop> shortlisted = new ArrayList<>();
            BusStop currentBusStop;
            for (int i = 0; i < busStops.size(); i++) {
                currentBusStop = busStops.get(i);
                int index = currentBusStop.getDescription().toLowerCase().indexOf(searchKeyword.toLowerCase());
                if (index == -1)
                    index = currentBusStop.getRoadName().toLowerCase().indexOf(searchKeyword.toLowerCase());
                if (index != -1)
                    shortlisted.add(currentBusStop);
            }
            recyclerViewAdapter.loadNewData(shortlisted);
        } else {
            recyclerViewAdapter.loadNewData(busStops);
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                this.searchKeyword = bundle.getString(BUS_STOP_SEARCH_KEYWORD);
            } else {
                this.searchKeyword = null;
            }
        } else {
            this.searchKeyword = null;
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
            Log.d(TAG, "onBusStopDataAvailable: data size: " + data.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recyclerViewAdapter.loadNewData(data);

        HashMap<String, BusStop> busStopHashMap = new HashMap<String, BusStop>();
        for (int i = 0; i < data.size(); i++) {
            BusStop busStop = data.get(i);
            busStopHashMap.put(busStop.getBusStopCode(), busStop);
        }
        try {
            Log.d(TAG, "onBusStopDataAvailable: map writing data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_STOPS_MAP_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busStopHashMap);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusStopDataAvailable: map writing data finished");
            Log.d(TAG, "onBusStopDataAvailable: Map size: " + busStopHashMap.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BusStopsListHolder.getInstance().setData((ArrayList)data);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = getIntent();
        String caller = getCallingActivity().getClassName().substring(28);
        Toast.makeText(this, caller, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemClick: calling activity: " + caller);
        Bundle bundle = new Bundle();
        BusStop selected = recyclerViewAdapter.getBusStop(position);
        if (selected == null)
            return;
        switch (caller) {
            case "ETAActivity":
                bundle.putSerializable(ETAActivity.ETA_SELECTED_BUSSTOP, selected);
                break;
            case "AlightingAlarmActivity":
                bundle.putSerializable(AlightingAlarmActivity.AA_SELECTED_BUSSTOP, selected);
                break;
            case "FareCalculatorActivity":
                bundle.putSerializable(FareCalculatorActivity.FC_SELECTED_BUSSTOP, selected);
                break;
            default:
                break;
        }
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemLongClick(View view, int position) {

    }
}
