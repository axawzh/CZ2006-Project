package com.klipspringercui.sgbusgo;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    static final String FILTER_SERVICE_NO = "FILTER_SERVICE_NO";
    static final int REQUEST_SEARCH = 1;
    static final int REQUEST_FILTER = 2;

//    static final int SEARCH_ALL = 0;
//    static final int SEARCH_BUS_NO = 1;

//    private int searchMode;
//    private int searchBusServiceNo = 0;

    private BusStopsRecyclerViewAdapter recyclerViewAdapter;
    private ArrayList<BusStop> busStops = null;

    private String searchKeyword;
    private String filterServiceNo;
    private int searchMode = SEARCHMODE_WITHOUTSN;
    private String searchAid;

    Button btnFilter = null;

    interface BusStopSelectionCallable {
        void onBusStopSelected(BusStop selection);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_selection);

        activateToolBar(false, R.string.title_activity_bus_stop_selection);

        busStops = new ArrayList<>();
        RecyclerView busStopRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_busStop);
        busStopRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new BusStopsRecyclerViewAdapter(this, busStops);
        busStopRecyclerView.setAdapter(recyclerViewAdapter);
        busStopRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider));
        busStopRecyclerView.addOnItemTouchListener(new RecyclerItemOnClickListener(this, busStopRecyclerView, this));

        btnFilter = (Button) findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(FilterListener);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            this.searchMode = bundle.getInt(SEARCH_MODE);
            if (this.searchMode == SEARCHMODE_WITHSN) {
                this.searchAid = bundle.getString(SEARCH_AID);
            }
        }

    }

    Button.OnClickListener FilterListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            // does not enable user to choose a bus stop of another bus service when the choices are restricted to a certain bus service.
            if (searchMode == SEARCHMODE_WITHSN)
                return;
            filterServiceNo = null;
            Intent intent = new Intent(BusStopSelectionActivity.this, BusServiceSelectionActivity.class);
            startActivityForResult(intent, REQUEST_FILTER);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        String url = BUS_STOPS_URL;

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
                    this.busStops = LocalDB.getInstance().getBusStopsData();
                } else {
                    String buttonText = "Refined by Service No." + searchAid;
                    btnFilter.setText(buttonText);
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
                this.busStops = LocalDB.getInstance().getBusStopsData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.busStops = LocalDB.getInstance().getBusStopsData();
            btnFilter.setText(getResources().getString(R.string.filter_serviceno));
        }

        if (this.filterServiceNo != null && this.filterServiceNo.length() > 0) {
            String filename = BUS_GROUPS_FILENAME + filterServiceNo + ".ser";
            try {
                Log.d(TAG, "onResume: recovering stored data - " + filename);
                FileInputStream fis = getApplicationContext().openFileInput(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);
                BusGroup busGroup = (BusGroup) ois.readObject();
                this.busStops = busGroup.getBusStops();
                if (this.busStops == null || this.busStops.size() == 0) {
                    Log.e(TAG, "onResume: bus group data damaged");
                    this.busStops = LocalDB.getInstance().getBusStopsData();
                } else {
                    String buttonText = "Refined by Service No.  " + filterServiceNo;
                    btnFilter.setText(buttonText);
                }
                recyclerViewAdapter.loadNewData(busStops);
                Toast.makeText(this, "Displaying Bus Stops of Service " + filterServiceNo, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onResume: successfully recovered stored data");
                ois.close();
                fis.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onResume: File not found -- loading all");
                this.busStops = LocalDB.getInstance().getBusStopsData();
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
            searchKeyword = null;
            Intent intent = new Intent(this, SearchActivity.class);
            startActivityForResult(intent, REQUEST_SEARCH);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Receive the selected bus service no. to refine bus stops displayed
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SEARCH && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                this.searchKeyword = bundle.getString(BUS_STOP_SEARCH_KEYWORD);
            } else {
                this.searchKeyword = null;
            }
        } else {
            this.searchKeyword = null;
        }
        if (requestCode == REQUEST_FILTER && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                this.filterServiceNo = bundle.getString(FILTER_SERVICE_NO);
            } else {
                this.filterServiceNo = null;
            }
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
        LocalDB.getInstance().setBusServicesData((ArrayList)data);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = getIntent();
        String caller = getCallingActivity().getClassName().substring(28);
        //Toast.makeText(this, caller, Toast.LENGTH_SHORT).show();
        //Log.d(TAG, "onItemClick: calling activity: " + caller);
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
            case "AddFrequentTripActivity":
                bundle.putSerializable(AddFrequentTripActivity.FT_SELECTED_BUSSTOP, selected);
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
