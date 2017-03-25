package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

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
            v = vi.inflate(R.layout.content_frequenttrip_listview_row, parent, false);
        }

        FrequentTrip p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.textTime);
            TextView tt2 = (TextView) v.findViewById(R.id.textBusStop);
            defaultTextColor = tt2.getTextColors();

            if (tt1 != null) {
                tt1.setText(p.getTime());
                FrequentTrip activated_ft = getActivatedFrequentTrip();
                if (activated_ft != null && activated_ft.getId() == p.getId()) {
                    // Set the time color to green to indicate it is activated
                    tt1.setTextColor(Color.parseColor("#33e550"));
                } else {
                    tt1.setTextColor(defaultTextColor);
                }
            }

            if (tt2 != null) {
                tt2.setText("Alight at: " + p.getAlightingBusStop().getDescription());
            }
        }

        return v;
    }

    private FrequentTrip getActivatedFrequentTrip() {
        FrequentTrip result = null;
        try {
            FileInputStream fis = getContext().openFileInput(ACTIVATED_FREQUENT_TRIP_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            result = (FrequentTrip) ois.readObject();
            ois.close();
            fis.close();
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
