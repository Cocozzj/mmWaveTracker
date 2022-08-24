package com.example.mmwavetracker.UI;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mmwavetracker.core.config;
import com.example.mmwavetracker.R;


public class FragmentMain3 extends Fragment {

    public static TextView NetworkTypetv,cpuTemperatureTV, batteryTemperatureTV, distanceTV, tracerInterval, pingEnabledFlag, tcpdumpSettings, isNSA5GTv;
    public static TextView sessionID;
    ViewGroup rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_main_3, container, false);

        //cpuTemperatureTV = rootView.findViewById(R.id.cpu_temperature_tv);
        //batteryTemperatureTV = rootView.findViewById(R.id.battery_temperature_tv);
        pingEnabledFlag = rootView.findViewById(R.id.ping_enabled_tv);
        tcpdumpSettings = rootView.findViewById(R.id.tcpdump_tv);
        tracerInterval = rootView.findViewById(R.id.tracer_interval_tv);

        updateTracerInterval();
        updatePingFlag();
        updateTCPDumpSettings();

        return rootView;

    }


    public void updatePingFlag() {
        if (config.PING_ENABLE_FLAG) {
            pingEnabledFlag.setText("Yes");
        } else {
            pingEnabledFlag.setText("No");
        }

    }

    public void updateTCPDumpSettings() {
        if (config.TCPDUMP_ENABLE_FLAG) {
            tcpdumpSettings.setText("-s" + config.TCPDUMP_SNAP_INTERVAL + " -C " + config.TCPDUMP_FILESIZE);
        } else {
            tcpdumpSettings.setText("Disabled");
        }
    }

    public void updateTracerInterval() {
        tracerInterval.setText(config.SAMPLING_RATE_IN_MS + " ms");
    }

}
