package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static com.klipspringercui.sgbusgo.BaseActivity.BUS_ROUTES_FILENAME;

/**
 * Created by Kevin on 15/3/17.
 */

class UploadData extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "UploadData";
    Context mContext;

    public UploadData(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: starts");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference busDataReference = storageReference.child("overview/BusData");
//        Log.d(TAG, "doInBackground: path " + storageReference.getPath());
//        Log.d(TAG, "doInBackground: path2 " + busDataReference.getPath());

        File[] allfiles = mContext.getFilesDir().listFiles();
        for (int i = 0; i < allfiles.length; i++) {
            if (allfiles[i].isDirectory())
                continue;
            String filename = allfiles[i].getName();
            if (filename.equals(BaseActivity.BUS_SERVICES_FILENAME) || filename.equals(BaseActivity.BUS_STOPS_FILENAME) || filename.equals(BaseActivity.BUS_ROUTES_FILENAME)) {
                Log.d(TAG, "doInBackground: uploading " + allfiles[i].getName());
                Uri file = Uri.fromFile(allfiles[i]);
                UploadTask uploadTask = busDataReference.child(allfiles[i].getName()).putFile(file);
                uploadTask.addOnFailureListener(flistener).addOnSuccessListener(slistener);
            }
        }
        return null;
    }

    private OnFailureListener flistener =  new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            // Handle unsuccessful uploads
        }
    };

    private OnSuccessListener slistener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            Log.d(TAG, "onSuccess: upload success");
            //Uri downloadUrl = taskSnapshot.getDownloadUrl();
        }
    };


}
