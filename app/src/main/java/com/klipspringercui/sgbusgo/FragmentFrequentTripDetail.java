package com.klipspringercui.sgbusgo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.klipspringercui.sgbusgo.BaseActivity.ACTIVATED_FREQUENT_TRIP_FILENAME;
import static com.klipspringercui.sgbusgo.BaseActivity.FREQUENT_TRIP_FILENAME;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentFrequentTripDetail.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class FragmentFrequentTripDetail extends DialogFragment {

    private static final String TAG = "FrequentTripDetail";
    private static final String FREQUENT_TRIP = "FERQUENT_TRIP";
    private static final String TRIP_DETAIL = "Trip Detail";

    private FrequentTrip ft;
    private FrequentTrip activated_ft;

    private OnFragmentInteractionListener mListener;
    private Context mContext;

    private DialogInterface.OnDismissListener onDismissListener;

    TextView tripTime;
    TextView startingBusStop;
    TextView alightingBusStop;
    TextView busService;
    Button btnActivate;
    Button btnDelete;

    public FragmentFrequentTripDetail() {
        // Required empty public constructor
    }

    public static FragmentFrequentTripDetail newInstance(FrequentTrip ft) {
        FragmentFrequentTripDetail fragment = new FragmentFrequentTripDetail();
        Bundle args = new Bundle();
        args.putSerializable(FREQUENT_TRIP, ft);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ft = (FrequentTrip) getArguments().getSerializable(FREQUENT_TRIP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MyProfileActivity) getActivity()).setActionBarTitle(TRIP_DETAIL);

        View view  = inflater.inflate(R.layout.fragment_fragment_frequent_trip_detail, container, false);

        tripTime = (TextView) view.findViewById(R.id.frequentTripDetailTime);
        startingBusStop = (TextView) view.findViewById(R.id.startingBusStop) ;
        alightingBusStop = (TextView) view.findViewById(R.id.alightingBusStop);
        busService = (TextView) view.findViewById(R.id.busService);
        btnActivate = (Button) view.findViewById(R.id.btnFTDetailActivate);
        btnDelete = (Button) view.findViewById(R.id.btnFTDetailDelete);

        tripTime.setText(ft.getTime());
        startingBusStop.setText("Starting Bus Stop: " + ft.getStartingBusStop().getDescription());
        alightingBusStop.setText("Alighting Bus Stop: " + ft.getAlightingBusStop().getDescription());
        busService.setText("Bus Service: " + ft.getServiceNo());

        activated_ft = getActivatedFrequentTrip();
        if (activated_ft != null && activated_ft.getId() == ft.getId()) {
            btnActivate.setText("Deactivate");
            btnActivate.setOnClickListener(deactivateOnClickListener);
        } else {
            btnActivate.setOnClickListener(activateOnClickListener);
        }

        btnDelete.setOnClickListener(deleteOnClickListener);

        // Inflate the layout for this fragment
        return view;
    }

    Button.OnClickListener activateOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                FileOutputStream fos = getActivity().openFileOutput(ACTIVATED_FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(ft);
                oos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "FT Detail: FileNotFound Exception");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "FT Detail: IO Exception");
                e.printStackTrace();
            }
            dismiss();
        }
    };

    Button.OnClickListener deactivateOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                // Hopefully this will clean the content of the file :)
                Log.d(TAG, "FT Detail: deacivate trip -> deleting file content");
                FileOutputStream fos = getActivity().openFileOutput(ACTIVATED_FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "FT Detail: FileNotFound Exception");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "FT Detail: IO Exception");
                e.printStackTrace();
            }

            dismiss();
        }
    };

    Button.OnClickListener deleteOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // A alert dialog asking for user's confirmation
            Log.d(TAG, "FT Detail: confirmation dialog");
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Deleting Trip")
                    .setMessage("Are you sure you want to delete this record?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(TAG, "FT Detail: yes button pressed");
                            boolean result = deleteSavedFrequentTrip(ft);
                            if (result) { showDeleteComplete(); }
                            else {showDeleteFail();}
                            dismiss();
                        }

                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    };

    private void showDeleteFail() {
        new AlertDialog.Builder(mContext)
                .setTitle("Deleting Trip")
                .setMessage("Delete failed, something went wrong!")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showDeleteComplete() {
        new AlertDialog.Builder(mContext)
                .setTitle("Deleting Trip")
                .setMessage("Record Deleted.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private boolean deleteSavedFrequentTrip(FrequentTrip item) {
        int itemId = item.getId();
        boolean result = false;
        ArrayList<FrequentTrip> tripList;
        try {
            Log.d(TAG, "FT Detail: deleteFT");
            FileInputStream fis =mContext.openFileInput(FREQUENT_TRIP_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            tripList = (ArrayList) ois.readObject();
            for (FrequentTrip trip : tripList) {
                if (trip.getId() == itemId) {
                    tripList.remove(trip);
                    Log.d(TAG, "FT Detail: deleteFT -> Record deleted");
                    result = true;
                    break;
                } else {
                    Log.d(TAG, "FT Detail: deleteFT -> No record found");
                }
            }
            ois.close();
            fis.close();
            if (result) {
                LocalDB.getInstance().setFrequentTripsData(tripList);
                FileOutputStream fos = mContext.openFileOutput(FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(tripList);
                oos.close();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FT Detail: deleteFT -> FileNotFound Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "FT Detail: deleteFT -> IO Exception");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "FT Detail: deleteFT -> ClassNotFound Exception");
            e.printStackTrace();
        }

        // If the deleted trip is also activated, deactivate it
        try {
            // Hopefully this will clean the content of the file :)
            Log.d(TAG, "FT Detail: delete FT -> deactivate trip");
            FileOutputStream fos = getActivity().openFileOutput(ACTIVATED_FREQUENT_TRIP_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FT Detail: FileNotFound Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "FT Detail: IO Exception");
            e.printStackTrace();
        }
        return result;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(FrequentTrip item) {
        if (mListener != null) {
            mListener.onFragmentInteraction(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            mContext = context;
            Log.d(TAG, "FT Detail: mListener assigned");
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Log.d(TAG, "FT Detail: mListener de-assigned");
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    // Must check null after invoking this method
    private FrequentTrip getActivatedFrequentTrip() {
        FrequentTrip result = null;
        try {
            FileInputStream fis = mContext.openFileInput(ACTIVATED_FREQUENT_TRIP_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            result = (FrequentTrip) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException e) {
            Log.d(TAG, "getActivatedFT: IO Exception");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "getActivatedFT: ClassNotFound Exception");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(FrequentTrip item);
    }

}
