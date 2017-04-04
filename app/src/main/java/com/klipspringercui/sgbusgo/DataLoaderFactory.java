package com.klipspringercui.sgbusgo;

import java.util.List;

/**
 * Created by Kevin on 3/4/17.
 */

class DataLoaderFactory {

    static final String ETA_URL = "http://datamall2.mytransport.sg/ltaodataservice/BusArrival";
    static final String FARE_URL = "https://data.gov.sg/api/action/datastore_search?resource_id=d9b3b8ec-ac41-41f1-b76f-70396125774d&limit=50";

    interface ETADataLoader {
        void run(String... params);
        List<ETAItem> runInSameThread(String... params);
    }

    interface DataLoader {
        void run(String... params);
    }

    interface ETADataAvailableCallable {
        void onETADataAvailable(List<ETAItem> data, String serviceNo, String busStopCode);
    }

    interface FareRateDataAvailableCallable {
        void onFareRateDataAvailable(List<FareRate> data);
    }

    interface BusStopDataAvailableCallable {
        void onBusStopDataAvailable(List<BusStop> data);
    }

    interface BusRouteDataAvailableCallable {
        void onBusRouteDataAvailable(List<BusGroup> data);
    }

    static ETADataLoader getETADataLoader(ETADataAvailableCallable callable) {
        return new GetJSONETAData(callable, ETA_URL);
    }

    static DataLoader getFareRateDataLoader(FareRateDataAvailableCallable callable) {
        return new GetJSONFareRateData(callable, FARE_URL);
    }

    static DataLoader getBusStopDataLoader(BusStopDataAvailableCallable callable) {
        return null;
    }

    static DataLoader getBusRouteDataLoader(BusRouteDataAvailableCallable callable) {
        return null;
    }
}
