package com.klipspringercui.sgbusgo;

import java.io.Serializable;

/**
 * Created by Zhenghao on 23/3/17.
 */

class FrequentTrip implements Serializable {
    private BusStop startingBusStop;
    private BusStop alightingBusStop;
    private String serviceNo;
    private int hour;
    private int minute;
    private int id;

    public FrequentTrip(BusStop startingBusStop, BusStop alightingBusStop, String serviceNo, int hour, int minute, int id) {
        this.startingBusStop = startingBusStop;
        this.alightingBusStop = alightingBusStop;
        this.serviceNo = serviceNo;
        this.hour = hour;
        this.minute = minute;
        this.id = id;
    }

    public BusStop getStartingBusStop() {
        return startingBusStop;
    }

    public BusStop getAlightingBusStop() {
        return alightingBusStop;
    }

    public String getServiceNo() {
        return serviceNo;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getTime() {
        String time = Integer.toString(hour) + ":" + Integer.toString(minute);
        return time;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "FrequentTrip: {" +
                "startingBusStop=" + startingBusStop +
                ", alightingBusStop=" + alightingBusStop +
                ", serviceNo='" + serviceNo + '\'' +
                ", hour=" + hour +
                ", minute=" + minute +
                ", id=" + id +
                '}';
    }
}
