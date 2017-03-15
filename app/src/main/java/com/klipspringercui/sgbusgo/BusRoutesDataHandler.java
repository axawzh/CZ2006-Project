package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.klipspringercui.sgbusgo.BaseActivity.BUS_GROUPS_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_ROUTES_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_SERVICES_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_STOPS_MAP_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_STOPS_URL;

/**
 * Created by Kevin on 15/3/17.
 */

class BusRoutesDataHandler extends AsyncTask<List<BusRoute>, Void, Void> {

    private static final String TAG = "BusRoutesDataHandler";
    private List<BusRoute> data;
    private Context mContext;

    public BusRoutesDataHandler(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(List<BusRoute>... params) {

        Log.d(TAG, "doInBackground: starts");
        if (params == null)
            return null;
        data = params[0];
        ArrayList<BusGroup> busGroups = new ArrayList<BusGroup>();
        ArrayList<String> busServicesList = new ArrayList<String>();
        String currentServiceNo = null;
        ArrayList<BusStop> currentBusStops = null;
        HashMap<String, BusStop> busStopsMap = null;


        try {
            Log.d(TAG, "doInBackground: recovering stored data");
            FileInputStream fis = mContext.openFileInput(BUS_STOPS_MAP_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            busStopsMap = (HashMap) ois.readObject();
            Log.d(TAG, "doInBackground: successfully recovered stored map data");
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "doInBackground: File not found -- loading");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < data.size(); i++) {
            BusRoute currentRoute= data.get(i);
            if (currentServiceNo == null) {
                currentServiceNo = currentRoute.getServiceNo();
                currentBusStops = new ArrayList<BusStop>();
            }
            if (!currentRoute.getServiceNo().equalsIgnoreCase(currentServiceNo)) {
                BusGroup busServiceGroup = new BusGroup(currentServiceNo, currentBusStops);
                busServicesList.add(currentServiceNo);
                busGroups.add(busServiceGroup);
                currentServiceNo = currentRoute.getServiceNo();
                currentBusStops = new ArrayList<BusStop>();
            }
            if (currentRoute.getServiceNo().equalsIgnoreCase(currentServiceNo)) {
                BusStop currentBusStop = busStopsMap.get(currentRoute.getBusStopCode());
                if (currentBusStop == null)
                    continue;
                try {
                    BusStop busStop = new BusStop(currentBusStop, currentRoute.getDistance());
                    currentBusStops.add(busStop);
                } catch (NullPointerException e ) {
                    //Log.e(TAG, "onBusRouteDataAvailable: null pointer ");
                }
            }
        }


        /**
         * Writing grouped bus data
         */
        Log.d(TAG, "doInBackground: writing group data");
        for (int i = 0; i < busGroups.size(); i++) {
            BusGroup busServiceGroup = busGroups.get(i);
            String filename = BUS_GROUPS_FILENAME + busServiceGroup.getServiceNo() + ".ser";
            try {

                FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(busServiceGroup);
                oos.close();
                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "doInBackground: writing group data finished: service groups size: " + busGroups.size());


        try {
            Log.d(TAG, "doInBackground: writing group holder data");
            FileOutputStream fos = mContext.openFileOutput(BUS_ROUTES_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busGroups);
            oos.close();
            fos.close();
            Log.d(TAG, "doInBackground: writing group holder data finished");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Writing services as list
         */
        try {
            Log.d(TAG, "doInBackground: writing all service no data");
            FileOutputStream fos = mContext.openFileOutput(BUS_SERVICES_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busServicesList);
            oos.close();
            fos.close();
            Log.d(TAG, "doInBackground: writing all service data finished service nos size: " + busServicesList.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BusServicesListHolder.getInstance().setData(busServicesList);
        return null;
    }

    public void runInSameThread(List<BusRoute> data) {
        Log.d(TAG, "runInSameThread: starts");
        doInBackground(data);
    };

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}
