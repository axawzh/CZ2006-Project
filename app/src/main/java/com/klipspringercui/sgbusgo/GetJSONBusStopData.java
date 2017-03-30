package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.data;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_STOPS_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_STOPS_MAP_FILENAME;

/**
 * Created by Kevin on 21/2/17.
 */

class GetJSONBusStopData extends AsyncTask<String, Void, List<BusStop>> implements GetRawData.DataDownloadCallable{

    private static final String TAG = "GetJSONBusStopData";
    
    private final BusStopDataAvailableCallable mCallable;
    private Context mContext;
    private String baseURL;
    private boolean processOnBackground;

    private boolean transmissionFlag;

    public GetJSONBusStopData(BusStopDataAvailableCallable mContext, Context context, String baseURL, boolean mode) {
        this.mCallable = mContext;
        this.mContext = context;
        this.baseURL = baseURL;
        this.processOnBackground = mode;
    }

    private List<BusStop> busStopsList = null;

    interface BusStopDataAvailableCallable {
        void onBusStopDataAvailable(List<BusStop> data, DownloadStatus status);
    }
    
    @Override
    protected List<BusStop> doInBackground(String... params) {
        int skipValue = 0;
        this.transmissionFlag = true;
        busStopsList = new ArrayList<BusStop>();
        GetRawData getRawData = new GetRawData(this);
        while (transmissionFlag) {
            getRawData.runInSameThread(baseURL + "?$skip=" + skipValue);
            skipValue += 50;
        }
        return null;
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        
        if (status == DownloadStatus.OK) {
            //Log.d(TAG, "onDownloadComplete: Data \n" + data);

            
            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("value");
                //Log.d(TAG, "onDownloadComplete: JSONArray length" + itemsArray.length());

                if (itemsArray.length() != 50)
                    transmissionFlag = false;
                
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonBusStop = itemsArray.getJSONObject(i);
                    String busStopCode = jsonBusStop.getString("BusStopCode");
                    String roadName = jsonBusStop.getString("RoadName");
                    String description = jsonBusStop.getString("Description");
                    double latitude = jsonBusStop.getDouble("Latitude");
                    double longitude = jsonBusStop.getDouble("Longitude");
                    BusStop busStopItem = new BusStop(busStopCode, roadName, description, latitude, longitude);
                    busStopsList.add(busStopItem);
                    //Log.d(TAG, "onDownloadComplete: " + busStopItem.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onDownloadComplete: JSONException" + e.getMessage());
            }
        }
    }

    @Override
    protected void onPostExecute(List<BusStop> busStops) {
        //Log.d(TAG, "onPostExecute: starts");
        super.onPostExecute(busStops);
        if (processOnBackground)
            updateData();
        else {
            if (mCallable != null)
                mCallable.onBusStopDataAvailable(this.busStopsList, DownloadStatus.OK);
        }
        //Log.d(TAG, "onPostExecute: ends");
    }

    private void updateData() {

        Log.d(TAG, "updateData: starts");
        List<BusStop> data = this.busStopsList;
        
        try {
            Log.d(TAG, "updateData: writing data");
            FileOutputStream fos = mContext.openFileOutput(BUS_STOPS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
            fos.close();
            Log.d(TAG, "updateData: writing data finished");
            Log.d(TAG, "updateData: data size: " + data.size());
        } catch (FileNotFoundException e) {
            Log.e(TAG, "updateData: File Not Found - BusStopsList");
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, BusStop> busStopsMap = new HashMap<String, BusStop>();
        
        for (int i = 0; i < data.size(); i++) {
            BusStop busStop = data.get(i);
            busStopsMap.put(busStop.getBusStopCode(), busStop);
        }
        
        try {
            Log.d(TAG, "updateData: map writing data");
            FileOutputStream fos = mContext.openFileOutput(BUS_STOPS_MAP_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busStopsMap);
            oos.close();
            fos.close();
            Log.d(TAG, "updateData: map writing data finished");
            Log.d(TAG, "updateData: Map size: " + busStopsMap.size());
        } catch (FileNotFoundException e) {
            Log.d(TAG, "updateData: File Not Found - BusStopsMap ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        LocalDB.getInstance().setBusStopsData((ArrayList) busStopsList);
    }
}
