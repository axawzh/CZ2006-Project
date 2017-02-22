package com.klipspringercui.sgbusgo;

import java.io.Serializable;

/**
 * Created by Kevin on 22/2/17.
 */

class BusStop implements Serializable {
    private String busStopCode;
    private String description;
    private String roadName;
    private double latitude;
    private double longitude;

    public BusStop(String busStopCode, String roadName, String description, double latitude, double longitude) {
        this.busStopCode = busStopCode;
        this.roadName = roadName;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getBusStopCode() {
        return busStopCode;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getRoadName() {
        return roadName;
    }

    public String toString() {
        return "Bus Stop: {" +
                " Code = " + busStopCode +
                ", RoadName = " + roadName +
                ", Description = " + description +
                ", Latitude = " + latitude +
                ", Longitude = " + longitude +
                " }";
    }
}
