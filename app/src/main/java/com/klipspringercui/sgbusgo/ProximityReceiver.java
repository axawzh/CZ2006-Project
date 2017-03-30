package com.klipspringercui.sgbusgo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import static android.location.LocationManager.KEY_PROXIMITY_ENTERING;

/**
 * Created by Kevin on 30/3/17.
 */

public class ProximityReceiver extends BroadcastReceiver {

    private static final String TAG = "ProximityReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: received");
        String action = intent.getAction();
        boolean enter = intent.getBooleanExtra(KEY_PROXIMITY_ENTERING, false);
        if (enter) {
            Log.d(TAG, "onReceive: entering");
        } else {
            Log.d(TAG, "onReceive: exiting");
        }

        Bundle bundle = intent.getExtras();
        if (action == AlightingAlarmActivity.ACTION_PROXIMITY_ALERT) {
            //BusStop arrival = (BusStop) bundle.getSerializable(AlightingAlarmActivity.AA_SELECTED_BUSSTOP);
            String arrival = bundle.getString(AlightingAlarmActivity.ALIGHTING_BUSSTOP);
            if (arrival == null) {
                Log.e(TAG, "onReceive: not bus stop specified");
                return;
            } else {
                Log.d(TAG, "onReceive: building notification");
                NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context);
                nBuilder.setSmallIcon(R.drawable.notification_bus_white)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle("You are about to arrive!")
                        .setContentText("approaching " + arrival)
                        .setPriority(Notification.PRIORITY_HIGH);
                Intent resultIntent = new Intent(context, AlightingAlarmActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(AlightingAlarmActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
                nBuilder.setContentIntent(resultPendingIntent);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, nBuilder.build());
                //Toast.makeText(context, "You are about to arrive at " + arrival, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "onReceive: wrong receive");
        }
    }
}
