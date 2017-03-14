package com.klipspringercui.sgbusgo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kevin on 14/3/17.
 */

class GetJSONFareRateData extends AsyncTask<String, Void, Void> {

    private static final String TAG = "GetJSONFareRateData";

    private String url;
    private FareRateDataAvailableCallable mCallable;

    private ArrayList<FareRate> rates;

    interface FareRateDataAvailableCallable {
        void onFareRateDataAvailable(List<FareRate> data);
    }

    public GetJSONFareRateData(FareRateDataAvailableCallable mCallable, String url) {
        this.url = url;
        this.mCallable = mCallable;
    }

    @Override
    protected Void doInBackground(String... params) {

        String rawData = getFareRawData(url);

        if (rawData == null) {
            Log.e(TAG, "doInBackground: download failure");
            return null;
        }

        this.rates = new ArrayList<>();

        try {
            JSONObject jsonData = new JSONObject(rawData);
            JSONArray records = jsonData.getJSONObject("result").getJSONArray("records");
            //Log.d(TAG, "doInBackground: " + records);

            for (int i = 0; i < records.length(); i++) {
                JSONObject item = records.getJSONObject(i);
                String rawDistance = item.getString("distance");
                int index1 = rawDistance.indexOf("km");
                int index2 = rawDistance.lastIndexOf("km");
                //Log.d(TAG, "doInBackground: index 1 " + index1 + " 2 " + index2);
                double distanceLow;
                double distanceUp;
                if (index1 == index2) {
                    if (rawDistance.indexOf("Up to") != -1) {
                        distanceLow = 0;
                        String high = rawDistance.substring(5, index1).trim();
                        distanceUp = Double.parseDouble(high);
                    } else {
                        distanceLow = Double.parseDouble(rawDistance.substring(4, index1).trim());
                        distanceUp = Double.MAX_VALUE;
                    }
                } else {
                    String low = rawDistance.substring(0, index1).trim();
                    distanceLow = Double.parseDouble(low);
                    int index3 = rawDistance.indexOf("-");
                    String high = rawDistance.substring(index3+1, index2).trim();
                    distanceUp = Double.parseDouble(high);
                }
                double rateAdult = Double.parseDouble(item.getString("adult_card_fare_per_ride"));
                double rateStudent = Double.parseDouble(item.getString("student_card_fare_per_ride"));
                double rateSenior = Double.parseDouble(item.getString("senior_citizen_card_fare_per_ride"));
                double rateDisabilities = Double.parseDouble(item.getString("persons_with_disabilities_card_fare_per_ride"));
                double rateCash = Double.parseDouble(item.getString("cash_fare_per_ride"));
                double rateWorkfare = Double.parseDouble(item.getString("workfare_transport_concession_card_fare_per_ride"));
                FareRate rate = new FareRate(distanceLow, distanceUp, rateAdult, rateStudent, rateSenior, rateDisabilities, rateCash, rateWorkfare);
                rates.add(rate);

//                Log.d(TAG, "doInBackground: data - " + rates);
            }
        } catch (JSONException e) {
            Log.e(TAG, "doInBackground: JSONException");
        }
        return null;
    }

    private String getFareRawData(String param) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(param);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            //Log.d(TAG, "doInBackground: Response code is: " + response);
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            //force the second expression to be evaluated first by putting the null up front.2
            while (null != (line = reader.readLine())) {
                sb.append(line).append("\n");
                //The new line character would be stripped off by readLine method so we need to bring it back.
            }
            //or
            //for(String line = reader.readLine(); line != null; line = reader.readLine())
            return sb.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception in reading data " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception - Permission needed " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing the stream" + e.getMessage());
                }
            }
        }
        Log.e(TAG, "getFareRawData: Download Failure");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (this.rates != null) {
            mCallable.onFareRateDataAvailable(this.rates);
        } else {
            Log.e(TAG, "onPostExecute: data error");
        }
    }
}
