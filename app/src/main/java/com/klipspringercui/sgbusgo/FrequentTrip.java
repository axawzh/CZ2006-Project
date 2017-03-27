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
        StringBuilder sb = new StringBuilder().append(pad(hour))
                .append(":").append(pad(minute));
        String time = sb.toString();
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

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    };
}
