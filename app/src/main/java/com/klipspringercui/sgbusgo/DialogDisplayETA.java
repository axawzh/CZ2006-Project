package com.klipspringercui.sgbusgo;

import android.content.Context;
import android.media.Image;
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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.klipspringercui.sgbusgo.R.id.busService;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DialogDisplayETA.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DialogDisplayETA#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogDisplayETA extends DialogFragment implements GetJSONETAData.ETADataAvailableCallable {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = "DialogDisplayETA";
    private static final String ETAD_BUS_STOP = "ETAD_BUS_STOP";
    private static final String ETAD_BUS_SERVICE_NO = "ETAD_BUS_SERVICE_NO";

    private List<ETAItem> etaList;
    private BusStop busStop;
    private String busServiceNo;

    private OnFragmentInteractionListener mListener;
    ETADRecyclerViewAdapter recyclerViewAdapter;
    ImageButton btnRefresh;
    ImageView imgRefresh;

    Animation animation;


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
    public static DialogDisplayETA newInstance(BusStop busStop, String busServiceNo) {
        Log.d(TAG, "newInstance: starts");
        DialogDisplayETA fragment = new DialogDisplayETA();

        Bundle args = new Bundle();
        args.putSerializable(ETAD_BUS_STOP, busStop);
        args.putString(ETAD_BUS_SERVICE_NO, busServiceNo);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            busStop = (BusStop) bundle.getSerializable(ETAD_BUS_STOP);
            busServiceNo = bundle.getString(ETAD_BUS_SERVICE_NO);
        }
        animation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(3000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().setTitle("Estimated Arrival Time");
        View view = inflater.inflate(R.layout.fragment_dialog_display_et, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRefresh = (ImageButton) view.findViewById(R.id.btnETARefresh);
        //imgRefresh = (ImageView) view.findViewById(R.id.btnETARefresh);
        btnRefresh.setOnClickListener(RefreshListener);
        RecyclerView etadRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_ETAD);
        etadRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewAdapter = new ETADRecyclerViewAdapter(this.etaList, this.busStop);
        etadRecyclerView.setAdapter(recyclerViewAdapter);
        getDialog().setTitle("Estimated Arrival Time");

    }

    ImageButton.OnClickListener RefreshListener = new ImageButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            GetJSONETAData getJSONETAData = new GetJSONETAData(DialogDisplayETA.this, BaseActivity.ETA_URL);
            //imgRefresh.setAnimation(animation);
            if (busServiceNo == null)
                getJSONETAData.execute(busStop.getBusStopCode());
            else
                getJSONETAData.execute(busStop.getBusStopCode(), busServiceNo);
        }
    };

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
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
        //This part is added
        GetJSONETAData getJSONETAData = new GetJSONETAData(this, BaseActivity.ETA_URL);
        if (this.busServiceNo == null)
            getJSONETAData.execute(busStop.getBusStopCode());
        else
            getJSONETAData.execute(busStop.getBusStopCode(), busServiceNo);
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

    @Override
    public void onETADataAvailable(List<ETAItem> data, String serviceNo, String busStopCode) {
        Log.d(TAG, "onETADataAvailable: called with data " + data);
        Toast.makeText(getActivity().getApplicationContext(), "Estimated Arrival Time Refreshed", Toast.LENGTH_SHORT).show();
        //imgRefresh.clearAnimation();
        this.etaList = data;
        recyclerViewAdapter.loadNewData(data, busStop);
    }
}
