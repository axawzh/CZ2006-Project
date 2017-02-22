package com.klipspringercui.sgbusgo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 21/2/17.
 */

class GetJSONBusStopData extends AsyncTask<String, Void, List<BusStop>> implements GetRawData.DataDownloadCallable{

    private static final String TAG = "GetJSONBusStopData";
    
    private String baseURL;
    private final DataAvailableCallable mCallable;

    public GetJSONBusStopData(DataAvailableCallable mContext, String baseURL) {
        this.mCallable = mContext;
        this.baseURL = baseURL;
    }

    private List<BusStop> busStopsList = null;

    interface DataAvailableCallable {
        void onDataAvailable(List<BusStop> data, DownloadStatus status);
    }
    
    @Override
    protected List<BusStop> doInBackground(String... params) {
        GetRawData getRawData = new GetRawData(this);
        getRawData.runInSameThread(baseURL);
        return null;
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {
        
        if (status == DownloadStatus.OK) {
            Log.d(TAG, "onDownloadComplete: Data \n" + data);
            busStopsList = new ArrayList<BusStop>();
            
            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("value");
                
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonBusStop = itemsArray.getJSONObject(i);
                    String busStopCode = jsonBusStop.getString("BusStopCode");
                    String roadName = jsonBusStop.getString("RoadName");
                    String description = jsonBusStop.getString("Description");
                    double latitude = jsonBusStop.getDouble("Latitude");
                    double longitude = jsonBusStop.getDouble("Longitude");
                    BusStop busStopItem = new BusStop(busStopCode, roadName, description, latitude, longitude);
                    busStopsList.add(busStopItem);
                    Log.d(TAG, "onDownloadComplete: " + busStopItem.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onDownloadComplete: JSONException" + e.getMessage());
            }
        }
    }

    @Override
    protected void onPostExecute(List<BusStop> busStops) {
        Log.d(TAG, "onPostExecute: starts");
        super.onPostExecute(busStops);
        if (mCallable != null)
            mCallable.onDataAvailable(this.busStopsList, DownloadStatus.OK);
        Log.d(TAG, "onPostExecute: ends");
    }
}
