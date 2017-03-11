package com.klipspringercui.sgbusgo;

import java.util.ArrayList;

/**
 * Created by Kevin on 11/3/17.
 */

class BusServicesListHolder {
    private ArrayList<String> busServices = new ArrayList<>();

    private static final BusServicesListHolder holder = new BusServicesListHolder();

    public void setData(ArrayList<String> data) {
        busServices = data;
    }

    public ArrayList<String> getData() {
        return busServices;
    }

    public static BusServicesListHolder getInstance() {
        return holder;
    }
}
