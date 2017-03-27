package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static com.klipspringercui.sgbusgo.BaseActivity.BUS_SERVICES_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_STOPS_FILENAME;

/**
 * Created by Kevin on 12/3/17.
 */

class LoadLocalData extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "LoadLocalData";
    static final int LOAD_OK = 0;
    static final int LOAD_FAIL = 1;
    private int flag;
    private Context mContext;
    private DataLoadCallable mCallable;

    private ArrayList<String> busServicesList;
    private ArrayList<BusStop> busStopsList;


    interface DataLoadCallable {
        void onDataLoaded (int flag);
    }


    public LoadLocalData(Context mContext, DataLoadCallable callable) {
        this.mContext = mContext;
        this.mCallable = callable;
    }

    @Override
    protected Void doInBackground(Void... params) {

        flag = LOAD_OK;

        try {
            Log.d(TAG, "doInBackground: recovering stored data");
            FileInputStream fis = mContext.openFileInput(BUS_SERVICES_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.busServicesList = (ArrayList) ois.readObject();
            Log.d(TAG, "doInBackground: successfully recovered stored data");
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            flag = LOAD_FAIL;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "doInBackground: Bus Service File not found -- loading");
            flag = LOAD_FAIL;
        } catch (IOException e) {
            e.printStackTrace();
            flag = LOAD_FAIL;
        }

        try {
            Log.d(TAG, "doInBackground: recovering stored bus stop list data");
            FileInputStream fis = mContext.openFileInput(BUS_STOPS_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.busStopsList = (ArrayList) ois.readObject();
            Log.d(TAG, "doInBackground: successfully recovered stored data");
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "doInBackground: class not found");
            flag = LOAD_FAIL;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "doInBackground: Bus Stops File not found");
            flag = LOAD_FAIL;
        } catch (IOException e) {
            e.printStackTrace();
            flag = LOAD_FAIL;
        }

        if (flag == LOAD_OK) {
            BusServicesListHolder.getInstance().setData(busServicesList);
            BusStopsListHolder.getInstance().setData(busStopsList);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mCallable.onDataLoaded(this.flag);
    }
}
