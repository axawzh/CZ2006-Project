package com.klipspringercui.sgbusgo;

import java.io.Serializable;

/**
 * Created by Kevin on 13/3/17.
 */

class ETAItem implements Serializable {
    private String serviceNo;
    private String arrival1;
    private String arrival2;
    private String arrival3;
    private String busStopCode;

    public ETAItem(String serviceNo, String busStopCode, String arrival1, String arrival2, String arrival3) {
        this.serviceNo = serviceNo;
        this.busStopCode = busStopCode;
        this.arrival1 = arrival1;
        this.arrival2 = arrival2;
        this.arrival3 = arrival3;
    }

    public String getServiceNo() {
        return serviceNo;
    }

    public String getArrival1() {
        return arrival1;
    }

    public String getArrival2() {
        return arrival2;
    }

    public String getArrival3() {
        return arrival3;
    }

    public String getBusStopCode() {
        return busStopCode;
    }

    @Override
    public String toString() {
        return "Estimated Arrival Time: {" +
                " Service No: " + serviceNo +
                " Next Bus: " + arrival1 +
                " Subsequent1: " + arrival2 +
                " Subsequent2: " + arrival3 +
                " }\n";
    }
}
