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

    public FrequentTrip(BusStop startingBusStop, BusStop alightingBusStop, String serviceNo, int hour, int minute) {
        this.startingBusStop = startingBusStop;
        this.alightingBusStop = alightingBusStop;
        this.serviceNo = serviceNo;
        this.hour = hour;
        this.minute = minute;
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

    @Override
    public String toString() {
        return "Frequent Trip: {" +
                "startingBusStop=" + startingBusStop +
                ", alightingBusStop=" + alightingBusStop +
                ", serviceNo='" + serviceNo + '\'' +
                ", hour=" + hour +
                ", minute=" + minute +
                '}';
    }
}
