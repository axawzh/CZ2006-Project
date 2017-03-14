package com.klipspringercui.sgbusgo;

/**
 * Created by Kevin on 21/2/17.
 */
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;




enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALISED, FAILED_OR_EMPTY, OK};

class GetRawData extends AsyncTask<String, Void, String> {

    private static final String ACCOUNT_KEY = "1yVK2IbuR/uCdkrfxrpSkw==";
    private static final String TAG = "GetRawData";
    private DownloadStatus mDownloadStatus;
    private final DataDownloadCallable mCallback;

    interface DataDownloadCallable {
        void onDownloadComplete(String data, DownloadStatus status);
    }

    //Use an interface to improve loose coupling
    public GetRawData(DataDownloadCallable callback) {
        super();
        this.mCallback = callback;
        mDownloadStatus = DownloadStatus.IDLE;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (params == null) {
            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
            return null;
        }

        try {

            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("AccountKey",ACCOUNT_KEY);
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            //Log.d(TAG, "doInBackground: Response code is: " + response);

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            //force the second expression to be evaluated first by putting the null up front.2
            while (null != (line = reader.readLine())) {
                sb.append(line).append("\n");
                //The new line character would be stripped off by readLine method so we need to bring it back.
            }
            //or
            //for(String line = reader.readLine(); line != null; line = reader.readLine())

            mDownloadStatus = DownloadStatus.OK;
            return sb.toString();

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IO Exception in reading data " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception - Permission needed " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: Error closing the stream" + e.getMessage());
                }
            }
        } //finally clause would be executed before return

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }

    /**
     * this method allows GetRawData to be called on the same thread, not as an AsyncTask
     * @param s
     */
    void runInSameThread(String s) {
        //Log.d(TAG, "runInSameThread: starts with param:" + s);
        // onPostExecute(doInBackground(s));  //using the AsyncTask callback methods as ordinary methods.

        //or, a better way which does not involve onPostExecute(), so nothing weird would happen
        if (mCallback != null)
            mCallback.onDownloadComplete(doInBackground(s), DownloadStatus.OK);

        //Log.d(TAG, "runInSameThread: ends");
    }

    /**
     * Super() is not called in this method. Because we do not want any AsyncTask code to run when we run it in runInSameThread
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        if (mCallback != null) {
            mCallback.onDownloadComplete(s, mDownloadStatus);
        }
    }
}