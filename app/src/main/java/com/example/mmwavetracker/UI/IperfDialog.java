package com.example.mmwavetracker.UI;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.mmwavetracker.core.config;
import com.example.mmwavetracker.R;
import com.example.mmwavetracker.activity.MapActivity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class IperfDialog extends AppCompatDialogFragment {
    EditText task;
    Context context;
    Switch verboseSw, udpSw, tcpdumpSw, downlinkSw;
    Spinner spinnerHostName, spinnerParallel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.iperf_dialog,null);

        //initializing

        final EditText etIperfPort = (EditText) view.findViewById(R.id.editText_iperf_port);
        spinnerParallel = (Spinner) view.findViewById(R.id.spin_choseParallel);
        final MultiAutoCompleteTextView mactv = view.findViewById(R.id.multiAutoCompleteTextViewIperfLabels);
        spinnerHostName = (Spinner) view.findViewById(R.id.spin_choseHostname);
        final EditText etIperfDuration = (EditText) view.findViewById(R.id.editText_iperf_duration);
        verboseSw = view.findViewById(R.id.verbose_sw);
        verboseSw.setEnabled(true);
        verboseSw.setChecked(config.LOG_VERBOSE_FLAG);
        tcpdumpSw = view.findViewById(R.id.iperf_tcpdump_sw);
        tcpdumpSw.setEnabled(true);
        tcpdumpSw.setChecked(MapActivity.iperf_tcpdump_flag);
        downlinkSw = view.findViewById(R.id.iperf_downlink_sw);
        downlinkSw.setEnabled(true);
        downlinkSw.setChecked(MapActivity.iperf_downlink_flag);

        // get tcpdump -s argument value
        final EditText etTcpDumpSvalue = (EditText) view.findViewById(R.id.iperf_tcpdump_snap_interval);
        etTcpDumpSvalue.setText(Integer.toString(MapActivity.iperf_tcpdump_interval));

        // get tcpdump -C argument value
        final EditText etTcpDumpCvalue = (EditText) view.findViewById(R.id.iperf_tcpdump_file_size_limit_et);
        etTcpDumpCvalue.setText(Integer.toString(MapActivity.iperf_tcpdump_filesize));

        //restore from the MapActivity data
        etIperfDuration.setText(Integer.toString(MapActivity.iperf_duration));
        mactv.setText(MapActivity.iperf_run_label);
        etIperfPort.setText(Integer.toString(MapActivity.iperf_port));

        // set iperf interval
        final EditText etIperfInterval = (EditText) view.findViewById(R.id.editText_iperf_interval);
        etIperfInterval.setText(Integer.toString(MapActivity.iperf_interval));

        ArrayAdapter<Integer> spinnerArrayAdapter1 = new ArrayAdapter<Integer>(context, android.R.layout.simple_spinner_item, config.IPERF_PARALLEL_VALUES);
        spinnerParallel.setAdapter(spinnerArrayAdapter1);
        int spinnerPosition = spinnerArrayAdapter1.getPosition(MapActivity.iperf_tcp_parallel);
        spinnerParallel.setSelection(spinnerPosition);
        spinnerParallel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
                MapActivity.iperf_tcp_parallel = config.DEFAULT_IPERF_PARALLEL;
            }
        });
        // setup run labels (auto complete)
        String autoCompletes[] = getResources().getStringArray(R.array.FileNameCompletions);

        //Load in the shared resources
        List<String> loadedPrefixes = new LinkedList<String>();

        try {
            loadedPrefixes = Files.readAllLines(Paths.get(config.GetConfigFileFullPath(config.SHARED_AUTO_COMPLETES)));
            //Remove Header GetconfigFileFullPath
            loadedPrefixes.remove(0);

            loadedPrefixes.addAll(Arrays.asList(autoCompletes));

            autoCompletes = loadedPrefixes.toArray(autoCompletes);
        } catch (Exception e) {
            Toast.makeText(context, "Unable to load shared Prefixes, using defaults.", Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, autoCompletes);
        mactv.setAdapter(arrayAdapter2);
        //Assign custom adapter to auto complete
//        mactv.setTokenizer(new SpacingTokenizer());



        // TCP or UDP, bandwidth (-b)
        udpSw = view.findViewById(R.id.udp_sw);
        final EditText etUDPBandwidth = (EditText) view.findViewById(R.id.editText_udp_bandwidth);
        if (MapActivity.iperf_udp_flag) {
            udpSw.setChecked(true);
            udpSw.setEnabled(true);
            spinnerParallel.setEnabled(false);
            etUDPBandwidth.setEnabled(true);
        } else {
            udpSw.setChecked(false);
            udpSw.setChecked(false);
            spinnerParallel.setEnabled(true);
            etUDPBandwidth.setEnabled(false);
        }
        etUDPBandwidth.setText(MapActivity.iperf_udp_bandwidth);
        udpSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    spinnerParallel.setEnabled(false);
                    etUDPBandwidth.setEnabled(true);
                } else {
                    spinnerParallel.setEnabled(true);
                    etUDPBandwidth.setEnabled(false);
                }
            }
        });

        // Populate Hostnames
        int position =0;
        try {
            List<String> iperf_allhosts = Files.readAllLines(Paths.get(config.GetConfigFileFullPath("hostnames.csv")));
            String[] mItems = new String[iperf_allhosts.size()];
            int i = 0;
            for (String iperf_host : iperf_allhosts) {
                String[] vals = iperf_host.split(config.CSV_DELIMIETER);
                MapActivity.iperf_HostNames_all.add(new Pair(vals[0], vals[1]));
                if(vals[1].equals(MapActivity.iperf_HostName) && vals[0].equals(MapActivity.iperf_HostName_label)){
                    position = i;
                }
                mItems[i] = vals[0];
                i++;
            }

            // Spinner code I fond online:
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    context, android.R.layout.simple_spinner_item, mItems);
            spinnerHostName.setAdapter(spinnerArrayAdapter);
            spinnerHostName.setSelection(position);
            spinnerHostName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                    MapActivity.iperf_HostName = MapActivity.iperf_HostNames_all.get(0).second;
                    MapActivity.iperf_HostName_label = MapActivity.iperf_HostNames_all.get(0).first;
                }
            });
        } catch (IOException e) {
            Toast.makeText(context, "Could not load the iperf hostnames.", Toast.LENGTH_SHORT).show();
        }

        builder.setView(view)
                .setTitle("Edit Iperf")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etIperfDuration.setText(Integer.toString(MapActivity.iperf_duration));
                        mactv.setText(MapActivity.iperf_run_label);
                        etIperfPort.setText(Integer.toString(MapActivity.iperf_port));

                    }
                })
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // check if UDP, validate bandwidth
                        if (udpSw.isChecked()) {
                            int length = etUDPBandwidth.getText().toString().length(); //MapActivity.iperf_udp_bandwidth.length();
                            if (length > 0 && !Pattern.matches("^[\\d]+[M|K]$", etUDPBandwidth.getText().toString())) {
                                MapActivity.iperf_udp_bandwidth = config.DEFAULT_IPERF_UDP_BANDWIDTH;
                                Toast.makeText(context, "Invalid input for -b, reset to default: 3000M", Toast.LENGTH_SHORT).show();
                            } else if (Pattern.matches("^[\\d]+[M|K]$", etUDPBandwidth.getText().toString())) {
                                // bandwidth input looks good
                                MapActivity.iperf_udp_flag = true;
                                MapActivity.iperf_udp_bandwidth = etUDPBandwidth.getText().toString();
                            }
                        } else {
                            MapActivity.iperf_udp_flag = false;
                        }

                        if(downlinkSw.isChecked()){
                            MapActivity.iperf_downlink_flag = true;
                        }
                        else {
                            MapActivity.iperf_downlink_flag = false;
                        }

                        if(verboseSw.isChecked()){
                            config.LOG_VERBOSE_FLAG=true;
                        }else{
                            config.LOG_VERBOSE_FLAG=false;
                        }

                        /* TCPDUMP */
                        if (tcpdumpSw.isChecked()) {
                            MapActivity.iperf_tcpdump_flag = true;
                        }
                        else {
                            MapActivity.iperf_tcpdump_flag = false;
                        }

                        // set tcpdump -s value
                        if (etTcpDumpSvalue.getText().toString().length() == 0) {
                            MapActivity.iperf_tcpdump_interval = config.DEFAULT_IPERF_TCPDUMP_SNAP_INTERVAL;
                        } else {
                            MapActivity.iperf_tcpdump_interval = Integer.parseInt(etTcpDumpSvalue.getText().toString());
                        }

                        // set tcpdump -C value
                        if (etTcpDumpCvalue.getText().toString().length() == 0) {
                            MapActivity.iperf_tcpdump_filesize = config.DEFAULT_IPERF_TCPDUMP_FILESIZE;
                        } else {
                            MapActivity.iperf_tcpdump_filesize = Integer.parseInt(etTcpDumpCvalue.getText().toString());
                        }

                        // set iperf label
                        if (mactv.getText().toString().length() == 0) {
                            MapActivity.iperf_run_label = "";
                        } else {
                            MapActivity.iperf_run_label = MapActivity.CorrectLabel(mactv.getText().toString());
                            Toast.makeText(context, "Run Label: " + MapActivity.iperf_run_label, Toast.LENGTH_SHORT).show();
                        }

                        // set iperf hostname
                        MapActivity.iperf_HostName = MapActivity.iperf_HostNames_all.get(spinnerHostName.getSelectedItemPosition()).second;
                        MapActivity.iperf_HostName_label = MapActivity.iperf_HostNames_all.get(spinnerHostName.getSelectedItemPosition()).first;

                        // set iperf parallel setting
                        MapActivity.iperf_tcp_parallel = config.IPERF_PARALLEL_VALUES[spinnerParallel.getSelectedItemPosition()];

                        // set iPerf Interval
                        MapActivity.iperf_interval = Integer.parseInt(etIperfInterval.getText().toString());

                        // set iperf port
                        if (etIperfPort.getText().toString().length() == 0) {
                            MapActivity.iperf_port = config.DEFAULT_IPERF_PORT;
                        } else {
                            MapActivity.iperf_port = Integer.parseInt(etIperfPort.getText().toString());
                        }

                        // set iperf duration
                        if (etIperfDuration.getText().toString().length() == 0) {
                            MapActivity.iperf_duration = config.DEFAULT_IPERF_DURATION;
                        } else {
                            MapActivity.iperf_duration = Integer.parseInt(etIperfDuration.getText().toString());
                        }
                        setIperfJSON();

                        SharedPreferences.Editor editor = MapActivity.app_shared_pref.edit();
                        editor.putInt("iperf_duration",MapActivity.iperf_duration);
                        editor.putString("iperf_HostName",MapActivity.iperf_HostName);
                        editor.putString("iperf_HostName_Label", MapActivity.iperf_HostName_label);
                        editor.putInt("iperf_parallel",MapActivity.iperf_tcp_parallel);
                        editor.putBoolean("iperf_udp_enabled", MapActivity.iperf_udp_flag);
                        editor.putString("iperf_udp_bandwidth", MapActivity.iperf_udp_bandwidth);
                        editor.putInt("iperf_interval", MapActivity.iperf_interval);
                        editor.putString("iperf_run_label",MapActivity.iperf_run_label);
                        editor.putInt("iperf_port",MapActivity.iperf_port);
                        editor.putBoolean("iperf_tcpdump", MapActivity.iperf_tcpdump_flag);
                        editor.putInt("iperf_tcpdump_filesize", MapActivity.iperf_tcpdump_filesize);
                        editor.putInt("iperf_tcpdump_interval", MapActivity.iperf_tcpdump_interval);
                        editor.apply();

                        FragmentAppIperf.settingTcpUdpTv.setText(String.valueOf(MapActivity.iperf_tcp_parallel));
                        FragmentAppIperf.durationTv.setText(String.valueOf(MapActivity.iperf_duration));
                        FragmentAppIperf.intervalTv.setText(String.valueOf(MapActivity.iperf_interval/1000.0).replaceAll("\\.0*$", ""));
                        FragmentAppIperf.portTv.setText(String.valueOf(MapActivity.iperf_port));
                        FragmentAppIperf.directionTv.setText(MapActivity.iperf_downlink_flag ? "-R" : "");
                        FragmentAppIperf.runLabelTv.setText(MapActivity.iperf_run_label);
                        // check if tcp/udp then set the string accordingly
                        FragmentAppIperf.hostnameTv.setText(MapActivity.iperf_HostName_label);
                        if (MapActivity.iperf_udp_flag) {
                            // udp enabled, set -b
                            FragmentAppIperf.flagTcpUdpTv.setText("UDP");
                            FragmentAppIperf.settingTcpUdpTv.setText("-b " + MapActivity.iperf_udp_bandwidth);
                        } else {
                            // tcp enabled, set -P
                            FragmentAppIperf.flagTcpUdpTv.setText("TCP");
                            FragmentAppIperf.settingTcpUdpTv.setText("-P " + String.valueOf(MapActivity.iperf_tcp_parallel));
                        }
                        FragmentAppIperf.tcpdumpTV.setText(Boolean.toString(MapActivity.iperf_tcpdump_flag));
                    }
                });
        return builder.create();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
    public void setIperfJSON() {
        if (MapActivity.iperf_duration > config.DEFAULT_IPERF_JSON_TIME_THRESHOLD) {
            MapActivity.iperf_json_flag = false;
            MapActivity.iperf_verbose_flag = 1;
        } else {
            MapActivity.iperf_json_flag = true;
            MapActivity.iperf_verbose_flag = 0;
        }
    }

    public static void restoreIperfSetup(){
        MapActivity.iperf_duration = MapActivity.app_shared_pref.getInt("iperf_duration",config.DEFAULT_IPERF_DURATION);
        MapActivity.iperf_HostName = MapActivity.app_shared_pref.getString("iperf_HostName","");
        MapActivity.iperf_HostName_label = MapActivity.app_shared_pref.getString("iperf_HostName_Label", "");
        MapActivity.iperf_port = MapActivity.app_shared_pref.getInt("iperf_port",config.DEFAULT_IPERF_PORT);
        MapActivity.iperf_run_label = MapActivity.app_shared_pref.getString("iperf_run_label","");
        MapActivity.iperf_tcp_parallel = MapActivity.app_shared_pref.getInt("iperf_parallel",config.DEFAULT_IPERF_PARALLEL);
        MapActivity.iperf_udp_bandwidth = MapActivity.app_shared_pref.getString("iperf_udp_bandwidth",config.DEFAULT_IPERF_UDP_BANDWIDTH);
        MapActivity.iperf_udp_flag = MapActivity.app_shared_pref.getBoolean("iperf_udp_enabled", false);
        MapActivity.iperf_interval = MapActivity.app_shared_pref.getInt("iperf_interval", config.DEFAULT_IPERF_INTERVAL);
        MapActivity.iperf_tcpdump_flag = MapActivity.app_shared_pref.getBoolean("iperf_tcpdump", false);
        MapActivity.iperf_tcpdump_filesize = MapActivity.app_shared_pref.getInt("iperf_tcpdump_filesize", config.DEFAULT_IPERF_TCPDUMP_FILESIZE);
        MapActivity.iperf_tcpdump_interval = MapActivity.app_shared_pref.getInt("iperf_tcpdump_interval", config.DEFAULT_IPERF_TCPDUMP_SNAP_INTERVAL);
    }
}
