package com.klipspringercui.sgbusgo;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Kevin on 14/3/17.
 */

class FareRatesListHolder {

    private ArrayList<FareRate> rates = new ArrayList<>();

    private static final FareRatesListHolder holder = new FareRatesListHolder();

    public static FareRatesListHolder getInstance() {
        return holder;
    }

    public void setData(ArrayList<FareRate> data) {
        this.rates = data;
    }

    public ArrayList<FareRate> getData() {
        return this.rates;
    }

}
