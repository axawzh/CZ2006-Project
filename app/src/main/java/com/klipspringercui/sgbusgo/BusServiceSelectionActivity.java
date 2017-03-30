package com.klipspringercui.sgbusgo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static android.provider.Contacts.SettingsColumns.KEY;

public class BusServiceSelectionActivity extends BaseActivity implements RecyclerItemOnClickListener.OnRecyclerClickListener {

    private static final String TAG = "BusServiceSelectionActi";
    static final String BUS_SERVICE_SEARCH_KEYWORD = "BUS SERVICE NO KEYWORD";
    private ArrayList<String> busServices;
    private String searchKeyword = null;

    private BusServicesRecyclerViewAdapter recyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_service_selection);
        activateToolBar(false);

        RecyclerView busServicesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_busService);
        busServicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new BusServicesRecyclerViewAdapter(this, busServices);
        busServicesRecyclerView.setAdapter(recyclerViewAdapter);
        busServicesRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.divider));
        busServicesRecyclerView.addOnItemTouchListener(new RecyclerItemOnClickListener(this, busServicesRecyclerView, this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivityForResult(intent, 0);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.busServices = LocalDB.getInstance().getBusServicesData();

        if (this.busServices.size() == 0) {
            try {
                Log.d(TAG, "onResume: recovering stored bus service data");
                FileInputStream fis = getApplicationContext().openFileInput(BUS_SERVICES_FILENAME);
                ObjectInputStream ois = new ObjectInputStream(fis);
                busServices = (ArrayList) ois.readObject();
                Log.d(TAG, "onResume: successfully recovered stored data");
                ois.close();
                fis.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "onResume: File not found -- loading");
            } catch (IOException e) {
                e.printStackTrace();
            }
            LocalDB.getInstance().setBusServicesData(busServices);
        }

        if (searchKeyword != null && searchKeyword.length() > 0) {
            ArrayList<String> shortlisted = new ArrayList<>();
            for (int i = 0; i < busServices.size(); i++) {
                String currentNo = busServices.get(i);
                int index = currentNo.toLowerCase().indexOf(searchKeyword.toLowerCase());
                if (index != -1) {
                    shortlisted.add(currentNo);
                }
            }
            recyclerViewAdapter.loadNewData(shortlisted);
        } else {
            recyclerViewAdapter.loadNewData(busServices);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                this.searchKeyword = bundle.getString(BUS_SERVICE_SEARCH_KEYWORD);
            } else {
                this.searchKeyword = null;
            }
        } else {
            this.searchKeyword = null;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = getIntent();
        String caller = getCallingActivity().getClassName().substring(28);
        Toast.makeText(this, caller, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemClick: calling activity: " + caller);
        Bundle bundle = new Bundle();
        String selected = recyclerViewAdapter.getBusServiceNo(position);
        if (selected == null)
            return;
        switch (caller) {
            case "ETAActivity":
                bundle.putString(ETAActivity.ETA_SELECTED_BUSSERVICENO, selected);
                break;
            case "AlightingAlarmActivity":
                bundle.putString(AlightingAlarmActivity.AA_SELECTED_BUSSERVICENO, selected);
                break;
            case "FareCalculatorActivity":
                bundle.putString(FareCalculatorActivity.FC_SELECTED_BUSSERVICENO, selected);
                break;
            case "AddFrequentTripActivity":
                bundle.putString(AddFrequentTripActivity.FT_SELECTED_BUSSERVICENO, selected);
                break;
            case "BusStopSelectionActivity":
                bundle.putString(BusStopSelectionActivity.FILTER_SERVICE_NO, selected);
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
