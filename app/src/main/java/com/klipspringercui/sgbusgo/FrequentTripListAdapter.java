package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhenghao on 23/3/17.
 */

class FrequentTripListAdapter extends ArrayAdapter<FrequentTrip>{
    private Context context;
    private ArrayList<FrequentTrip> listFT;

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

            if (tt1 != null) {
                tt1.setText(p.getTime());
            }

            if (tt2 != null) {
                tt2.setText("Alight at: " + p.getAlightingBusStop().getDescription());
            }
        }

        return v;
    }
}
