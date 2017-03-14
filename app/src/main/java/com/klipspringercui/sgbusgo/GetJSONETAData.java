package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 12/3/17.
 */

class GetJSONETAData extends AsyncTask<String, Void, Void> implements GetRawData.DataDownloadCallable {

    private static final String TAG = "GetJSONETAData";
    private String busStopCode;
    private String serviceNo = null;
    private List<ETAItem> etas;

    private String baseURL;
    private final ETADataAvailableCallable mCallable;

    interface ETADataAvailableCallable {
        void onETADataAvailable(List<ETAItem> data);
    }

    public GetJSONETAData(ETADataAvailableCallable callable, String baseURL) {
        this.baseURL = baseURL;
        this.mCallable = callable;
    }

    /**
     *
     * @param  params : params[0] - busStopCode, params[1] - busServiceNo (if available)
     * @return
     */
    @Override
    protected Void doInBackground(String... params) {
        Log.d(TAG, "doInBackground: with parameter " + params[0]);
        this.busStopCode = params[0];
        if (params.length == 2)
            this.serviceNo = params[1];
        else
            this.serviceNo = null;
        String url = createURL();
        Log.d(TAG, "doInBackground: url - " + url);
        GetRawData getRawData = new GetRawData(this);
        getRawData.runInSameThread(url);
        return null;
    }

    private String createURL() {
        if (this.serviceNo == null)
            return Uri.parse(baseURL).buildUpon().appendQueryParameter("BusStopID", this.busStopCode).
                    appendQueryParameter("SST", "True").build().toString();
        else
            return Uri.parse(baseURL).buildUpon().appendQueryParameter("BusStopID", this.busStopCode).
                    appendQueryParameter("ServiceNo", this.serviceNo).
                    appendQueryParameter("SST", "True").build().toString();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (this.etas != null)
            Log.d(TAG, "onPostExecute: returning data - " + etas);
            mCallable.onETADataAvailable(this.etas);
    }

    @Override
    public void onDownloadComplete(String data, DownloadStatus status) {

        if (status == DownloadStatus.OK) {
            this.etas = new ArrayList<ETAItem>();

            try {
                JSONObject jsonData = new JSONObject(data);
                JSONArray itemsArray = jsonData.getJSONArray("Services");

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject jsonETAItem = itemsArray.getJSONObject(i);
                    String service = jsonETAItem.getString("ServiceNo");
                    JSONObject jsonNext = jsonETAItem.getJSONObject("NextBus");
                    String arrival1 = jsonNext.getString("EstimatedArrival");
                    JSONObject jsonSub = jsonETAItem.getJSONObject("SubsequentBus");
                    String arrival2 = jsonSub.getString("EstimatedArrival");
                    JSONObject jsonSub3 = jsonETAItem.getJSONObject("SubsequentBus3");
                    String arrival3 = jsonSub3.getString("EstimatedArrival");
                    if (service == null)
                        Log.e(TAG, "onDownloadComplete: JSON data error");
                    etas.add(new ETAItem(service, arrival1, arrival2, arrival3));
                }

            } catch (JSONException e) {
                Log.e(TAG, "onDownloadComplete: JSON data error");
            } catch (NullPointerException e) {
                Log.e(TAG, "onDownloadComplete: null pointer");
            }
        }

    }
}
