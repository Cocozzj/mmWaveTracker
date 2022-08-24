package com.example.mmwavetracker.Service;

import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.example.mmwavetracker.IMonitoringServiceInterface;
import com.example.mmwavetracker.R;
import com.example.mmwavetracker.UI.FragmentMain1;
import com.example.mmwavetracker.UI.FragmentMain2;
import com.example.mmwavetracker.activity.MapActivity;
import com.example.mmwavetracker.core.Device;
import com.example.mmwavetracker.core.DisplayPhoneState;
import com.example.mmwavetracker.core.config;
import com.example.mmwavetracker.data.Report;
import com.example.mmwavetracker.data.SessionSummary;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import eu.chainfire.libsuperuser.Shell;

public class MonitoringService extends Service {

    /* Monitoring Variables */

    //Foreground service data
    public static final int FOREGROUNDSERVICEID = 102;
    public static final int NOTIFICATIONID = 1;

    public static String FOREGROUNDSERVICE_CHANNELNAME;
    public static String FOREGROUNDSERVICE_CHANNELID;
    public static String FOREGROUNDSERVICE_DESCRIPTION;

    public static NotificationManager notificationManager = null;
    public static NotificationChannel Channel = null;

    //Intent communication attachment
    public final IBinder mBinder = new LocalBinder();
    public PendingIntent stopMonitoringServiceIntent;

    //Threading Data
    public MonitoringUpdateHandler monitoringUpdateHandler = new MonitoringUpdateHandler();
    // \todo AN double-check, changed private -> public
    public MonitoringThread monitoringThread = new MonitoringThread(monitoringUpdateHandler);

    public int SampleRate = config.SAMPLING_RATE_IN_MS;
    public static final int LOWERBASESAMPLERATE = 10;

    //Network Sampling Data
    public  Report report;
    //Managers
    private Context context;
    private Device device;
    private TelephonyManager tm;
    private DisplayPhoneState displayPhoneState;

    //Location Info
    public FusedLocationProviderClient fuse;
    public boolean LocationDataReady = false;
    public PositionCallBack positionReciever = new PositionCallBack();

    //ActivityRecognition API
    public ActivityRecognitionClient actCli;
    public Intent actIntentService;
    public PendingIntent actPendingIntent;
    public BroadcastReceiver broadcastReceiver;
    public ArrayList<DetectedActivity> detectedActivities;
    public double batteryTemperature;

    // Get mobility activity
    Queue<Location> mobilityLabelQueue = new CircularFifoQueue<>(config.MOBILITYLABEL_QUEUE_SIZE);

    // Calculate Speed
    Queue<Long> rxBytes = new CircularFifoQueue<>(config.SPEED_CALCULATION_WINDOW_SIZE);
    Queue<Long> txBytes = new CircularFifoQueue<>(config.SPEED_CALCULATION_WINDOW_SIZE);
    Queue<Instant> timestamps_rxtx_bytes_calculation = new CircularFifoQueue<>(config.SPEED_CALCULATION_WINDOW_SIZE);

    //Name of File (Unix time)
    private static String sessionNumber;

    //Part of File
    private int part;

    //File counter
    private int counter;
    private int pingCounter;

    //Current path of log file
    private Path saveFilePath;
    private Path saveSessionFilePath;
    private Path savePingFilePath;

    //Handoff analysis
    private String prevNrStatus = "";
    private String prevMCid = "";
    private String prevAccessNetworkTech = "";

    //Buffer of ReportLines and pingLines
    private List<String> reportLines;
    private List<String> pingLines;

    // Maximum number of lines in each file
    private static final int MAX_LINES_PER_CHUNK = config.MAX_BATCHES_PER_CHUNK * config.MAX_LINES_PER_BATCH;

    /*  Ping Variables */
    public String ServerTarget = null;
//    private PingUpdateHandler pingUpdateHandler = new PingUpdateHandler();
///    public PingThread pingThread = new PingThread(pingUpdateHandler);

    /* TCPDUMP Variables */
    private String tcpdump_command;

    /* Room Database related */
//    private SessionSummaryDatabase db_session_summary;
//    private SessionSummary db_session_summary_record;
//    private SessionDetail db_session_detail_record;
    private Context cont;

    public SessionSummary curSession;

    private LocationRequest locationRequest;

    // Get session number
    public static String getSessionNumber() {
        return sessionNumber;
    }

//    public static FiveGTracker appState;

    // TCPDUMP related
    Shell.Interactive shell;

    public class LocalBinder extends Binder {
        public MonitoringService getService() {
            return MonitoringService.this;
        }
    }

    private void PrepareService() {

        //Load the App text info from strings sheet
        String appName = getText(R.string.app_name).toString();

        FOREGROUNDSERVICE_CHANNELNAME = appName;
        FOREGROUNDSERVICE_CHANNELID = appName;

        //Setup the notification channel, this is necessary for Foreground Services as they don't have UI otherwise
        Channel = new NotificationChannel(FOREGROUNDSERVICE_CHANNELID, FOREGROUNDSERVICE_CHANNELNAME, NotificationManager.IMPORTANCE_LOW);
        Channel.setDescription(FOREGROUNDSERVICE_DESCRIPTION);
        Channel.setVibrationPattern(new long[]{0});
        Channel.enableVibration(true);
        Channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(Channel);

        //Also setup the notification bar action
        Intent stopServiceIntent = new Intent(this, StopServiceReceiver.class);

        stopServiceIntent.setAction("umn.utils." + appName.toLowerCase() + ".Service");
        stopServiceIntent.putExtra(FOREGROUNDSERVICE_CHANNELID, 0);
        stopMonitoringServiceIntent = PendingIntent.getBroadcast(this, 0, stopServiceIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new Notification.Builder(this, Channel.getId())
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .addAction(R.drawable.ic_launcher_background, "Stop Service", stopMonitoringServiceIntent)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setAutoCancel(false)
                        .build();

        //To explicitly make this a "foreground" service instead of just a service, we must call this, with a created notification channel within 5 seconds of the service starting
        startForeground(FOREGROUNDSERVICEID, notification);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        PrepareService();
        //Setup Sampling Systems
        context = getApplicationContext();
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        displayPhoneState = new DisplayPhoneState(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tm.listen(displayPhoneState, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED);
        }
        device=new Device(context);

        //Setup the Location Updates
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(config.SAMPLING_RATE_IN_MS * 2L);
        locationRequest.setFastestInterval(config.SAMPLING_RATE_IN_MS);
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY);

        fuse = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fuse.requestLocationUpdates(locationRequest, this.positionReciever, null);

        // check if rooted
        if (config.checkRooted()) {
            Toast.makeText(getApplicationContext(), "Device is rooted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Device is not rooted", Toast.LENGTH_SHORT).show();
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(config.BROADCAST_DETECTED_ACTIVITY)) {
                    detectedActivities = (ArrayList<DetectedActivity>) intent.getSerializableExtra("all_activities");
                } else if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                    batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                    // convert to C
                    batteryTemperature = batteryTemperature * 1.0 / 10;
                }
            }
        }
        ;

        actCli = new ActivityRecognitionClient(this);
        actIntentService = new Intent(this, DetectedActivitiesIntentService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            actPendingIntent = PendingIntent.getService(this, 1, actIntentService, PendingIntent.FLAG_MUTABLE);
        }

        IntentFilter filter = new IntentFilter();
        Log.i("!AN!MonitorService", "Before registerReciever");
        filter.addAction(config.BROADCAST_DETECTED_ACTIVITY);
        registerReceiver(broadcastReceiver, filter);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    //GPS related
    private class PositionCallBack extends LocationCallback {
        Location lastLoc;

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            lastLoc = locationResult.getLastLocation();
            if (!LocationDataReady) {
                LocationDataReady = true;
            }
        }
    }

    private boolean resetReportLogBuffer() {
        reportLines = new LinkedList<>();
        return true;
    }

    private boolean createLogFileIfNotExists() throws NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // check if file exists
        boolean reportFileExists = Files.exists(saveFilePath);
        // create file and add header if file does not exist
        if (!reportFileExists) {
            //Ensure the directories exist
            Path dirPath = Paths.get(config.GetFullPath());
            try {
                Files.createDirectories(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<String> headerLine = new LinkedList<>();
            headerLine.add(Report.REPORTHEADER + Report.LOCATIONHEADER + Report.SIGNALFEATURE + Report.REPORTHEADER_VERBOSE_ADDITIONAL);
            //Make the header
            try {
                Files.write(saveFilePath, headerLine, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public class MonitoringUpdateHandler extends Handler {
        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // the report to be written
            Report rep = (Report) msg.obj;
            try {
                // stringify the report
                reportLines.add(rep.toString());
                counter++;
                //Log.e("ReportData", "Log: " + reportLines);
                // Write to file if batch size is met
                if (counter % config.MAX_LINES_PER_BATCH == 0) {
                    //Just append
                    Files.write(saveFilePath, reportLines, StandardOpenOption.APPEND);
                    resetReportLogBuffer();
                }

                //Go to Next Chunk File
                if (counter > MAX_LINES_PER_CHUNK) {
                    Log.i("!AN!MonitorService", "Part " + part + " finished. Counter=" + counter + "; Moving to next part.");
                    counter = 0;
                    part += 1;
                    saveFilePath = Paths.get(config.GetFullPath(config.DEVICE_ID + "-" + sessionNumber + "-" + String.format("%02d", part) + config.CSV_EXTENSION));
                    if (!createLogFileIfNotExists()) {
                        Log.i("!AN!MonitorService", "Error running createLogFileIfNotExists()");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class MonitoringThread extends Thread {
        public boolean ThreadActive = false;
        private MonitoringUpdateHandler ReportHandler;

        public MonitoringThread(MonitoringUpdateHandler reportingHandler) {
            this.ReportHandler = reportingHandler;
        }

        @Override
        public void run() {
            try {
                while (this.ThreadActive) {
                    //Take time to remove from the sample wait
                    Instant startTime = Instant.now();

                    report = NetworkInfoToReport();
                    if (detectedActivities != null)
                        report.setActivities(detectedActivities);
                    //report.setBatteryTemperature(batteryTemperature);
                   // report.getIsNSA5G();
                    if (report != null) {
                        Message msg = new Message();
                        msg.obj = report;
                        this.ReportHandler.sendMessage(msg);
                        curSession.processNetworkReport(getSessionNumber(), report);
                        BroadcastNetworkReportIntent();
                    }
                    //Final timestamp for time lineup
                    Instant endTime = Instant.now();
                    try {
                        long timedif = 0;
                        timedif = Duration.between(startTime, endTime).toMillis();
                        if (timedif > config.SAMPLING_RATE_IN_MS) {
                            timedif = 0;
                        }
                        sleep(config.SAMPLING_RATE_IN_MS - timedif);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                this.ThreadActive = false;
            }
        }
    }

    public void StartTracing() {
        //Spin up Monitoring thread, pass it the handler
        if (monitoringThread == null || !monitoringThread.ThreadActive) {
            monitoringThread = new MonitoringThread(monitoringUpdateHandler);
            monitoringThread.ThreadActive = true;

            // Name of File /Session Number (UNIX Time)
            sessionNumber = String.valueOf(Instant.now().getEpochSecond());
            FragmentMain2.sessionID.setText(sessionNumber);
            // create new session with userDH key and serverKeyId
            this.curSession = new SessionSummary(sessionNumber);

            for (int i = 0; i <= config.MOBILITYLABEL_QUEUE_SIZE; i++) {
                mobilityLabelQueue.add(this.positionReciever.lastLoc);
            }

            for (int i = 0; i <= config.SPEED_CALCULATION_WINDOW_SIZE; i++) {
                rxBytes.add((long) 0);
                txBytes.add((long) 0);
                timestamps_rxtx_bytes_calculation.add(Instant.now());
            }

            //Part of File
            part = 1;

            //File counter
            counter = 0;
            pingCounter = 0;

            // Set file path
            saveFilePath = Paths.get(config.GetFullPath(config.DEVICE_ID + "-" + sessionNumber + "-" + String.format("%02d", part) + config.CSV_EXTENSION));
            saveSessionFilePath = Paths.get(config.GetFullPath(config.DEVICE_ID + "-SessionSummary" + config.CSV_EXTENSION));
//            savePingFilePath = Paths.get(config.GetFullPath(config.DEVICE_ID + "-" + sessionNumber + config.PING_FILENAME));

            resetReportLogBuffer();

            // create log file
            try {
                if (!createLogFileIfNotExists())
                    Log.i("!AN!MonitorService", "Error running createLogFileIfNotExists()");
            } catch (NoSuchPaddingException | UnsupportedEncodingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
                e.printStackTrace();
            }

            monitoringThread.start();

            requestActivityUpdatesButtonHandler();

            Toast.makeText(this, "Tracing Started", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    public Report NetworkInfoToReport() {
        context= getApplicationContext();
        report = new Report();

        try {
            //Get Current Timestamp
            Instant currentTime = Instant.now();
            report.device=this.device;
            report.setContext(context);
//            Network activeNetwork = connMgr.getActiveNetwork();
//            report.AllNets = connMgr.getAllNetworks();
//            report.CellInfos = tm.getAllCellInfo();
            report.LocationData = this.positionReciever.lastLoc;
            report.TimeStamp = currentTime;
//            report.ActiveNetwork = connMgr.getActiveNetwork();
//            report.ActiveNetworkProperties = connMgr.getLinkProperties(activeNetwork);
//            report.RawServiceState = tm.getServiceState();//           report.accessNetworkTech = report.getAccessNetworkTech();
//            report.SignalStrength = tm.getSignalStrength();
            report.displayPhoneState = displayPhoneState;
//            StringBuilder ltePCIs = new StringBuilder();
//            StringBuilder lteCIs = new StringBuilder();
//            StringBuilder lteBwds = new StringBuilder();
//            StringBuilder nrPCIs = new StringBuilder();
//            StringBuilder nrCIs = new StringBuilder();
//            List<CellInfo> cells = tm.getAllCellInfo();
//            for (CellInfo cell : cells) {
//                if (cell instanceof CellInfoLte) {
//                    CellInfoLte cellInfoLte = (CellInfoLte) cell;
//                    CellIdentityLte cellIdentityLte = (CellIdentityLte) cellInfoLte.getCellIdentity();
//                    ltePCIs.append(cellIdentityLte.getPci()).append(";");
//                    lteBwds.append(cellIdentityLte.getBandwidth()).append(";");
//                    lteCIs.append(cellIdentityLte.getCi()).append(";");
//                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    if (cell instanceof CellInfoNr) {
//                        CellInfoNr cellInfoNr = (CellInfoNr) cell;
//                        CellIdentityNr cellIdentityNr = (CellIdentityNr) cellInfoNr.getCellIdentity();
//                        nrPCIs.append(cellIdentityNr.getPci()).append(";");
//                        nrCIs.append(cellIdentityNr.getNci()).append(";");
//                    }
//                }
//            }
//            report.ltePCIs = ltePCIs.toString();
//            report.lteCIs = lteCIs.toString();
//            report.lteBwds = lteBwds.toString();
//            report.nrPCIs = ltePCIs.toString();
//            report.nrCIs = nrCIs.toString();

//            // TODO: report.CPU Power Usage
//            report.setCurrentNow();
//            report.setVoltageNow();

            //get the CPUUsage only when the phone is rooted
//            if (config.DEVICE_ROOTED) {
//                report.setCPUUsage();
//            }
//            report.setAvailableHeapMemoryUsage();



            report.RxMobileBytes = TrafficStats.getMobileRxBytes();
            report.TxMobileBytes = TrafficStats.getMobileTxBytes();

            report.RxMobilePackets = TrafficStats.getMobileRxPackets();
            report.TxMobilePackets = TrafficStats.getMobileTxPackets();

            report.RxMobileBytesUid = TrafficStats.getUidRxBytes(MapActivity.my_app_id);
            report.TxMobileBytesUid = TrafficStats.getUidTxBytes(MapActivity.my_app_id);

            report.RxMobilePacketsUid = TrafficStats.getUidRxPackets(MapActivity.my_app_id);
            report.TxMobilePacketsUid = TrafficStats.getUidTxPackets(MapActivity.my_app_id);
//
            report.setNrStatus();
            report.setMCid();
            report.setHandoffType(prevNrStatus, prevMCid, prevAccessNetworkTech);
            report.setSpeeds(rxBytes.peek(), txBytes.peek(), timestamps_rxtx_bytes_calculation.peek());

            // set mobility label using old approach
            try {
                report.setMobilityActivityv1(mobilityLabelQueue.peek());
            } catch (Exception e2) {
                e2.printStackTrace();
                report.setMobilityLabelv1(config.MOBILITYLABEL_INVALID);
            }

            // append historical data for future use
            mobilityLabelQueue.add(report.LocationData);
            prevNrStatus = report.getNrStatus();
            prevMCid = report.getMCid();
            prevAccessNetworkTech = report.getAccessNetworkTech();

            rxBytes.add(report.RxMobileBytes);
            txBytes.add(report.TxMobileBytes);
            timestamps_rxtx_bytes_calculation.add(currentTime);




        } catch (Exception e) {
            e.printStackTrace();
            report = null;
        }
        return report;
    }

    public void BroadcastNetworkReportIntent() {
        //create and initialize an intent
        Intent intent = new Intent(config.BROADCAST_SESSION_INFO);

        //create a Bundle object
        Bundle extras = new Bundle();
        //Adding key value pairs to this bundle
        //there are quite a lot data types you can store in a bundle


        extras.putDouble("Session_secondsIn5G", curSession.getNSecondsIn5G());
        extras.putDouble("Session_Distance", curSession.getDistanceCovered());
        extras.putInt("Session_TowersNum", curSession.getNTowers());
        extras.putString("Session_LastTime5GSeen", curSession.getLastTime5GSeen());
        extras.putDouble("Session_PercentageOf5G", curSession.getPercentageOf5G());
        extras.putInt("Session_Handoffs", curSession.getNHandoffs());
        extras.putInt("Session_VerticalHandoffs", curSession.getNVerticalHandoffs());
        extras.putInt("Session_HorizontalHandoffs", curSession.getNHorizontalHandoffs());
        extras.putString("Session_currentNrStatus", curSession.getCurrentNrStatus());
        extras.putParcelable("Session_currentLocation", curSession.getCurLocation());
        extras.putInt("Session_currentHandoffOrientation", curSession.getCurHandoffOrientation());
        extras.putString("Session_currentMobilityActivity", curSession.getCurMobilityActivity());
        extras.putString("Session_currentMobilityLabelActivity", curSession.getCurMobilityLabelActivity());
        extras.putInt("Session_currentMobilityConfidence", curSession.getCurMobilityConfidence());
        extras.putFloat("Session_currentMobilitySpeed", curSession.getCurMobilitySpeed());
        extras.putString("Session_currentTower", curSession.getCurMCid());
        extras.putFloat("Session_currentMobilityOrientation", curSession.getCurMobilityOrientation());
        extras.putFloat("Session_currentCPUTemperature", curSession.getCpuTemperature());
        extras.putDouble("Session_currentBatteryTemperature", curSession.getBatteryTemperature());
        extras.putString("Session_rxSpeed", curSession.getRxSpeed());
        extras.putString("Session_txSpeed", curSession.getTxSpeed());
        extras.putString("Session_4GSignalStrength", curSession.getSignalStrength4G());
        extras.putString("Session_5GSignalStrength", curSession.getSignalStrength5G());
        extras.putBoolean("Session_isNSA5G", curSession.getIsNSA5G());
        extras.putBoolean("Session_isSA5G", curSession.getIsSA5G());
        extras.putString("Session_networkService", curSession.getNetworkService());
        extras.putString("Session_networkType", curSession.getNetworkType());
        extras.putString("Session_lteCi", curSession.getLteCi());
       // Log.i("Report1111","+"+curSession.getLteCi());
        extras.putString("Session_ltePci", curSession.getLtePci());
        extras.putString("Session_nrCi", curSession.getNrCi());
        extras.putString("Session_nrPci", curSession.getNrPci());

        //attach the bundle to the Intent object
        intent.putExtras(extras);

        sendBroadcast(intent);

    }

    public void requestActivityUpdatesButtonHandler() {
        @SuppressLint("MissingPermission")
        Task<Void> task = actCli.requestActivityUpdates(config.SAMPLING_RATE_IN_MS, actPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(),
                                "Successfully requested activity updates",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                                "Requesting activity updates failed to start - " + e.getMessage(),
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    public void resetUI(){
        FragmentMain2.sessionID.setText("-");
        FragmentMain2.isNSA5GTv.setText("-");
        FragmentMain2.NetworkTypetv.setText("-");
        FragmentMain1.signalStrength4GTV.setText("-");
        FragmentMain1.signalStrength5GTV.setText("-");
        FragmentMain1.towersNumTV.setText("-");
        FragmentMain1.curTowerTV.setText("-");
        FragmentMain2.lteCitv.setText("-");
        FragmentMain2.ltePcitv.setText("-");
        FragmentMain2.nrCitv.setText("-");
        FragmentMain2.nrPcitv.setText("-");

    }

    public void StopTracing() {
        Toast.makeText(this, "Tracing Stopped", Toast.LENGTH_LONG).show();
        if (monitoringThread != null && monitoringThread.ThreadActive) {
            monitoringThread.ThreadActive = false;
            try {
                // save unsaved logs.
                if (reportLines.size() > 0) {
                    // Append
                    Files.write(saveFilePath, reportLines, StandardOpenOption.APPEND);
                    resetReportLogBuffer();
                }
                curSession.endSession(part, tcpdump_command);
                if (!saveSessionSummaryEntry()) {
                    Log.e("!AN!MonitorService", "Error saving session summary");
                }
                tcpdump_command = null;
                // reset UI
                resetUI();

                monitoringThread.join();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            } finally {
                monitoringThread = null;
            }
        }

        if (config.TCPDUMP_ENABLE_FLAG && shell != null) {
            // return the instance to the pool
//            shell.close();
            try{
                List<String> out = Shell.SU.run("ps -A | grep tcpdump");
                assert out != null;
                for(String x : out) {
                    String[] temp = x.split("\\s+");
                    int pid = Integer.parseInt(temp[1]);
                    List<String> exitOutput =  Shell.SU.run("kill -9 " + pid);
                    Log.d("!AN!TCPDUMP", "Killed pid:" + pid);
                }
            }
            catch(Exception ex) {
                ex.printStackTrace();
                //retVal = -1;
                throw ex;
            }
            Log.d("!AN!TCPDUMP", "Closed TCPDUMP shell.");
            Toast.makeText(this, "TCPDUMP stopped", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean saveSessionSummaryEntry() {
        try {
            // check if file exists
            boolean sessionLogFileExists = Files.exists(saveSessionFilePath);
            List<String> sessionLines = new LinkedList<>();
            // get sessionLine
            if (!sessionLogFileExists) {
                //Ensure the directories exist
                Path dirPath = Paths.get(config.GetFullPath());
                Files.createDirectories(dirPath);
                // add header
                sessionLines.add(curSession.getHeader());
                // add log
                sessionLines.add(curSession.toString());
                Files.write(saveSessionFilePath, sessionLines, StandardOpenOption.CREATE);
            } else {
                // append log to existing file
                sessionLines.add(curSession.toString());
                Files.write(saveSessionFilePath, sessionLines, StandardOpenOption.APPEND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("!TEST!","intent/"+intent.getIntExtra(config.SAMPLE_RATE_ARG, config.SAMPLING_RATE_IN_MS));
        this.SampleRate = intent.getIntExtra(config.SAMPLE_RATE_ARG, config.SAMPLING_RATE_IN_MS);
        this.cont = getApplicationContext();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);

        //Stop the Tracing Thread
        this.StopTracing();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //Reporting to bound activities
    public class ReportingInterface extends IMonitoringServiceInterface.Stub {
        public MonitoringService getService() {
            //Called by clients
            return MonitoringService.this;
        }
    }

    public boolean SetSampleRate(int newSampleRate) {
        if (newSampleRate >= LOWERBASESAMPLERATE) {
            this.SampleRate = newSampleRate;
            return true;
        } else {
            return false;
        }
    }
    public boolean ServiceReady() {
        //If we can get a full sample it is ready
        if (config.RRC_PROBE_ENABLED) return true;
        return this.LocationDataReady;
    }

}
