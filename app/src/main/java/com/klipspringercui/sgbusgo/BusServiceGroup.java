package com.klipspringercui.sgbusgo;

import java.util.ArrayList;

/**
 * Created by Kevin on 3/4/17.
 */

class BusServiceGroup {
    private String serviceNo;
    private ArrayList<BusStop> busStops;

    public BusServiceGroup(String serviceNo, ArrayList<BusStop> busStops) {
        this.serviceNo = serviceNo;
        this.busStops = busStops;
    }

    public String getServiceNo() {
        return serviceNo;
    }

    public ArrayList<BusStop> getBusStops() {
        return busStops;
    }

    public BusStop getBusStop(int index) {
        return this.busStops.get(index);
    }

}
