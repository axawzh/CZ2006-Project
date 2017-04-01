package com.klipspringercui.sgbusgo;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Kevin on 30/3/17.
 */

class LocalDB {

    private ArrayList<String> busServices = new ArrayList<>();
    private ArrayList<BusStop> busStops = new ArrayList<>();
    private ArrayList<FareRate> rates = new ArrayList<>();
    private ArrayList<FrequentTrip> frequentTrips = new ArrayList<>();
    private CurrentTrip currentTrip = null;

    private static final LocalDB holder = new LocalDB();

    public static LocalDB getInstance() {
        return holder;
    }

    public void setFrequentTripsData(ArrayList<FrequentTrip> data) {
        this.frequentTrips = data;
    }

    public ArrayList<FrequentTrip> getFrequentTripsData() {
        return this.frequentTrips;
    }

    public void setFareRatesData(ArrayList<FareRate> data) {
        this.rates = data;
    }

    public ArrayList<FareRate> getFareRatesData() {
        return this.rates;
    }

    public void setBusServicesData(ArrayList<String> data) {
        busServices = data;
    }

    public ArrayList<String> getBusServicesData() {
        return busServices;
    }

    public void setBusStopsData(ArrayList<BusStop> data) {
        busStops = data;
    }

    public ArrayList<BusStop> getBusStopsData() {
        return busStops;
    }

    public CurrentTrip getCurrentTrip() {
        return currentTrip;
    }

    public void setCurrentTrip(CurrentTrip currentTrip) {
        this.currentTrip = currentTrip;
    }
}
