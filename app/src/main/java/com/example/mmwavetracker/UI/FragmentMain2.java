package com.example.mmwavetracker.UI;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mmwavetracker.R;


public class FragmentMain2 extends Fragment {

    public static TextView NetworkTypetv, distanceTV, tracerInterval, pingEnabledFlag, tcpdumpSettings, isNSA5GTv,isSA5GTv,ConnectTypetv,lteCitv,ltePcitv,nrCitv,nrPcitv;
    public static TextView sessionID;
    ViewGroup rootView;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_main_2, container, false);

        distanceTV = rootView.findViewById(R.id.distance_tv);

        sessionID = rootView.findViewById(R.id.session_id_tv);
        isNSA5GTv = rootView.findViewById(R.id.fivedNSAType_tv);
        isSA5GTv = rootView.findViewById(R.id.fivedSAType_tv);
        ConnectTypetv = rootView.findViewById(R.id.ConnectType_tv);
        NetworkTypetv = rootView.findViewById(R.id.NetworkType_tv);
        lteCitv=rootView.findViewById(R.id.lteCi_tv);
        ltePcitv=rootView.findViewById(R.id.ltePci_tv);
        nrCitv=rootView.findViewById(R.id.nrCi_tv);
        nrPcitv=rootView.findViewById(R.id.nrPci_tv);


        return rootView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

}

