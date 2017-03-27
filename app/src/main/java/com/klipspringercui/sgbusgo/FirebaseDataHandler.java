package com.klipspringercui.sgbusgo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static com.klipspringercui.sgbusgo.BaseActivity.BUS_GROUPS_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_ROUTES_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.BUS_STOPS_FILENAME;
import static com.klipspringercui.sgbusgo.LoadLocalData.LOAD_FAIL;

/**
 * Created by Kevin on 15/3/17.
 */

class FirebaseDataHandler extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "FirebaseDataHandler";
    private Context mContext;
    ProgressDialog processDialog = null;

    public FirebaseDataHandler(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        ArrayList<BusGroup> busGroupsList = null;
        try {
            Log.d(TAG, "doInBackground: reading downloaded bus route data");
            FileInputStream fis = mContext.openFileInput(BUS_ROUTES_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            busGroupsList = (ArrayList) ois.readObject();
            Log.d(TAG, "doInBackground: successfully read bus route data");
            ois.close();
            fis.close();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "doInBackground: class not found");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "doInBackground: Bus Groups File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: writing group data");
        for (int i = 0; i < busGroupsList.size(); i++) {
            BusGroup busServiceGroup = busGroupsList.get(i);
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
        Log.d(TAG, "doInBackground: writing group data finished: service groups size: " + busGroupsList.size());
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        processDialog = new ProgressDialog(mContext);
        processDialog.setTitle("Processing data");
        processDialog.setMessage("Just a few seconds");
        processDialog.setCancelable(false);
        processDialog.show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (processDialog != null && processDialog.isShowing())
            processDialog.dismiss();
    }
}
