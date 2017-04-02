package com.klipspringercui.sgbusgo;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import com.klipspringercui.sgbusgo.MyProfileActivity;

import static org.junit.Assert.*;

/**
 * Created by Zhenghao on 2/4/17.
 */

@RunWith(AndroidJUnit4.class)
public class getFrequentTripListTest {
    private static final String TAG = "TEST";
    public static final String TEST_FILENAME = "TEST_FILE.ser";

    public static final String BUSSTOP_CODE = "12345";
    public static final String BUSSTOP_ROAD = "testRoad";
    public static final String BUSSTOP_DESCRIPTION = "test";
    public static final String BUS_SREVICENO = "179";

    public static final BusStop TEST_STARTINGBS = new BusStop(BUSSTOP_CODE, BUSSTOP_ROAD, BUSSTOP_DESCRIPTION, 123.34, 345.56);
    public static final BusStop TEST_ALIGHTINGBS = new BusStop(BUSSTOP_CODE, BUSSTOP_ROAD, BUSSTOP_DESCRIPTION, 123.34, 345.56);
    public static final FrequentTrip TEST_TRIP = new FrequentTrip(TEST_STARTINGBS, TEST_ALIGHTINGBS, BUS_SREVICENO, 5, 30, 123456);

    ArrayList<FrequentTrip> TEST_LIST= new ArrayList<FrequentTrip>();

    @Before
    public void createTextList() {
        TEST_LIST.add(TEST_TRIP);
        try{
            FileOutputStream fos = new FileOutputStream(TEST_FILENAME);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(TEST_LIST);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Test: FileNotFound Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Test: IO Exception");
            e.printStackTrace();
        }

    }
    @Test
    public void WhiteBoxText() {
        MyProfileActivity test_activity = new MyProfileActivity();

        ArrayList<FrequentTrip> test = test_activity.getSavedFrequentTripList(TEST_FILENAME);
        assertEquals(TEST_LIST, test);
    }

}