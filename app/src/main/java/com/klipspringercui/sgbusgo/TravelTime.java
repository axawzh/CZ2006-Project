package com.klipspringercui.sgbusgo;

import java.io.Serializable;

/**
 * Created by Zhenghao on 23/3/17.
 */

class TravelTime implements Serializable{
    private int hour;
    private int minute;

    public TravelTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String
}
