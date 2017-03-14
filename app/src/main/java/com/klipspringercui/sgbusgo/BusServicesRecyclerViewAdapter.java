package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 12/3/17.
 */

class BusServicesRecyclerViewAdapter extends RecyclerView.Adapter<BusServicesRecyclerViewAdapter.BusServiceViewHolder> {

    private List<String> busServicesList;
    private Context mContext;

    static class BusServiceViewHolder extends RecyclerView.ViewHolder {
        TextView txtServiceNo;
        public BusServiceViewHolder(View itemView) {
            super(itemView);
            this.txtServiceNo = (TextView) itemView.findViewById(R.id.txtServiceNo);
        }
    }

    public BusServicesRecyclerViewAdapter(Context mContext, List<String> busServicesList) {
        this.busServicesList = busServicesList;
        this.mContext = mContext;
    }

    @Override
    public BusServiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bus_service_browse, parent, false);
        return new BusServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusServiceViewHolder holder, int position) {
        if (this.busServicesList == null || this.busServicesList.size() == 0) {
            holder.txtServiceNo.setText(R.string.bus_service_no_match);
        } else {
            holder.txtServiceNo.setText(this.busServicesList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return (this.busServicesList != null && this.busServicesList.size() > 0)? this.busServicesList.size() : 1;
    }

    public String getBusServiceNo(int position) {
        return (this.busServicesList != null && this.busServicesList.size() > 0)? this.busServicesList.get(position) : null;
    }

    public void loadNewData(List<String> data) {
        this.busServicesList = data;
        notifyDataSetChanged();
    }
}
