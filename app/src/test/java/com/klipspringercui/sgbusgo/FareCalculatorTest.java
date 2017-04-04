package com.klipspringercui.sgbusgo;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Kevin on 4/4/17.
 */

public class FareCalculatorTest {

    class TestCase {
        double start;
        double end;
        double fare;

        public TestCase(double start, double end, double fare) {
            this.start = start;
            this.end = end;
            this.fare = fare;
        }

        public double getStart() {
            return start;
        }

        public double getEnd() {
            return end;
        }

        public double getFare() {
            return fare;
        }
    }

    ArrayList<FareRate> fareRates;
    ArrayList<TestCase> testCases;
    static final int NOOFTEST = 20;

    @Test
    public void AdultFareCalculatorTest() {

        populateTestCases();
        FareCalculator calculator = FareCalculatorFactory.getFareCalculator(PaymentMethod.ADULT);
        calculator.setFareRates(fareRates);
        if (this.fareRates != null && testCases != null) {

        }
        //assertEquals(case1.getFare(), calculator.calculate(case1.start, case1.end), 0.001);

    }

    public void populateTestCases() {

        fareRates = new ArrayList<>();
        testCases = new ArrayList<>();
        testCases.add(new TestCase(-1,-1,-1));
        int num = 0;
        while (true) {

            fareRates.add(new FareRate());
        }


    }

}
