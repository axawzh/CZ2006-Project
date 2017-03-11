package com.klipspringercui.sgbusgo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Kevin on 22/2/17.
 */

class BusStop implements Serializable, Parcelable {

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

    public BusStop(Parcel source) {
        this.busStopCode = source.readString();
        this.description = source.readString();
        this.roadName = source.readString();
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
    }

    static final Parcelable.Creator CREATOR = new Parcelable.Creator<BusStop>(){
        @Override
        public BusStop createFromParcel(Parcel source) {
            return new BusStop(source);
        }

        @Override
        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.busStopCode);
        dest.writeString(this.description);
        dest.writeString(this.roadName);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
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
