package com.klipspringercui.sgbusgo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends BaseActivity implements GetJSONBusRouteData.BusRoutesDataAvailableCallable,
                                                        GetJSONBusStopData.BusStopDataAvailableCallable,
                                                        LoadLocalData.DataLoadCallable {

    private static final String TAG = "MainActivity";
    private static final String BUS_STOPS_MAP = "BUT STOPS MAP";
    private static final String BUS_STOP_LIST = "BUT STOPS LIST";
    private static final String BUS_SERVICES_SET = "BUS SERVICES SET";
    private static final String BUS_SERVICES_LIST = "BUS SERVICES LIST";

    private HashMap<String, BusStop> busStopsMap = null;
    private ArrayList<BusStop> busStopsList = null;
    private ArrayList<String> busServicesList = null;

    Button btnETA = null;
    Button btnAA = null;
    Button btnFC = null;

    Button btnTestUpdate = null;
    Button btnTestUpload = null;
    Button btnTestDownload = null;
    Button btnTestFireDownload = null;

    private boolean[] reloadFlags;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    ProgressDialog downloadDialog = null;
    ProgressDialog loadingDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activateToolBar(false);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btnETA = (Button) findViewById(R.id.btnETA);
        btnFC = (Button) findViewById(R.id.btnFareCalculator);
        btnAA = (Button) findViewById(R.id.btnAlightingAlarm);

        btnETA.setOnClickListener(mainActivityButtonListener);
        btnFC.setOnClickListener(mainActivityButtonListener);
        btnAA.setOnClickListener(mainActivityButtonListener);

        btnTestUpdate = (Button) findViewById(R.id.btnTestUpdate);
        btnTestDownload = (Button) findViewById(R.id.btnTestDownload);
        btnTestUpload = (Button) findViewById(R.id.btnTestUpload);
        btnTestFireDownload = (Button) findViewById(R.id.btnTestFireDownload);

        btnTestFireDownload.setOnClickListener(testOnClickListener);
        btnTestDownload.setOnClickListener(testOnClickListener);
        btnTestUpdate.setOnClickListener(testOnClickListener);
        btnTestUpload.setOnClickListener(testOnClickListener);

        mAuth = FirebaseAuth.getInstance();

        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();


        Toast.makeText(this, "Loading data", Toast.LENGTH_SHORT).show();


        showLoadingDialog();
        LoadLocalData loadLocalData = new LoadLocalData(this, this);
        loadLocalData.execute();

        reloadFlags = new boolean[3];
        reloadFlags[0] = false;
        reloadFlags[1] = false;
        reloadFlags[2] = false;

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    Button.OnClickListener mainActivityButtonListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch(id) {
                case R.id.btnETA:
                    Intent intentETA = new Intent(MainActivity.this, ETAActivity.class);
                    startActivity(intentETA);
                    break;
                case R.id.btnFareCalculator:
                    Intent intentFC = new Intent(MainActivity.this, FareCalculatorActivity.class);
                    startActivity(intentFC);
                    break;
                case R.id.btnAlightingAlarm:
                    Intent intentAA = new Intent(MainActivity.this, AlightingAlarmActivity.class);
                    startActivity(intentAA);
                    break;
                default:
                    break;
            }
        }
    };

    Button.OnClickListener testOnClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch(id) {
                case R.id.btnTestUpload:
                    Toast.makeText(MainActivity.this, "Uploading", Toast.LENGTH_SHORT).show();
                    UploadData uploader = new UploadData(getApplicationContext());
                    uploader.execute();
                    Toast.makeText(MainActivity.this, "Upload finishes", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btnTestDownload:
                    downloadData(false);
                    break;
                case R.id.btnTestUpdate:
                    downloadData(true);
                    break;
                case R.id.btnTestFireDownload:
                    downloadFirebaseData();
                default:
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        dismissDownloadDialog();
        dismissLoadingDialog();
        dismissConnectionDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.busServicesList = BusServicesListHolder.getInstance().getData();
        this.busStopsList = BusStopsListHolder.getInstance().getData();

        if (this.busServicesList.size() != 0 && this.busStopsList.size() != 0)  {
            Log.d(TAG, "onResume: data restored with size " + this.busStopsList.size() + " and " + this.busServicesList.size());
            return;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onDataLoaded(int flag) {
        dismissLoadingDialog();
        if (flag == LoadLocalData.LOAD_OK) {
            Log.d(TAG, "onDataLoaded: Local Cached Data Successful Loaded");
            Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "onDataLoaded: Local Cached Data Load Failure");
            downloadFirebaseData();
        }
    }

    private void downloadData(boolean runOnBackground) {

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            showConnectionDialog();
            return;
        }
        Toast.makeText(MainActivity.this, "Downloading Bus Stop Data", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: Downloading Bus Stop Data");
        GetJSONBusStopData getJSONBusStopData = new GetJSONBusStopData(MainActivity.this, getApplicationContext(), BUS_STOPS_URL, runOnBackground);
        getJSONBusStopData.execute();
        Toast.makeText(MainActivity.this, "Downloading Bus Routes Data", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: Downloading Bus Route Data");
        GetJSONBusRouteData getJSONData = new GetJSONBusRouteData(MainActivity.this, getApplicationContext(), BUS_ROUTES_URL, runOnBackground);
        getJSONData.execute();

    }

    private void downloadFirebaseData() {

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            showConnectionDialog();
            return;
        }

        showDownloadDialog();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference busDataRef = storageRef.child("overview/BusData");
        File busRouteFile = new File(getFilesDir(), BUS_ROUTES_FILENAME);

        OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: download failure");
            }
        };

        busDataRef.child(BUS_ROUTES_FILENAME).getFile(busRouteFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: download bus route file");
                reloadLocalData(3);
                if (MainActivity.this.downloadDialog != null && MainActivity.this.downloadDialog.isShowing())
                    MainActivity.this.downloadDialog.dismiss();
                FirebaseDataHandler handler = new FirebaseDataHandler(MainActivity.this);
                handler.execute();
            }
        }).addOnFailureListener(failureListener);

        File busServiceFile = new File(getFilesDir(), BUS_SERVICES_FILENAME);
        busDataRef.child(BUS_SERVICES_FILENAME).getFile(busServiceFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: download bus service data");
                reloadLocalData(1);
            }
        }).addOnFailureListener(failureListener);

        File busStopFile = new File(getFilesDir(), BUS_STOPS_FILENAME);
        busDataRef.child(BUS_STOPS_FILENAME).getFile(busStopFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: download bus stop file");
                reloadLocalData(2);
            }
        }).addOnFailureListener(failureListener);

        Log.d(TAG, "downloadFirebaseData: download complete, loading from files");
    }

    @Override
    public void onBusRouteDataAvailable(List<BusRoute> data, DownloadStatus status) {

        BusRoutesDataHandler handler = new BusRoutesDataHandler(getApplicationContext());
        handler.execute(data);

    }

    private void reloadLocalData(int flagNo) {
        switch(flagNo) {
            case 1:
                this.reloadFlags[0] = true;
                break;
            case 2:
                this.reloadFlags[1] = true;
                break;
            case 3:
                this.reloadFlags[2] = true;
                break;
            default:
                break;
        }
        if (reloadFlags[0] && reloadFlags[1] && reloadFlags[2]) {
            dismissDownloadDialog();
            Toast.makeText(this, "Data updated - reloading", Toast.LENGTH_SHORT).show();
            showLoadingDialog();
            LoadLocalData loader = new LoadLocalData(this, this);
            loader.execute();
            reloadFlags[0] = false;
            reloadFlags[1] = false;
            reloadFlags[2] = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBusStopDataAvailable(List<BusStop> data, DownloadStatus status) {
        Log.d(TAG, "onDataAvailable: " + data);
        this.busStopsList = (ArrayList) data;
        try {
            Log.d(TAG, "onBusStopDataAvailable: writing data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_STOPS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusStopDataAvailable: writing data finished");
            Log.d(TAG, "onBusStopDataAvailable: data size: " + data.size());
        } catch (FileNotFoundException e) {
            Log.e(TAG, "onBusStopDataAvailable: File Not Found - BusStopsList");
        } catch (IOException e) {
            e.printStackTrace();
        }

        busStopsMap = new HashMap<String, BusStop>();
        for (int i = 0; i < data.size(); i++) {
            BusStop busStop = data.get(i);
            busStopsMap.put(busStop.getBusStopCode(), busStop);
        }
        try {
            Log.d(TAG, "onBusStopDataAvailable: map writing data");
            FileOutputStream fos = getApplicationContext().openFileOutput(BUS_STOPS_MAP_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(busStopsMap);
            oos.close();
            fos.close();
            Log.d(TAG, "onBusStopDataAvailable: map writing data finished");
            Log.d(TAG, "onBusStopDataAvailable: Map size: " + busStopsMap.size());
        } catch (FileNotFoundException e) {
            Log.d(TAG, "onBusStopDataAvailable: File Not Found - BusStopsMap ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        BusStopsListHolder.getInstance().setData(busStopsList);
    }

    private void showLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing())
            return;
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle("Loading");
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
    }

    private void showDownloadDialog() {
        if (downloadDialog != null && downloadDialog.isShowing())
            return;
        downloadDialog = new ProgressDialog(this);
        downloadDialog.setTitle("Downloading...");
        downloadDialog.setMessage("just a few a few seconds");
        downloadDialog.setCancelable(false);
        downloadDialog.show();
    }

    private void dismissDownloadDialog() {
        if (downloadDialog != null && downloadDialog.isShowing())
            downloadDialog.dismiss();
    }
}
