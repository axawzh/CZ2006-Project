package com.klipspringercui.sgbusgo;

import java.io.Serializable;

/**
 * Created by Kevin on 24/2/17.
 */

class BusRoute implements Serializable{
    private String serviceNo;
    private String busStopCode;
    private double distance;
    private int busStopSequence;

    public BusRoute(String serviceNo, String busStopCode, int busStopSequence, double distance) {
        this.serviceNo = serviceNo;
        this.busStopCode = busStopCode;
        this.busStopSequence = busStopSequence;
        this.distance = distance;
    }

    public String getServiceNo() {
        return serviceNo;
    }

    public double getDistance() {
        return distance;
    }

    public String getBusStopCode() {
        return busStopCode;
    }

    public int getBusStopSequence() {
        return busStopSequence;
    }

    public String toString() {
        return "Bus Route: {" +
                " ServiceNo: " + serviceNo +
                " BusStopSeq: " + busStopSequence +
                " BusStopCode: " + busStopCode +
                " Distance: " + distance +
                " }";
    }
}
