package com.klipspringercui.sgbusgo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Kevin on 3/4/17.
 */

class BusGroup implements Serializable, Parcelable {

    private String serviceNo;
    private ArrayList<BusStop> busStops;

    public BusGroup(String serviceNo, ArrayList<BusStop> busStops) {
        this.serviceNo = serviceNo;
        this.busStops = busStops;
    }

    public BusGroup(Parcel source) {
        this.serviceNo = source.readString();
        source.readTypedList(this.busStops, BusStop.CREATOR);
    }

    static final Parcelable.Creator CREATOR = new Parcelable.Creator<BusGroup>(){
        @Override
        public BusGroup createFromParcel(Parcel source) {
            return new BusGroup(source);
        }

        @Override
        public BusGroup[] newArray(int size) {
            return new BusGroup[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceNo);
        dest.writeTypedList(busStops);
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
