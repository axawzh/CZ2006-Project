package com.klipspringercui.sgbusgo;

import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

        @Override
        public String toString() {
            return "Test case start: " + start + " end: " + end + " fare: " + fare + "\n";
        }
    }

    ArrayList<FareRate> fareRates;
    ArrayList<TestCase> testCases;
    static final int NOOFTEST = 20;

    @Test
    public void AdultFareCalculatorTest() {

        populateTestCases();

        FareCalculator calculator = FareCalculatorFactory.getFareCalculator(PaymentMethod.ADULT);
        if (this.fareRates != null && testCases != null) {
            calculator.setFareRates(fareRates);
            for (int i = 0; i <= NOOFTEST; i++) {
                double result = calculator.calculate(testCases.get(i).getStart(), testCases.get(i).getEnd());
                double expected = (double) testCases.get(i).getFare() / 100;
                assertEquals(testCases.get(i).getFare(), result, 0.0001);
            }
        }
    }

    public void populateTestCases() {

        fareRates = new ArrayList<>();
        testCases = new ArrayList<>();
        int[] distances = new int[NOOFTEST+1];
        int[] rates = new int[NOOFTEST+1];

        Random random = new Random();
        for (int i= 0; i < NOOFTEST+1; i++) {
            distances[i] = random.nextInt(5000);
            rates[i] = random.nextInt(300);
        }

        distances[0] = 0;

        Arrays.sort(distances);
        Arrays.sort(rates);

        int num = 0;

        testCases.add(new TestCase(-1,0,-1));
        testCases.add(new TestCase(0,-1,-1));
        while (true) {
            double distanceLo = (double) distances[num] / 100;
            double distanceUp;
            if (num == NOOFTEST)
                distanceUp = Double.MAX_VALUE;
            else
                distanceUp = (double) distances[num + 1] / 100;
            fareRates.add(new FareRate(distanceLo,distanceUp,rates[num],0,0,0,0,0));
            double distance;
            if (num == 0) {
                distance = random.nextInt(distances[num+1]);
            } else if (num == NOOFTEST) {
                distance = random.nextInt(5000) + 5000;
            } else {
                distance = random.nextInt(distances[num+1] - distances[num]) + distances[num];
            }
            distance = distance / 100;
            testCases.add(new TestCase(1.0, 1.0 + distance, (double) rates[num] / 100));
            num++;
            if (num > NOOFTEST)
                break;
        }

        for (int i=0; i < fareRates.size(); i++) {
            System.out.print(fareRates.get(i));
            System.out.print(testCases.get(i));
        }
        System.out.print(testCases.get(fareRates.size()));

    }

}
