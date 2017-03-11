package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Kevin on 22/2/17.
 */

class BusStopsRecyclerViewAdapter extends RecyclerView.Adapter<BusStopsRecyclerViewAdapter.BusStopViewHolder> {

    private static final String TAG = "BusStopsRecViewAdapter";

    private List<BusStop> busStopList;
    private Context mContext;


    static class BusStopViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView roadName;
        public BusStopViewHolder(View itemView) {
            super(itemView);
            this.description = (TextView) itemView.findViewById(R.id.description);
            this.roadName = (TextView) itemView.findViewById(R.id.road_name);
        }
    }

    public BusStopsRecyclerViewAdapter(Context mContext, List<BusStop> busStopList) {
        this.mContext = mContext;
        this.busStopList = busStopList;
    }

    public void loadNewData(List<BusStop> busStopList) {
        this.busStopList = busStopList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (this.busStopList != null && this.busStopList.size() > 0)? this.busStopList.size() : 1;
    }

    public BusStop getBusStop(int position) {
        return (this.busStopList != null && this.busStopList.size() > 0)? this.busStopList.get(position) : null;
    }

    @Override
    public BusStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bus_stop_browse, parent, false);
        return new BusStopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusStopViewHolder holder, int position) {
        if (this.busStopList != null && this.busStopList.size() == 0) {
            holder.description.setText(R.string.bus_stop_no_match);
            holder.roadName.setText("");
        } else {
            BusStop busStopItem = this.busStopList.get(position);
            holder.description.setText(busStopItem.getDescription());
            holder.roadName.setText(busStopItem.getRoadName());
        }

    }


}
