package com.klipspringercui.sgbusgo;


import java.util.ArrayList;

/**
 * Created by Kevin on 11/3/17.
 */

class BusStopsListHolder {

    private ArrayList<BusStop> busStops = new ArrayList<>();

    private static final BusStopsListHolder holder = new BusStopsListHolder();

    public void setData(ArrayList<BusStop> data) {
        busStops = data;
    }

    public ArrayList<BusStop> getData() {
        return busStops;
    }

    public static BusStopsListHolder getInstance() {
        return holder;
    }

}
