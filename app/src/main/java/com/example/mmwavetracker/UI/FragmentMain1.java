package com.example.mmwavetracker.UI;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mmwavetracker.R;
import com.example.mmwavetracker.Service.MonitoringService;

public class FragmentMain1 extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public static TextView towersNumTV;
    public static TextView percentage5GTV;
    public static TextView curTowerTV;
    public static TextView verticalHandoffTV;
    public static TextView horizontalHandoffTV;
    public static TextView signalStrength4GTV;
    public static TextView signalStrength5GTV;
    ViewGroup rootView;

    //Service Management, handles setup, binding, and access to the monitoring service

    public static MonitoringService monitorService = null;
    public static boolean serviceBound = false;

    //variables for timer
    public static Chronometer timer;

    Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_main_1, container, false);

        timer = (Chronometer) rootView.findViewById(R.id.TimerChronometer);
        towersNumTV = rootView.findViewById(R.id.tower_num_tv);
        percentage5GTV = rootView.findViewById(R.id.percentage_5g_tv);
        curTowerTV = rootView.findViewById(R.id.cur_tower_tv);
        verticalHandoffTV = rootView.findViewById(R.id.vertical_handoff_tv);
        horizontalHandoffTV = rootView.findViewById(R.id.horizontal_handoff_tv);

        signalStrength4GTV = rootView.findViewById(R.id.g4g_strength_tv);
        signalStrength5GTV = rootView.findViewById(R.id.ssrsrp_tv);

        return rootView;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

}