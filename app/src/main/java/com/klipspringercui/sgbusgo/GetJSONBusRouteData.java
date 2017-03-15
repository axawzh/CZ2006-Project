package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 24/2/17.
 */

class GetJSONBusRouteData extends AsyncTask<String, Void, List<BusRoute>> implements GetRawData.DataDownloadCallable{

    private static final String TAG = "GetJSONBusRouteData";

    private Context mContext;
    private BusRoutesDataAvailableCallable mCallable;
    private String baseURL;
    private boolean transmissionFlag;
    private List<BusRoute> busRoutesList;
    private boolean processOnBackground;

    interface BusRoutesDataAvailableCallable {
        void onBusRouteDataAvailable(List<BusRoute> data, DownloadStatus status);
    }


    public GetJSONBusRouteData(BusRoutesDataAvailableCallable mCallable, Context mContext, String baseURL, boolean mode) {
        this.mCallable = mCallable;
        this.baseURL = baseURL;
        this.mContext = mContext;
        this.processOnBackground = mode;
    }

    @Override
    protected List<BusRoute> doInBackground(String... params) {
        transmissionFlag = true;
        int skipValue = 0;
        busRoutesList = new ArrayList<BusRoute>();
        while (transmissionFlag) {
            GetRawData getRawData = new GetRawData(this);
            getRawData.runInSameThread(baseURL + "?$skip=" + skipValue);
            skipValue += 50;
        }
        return null;
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {

        if (status == DownloadStatus.OK) {
//            Log.d(TAG, "onDownloadComplete: Data \n" + data);

            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("value");
//                Log.d(TAG, "onDownloadComplete: JSONArray length" + itemsArray.length());

                if (itemsArray.length() != 50)
                    transmissionFlag = false;

                for (int i = 0; i < itemsArray.length(); i++) {

                    JSONObject jsonBusRoute = itemsArray.getJSONObject(i);
                    String serviceNo = jsonBusRoute.getString("ServiceNo");
                    String busStopCode = jsonBusRoute.getString("BusStopCode");
                    double distance;
                    if (jsonBusRoute.isNull("Distance")) {
                        distance = 0;
                    } else {
                        distance = jsonBusRoute.getDouble("Distance");
                    }
                    int seq = jsonBusRoute.getInt("StopSequence");
                    BusRoute busRouteItem = new BusRoute(serviceNo, busStopCode, seq, distance);
                    busRoutesList.add(busRouteItem);
//                    Log.d(TAG, "onDownloadComplete: " + busRouteItem.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
//                Log.e(TAG, "onDownloadComplete: JSONException" + e.getMessage());
            }
        }
    }

    @Override
    protected void onPostExecute(List<BusRoute> busRoutes) {
        Log.d(TAG, "onPostExecute: starts");
        super.onPostExecute(busRoutes);

        if (processOnBackground) {
            BusRoutesDataHandler handler = new BusRoutesDataHandler(mContext);
            handler.runInSameThread(this.busRoutesList);
        } else {
            if (mCallable != null)
                mCallable.onBusRouteDataAvailable(this.busRoutesList, DownloadStatus.OK);
        }
        Log.d(TAG, "onPostExecute: ends");
    }
}
