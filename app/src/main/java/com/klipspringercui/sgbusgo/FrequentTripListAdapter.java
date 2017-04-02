package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static com.klipspringercui.sgbusgo.BaseActivity.ACTIVATED_FREQUENT_TRIP_FILENAME;

/**
 * Created by Zhenghao on 23/3/17.
 */

class FrequentTripListAdapter extends ArrayAdapter<FrequentTrip>{
    private Context context;
    private ArrayList<FrequentTrip> listFT;
    private ColorStateList defaultTextColor;

    private static final String TAG = "FrequentTripListAdapter";

    public FrequentTripListAdapter(@NonNull Context context, @NonNull ArrayList<FrequentTrip> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.frequent_trip_browse, parent, false);
        }

        FrequentTrip p = getItem(position);
        FrequentTrip activated_ft = getActivatedFrequentTrip();

        if (p != null) {
            TextView txtTime = (TextView) v.findViewById(R.id.textTime);
            TextView txtStartBusStop = (TextView) v.findViewById(R.id.txtStartBusStop);
            TextView txtAlightBusStop = (TextView) v.findViewById(R.id.txtAlightBusStop);
            TextView txtBusService = (TextView) v.findViewById(R.id.txtBusService);

            defaultTextColor = txtAlightBusStop.getTextColors();

            if (txtTime != null) {
                txtTime.setText(p.getTime());
                if (activated_ft != null && activated_ft.getId() == p.getId()) {
                    // Set the time color to green to indicate it is activated
                    txtTime.setTextColor(Color.parseColor("#61DB49"));
                } else {
                    txtTime.setTextColor(defaultTextColor);
                }
            }
            String displayText = "From: " + p.getStartingBusStop().getDescription();
            if (displayText.length() > 22)
                displayText = displayText.substring(0,22);
            String displayText2 = "To: " + p.getAlightingBusStop().getDescription();
            if (displayText2.length() > 22)
                displayText2 = displayText2.substring(0,22);
            txtStartBusStop.setText(displayText);
            txtAlightBusStop.setText(displayText2);
            txtBusService.setText(String.format("Service No: %s", p.getServiceNo()));
        }
        return v;
    }

    public void loadNewData(ArrayList<FrequentTrip> data) {
        this.listFT = data;
        notifyDataSetChanged();
    }

    private FrequentTrip getActivatedFrequentTrip() {
        FrequentTrip result = null;
        try {
            FileInputStream fis = getContext().openFileInput(ACTIVATED_FREQUENT_TRIP_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            result = (FrequentTrip) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            Log.d(TAG, "getActivatedFT: IO Exception");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "getActivatedFT: ClassNotFound Exception");
            e.printStackTrace();
        }
        return result;
    }
}
