package com.example.mmwavetracker.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.mmwavetracker.core.config;
import com.example.mmwavetracker.R;
import com.example.mmwavetracker.Service.MonitoringService;
import com.example.mmwavetracker.activity.MapActivity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

public class IperfConfirmDialog extends AppCompatDialogFragment {

    public IperfConfirmDialog(String r_value) {
        this.r_value = r_value;
    };

    String r_value;
    public static Spinner statusSp;
    public static EditText commentEt;
    public static TextView resultTv;
    Context context;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.activity_iperf_confirm_dialog, null);
        commentEt = view.findViewById(R.id.iperfc_cmt_et);
        statusSp = view.findViewById(R.id.iperfc_status_sp);
        resultTv = view.findViewById(R.id.iperfc_rvalue_tv);
        resultTv.setText(r_value);

        // set up array adapter for status
        ArrayAdapter<String> statusArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, config.IPERF_STATUS_VALUES);
        statusSp.setAdapter(statusArrayAdapter);
        int statusSpPosition = statusArrayAdapter.getPosition(config.IPERF_STATUS_VALUES[0]);
        statusSp.setSelection(statusSpPosition);
        statusSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // configure the dialog
        builder.setView(view)
                .setTitle("Confirm Status")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // validate and save everything
                        if (saveConfirmStatus()) {
                            Toast.makeText(context, "iperf session saved", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public static boolean saveConfirmStatus() {
        try {
            Path saveSessionFilePath = Paths.get(config.GetFullPath("Iperf/" + config.DEVICE_ID + "-iPerfSummary" + config.CSV_EXTENSION));
            // check if file exists
            boolean sessionLogFileExists = Files.exists(saveSessionFilePath);

            // add log
            StringJoiner strJoiner = new StringJoiner(config.CSV_DELIMIETER);
            List<String> sessionLines = new LinkedList<>();

            try {

                strJoiner.add(config.SampleDateFormat.format(FragmentAppIperf.starttime));
                strJoiner.add(config.SampleDateFormat.format(FragmentAppIperf.endtime));
                strJoiner.add(MonitoringService.getSessionNumber());
                strJoiner.add(Integer.toString(config.RUN_NUMBER_CURRENT));
                strJoiner.add(MapActivity.iperf_run_label);
                strJoiner.add(Integer.toString(MapActivity.iperf_duration));
                strJoiner.add(MapActivity.iperf_HostName);
                strJoiner.add(Integer.toString(MapActivity.iperf_tcp_parallel));
                strJoiner.add(Boolean.toString(MapActivity.iperf_tcpdump_flag));
                strJoiner.add(config.IPERF_STATUS_VALUES[statusSp.getSelectedItemPosition()]);
                strJoiner.add(Boolean.toString(MapActivity.iperf_downlink_flag));
                strJoiner.add(commentEt.getText().toString());
            } catch (Exception ex) {
                Log.e("EXCEPTION", ex.toString());
            }

            // get sessionLine
            if (!sessionLogFileExists) {

                //Ensure the directories exist
                Path dirPath = Paths.get(config.GetFullPath());
                Files.createDirectories(dirPath);

                // add header
                sessionLines.add("StartTimestamp,EndTimeStamp,SessionID,RunNumber,Labels," +
                        "Duration,ServerHostName,Parallel,TCPDUMP_Client_Flag,Rating,Downlink,Comments");

                sessionLines.add(strJoiner.toString());

                Files.write(saveSessionFilePath, sessionLines, StandardOpenOption.CREATE);
            }
            else {

                sessionLines.add(strJoiner.toString());
                // append log to existing file
                Files.write(saveSessionFilePath, sessionLines, StandardOpenOption.APPEND);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}