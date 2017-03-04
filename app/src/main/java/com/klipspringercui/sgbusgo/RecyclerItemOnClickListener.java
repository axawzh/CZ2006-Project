package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Kevin on 24/2/17.
 */

class RecyclerItemOnClickListener extends RecyclerView.SimpleOnItemTouchListener {
    private static final String TAG = "RecyclerClickListener";

    interface OnRecyclerClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private final OnRecyclerClickListener listener;
    private final GestureDetectorCompat gestureDetector;

    /**
     * The listener need a context for the GestureDetector, and reference to the recyclerView
     * @param context
     * @param recyclerView
     * @param listener
     */
    public RecyclerItemOnClickListener(Context context, final RecyclerView recyclerView, final OnRecyclerClickListener listener) {

        this.listener = listener;
        this.gestureDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //Log.d(TAG, "onSingleTapUp: starts");
                //Log.d(TAG, "onSingleTapUp: tap on x=" + e.getX() + " y=" + e.getY());
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());  //find what is underneath
                if (childView != null && listener != null) {
                    Log.d(TAG, "onSingleTapUp: short press calling listener.onItemClick");
                    listener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
                }


                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: starts");
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && listener != null) {
                    Log.d(TAG, "onLongPress: long press calling listener.onItemLongClick");
                    listener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }
        });

    }

    /**
     * onInterceptTouchEvent would intercept any touch event detected by android, before it is passed to the
     * Recycler view to be handled
     *
     * @param rv
     * @param e
     * @return true if the touch event has been handled. In this case, return true would mean tapping has no effect on
     * the view, since the touch event would not be sent to the view.
     * return false if the touch event cannot be handled, the the event would be passed to the Recycler View.
     */
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //  Log.d(TAG, "onInterceptTouchEvent: Touch intercepted");
        if (gestureDetector != null) {
            boolean result = gestureDetector.onTouchEvent(e);
            // Log.d(TAG, "onInterceptTouchEvent: return" + result);
            return result;
        } else {
            //  Log.d(TAG, "onInterceptTouchEvent: return false");
            return false;
        }
    }
}


