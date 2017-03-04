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
    private final BusStopDataAvailableCallable mCallable;
    private boolean transmissionFlag;

    public GetJSONBusStopData(BusStopDataAvailableCallable mContext, String baseURL) {
        this.mCallable = mContext;
        this.baseURL = baseURL;
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
        if (mCallable != null)
            mCallable.onBusStopDataAvailable(this.busStopsList, DownloadStatus.OK);
        //Log.d(TAG, "onPostExecute: ends");
    }
}
