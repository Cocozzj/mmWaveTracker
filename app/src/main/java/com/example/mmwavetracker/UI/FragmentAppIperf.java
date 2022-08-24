package com.example.mmwavetracker.UI;

import static com.example.mmwavetracker.UI.FragmentMain1.monitorService;
import static com.example.mmwavetracker.activity.MapActivity.iperf_HostName;
import static com.example.mmwavetracker.activity.MapActivity.iperf_downlink_flag;
import static com.example.mmwavetracker.activity.MapActivity.iperf_duration;
import static com.example.mmwavetracker.activity.MapActivity.iperf_interval;
import static com.example.mmwavetracker.activity.MapActivity.iperf_json_flag;
import static com.example.mmwavetracker.activity.MapActivity.iperf_port;
import static com.example.mmwavetracker.activity.MapActivity.iperf_run_label;
import static com.example.mmwavetracker.activity.MapActivity.iperf_tcp_parallel;
import static com.example.mmwavetracker.activity.MapActivity.iperf_udp_bandwidth;
import static com.example.mmwavetracker.activity.MapActivity.iperf_udp_flag;
import static com.example.mmwavetracker.activity.MapActivity.iperf_verbose_flag;
import static com.example.mmwavetracker.activity.MapActivity.running;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mmwavetracker.core.config;
import com.example.mmwavetracker.R;
import com.example.mmwavetracker.Service.IperfJniHandler;
import com.example.mmwavetracker.activity.MapActivity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Shell;
import hkhc.iperfwrapper.AndroidDefaults;
import hkhc.iperfwrapper.Iperf3;
import hkhc.iperfwrapper.IperfException;


public class FragmentAppIperf extends Fragment implements View.OnClickListener {

    private static final int IPERF_TERMINATED_AUTOMATICALLY = 101;
    private static final int IPERF_TERMINATED_BY_USER = 102;
    private static final int IPERF_TERMINATED_FORCEFULLY = 103;
    private static final int IPERF_RUNNING = 104;
    private static final int IPERF_FILE_STILL_UPDATING = 201;
    private static final int IPERF_FILE_DOESNOT_EXIST = 202;
    private static final int IPERF_JSON_FILE_WRITTEN_ATEND = 203;
    private static final int IPERF_FILE_STOPPED_UPDATING = 204;
    private static final int IPERF_FILE_STILL_UPDATING_TESTDONE = 205;
    private static final int IPERF_FILE_SUCCESSFULLY_WRITTEN = 206;
    //iperf related
    public static Iperf3 iperf3 = null;
    public static TextView hostnameTv, flagTcpUdpTv, settingTcpUdpTv, durationTv, directionTv, portTv, runLabelTv, intervalTv, elapsedTv, tcpdumpTV;
    //xml
    public static ToggleButton iPerfBt;
    private static int iperf_termination_state;          /* used to check whether the iPerf session
                                                            was terminated by the user, or
                                                            automatically due to either successful
                                                            finish or error, or forcefully ended due
                                                             to either an error or time has passed*/
    // iPerf file status
    private static String iperf_file_name;
    private static int iperf_file_status;
    final int IPERF_FILE_CHECK_THRESHOLD = 11;     /* Time span till last modified date.  This time
                                                    has to be more than conn. timeout defined in
                                                   SERVER_CONNECTION_TIMEOUT in iperf.h */
    final int IPERF_FILE_CHECK_INTERVAL = 5;   // Time to wait before checking file status
    final int IPERF_INITIAL_CHECK_DELAY = 3;   // Time to wait for file creation before status check
    Context context;
    ViewGroup rootView;
    CountDownTimer iperfCountDownTimer;
    // time left till iPerf test is finished
    long iperfTimeLeft;
    Button editIperf;
    TextView runNumberTV;
    /* Room Database related */

    /* TCPDUMP Variables */
    private String tcpdump_command;
    Shell.Interactive shell;

    // per session summary for Iperf
    public static Instant starttime, endtime;

    public FragmentAppIperf() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_app_iperf, container, false);

        // get all UI elements
        iPerfBt = rootView.findViewById(R.id.toggleButtonIperf);
        editIperf = rootView.findViewById(R.id.edit_iperf_bt);
        hostnameTv = rootView.findViewById(R.id.hostname_tv);
        intervalTv = rootView.findViewById(R.id.iperf_interval_tv);
        flagTcpUdpTv = rootView.findViewById(R.id.tcp_udp_tv);
        settingTcpUdpTv = rootView.findViewById(R.id.tcp_udp_setting_tv);
        durationTv = rootView.findViewById(R.id.duration_tv);
        directionTv = rootView.findViewById(R.id.reverse_title_tv);
        portTv = rootView.findViewById(R.id.port_tv);
        runLabelTv = rootView.findViewById(R.id.run_label_tv);
        runNumberTV = rootView.findViewById(R.id.run_number_tv);
        elapsedTv = rootView.findViewById(R.id.iperfapp_elapsed_time);
        tcpdumpTV = rootView.findViewById(R.id.tcpdump_tv);

        // Initializing the values of the Iperf TextViews
        hostnameTv.setText(MapActivity.iperf_HostName_label);
        intervalTv.setText(String.valueOf(MapActivity.iperf_interval/1000.0).replaceAll("\\.0*$", ""));
        durationTv.setText(String.valueOf(MapActivity.iperf_duration));
        directionTv.setText(iperf_downlink_flag ? "-R" : "");
        portTv.setText(String.valueOf(MapActivity.iperf_port));
        runLabelTv.setText(MapActivity.iperf_run_label);
        runNumberTV.setText(Integer.toString(config.RUN_NUMBER_CURRENT));
        tcpdumpTV.setText(Boolean.toString(MapActivity.iperf_tcpdump_flag));

        // check if tcp/udp then set the string accordingly
        if (MapActivity.iperf_udp_flag) {
            // udp enabled, set -b
            flagTcpUdpTv.setText("UDP");
            settingTcpUdpTv.setText("-b " + MapActivity.iperf_udp_bandwidth);
        } else {
            // tcp enabled, set -P
            flagTcpUdpTv.setText("TCP");
            settingTcpUdpTv.setText("-P " + MapActivity.iperf_tcp_parallel);
        }

        iPerfBt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new RunIperf(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    if (iperf_termination_state != IPERF_TERMINATED_AUTOMATICALLY &&
                            iperf_termination_state != IPERF_TERMINATED_FORCEFULLY) {
                        System.out.println("Iperf termination called by user!");
                        terminateIperfSession(IPERF_TERMINATED_BY_USER);
                    }
                }
            }
        });

        editIperf.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        // implements your things
        switch (v.getId()) {
            case R.id.edit_iperf_bt:
                IperfDialog iperfDialog = new IperfDialog();
                iperfDialog.show(getFragmentManager(), "Edit Iperf");
                break;

            case R.id.toggleButtonIperf:
                if (iPerfBt.isChecked()) {
                    new RunIperf(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    if (iperf_termination_state != IPERF_TERMINATED_AUTOMATICALLY &&
                            iperf_termination_state != IPERF_TERMINATED_FORCEFULLY) {
                        System.out.println("Iperf termination called by user!");
                        terminateIperfSession(IPERF_TERMINATED_BY_USER);
                    }
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    /* TCPDUMP COMMAND */
    private void sendTCPDumpCommand() {
        shell.addCommand(new String[] {tcpdump_command}, 0,
                new Shell.OnCommandResultListener() {
                    public void onCommandResult(int commandCode, int exitCode, @NonNull List<String> output) {
                        if (exitCode < 0) {
                            Log.e("!Iperf!TCPDUMP", "Error executing commands: exitCode." + exitCode);
                        }
                    }
                });
        Log.i("!Iperf!TCPDUMP", "Started TCPDUMP shell.");
        Toast.makeText(context, "TCPDUMP Started", Toast.LENGTH_SHORT).show();
    }


    public String constructIperfFileName(boolean withExt) {
        String iperfFileName;
        iperfFileName = config.DEVICE_ID + "-run" + config.RUN_NUMBER_CURRENT;
        if (!iperf_run_label.equals(""))
            iperfFileName += '-' + iperf_run_label;

        iperfFileName += "-iPerf";

        if (withExt) {
            if (iperf_json_flag)
                iperfFileName += config.JSON_EXTENSION;
            else
                iperfFileName += config.txt_EXTENSION;
        }

        return iperfFileName;
    }

    public String getIperfFileName() {
        return iperf_file_name;
    }

    public boolean isCurrentIperfJSON() {
        String iperf_FileName = getIperfFileName();
        return iperf_FileName.endsWith(config.JSON_EXTENSION);
    }

    public String getIperfFileSize() {
        // If the file exists, return its size
        String iperfFilePath_str = config.GetFullPath("Iperf/" + getIperfFileName());
        Path iperfFilePath = Paths.get(iperfFilePath_str);

        // check if file exists
        boolean iperfFileExists = Files.exists(iperfFilePath);
        if (iperfFileExists) {
            File file = new File(iperfFilePath_str);
            // Get file size in bytes
            long fileSizeInBytes = file.length();
            // Convert the bytes to KiloBytes (1 KB = 1024 Bytes)
            double fileSizeInKB = fileSizeInBytes * 1.0 / 1024;
            // Convert the KiloBytes to MegaBytes (1 MB = 1024 KBytes)
            double fileSizeInMB = fileSizeInKB * 1.0 / 1024;
            // Convert the MegaBytes to GigaBytes (1 GB = 1024 MBytes)
            double fileSizeInGB = fileSizeInMB * 1.0 / 1024;

            if (fileSizeInBytes == 0)
                return " File is Empty!";
            if (fileSizeInBytes < 1024)
                return " File Size is " + fileSizeInBytes + " B!";
            if (fileSizeInKB < 1024)
                return " File Size is " + Math.round(fileSizeInKB) + " KB!";
            else if (fileSizeInMB < 1024)
                return " File Size is " + Math.round(fileSizeInMB) + " MB!";
            else
                return " File Size is " + Math.round(fileSizeInGB) + " GB!";
        }
        return " File Does not Exist!";
    }

    public String getIperfLoggingTag() {
        return "IPERF: Run " + config.RUN_NUMBER_CURRENT + " time left " + iperfTimeLeft + " ";
    }

    public void startCountDownTimer(final int timer_duration) {
        iperfCountDownTimer = new CountDownTimer(timer_duration * 1000, 1000) {


            public void onTick(long millisUntilFinished) {
                FragmentParent.iperfProgressTV.setVisibility(View.VISIBLE);
                FragmentParent.iperfProgressTV.setText((int) (100 - millisUntilFinished / timer_duration / 10) + "%");
                FragmentParent.iperfProgressPB.setVisibility(View.VISIBLE);
                iperfTimeLeft = millisUntilFinished / 1000;
                FragmentAppIperf.elapsedTv.setText(Long.toString(timer_duration - iperfTimeLeft - 1) + " sec.");

                if (iperfTimeLeft > 0 && timer_duration - iperfTimeLeft >= IPERF_INITIAL_CHECK_DELAY
                        && iperfTimeLeft % IPERF_FILE_CHECK_INTERVAL == 0) {
                    // Avoid checking the file existence at the beg before it is being created
                    // Regularly check that file status to make sure the data is being received
                    new CheckIperfStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            public void onFinish() {
                System.out.println("Iperf timer onfinish is called!");
                iperfTimeLeft = 0;
                FragmentAppIperf.elapsedTv.setText("Finished!");
                iperfCountDownTimer = null;
                if (iperf_termination_state == IPERF_RUNNING) {
                    // If the time is finished, but the server did not close the connection, then
                    // forcefully close it after IPERF_FILE_CHECK_INTERVAL secs
                    System.out.println("Iperf check file status after " + IPERF_FILE_CHECK_INTERVAL
                            + " secs!");
                    checkIperfStatusTimer(IPERF_FILE_CHECK_INTERVAL);
                }
            }
        }.start();
    }

    public void checkIperfStatusTimer(int timer_duration) {
        new CountDownTimer(timer_duration * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (iperf_termination_state == IPERF_RUNNING) {
                    System.out.println("Timer is finished, Iperf calling terminate forcefully!");
                    terminateIperfSession(IPERF_TERMINATED_FORCEFULLY);
                }

            }
        }.start();
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

    public void terminateIperfSession(int termination_state) {
        System.out.println(getIperfLoggingTag() + "terminateIperfSession is Called W State " +
                termination_state);
        iperf_termination_state = termination_state;
        // When the session is done either automatically, or forcefully, check the file status
        new CheckIperfStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void endIperfSession() {
        /* End session checks the error message and error code, the termination mode,
        displays the iPerf session summary to the user at the end. It also resets the iperf settings
        to be ready to run the next session.
        * */
        if (iperf3 != null) {
            System.out.println(getIperfLoggingTag() + "endIperfSession is Called");

            String iperf3_result_message = "";
            int iperf3_result_code = 0;
            iperf3.getTestResultMessage();
            iperf3_result_message = iperf3.testResultMessage;
            if (iperf3_result_message == null)
                iperf3_result_message = "";
            else
                iperf3_result_message += "!";
            iperf3_result_code = iperf3.testResult;

            String iperfEndResultUserMessage = "";
            String iperfEndResultLoggingMessage = getIperfLoggingTag() + "Termination state: " +
                    iperf_termination_state + " Error Code: " + iperf3_result_code +
                    " Error Message: " + iperf3_result_message + " ";

            String fileSize = getIperfFileSize(); // Check file size

            if (iperf_termination_state != IPERF_TERMINATED_AUTOMATICALLY &&
                    iperf_termination_state != IPERF_TERMINATED_FORCEFULLY &&
                    iperf_termination_state != IPERF_RUNNING) {
                iperfEndResultUserMessage = "iPerf Session Terminated by User!" + fileSize;
            } else if (iperf_file_status == IPERF_FILE_STOPPED_UPDATING) {
                iperfEndResultUserMessage = "iPerf Session Ended!" + iperf3_result_message +
                        " No Data is being received from the Server!" + fileSize;
            } else if (iperf_file_status == IPERF_FILE_DOESNOT_EXIST) {
                iperfEndResultUserMessage = "iPerf Session Ended!" + iperf3_result_message +
                        " File was deleted by User!";
            } else if (iperf3_result_code != 0 || iperfTimeLeft > 0) {
                iperfEndResultUserMessage = "iPerf Session Ended: " + iperf3_result_message;
            } else if (fileSize.equals(" File is Empty!")) {
                iperfEndResultUserMessage = "iPerf Session Ended: " + iperf3_result_message +
                        fileSize;
            } else {
                // Session terminated successfully
                iperfEndResultUserMessage = "iPerf Session Ended Successfully!" + fileSize;
            }

            iperfEndResultLoggingMessage += iperfEndResultUserMessage;


            if (iperf_termination_state != IPERF_TERMINATED_AUTOMATICALLY) {
                iperf3.endTest();
            }

            // Stop the time if it did not stop automatically
            if (iperfCountDownTimer != null) {
                System.out.println(iperfEndResultLoggingMessage + "timer cancellation is called");
                iperfCountDownTimer.cancel();
                iperfCountDownTimer = null;
            }

            // Increment the number of iperf runs associated with the current session and append run_number
            monitorService.curSession.appendIperfRunNumber(Integer.toString(config.RUN_NUMBER_CURRENT));

            // insert the current tracker part in the local database
            String cur_iperf_ext;
            if (isCurrentIperfJSON())
                cur_iperf_ext = config.JSON_EXTENSION;
            else
                cur_iperf_ext = config.txt_EXTENSION;
//            db_session_detail_record = new SessionDetail(monitorService.curSession.getSessionId(),
//                    monitorService.appState.getUser().getUserid().toString(),
//                    monitorService.curSession.getNIperfRuns(),
//                    cur_iperf_ext, config.IPERF, getIperfFileName(),
//                    monitorService.appState.getAESIV(), monitorService.appState.getAESString(),
//                    monitorService.appState.getUserDHPublicKey(),
//                    monitorService.appState.getServerPublickeyId());
//
//            monitorService.insertIperfDetail(db_session_detail_record);

            iperf3.freeTest();
            iperf3 = null;
            FragmentParent.iperfProgressTV.setVisibility(View.INVISIBLE);
            FragmentParent.iperfProgressPB.setVisibility(View.INVISIBLE);

            if (iPerfBt.isChecked()) {
                iPerfBt.setChecked(false);
            }
            // Logging iperf test result and display it to the User
            System.out.println(iperfEndResultLoggingMessage);
            //Toast.makeText(context, iperfEndResultUserMessage, Toast.LENGTH_LONG).show();

            // Displaying end result of the iPerf session to the user in AlertDialog
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            });
//            alertDialogBuilder.setMessage(iperfEndResultUserMessage);
//            alertDialogBuilder.show();
            IperfConfirmDialog iperfConfirmDialog = new IperfConfirmDialog(iperfEndResultUserMessage);
            iperfConfirmDialog.show(getChildFragmentManager(), "Iperf Session");
        }
    }

    class RunIperf extends AsyncTask<String, String, String> {
        private Context mContext;

        RunIperf(Context context) {
            mContext = context;
        }

        /**
         * Before starting background thread
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.out.println("Iperf Session Started");

            Toast.makeText(mContext, "iPerf Session Started", Toast.LENGTH_SHORT).show();
            runNumberTV = rootView.findViewById(R.id.run_number_tv);
            config.incrementRunNumber();
            runNumberTV.setText(Integer.toString(config.RUN_NUMBER_CURRENT));
            config.persistCurrentRunNumber(config.RUN_NUMBER_CURRENT);

            /* Run TCPDump if flag enabled and Root */
            if (MapActivity.iperf_tcpdump_flag && config.checkRooted()) {

                tcpdump_command = "tcpdump -i any -s " + MapActivity.iperf_tcpdump_interval + " -w " +
                        config.GetTCPDumpFullPath(Integer.toString(config.RUN_NUMBER_CURRENT)) +
                        " -C " + MapActivity.iperf_tcpdump_filesize;

                // Prepare shell command
                shell = new Shell.Builder().
                        useSU().
                        setWantSTDERR(true).
                        setMinimalLogging(true).
                        open(new Shell.OnShellOpenResultListener() {
                            // Callback to report whether the shell was successfully started up
                            @Override
                            public void onOpenResult(boolean success, int reason) {
                                if (!success) {
                                    Log.e("!Iperf!TCPDUMP", "Error opening root shell.");
                                    Toast.makeText(context, "TCPDUMP Failed to start!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Shell is up: send command;
                                    sendTCPDumpCommand();
                                    Toast.makeText(context, "Tcpdump running", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(context, "not running tcpdump", Toast.LENGTH_SHORT).show();
            }

            iperfTimeLeft = iperf_duration + 1;
            startCountDownTimer(iperf_duration + 1);
        }

        /**
         * Start Iperf in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            try {
                if (running) {
                    // Initialize the iPerf Session settings
                    String saveFilePath;
                    String iperfTitle;
                    setIperfJSON();
                    if (iperf_run_label == null)
                        iperf_run_label = "";
                    iperf_file_name = constructIperfFileName(true);
                    saveFilePath = config.GetFullPath("Iperf/" + iperf_file_name);
                    iperfTitle = constructIperfFileName(false);
                    IperfJniHandler.initializeIperfSettings(saveFilePath, iperf_json_flag, iperfTitle);
                    iperf_termination_state = IPERF_RUNNING;
                    char udp = 't';

                    if (iperf_udp_flag) {
                        udp = 'u';
                        iperf_tcp_parallel = 1;
                    }
                    starttime = Instant.now();
                    iperf3 = new Iperf3();

                    iperf3
                            .newTest()
                            .defaults(new AndroidDefaults(context))
                            .testRole(Iperf3.ROLE_CLIENT)
                            .hostname(iperf_HostName)
                            .logfile(saveFilePath)
                            .outputJson(iperf_json_flag)
                            .durationInSeconds(iperf_duration)
                            .serverPort(iperf_port)
                            .intervalInMilliSeconds(iperf_interval)
                            .protocol(udp)
                            .reverse(iperf_downlink_flag ? 1 : 0)
                            .verbose(iperf_verbose_flag)
                            .udp_bandwidth(iperf_udp_bandwidth)
                            .numParallelStreams(iperf_tcp_parallel)
                            .title(iperfTitle)
                            .runClient();


                }
            } catch (IperfException e) {
                e.printStackTrace();
                iperfTimeLeft = -1;

            } finally {
            }
            return "done";

        }


        /**
         * After completing background task
         **/
        @Override
        protected void onPostExecute(String file_url) {
            if (iperf_termination_state == IPERF_RUNNING) {
                System.out.println("Iperf Calling terminate automatically!");
                terminateIperfSession(IPERF_TERMINATED_AUTOMATICALLY);
            }
            endtime = Instant.now();
            tcpdump_command = null;

            /* end the tcpdump session and check file size */
            if (MapActivity.iperf_tcpdump_flag && shell != null) {
                try{
                    @SuppressLint("WrongThread") List<String> out = Shell.SU.run("ps -A | grep tcpdump");
                    for(String x : out) {
                        String[] temp = x.split("\\s+");
                        Integer pid =  Integer.valueOf(temp[1]);
                        @SuppressLint("WrongThread") List<String> exitOutput =  Shell.SU.run("kill -9 " + pid.toString());
                        Log.d("!AN!TCPDUMP", "Killed pid:" + pid.toString());
                    }
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                    throw ex;
                }

                Log.d("!Iperf!TCPDUMP", "Closed TCPDUMP shell.");
                Toast.makeText(context, "TCPDUMP stopped", Toast.LENGTH_SHORT).show();
            }
        }

    }

    class CheckIperfStatus extends AsyncTask<Void, Void, Integer> {
        /**
         * Check the status of Iperf File
         */
        @Override
        protected Integer doInBackground(Void... x) {
            try {
                System.out.println(getIperfLoggingTag() + "CheckIperfStatus is Called");
                String iperf_FileName = getIperfFileName();
                String iperfFilePath_str = config.GetFullPath("Iperf/" + iperf_FileName);
                Path iperfFilePath = Paths.get(iperfFilePath_str);

                // check if file exists
                boolean iperfFileExists = Files.exists(iperfFilePath);
                if (iperfFileExists) {
                    File file = new File(iperfFilePath_str);
                    Date lastModDate = new Date(file.lastModified());
                    Date currentTime = Calendar.getInstance().getTime();
                    long diffInMs = currentTime.getTime() - lastModDate.getTime();
                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);

                    boolean current_iperf_json = isCurrentIperfJSON();
                    if (current_iperf_json && iperfTimeLeft > 0) {
                        iperf_file_status = IPERF_JSON_FILE_WRITTEN_ATEND;
                        System.out.println(getIperfLoggingTag() + ": File is JSON Written at the end");
                    } else if (diffInSec >= IPERF_FILE_CHECK_THRESHOLD && iperfTimeLeft > 0) {
                        iperf_file_status = IPERF_FILE_STOPPED_UPDATING;
                        System.out.println(getIperfLoggingTag() + ": File Stopped Updating, " +
                                "Last Modified " + diffInSec + " Sec. ago!");
                    } else if (diffInSec >= IPERF_FILE_CHECK_THRESHOLD && iperfTimeLeft == 0) {
                        iperf_file_status = IPERF_FILE_SUCCESSFULLY_WRITTEN;
                        System.out.println(getIperfLoggingTag() + ": File Last Modified " + diffInSec
                                + " Sec. ago!");
                    } else if (diffInSec < IPERF_FILE_CHECK_THRESHOLD && iperfTimeLeft == 0) {
                        iperf_file_status = IPERF_FILE_STILL_UPDATING_TESTDONE;
                        System.out.println(getIperfLoggingTag() + ": File Last Modified " + diffInSec
                                + " Sec. ago!, Test is Finished");
                    } else {
                        iperf_file_status = IPERF_FILE_STILL_UPDATING;
                        System.out.println(getIperfLoggingTag() + ": File Last Modified " + diffInSec
                                + " Sec. ago!");
                    }
                } else {
                    iperf_file_status = IPERF_FILE_DOESNOT_EXIST;
                    System.out.println(getIperfLoggingTag() + ": File Does not Exist!");
                }

            } catch (Exception e) {
                e.printStackTrace();

            } finally {

            }
            return iperf_file_status;
        }


        /**
         * After completing background task
         **/
        @Override
        protected void onPostExecute(Integer z) {
            // IF the file is not updated or does not exist, then end it forcefully
            if ((iperf_file_status == IPERF_FILE_DOESNOT_EXIST ||
                    iperf_file_status == IPERF_FILE_STOPPED_UPDATING) &&
                    iperf_termination_state == IPERF_RUNNING) {
                System.out.println("Iperf file has stopped, calling terminate forcefully!");
                terminateIperfSession(IPERF_TERMINATED_FORCEFULLY);
            }
            // The session is ended so call endIperfSession
            if (iperf_termination_state != IPERF_RUNNING)
                endIperfSession();
        }
    }


}