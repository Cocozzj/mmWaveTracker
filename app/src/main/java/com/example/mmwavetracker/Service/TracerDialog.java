package com.example.mmwavetracker.Service;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.mmwavetracker.core.config;
import com.example.mmwavetracker.R;
import com.example.mmwavetracker.UI.FragmentMain2;
import com.example.mmwavetracker.activity.MapActivity;

public class TracerDialog extends AppCompatDialogFragment {
    EditText pingBatch, pingInterval, trackerBatch;
    Context context;
    Switch pingSw, tcpdumpSw, rrcProbeSw;
    Spinner spTrackerBrightness;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.tracer_dialog, null);

        // get sampling interval
        final EditText etTracerInterval = (EditText) view.findViewById(R.id.editText_tracer_interval);
        etTracerInterval.setText(Integer.toString(config.SAMPLING_RATE_IN_MS));

        // get tcpdump -s argument value
        final EditText etTcpDumpSvalue = (EditText) view.findViewById(R.id.et_tcpdump_snap_interval);
        etTcpDumpSvalue.setText(Integer.toString(config.TCPDUMP_SNAP_INTERVAL));

        // get tcpdump -C argument value
        final EditText etTcpDumpCvalue = (EditText) view.findViewById(R.id.et_tcpdump_file_size_limit);
        etTcpDumpCvalue.setText(Integer.toString(config.TCPDUMP_FILESIZE));

        // brightness
        spTrackerBrightness = (Spinner) view.findViewById(R.id.spin_TracerBrightness);
        // Populate Screen Brightness
        ArrayAdapter<String> statusArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, config.SCREEN_BRIGHTNESS_OPTS);
        spTrackerBrightness.setAdapter(statusArrayAdapter);
//        int statusSpPosition = statusArrayAdapter.getPosition();
        spTrackerBrightness.setSelection(config.SCREEN_BRIGHTNESS_POSITION);
        spTrackerBrightness.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Tracker batch
        trackerBatch = (EditText) view.findViewById(R.id.et_tracer_batch);
        trackerBatch.setText(Integer.toString(config.MAX_LINES_PER_BATCH));


        // ping settings
        pingBatch = (EditText) view.findViewById(R.id.et_ping_batch);
        pingBatch.setText(Integer.toString(config.MAX_PING_LINES_PER_BATCH));
        pingInterval = (EditText) view.findViewById(R.id.et_ping_interval);
        pingInterval.setText(Integer.toString(config.PING_DELAY_IN_MS));

        // ping enable/disable
        pingSw = view.findViewById(R.id.ping_enable_disable);
        if ((MapActivity.running) || config.RRC_PROBE_ENABLED) {
            pingSw.setEnabled(false);
            pingBatch.setEnabled(false);
            pingInterval.setEnabled(false);
        } else {
            pingSw.setEnabled(true);
            pingBatch.setEnabled(true);
            pingInterval.setEnabled(true);
        }
        pingSw.setChecked(config.PING_ENABLE_FLAG);
        pingBatch.setEnabled(config.PING_ENABLE_FLAG);
        pingInterval.setEnabled(config.PING_ENABLE_FLAG);
        pingSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pingBatch.setEnabled(isChecked);
                pingInterval.setEnabled(isChecked);
            }

        });

        // rrc_switch enable/disable
        rrcProbeSw = view.findViewById(R.id.rrc_enable_disable);
        if (MapActivity.running) {
            rrcProbeSw.setEnabled(false);
        } else {
            rrcProbeSw.setEnabled(true);
        }
        rrcProbeSw.setChecked(config.RRC_PROBE_ENABLED);

        // tcpdump enable/disable
        tcpdumpSw = view.findViewById(R.id.sw_tcpdump_enable_disable);
        if (MapActivity.running) {
            tcpdumpSw.setEnabled(false);
        } else {
            tcpdumpSw.setEnabled(true);
        }
        tcpdumpSw.setChecked(config.TCPDUMP_ENABLE_FLAG);
        etTcpDumpCvalue.setEnabled(config.TCPDUMP_ENABLE_FLAG);
        etTcpDumpSvalue.setEnabled(config.TCPDUMP_ENABLE_FLAG);
        tcpdumpSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etTcpDumpCvalue.setEnabled(isChecked);
                etTcpDumpSvalue.setEnabled(isChecked);
            }

        });

        builder.setView(view)
                .setTitle("Trace config.")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // set 5G Tracker sampling interval
                        if (etTracerInterval.getText().toString().length() == 0) {
                            config.SAMPLING_RATE_IN_MS = config.DEFAULT_SAMPLING_RATE_IN_MS;
                        } else {
                            config.SAMPLING_RATE_IN_MS = Integer.parseInt(etTracerInterval.getText().toString());
                        }

                        // set batch
                        config.MAX_LINES_PER_BATCH = Integer.parseInt(trackerBatch.getText().toString());

                        // set screen brightness option
                        config.SCREEN_BRIGHTNESS_POSITION = spTrackerBrightness.getSelectedItemPosition();

                        // set tcpdump -s value
                        if (etTcpDumpSvalue.getText().toString().length() == 0) {
                            config.TCPDUMP_SNAP_INTERVAL = config.DEFAULT_TCPDUMP_SNAP_INTERVAL;
                        } else {
                            config.TCPDUMP_SNAP_INTERVAL = Integer.parseInt(etTcpDumpSvalue.getText().toString());
                        }

                        // set tcpdump -C value
                        if (etTcpDumpCvalue.getText().toString().length() == 0) {
                            config.TCPDUMP_FILESIZE = config.DEFAULT_TCPDUMP_FILESIZE;
                        } else {
                            config.TCPDUMP_FILESIZE = Integer.parseInt(etTcpDumpCvalue.getText().toString());
                        }

                        // ping enable/disable
                        if (pingSw.isChecked()) {
                            config.PING_ENABLE_FLAG = true;
                            config.PING_DELAY_IN_MS = Integer.parseInt(pingInterval.getText().toString());
                            config.MAX_PING_LINES_PER_BATCH = Integer.parseInt(pingBatch.getText().toString());

                            FragmentMain2.pingEnabledFlag.setText("Yes");
                        } else {
                            config.PING_ENABLE_FLAG = false;
                            FragmentMain2.pingEnabledFlag.setText("No");
                        }

                        // rrc_probe enable/disable
                        if (rrcProbeSw.isChecked()) {
                            config.RRC_PROBE_ENABLED = true;
                            //FragmentMain2.pingEnabledFlag.setText("Yes");
                        } else {
                            config.RRC_PROBE_ENABLED = false;
                            //FragmentMain2.pingEnabledFlag.setText("No");
                        }

                        // tcpdump enable/disable
                        if (tcpdumpSw.isChecked()) {
                            config.TCPDUMP_ENABLE_FLAG = true;
                            FragmentMain2.tcpdumpSettings.setText("-s" + config.TCPDUMP_SNAP_INTERVAL +" -C " + config.TCPDUMP_FILESIZE);
                        } else {
                            config.TCPDUMP_ENABLE_FLAG = false;
                            FragmentMain2.tcpdumpSettings.setText("Disabled");
                        }

                        // tracker sampling interval
                        FragmentMain2.tracerInterval.setText(config.SAMPLING_RATE_IN_MS + " ms");

                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}

