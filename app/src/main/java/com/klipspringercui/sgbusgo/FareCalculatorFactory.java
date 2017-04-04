package com.klipspringercui.sgbusgo;

import android.util.Log;

import java.util.ArrayList;

import static com.klipspringercui.sgbusgo.R.id.btnCalculate;
import static com.klipspringercui.sgbusgo.R.id.txtFareResult;

/**
 * Created by Kevin on 4/4/17.
 */
enum PaymentMethod {STUDENT, ADULT, SENIOR, CASH, DISABILITIES, WORKFARE}

abstract class FareCalculator {
    protected ArrayList<FareRate> fareRates = null;

    public FareCalculator() {
        this.fareRates = LocalDB.getInstance().getFareRatesData();
    }

    public boolean dataAvailable() {
        if (fareRates == null)
            this.fareRates = LocalDB.getInstance().getFareRatesData();
        return (fareRates != null);
    }

    abstract double calculate(double start, double end);

    public void setFareRates(ArrayList<FareRate> rates) {
        this.fareRates = rates;
    }
}

class AdultFareCalculator extends FareCalculator{
    public AdultFareCalculator() {
        super();
    }
    double calculate(double start, double end) {
        if (start < 0 || end < 0)
            return -1;
        if (start == end)
            return 0.0;
        double distance = Math.abs(start - end);
        double result = -1;
        for (int i = 0; i < fareRates.size(); i++) {
            if (fareRates.get(i).getDistanceUp() > distance) {
                result = fareRates.get(i).getRateAdult();
                return (double) Math.round(result) / 100;
            }
        }
        return -1;
    }
}

class SeniorFareCalculator extends FareCalculator{
    public SeniorFareCalculator() {
        super();
    }
    double calculate(double start, double end) {
        if (start == end)
            return 0.0;
        if (start < 0 || end < 0)
            return -1;
        double distance = Math.abs(start - end);
        double result = -1;
        for (int i = 0; i < fareRates.size(); i++) {
            if (fareRates.get(i).getDistanceUp() > distance) {
                result = fareRates.get(i).getRateSenior();
                return (double) Math.round(result) / 100;
            }
        }
        return -1;
    }
}

class StudentFareCalculator extends FareCalculator{
    public StudentFareCalculator() {
        super();
    }
    double calculate(double start, double end) {
        if (start == end)
            return 0.0;
        if (start < 0 || end < 0)
            return -1;
        double distance = Math.abs(start - end);
        double result = -1;
        for (int i = 0; i < fareRates.size(); i++) {
            if (fareRates.get(i).getDistanceUp() > distance) {
                result = fareRates.get(i).getRateStudent();
                return (double) Math.round(result) / 100;
            }
        }
        return -1;
    }
}

class CashFareCalculator extends FareCalculator{
    public CashFareCalculator() {
        super();
    }
    double calculate(double start, double end) {
        if (start == end)
            return 0.0;
        if (start < 0 || end < 0)
            return -1;
        double distance = Math.abs(start - end);
        double result = -1;
        for (int i = 0; i < fareRates.size(); i++) {
            if (fareRates.get(i).getDistanceUp() > distance) {
                result = fareRates.get(i).getRateCash();
                return (double) Math.round(result) / 100;
            }
        }
        return -1;
    }
}

class WorkfareFareCalculator extends FareCalculator{
    public WorkfareFareCalculator() {
        super();
    }
    double calculate(double start, double end) {
        if (start == end)
            return 0.0;
        if (start < 0 || end < 0)
            return -1;
        double distance = Math.abs(start - end);
        double result = -1;
        for (int i = 0; i < fareRates.size(); i++) {
            if (fareRates.get(i).getDistanceUp() > distance) {
                result = fareRates.get(i).getRateWorkfare();
                return (double) Math.round(result) / 100;
            }
        }
        return -1;
    }
}

class DisabilitiesFareCalculator extends FareCalculator{
    public DisabilitiesFareCalculator() {
        super();
    }
    double calculate(double start, double end) {
        if (start == end)
            return 0.0;
        if (start < 0 || end < 0)
            return -1;
        double distance = Math.abs(start - end);
        double result = -1;
        for (int i = 0; i < fareRates.size(); i++) {
            if (fareRates.get(i).getDistanceUp() > distance) {
                result = fareRates.get(i).getRateDisabilities();
                return (double) Math.round(result) / 100;
            }
        }
        return -1;
    }
}

public class FareCalculatorFactory {

    public static FareCalculator getFareCalculator(PaymentMethod method) {
        switch (method) {
            case STUDENT:
                return new StudentFareCalculator();
            case ADULT:
                return new AdultFareCalculator();
            case SENIOR:
                return new SeniorFareCalculator();
            case DISABILITIES:
                return new DisabilitiesFareCalculator();
            case WORKFARE:
                return new WorkfareFareCalculator();
            case CASH:
                return new CashFareCalculator();
            default:
                break;
        }
        return null;
    }
}
