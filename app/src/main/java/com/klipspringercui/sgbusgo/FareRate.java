package com.klipspringercui.sgbusgo;

/**
 * Created by Kevin on 14/3/17.
 */

class FareRate {

    private double distanceLow;
    private double distanceUp;
    private double rateAdult;
    private double rateStudent;
    private double rateSenior;
    private double rateDisabilities;
    private double rateCash;
    private double rateWorkfare;

    public FareRate(double distanceLow, double distanceUp, double rateAdult, double rateStudent, double rateSenior, double rateDisabilities, double rateCash, double rateWorkfare) {
        this.distanceLow = distanceLow;
        this.distanceUp = distanceUp;
        this.rateAdult = rateAdult;
        this.rateStudent = rateStudent;
        this.rateSenior = rateSenior;
        this.rateDisabilities = rateDisabilities;
        this.rateCash = rateCash;
        this.rateWorkfare = rateWorkfare;
    }

    public double getDistanceLow() {
        return distanceLow;
    }

    public double getDistanceUp() {
        return distanceUp;
    }

    public double getRateAdult() {
        return rateAdult;
    }

    public double getRateStudent() {
        return rateStudent;
    }

    public double getRateSenior() {
        return rateSenior;
    }

    public double getRateDisabilities() {
        return rateDisabilities;
    }

    public double getRateCash() {
        return rateCash;
    }

    public double getRateWorkfare() {
        return rateWorkfare;
    }

    @Override
    public String toString() {
        return "FareRate: { " +
                " Distance: " + distanceLow + " ~ " + distanceUp +
                " rate for adult: " + rateAdult +
                " rate for student " + rateStudent +
                " rate for Senior: " + rateSenior +
                " rate for Disabilities: " + rateDisabilities +
                " rate for cash: " + rateCash +
                " rate for workforce: " + rateWorkfare +
                " }\n";
    }
}
