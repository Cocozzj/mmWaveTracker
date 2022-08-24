package com.example.mmwavetracker.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mmwavetracker.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class config {
    // Permission Settings
    public static int NUM_PERMISSIONS_NEEDED = 11;
    public static String[] PERMISSION_NAMES_FULL = new String[] {
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "ACCESS_BACKGROUND_LOCATION",
            "ACCESS_LOCATION_EXTRA_COMMANDS",
            "WRITE_EXTERNAL_STORAGE",
            "READ_EXTERNAL_STORAGE",
            "READ_PHONE_STATE",
            "ACTIVITY_RECOGNITION",
            "WRITE_SETTINGS",
            "USAGE_ACCESS_SETTINGS",
            "INTERNET"
    };
    public static String[] PERMISSION_NAMES = new String[] {
            "Fine Location",
            "Coarse Location",
            "Background Location",
            "Location Extra",
            "Write External",
            "Read External",
            "Read Phone State",
            "Activity Recognition",
            "Write Settings",
            "Usage Access Settings",
            "Internet"
    };

    // iPerf Default
    public static final int DEFAULT_IPERF_JSON_TIME_THRESHOLD = 200; // to determine whether client uses -J or not
    public static final int DEFAULT_IPERF_PORT = 5201;
    public static final int DEFAULT_IPERF_DURATION = 60;
    public static final int DEFAULT_IPERF_INTERVAL = 1000;
    public static final int DEFAULT_IPERF_PARALLEL = 8;
    public static final boolean DEFAULT_IPERF_DOWNLINK = true; // true: downlink; false: uplink
    public static final String DEFAULT_IPERF_UDP_BANDWIDTH = "3000M";
    public static final Integer[] IPERF_PARALLEL_VALUES = new Integer[]{8, 1, 4};
    public static int DEFAULT_IPERF_TCPDUMP_SNAP_INTERVAL = 96;
    public static int DEFAULT_IPERF_TCPDUMP_FILESIZE = 100;
    public static final String[] IPERF_STATUS_VALUES = new String[] {"good", "average", "bad"};

    public static boolean lacksPermission(Context mContexts, String permission) {

        return ContextCompat.checkSelfPermission(mContexts, permission) != PackageManager.PERMISSION_GRANTED;

    }

    public static boolean hasMissingPermissions(Context mContexts) {
        return ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || !Settings.System.canWrite(mContexts);
        //|| ActivityCompat.checkSelfPermission(mContexts, Manifest.permission.PACKAGE_USAGE_STATS) != PackageManager.PERMISSION_GRANTED;
    }
    public static final String AppName="mmWaveCapture";

    // file types
    public static final String CSV_DELIMIETER = ",";
    public static final String CSV_EXTENSION = ".csv";
    public static final String txt_EXTENSION = ".txt";
    public static final String JSON_EXTENSION = ".json";
    public static final String PCAP_EXTENSION = ".pcap";

    public static final String TRACKER = "tracker";
    public static final String IPERF = "iperf";
    public static final String PING = "ping";
    public static final String TCPDUMP = "tcpdump";

    public static int RUN_NUMBER_INITIAL_VALUE;
    public static int RUN_NUMBER_CURRENT;
    public static final String RUN_NUMBER_CONFIG_FILENAME = "run_number.conf";
    public static String CURRENT_UNIT = "mile";

    // Device settings
    public static String DEVICE_ID;
    public static final int DEVICE_VERIFIED_NUMBER=3;
    public static final int DEVICE_VERIFIED_SUCCESS=100;
    public static final int DEVICE_VERIFIED_FAILED=101;
    public static final int DEVICE_PERMISSION_FAILED=102;
    public static final int DEVICE_MODEL_FAILED=103;
    public static final int DEVICE_5GCHECK_FAILED=104;

    // Network Type
    public static final String NETWORK_NOT_CONNECTED="NOT_CONNECTED";
    public static final String NETWORK_TYPE_WIFI="WIFI";
    public static final String NETWORK_TYPE_MOBILE="MOBILE";
    public static final String NETWORK_TYPE_LTE="LTE";
    public static final String NETWORK_TYPE_5GSA="SA_NR";
    public static final String NETWORK_TYPE_MMWAVE="MMWAVE";
    public static final String NETWORK_TYPE_5GNSA="5GNSA";
    public static final String NETWORK_TYPE_NR_ADVANCED="5GNR+";
    public static final String NETWORK_TYPE_LTEPRO="LTEPRO";
    public static final String NETWORK_TYPE_LTECA="LTEPCA";
    public static final String NETWORK_TYPE_NONE="NONE";
    public static final String NETWORK_TYPE_UNKNOWN="UNKNOWN";

    //The sampling rate in milliseconds
    public static final int DEFAULT_SAMPLING_RATE_IN_MS = 1000;
    public static int SAMPLING_RATE_IN_MS = 1000;

    // Congestion Control Default
    public static final int CC_DEFAULT_SSH_PORT = 22;
    public static final String CC_DEFAULT_SCHEME = "cubic";
    public static final String[] CC_SCHEMES = new String[]{"bbr", "bbr2", "cubic", "reno", "vegas"};
    public static final int CC_DEFAULT_DURATION = 30; // seconds
    public static final int CC_DEFAULT_PORT = 5201;
    public static final int CC_DEFAULT_PARALLEL_CONN = 1;
    public static final Integer[] CC_PARALLEL_VALUES = new Integer[] {1, 4, 8};
    public static final String[] CC_STATUS_VALUES = new String[] {"good", "average", "bad"};
    public static int DEFAULT_CC_TCPDUMP_SNAP_INTERVAL = 96;
    public static int DEFAULT_CC_TCPDUMP_FILESIZE = 100;

    // TCPDUMP settings
    public static boolean TCPDUMP_ENABLE_FLAG = true;
    public static int TCPDUMP_SNAP_INTERVAL = 96;
    public static int TCPDUMP_FILESIZE = 100;
    public static int DEFAULT_TCPDUMP_SNAP_INTERVAL = 96;
    public static int DEFAULT_TCPDUMP_FILESIZE = 100;

    // Ping settings
    public static boolean PING_ENABLE_FLAG = true;
    public static final int PING_TTL = 255;
    public static final String PING_FILENAME = "-Ping.csv";
    public static int PING_DELAY_IN_MS = 1000;
    public static final int DEFAULT_PING_DELAY_IN_MS = 1000;
    public static String PING_SERVER_TARGET = "cse-5g-dev-web.oit.umn.edu";

    // UDP Client Default
    public static final String UC_DEFAULT_SSH_PORT = "22";
    public static final String UC_DEFAULT_DURATION = "10"; // seconds
    public static final String UC_DEFAULT_PORT = "4000";
    public static final String UC_DEFAULT_RATE = "2.0";
    public static final String UC_DEFAULT_SERVER_SCRIPT_PATH = "~/udp-sender-receiver/start_server.sh";
    public static final String UC_DEFAULT_SERVER_PATH = "~/udp-sender-receiver";
    public static final String[] UC_STATUS_VALUES = new String[] {"good", "average", "bad"};

    //MobilityLabel queue size
    public static final int MOBILITYLABEL_QUEUE_SIZE = 20;
    public static final int MOBILITYLABEL_STATIONARY_MAX_THRESHOLD_METERS = 3;
    public static final int MOBILITYLABEL_WALKING_MAX_THRESHOLD_METERS = 25;

    public static final String NR_CONNECTED_KEYWORD = "CONNECTED";
    public static final String NR_NOT_RESTRICTED_KEYWORD = "NOT_RESTRICTED";
    public static final String NR_RESTRICTED_KEYWORD = "RESTRICTED";
    public static final String NRC_NONE_KEYWORD = "NONE";
    public static final String FIVEGNETWORKNAME = "5G";

    //For calculating Speed
    public static final int SPEED_CALCULATION_WINDOW_SIZE = 2;

    // handoff orientation for map markers
    public static final int NO_HANDOFF = 0;
    public static final int VERTICAL_HANDOFF = 1;
    public static final int HORIZONTAL_HANDOFF = 2;
    public static final int VERTICAL_HORIZONTANL_HANDOFF = 3;

    // Report log batch size settings
    public static int MAX_LINES_PER_BATCH = 60; // i.e. every 30 seconds when sampling rate is 500ms
    public static int MAX_PING_LINES_PER_BATCH = 30; // i.e. every 30 seconds when sampling rate is 1ms
    public static int MAX_BATCHES_PER_CHUNK = 4000;
    public static final int DEFAULT_MAX_LINES_PER_BATCH = 60; // i.e. every 30 seconds when sampling rate is 500ms
    public static final int DEFAULT_MAX_PING_LINES_PER_BATCH = 30; // i.e. every 30 seconds when sampling rate is 1ms

    public static boolean RRC_PROBE_ENABLED=false;

    // intent passing between components
    public static final String BROADCAST_DETECTED_ACTIVITY = "com.example.mmWaveTracker.detect_activites";

    // intent passing session info between MonitoringService and MapActivity
    public static final String BROADCAST_SESSION_INFO = "com.example.mmWaveTracker.session_info";

    // brightness setting
    public static final String[] SCREEN_BRIGHTNESS_OPTS = new String[] {"Max", "50%", "Min", "Disable"};
    public static int SCREEN_BRIGHTNESS_POSITION = 1;
    public static final int DEFAULT_SCREEN_BRIGHTNESS_POSITION = 3;

    //MobilityLabel Codes
    public static final int MOBILITYLABEL_STATIONARY = 101;
    public static final int MOBILITYLABEL_WALKING = 102;
    public static final int MOBILITYLABEL_DRIVING = 103;
    public static final int MOBILITYLABEL_INVALID = -1;

    // Verbose Mode
    public static boolean LOG_VERBOSE_FLAG = true;

    //The String ID of the passed Sampling Rate
    public static final String SAMPLE_RATE_ARG = "SampleRate";

    //Phone rooted status
    public static boolean DEVICE_ROOTED = false;

    // Shared Auto Complete File
    public static final String SHARED_AUTO_COMPLETES = "SharedAutoCompletes" + CSV_EXTENSION;
    public static final String RUN_LABEL_DELIMTER = "_";

    // Throughput Coloring
    public static boolean DRAW_THROUGHPUT_FLAG = false;

    public static final int POOR_THROUGHPUT_COLOR = Color.parseColor("#FF0029");
    public static final int LTE_THROUGHPUT_COLOR = Color.parseColor("#FF5D00");
    public static final int LTECA_THROUGHPUT_COLOR = Color.parseColor("#FFE800");
    public static final int LOW_THROUGHPUT_COLOR = Color.parseColor("#8AFF00");
    public static final int GOOD_THROUGHPUT_COLOR = Color.parseColor("#00FF01");
    public static final int EXCELLENT_THROUGHPUT_COLOR = Color.parseColor("#00FF8C");

    public static final int POOR_THROUGHPUT_RANGE = 50;
    public static final int LTE_THROUGHPUT_RANGE = 150;
    public static final int LTECA_THROUGHPUT_RANGE = 300;
    public static final int LOW_THROUGHPUT_RANGE = 750;
    public static final int GOOD_THROUGHPUT_RANGE = 1200;



    public static String GetFullPath(String filename) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Logs/" + filename;
    }

    public static String GetFullPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Logs/";
    }

    public static String GetConfigFileFullPath(String filename) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Config/" + filename;
    }

    public static String GetTCPDumpFullPath(String filename) {
        try {
            Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +"/" + AppName + "/TcpDump/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "/sdcard/" + AppName + "/TcpDump/" + DEVICE_ID + "-" + filename + "-" + TCPDUMP +  PCAP_EXTENSION;
    }

    public static void initializeCommonFiles(Context context) {
        Device deviceStatus= new Device(context);
        // check if 5GTracker dir exists in sdcard
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName);
        if(!dir.isDirectory()) {
            try {
                Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName));
                Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Logs"));
                Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Logs"+ "/Iperf"));
//                Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Logs"+ "/CC-Logs"));
//                Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Logs"+ "/UC-Logs"));
//                Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/TcpDump"));
                Files.createDirectories(Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + AppName + "/Config"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Config", "5GTracker dir already exists!");
        }

        // check if commonsettings file exists
        File settings_file = new File(config.GetConfigFileFullPath("CommonSettings.csv"));
        if(!settings_file.exists()){
            try {
                settings_file.createNewFile();
                // add device id and ping target
                try (FileOutputStream o_stream = new FileOutputStream(settings_file)) {
                    // header
                    o_stream.write("fieldName,fieldvalue\n".getBytes());
                    Date date= new Date();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
                    // device id
//                    final int min_range = 100;
//                    final int max_range = 999;
//                    final int random_id = new Random().nextInt((max_range - min_range) + 1) + min_range;
                    String device = "deviceID," + deviceStatus.getPhoneName() + "_" + dateFormat.format(date) + "\n";
                    o_stream.write(device.getBytes());
                    DEVICE_ID=device;
                    // ping target
                    o_stream.write("pingTarget,cse-5g-dev-web.oit.umn.edu".getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Config", "Common settings already exists!");
        }

        // check if run number file exists
        File run_file = new File(config.GetConfigFileFullPath(RUN_NUMBER_CONFIG_FILENAME));
        if(!run_file.exists()) {
            try {
                run_file.createNewFile();
                // add run number
                try (FileOutputStream o_stream = new FileOutputStream(run_file)) {
                    final int min_range = 10000;
                    final int max_range = 80000;
                    final int random_id = new Random().nextInt((max_range - min_range) + 1) + min_range;
                    o_stream.write(Integer.toString(random_id).getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Config", "run number file already exists!");
        }

        // check if hostnames file exists
        File hostname_file = new File(config.GetConfigFileFullPath("hostnames.csv"));
        if(!hostname_file.exists()) {
            try {
                hostname_file.createNewFile();
                // copy hostnames file
                try (FileOutputStream o_stream = new FileOutputStream(hostname_file)) {
                    InputStream ins = context.getResources().openRawResource(R.raw.hostnames);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
                    try {
                        String line = reader.readLine();
                        while (line != null) {
                            line = line + "\n";
                            o_stream.write(line.getBytes());
                            line = reader.readLine();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("Config", "hostnames file already exists!");
        }

//        // check if sharedautocomplete file exists
//        File autocomplete_file = new File(config.GetConfigFileFullPath("SharedAutoCompletes.csv"));
//        if(!autocomplete_file.exists()) {
//            try {
//                autocomplete_file.createNewFile();
//                // copy autocompletes file
//                try (FileOutputStream o_stream = new FileOutputStream(autocomplete_file)) {
//                    InputStream ins = context.getResources().openRawResource(R.raw.sharedautocompletes);
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
//                    try {
//                        String line = reader.readLine();
//                        while (line != null) {
//                            line = line + "\n";
//                            o_stream.write(line.getBytes());
//                            line = reader.readLine();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Log.d("Config", "autocomplete file already exists!");
//        }
    }

    public static int readInt(FileInputStream in) {
        Scanner scanner = new Scanner(in);
        return scanner.nextInt();
    }

    public static boolean loadDeviceSettings() {
        // Load device ID
        try {
            List<String> app_Settings = Files.readAllLines(Paths.get(config.GetConfigFileFullPath("CommonSettings.csv")));

            for (String setting : app_Settings) {
                String[] vals = setting.split(config.CSV_DELIMIETER);

                if (vals[0].equals("deviceID"))
                    DEVICE_ID = vals[1];
                else if (vals[0].equals("pingTarget"))
                    PING_SERVER_TARGET = vals[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // Load initial run number
        try {
            FileInputStream runNumberFile = new FileInputStream(GetConfigFileFullPath(RUN_NUMBER_CONFIG_FILENAME));
            RUN_NUMBER_INITIAL_VALUE = readInt(runNumberFile);
            RUN_NUMBER_CURRENT = RUN_NUMBER_INITIAL_VALUE;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static int getInitialRunNumber() {
        try {
            FileInputStream runNumberFile = new FileInputStream(GetConfigFileFullPath(RUN_NUMBER_CONFIG_FILENAME));
            RUN_NUMBER_INITIAL_VALUE = readInt(runNumberFile);
            RUN_NUMBER_CURRENT = RUN_NUMBER_INITIAL_VALUE;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return RUN_NUMBER_INITIAL_VALUE;
    }

    public static boolean checkRooted()
    {
        try
        {
            Process p = Runtime.getRuntime().exec("su", null, new File("/"));
            DataOutputStream os = new DataOutputStream( p.getOutputStream());
            os.writeBytes("pwd\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            p.destroy();
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    // Date formatting
    public static final DateTimeFormatter iperfDateFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter SampleDateFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS z")
            .withZone(ZoneId.systemDefault());

    public static final DateTimeFormatter fileNameDateFormat = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    public static int incrementRunNumber() {
        RUN_NUMBER_CURRENT++;
        return RUN_NUMBER_CURRENT;
    }
    public static boolean persistCurrentRunNumber(int cur_run_number) {
        // save current run_number to file
        RUN_NUMBER_CURRENT = cur_run_number;
        try {
            FileOutputStream runNumberFile = new FileOutputStream(GetConfigFileFullPath(RUN_NUMBER_CONFIG_FILENAME), false);
            runNumberFile.write(Integer.toString(RUN_NUMBER_CURRENT).getBytes());
            runNumberFile.flush();
            runNumberFile.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
