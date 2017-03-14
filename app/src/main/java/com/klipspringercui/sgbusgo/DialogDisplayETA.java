package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DialogDisplayETA.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DialogDisplayETA#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogDisplayETA extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ETAD_BUSSTOP = "ETAD_BUSSTOP";
//    private static final String ETAD_BUSSERVICENO = "ETAD_BUSSERVICENO";
//    private static final String ETAD_NEXTETA = "ETAD_NEXTETA";
//    private static final String ETAD_SUBETA = "ETAD_SUBETA";
//    private static final String ETAD_SUBTETA = "ETAD_SUBTETA";
    private static final String TAG = "DialogDisplayETA";

    private static final String ETAD_ITEMNUM = "ETAD_ITEMNUM";
    private static final String ETAD_ITEM = "ETAD_ITEM";
    private static final String ETAD_BUSSTOP = "ETAD_BUSSTOP";

    // TODO: Rename and change types of parameters
//    private String serviceNo;
//    private String busStop;
//    private String nextETA;
//    private String subETA;
//    private String sub3ETA;
//
//    TextView txtBusStop = null;
//    TextView txtBusServiceNo = null;
//    TextView txtNextETA = null;
//    TextView txtSubETA = null;
//    TextView txtSub3ETA = null;

    private List<ETAItem> etaList;
    private BusStop busStop;

    private OnFragmentInteractionListener mListener;
    ETADRecyclerViewAdapter recyclerViewAdapter;


    public DialogDisplayETA() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DialogDisplayETA.
     */
    // TODO: Rename and change types and number of parameters
    public static DialogDisplayETA newInstance(List<ETAItem> data, BusStop busStop) {
        DialogDisplayETA fragment = new DialogDisplayETA();
        Bundle args = new Bundle();
        args.putInt(ETAD_ITEMNUM, data.size());
        args.putSerializable(ETAD_BUSSTOP, busStop);
        for (int i = 0; i < data.size(); i++) {
            String key = ETAD_ITEM + i;
            args.putSerializable(key, data.get(i));
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            etaList = new ArrayList<ETAItem>();
            busStop = (BusStop) bundle.getSerializable(ETAD_BUSSTOP);
            int itemCount = bundle.getInt(ETAD_ITEMNUM);
            for (int i = 0; i < itemCount; i++) {
                String key = ETAD_ITEM + i;
                ETAItem item = (ETAItem) bundle.getSerializable(key);
                etaList.add(item);
            }
        }
        Log.d(TAG, "onCreate: etaList size : " + etaList.size());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_display_et, container, false);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        txtBusServiceNo = (TextView) view.findViewById(R.id.txtETADServiceNo);
//        txtBusStop = (TextView) view.findViewById(R.id.txtETADBusStop);
//        txtNextETA = (TextView) view.findViewById(R.id.txtETADNext);
//        txtSubETA = (TextView) view.findViewById(R.id.txtETADSub);
//        txtSub3ETA = (TextView) view.findViewById(R.id.txtETADSub3);
//
//        txtBusServiceNo.setText(this.serviceNo);
//        txtBusStop.setText(this.busStop);
//        txtNextETA.setText(this.nextETA);
//        txtSubETA.setText(this.subETA);
//        txtSub3ETA.setText(this.sub3ETA);
        RecyclerView etadRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_ETAD);
        etadRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewAdapter = new ETADRecyclerViewAdapter(this.etaList, this.busStop);
        etadRecyclerView.setAdapter(recyclerViewAdapter);
        getDialog().setTitle("Estimated Arrival Time");

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void onFragmentInteraction(Uri uri);
    }
}
