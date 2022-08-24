package com.example.mmwavetracker.activity;

import static com.example.mmwavetracker.UI.FragmentMain1.monitorService;
import static com.example.mmwavetracker.UI.FragmentMain1.serviceBound;
import static com.example.mmwavetracker.UI.FragmentMain1.timer;
import static com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.mmwavetracker.R;
import com.example.mmwavetracker.Service.MonitoringService;
import com.example.mmwavetracker.Service.TracerDialog;
import com.example.mmwavetracker.UI.FragmentAppIperf;
import com.example.mmwavetracker.UI.FragmentParent;
import com.example.mmwavetracker.UI.WrapContentViewPager;
import com.example.mmwavetracker.core.Device;
import com.example.mmwavetracker.core.config;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;


public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {
    public static GoogleMap map;
    // A default location (Minneapolis, Minnesota) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(44.9745483, -93.2586308);
    private static final int DEFAULT_ZOOM = 18;

    //public static UIBindConnection serviceConnection;
    public static Location lastDataLocation = null;
    private String lastNrStatus = null;

    //Content resolver used as a handle to the system's settings (screen brightness)
    private ContentResolver cResolver;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    Location currentLocation;

    // List of app names and UIDs to track for background data
    public static List<Pair<String, String>> iperf_HostNames_all = new LinkedList();

    // My app's UID
    public static int my_app_id = 0;

    // Iperf settings
    public static String iperf_HostName;
    public static String iperf_HostName_label;
    public static int iperf_port = config.DEFAULT_IPERF_PORT;
    public static int iperf_duration = config.DEFAULT_IPERF_DURATION;
    public static int iperf_tcp_parallel = config.DEFAULT_IPERF_PARALLEL;
    public static boolean iperf_downlink_flag = config.DEFAULT_IPERF_DOWNLINK;
    public static boolean iperf_udp_flag = false;
    public static int iperf_interval;
    public static String iperf_udp_bandwidth;
    public static boolean iperf_json_flag = false;
    public static int iperf_verbose_flag = 0;
    public static String iperf_run_label;
    public static boolean iperf_tcpdump_flag = false;
    public static int iperf_tcpdump_interval = config.DEFAULT_IPERF_TCPDUMP_SNAP_INTERVAL;
    public static int iperf_tcpdump_filesize = config.DEFAULT_IPERF_TCPDUMP_FILESIZE;

    // RRC-Probe settings
    public static String rrcProbe_HostName_label;
    public static String rrcProbe_server_HostName;
    public static String rrcProbe_server_username;
    public static String rrcProbe_server_pw;
    public static String rrcProbe_server_port;
    public static String rrcProbe_server_script_path;
    public static String rrcProbe_run_label;
    public static String rrcProbe_server_logs_path;
    public static String rrcProbe_start;
    public static String rrcProbe_end;
    public static String rrcProbe_loop;
    public static String rrcProbe_delta;
    public static int rrcProbe_brightness_position;

    static List<Pair<String, String>> rrcProbe_HostNames_all = new LinkedList();

    // Congestion Control Settings
    static List<Pair<String, String>> cc_HostNames_all = new LinkedList();
    public static String cc_HostName;
    public static String cc_HostName_label;
    public static String cc_Ssh_Username;
    public static String cc_Ssh_Password;
    public static int cc_Ssh_Port = config.CC_DEFAULT_SSH_PORT;
    public static String cc_scheme = config.CC_DEFAULT_SCHEME;
    public static int cc_duration = config.CC_DEFAULT_DURATION;
    public static int cc_port = config.CC_DEFAULT_PORT;
    public static int cc_parallel = config.CC_DEFAULT_PARALLEL_CONN;
    public static boolean cc_json_flag = true;
    public static int cc_verbose_flag = 0;
    public static String cc_run_label;
    public static boolean cc_dump_flag = true;
    public static boolean cc_local_tcpdump_flag = false;
    public static boolean cc_run_server_flag = true;
    public static int cc_tcpdump_interval = config.DEFAULT_CC_TCPDUMP_SNAP_INTERVAL;
    public static int cc_tcpdump_filesize = config.DEFAULT_CC_TCPDUMP_FILESIZE;
    public static boolean cc_downlink_flag = true;
    public static boolean cc_use_tcp_flag = true;
    public static String cc_udp_bdw;
    public static SharedPreferences cc_shared_prefs;

    // UDP Client Fragment Settings
    static List<Pair<String, String>> uc_hostnames_all = new LinkedList();
    public static String uc_hostname_label = "";
    public static String uc_hostname_address = "";
    public static String uc_hostname_port = config.UC_DEFAULT_PORT;
    public static String uc_server_ssh_username = "";
    public static String uc_server_ssh_pass = "";
    public static String uc_server_ssh_port = config.UC_DEFAULT_SSH_PORT;
    public static String uc_server_script_path = config.UC_DEFAULT_SERVER_SCRIPT_PATH;
    public static String uc_server_path = config.UC_DEFAULT_SERVER_PATH;
    public static String uc_run_label = "";
    public static String uc_pkt_rate = config.UC_DEFAULT_RATE;
    public static String uc_duration = config.UC_DEFAULT_DURATION;
    public static boolean uc_direction = true;
    public static SharedPreferences uc_shared_prefs;
    public static SharedPreferences app_shared_pref;

    //
    Device device;
    public Context context;

    private Intent MonitoringServiceIntent;
    //side bar
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView navigationView;
    View headerView;
    TextView headerUserName, headerAppVersion;
    ImageView headerUserIcon;

    //on map
    public static ImageButton reCenter;
    public static ImageView walkT, standT, runT, bikeT, carT, walkM, standM, runM, bikeM, carM;
    public static TextView mobilitySpeedTV, curMobilityOrientationTV, mobilitySpeedUnitTV;

    //bottomsheet
    public static BottomSheetBehavior bottomsheet;

    //action bar
    public static boolean running;

    private boolean locationPermissionGranted;
    private PlacesClient placesClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    ServiceConnection serviceConnection;

    //viewer pager
    private static final int NUM_PAGES = 3;
    private WrapContentViewPager mPager;
    //private ScreenSlidePagerAdapter pagerAdapter, tabAdapter;
    private String state = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // \todo exception raised here
        context = getApplicationContext();

        config.initializeCommonFiles(getApplicationContext());
        setContentView(R.layout.activity_maps);
        config.loadDeviceSettings();

        config.DEVICE_ROOTED = config.checkRooted();

        // load initial Iperf Run Number
        config.getInitialRunNumber();

        mPager = (WrapContentViewPager) findViewById(R.id.bottom_pager);
        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new FragmentParent());
        mPager.setAdapter(pagerAdapter);

        running = false;
        walkT = findViewById(R.id.walk_t);
        runT = findViewById(R.id.run_t);
        carT = findViewById(R.id.car_t);
        bikeT = findViewById(R.id.bike_t);
        standT = findViewById(R.id.standing_t);
        walkM = findViewById(R.id.walk_m);
        runM = findViewById(R.id.run_m);
        carM = findViewById(R.id.car_m);
        bikeM = findViewById(R.id.bike_m);
        standM = findViewById(R.id.stand_m);
        View nestscrollView = findViewById(R.id.bottom_sheet);
        bottomsheet= BottomSheetBehavior.from(nestscrollView);
        mobilitySpeedTV = findViewById(R.id.mobility_speed_tv);
        curMobilityOrientationTV = findViewById(R.id.cur_mobility_orientation_tv);
        mobilitySpeedUnitTV = findViewById(R.id.speed_unit_iv);

        bottomsheet.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_DRAGGING: {
                        state = "DRAGGING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_SETTLING: {
                        state = "SETTLING";
                        break;
                    }
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        state = "EXPANDED";
                        FragmentParent.toggle.setChecked(true);
                        map.setPadding(0, 0, 0, 800);
                        reCenter.performClick();
                        break;
                    }
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        state = "COLLAPSED";
                        FragmentParent.toggle.setChecked(false);
                        map.setPadding(0, 0, 0, 0);
                        reCenter.performClick();
                        break;
                    }
                    case BottomSheetBehavior.STATE_HIDDEN: {
                        state = "HIDDEN";
                        break;
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.mapLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
//        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();

        serviceConnection = new UIBindConnection();
        reCenter = findViewById(R.id.re_center_ib);
        reCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
            }
        });

        verifyStoragePermission(MapActivity.this);

        //Special permissions for the other app monitoring
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(), getPackageName());
        }
        if (mode == AppOpsManager.MODE_ALLOWED) {
        } else {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }


        device = new Device(context);

        if (device.getConnectType() == config.NETWORK_TYPE_WIFI) {
            Toast.makeText(this, "Please turn WIFI OFF!", Toast.LENGTH_LONG).show();
        }

//
        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_api));
        placesClient = Places.createClient(this);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent previousIntent = getIntent();

        //Setup intent
        MonitoringServiceIntent = new Intent(this.getBaseContext(), MonitoringService.class);
        MonitoringServiceIntent.putExtra(config.SAMPLE_RATE_ARG, config.SAMPLING_RATE_IN_MS);

        //Start Monitor Service
        startForegroundService(MonitoringServiceIntent);

        if (CheckServiceStatus() && !serviceBound) {
            //Service is online, rebind
            bindService(MonitoringServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }



        try {
            my_app_id = this.getPackageManager().getApplicationInfo(this.getPackageName(), 0).uid;
            Log.i("!MapActivity!", "App UID is " + Integer.toString(my_app_id));
        } catch (Exception e) {
            Log.i("!MapActivity!", "Error getting App's UID");
            e.printStackTrace();
        }

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setOnCameraMoveStartedListener(this);

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                currentLocation = location;
            }
        });

        getLocationPermission();

        //getCurLocation();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }

    @SuppressLint("MissingPermission")
    public void getCurLocation() {
        try {
            if (locationPermissionGranted) {
                mFusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }
                }).addOnSuccessListener(location -> {
                    currentLocation = location;
                    // use this current location
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            reCenter.setVisibility(View.VISIBLE);
//            Toast.makeText(this, "The user gestured on the map.",
//                    Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
//            Toast.makeText(this, "The user tapped something on the map.",
//                    Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            reCenter.setVisibility(View.GONE);
//            Toast.makeText(this, "The app moved the camera.",
//                    Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    //
    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                @SuppressLint("MissingPermission") Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d("Map Error", "Current location is null. Using defaults.");
                            Log.e("Map Error", "Exception: %s", task.getException());
                            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public static String CorrectLabel(String label) {

        if (label == null || label.isEmpty() || label.length() <= 0) {
            return null;
        }
        //Take label, split by delimiter
        String labelPieces[] = label.split(config.RUN_LABEL_DELIMTER);
        //Sort by numeric alpha
        Arrays.sort(labelPieces);
        //Reassemble, and then add the device id
        StringJoiner strjoiner = new StringJoiner(config.RUN_LABEL_DELIMTER);
        for (String piece : labelPieces) {
            strjoiner.add(piece);
        }
        return strjoiner.toString();
    }

    @SuppressWarnings("deprecation")
    private boolean CheckServiceStatus() {
        Log.e("!MapActivity+Context.ACTIVITY_SERVICE!", getSystemService(Context.ACTIVITY_SERVICE)+"");
        ActivityManager actMan = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Log.e("!MapActivity+Context.ACTIVITY_SERVICE!", actMan+"");
        List<ActivityManager.RunningServiceInfo> serviceManagers = actMan.getRunningServices(Integer.MAX_VALUE);
        if (serviceManagers.size() > 0) {
            if (serviceManagers.get(0).service.getClassName().equals(MonitoringService.class.getName())) {
                return serviceManagers.get(0).started;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private class UIBindConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MonitoringService.LocalBinder bindInterface = (MonitoringService.LocalBinder) service;
            monitorService = bindInterface.getService();
            serviceBound = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serviceBound) {
            unbindService(serviceConnection);
        }

        if (FragmentParent.broadcastReceiver != null) {
            unregisterReceiver(FragmentParent.broadcastReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.legends,menu);
        MenuItem item = menu.findItem(R.id.legend_tool);
        return super.onCreateOptionsMenu(menu);
    }

    private boolean CheckIfTraceActive() {

        boolean traceActive = false;
        if (serviceBound && monitorService != null && monitorService.monitoringThread != null) {
            traceActive = monitorService.monitoringThread.ThreadActive;
        }
        return traceActive;
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    @SuppressLint("MissingSuperCall")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    int drawableId = 0;
                    drawableId = bundle.getInt("IconId");
                    String name = bundle.getString("Name");
                    if (name != null && !name.equals("")) {
                        headerUserName.setText(name);
                    }
                    if (drawableId != 0) {
                        headerUserIcon.setImageResource(drawableId);
                    }

                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mList = new ArrayList<>();
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }
        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        public void addFragment(Fragment fragment) {
            mList.add(fragment);
        }
        public CharSequence getPageTitle(int position) {
//            switch (position){
//                case 0:
//                    return "Summary";
//                case 1:
//                    return "Graph";
//                default:
//                    return "";
//            }
            return "";
        }
//

    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.play_and_pause:
                if (running) {
                    timer.stop();
                    // change the icon
                    FragmentAppIperf.iPerfBt.setEnabled(false);
                    FragmentAppIperf.iPerfBt.setTextColor(ContextCompat.getColor(getApplicationContext(),
                            R.color.colorGoldDisabled));
//                    FragmentAppCC.runBt.setEnabled(false);
//                    FragmentAppCC.runBt.setTextColor(getResources().getColor(R.color.colorGoldDisabled));
                    running = false;
                    item.setIcon(R.drawable.play);

                    // Stop Iperf if it is running
                    System.out.println("5GTracker Session Ended, so terminate Iperf!");
                    if (FragmentAppIperf.iPerfBt.isChecked()) {
                        FragmentAppIperf.iPerfBt.setChecked(false);
                    }

                    if (serviceBound && CheckIfTraceActive() == true) {
                        monitorService.StopTracing();
                        Log.i("MapsActivity", "Stopped tracing");
                    }

                    Circle circle = MapActivity.map.addCircle(new CircleOptions()
                            .center(new LatLng(lastDataLocation.getLatitude(), lastDataLocation.getLongitude()))
                            .radius(1)
                            .zIndex(1)
                            .fillColor(Color.BLACK));

                    // turn off keep screen on
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                    // set screen brightness to 50%
                    WindowManager.LayoutParams layout = this.getWindow().getAttributes();
                    layout.screenBrightness = (255 / 2) / (float)255;
                    this.getWindow().setAttributes(layout);
                    Log.d("!MapActivity!", "Screen brightness set to 50%");
                }else {
                    if (device.getConnectType()==config.NETWORK_TYPE_WIFI) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        // TODO this code is repeated, please merge them and create a function
                                        //yes button clicked
                                        timer.setBase(SystemClock.elapsedRealtime());
                                        timer.start();
                                        running = true;
                                        item.setIcon(R.drawable.stop);

                                        if (serviceBound && monitorService.ServiceReady() && !CheckIfTraceActive()) {
                                            // every time we press start, a new session is assigned

                                            monitorService.StartTracing();
                                            Log.i("!MapActivity!", "Started tracing");
                                        }
                                        FragmentAppIperf.iPerfBt.setEnabled(true);
//                                        FragmentAppCC.runBt.setEnabled(true);
//                                        FragmentAppCC.runBt.setTextColor(getResources().getColor(R.color.colorMaroon));
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Are you sure you want to proceed using WIFI?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                    }else{
                        timer.setBase(SystemClock.elapsedRealtime());
                        timer.start();
                        running = true;
                        FragmentAppIperf.iPerfBt.setEnabled(true);
                        FragmentAppIperf.iPerfBt.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                R.color.colorMaroon));
//                        FragmentAppCC.runBt.setEnabled(true);
//                        FragmentAppCC.runBt.setTextColor(getResources().getColor(R.color.colorMaroon));
                        item.setIcon(R.drawable.stop);


                        if (serviceBound && monitorService.ServiceReady() && CheckIfTraceActive() == false) {
                            // every time we press start, a new session is assigned
                            monitorService.StartTracing();
                            Log.i("MapActivity", "Started tracing");
                        }

                        // keep screen on
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        // disable adaptive screen brightness.
                        cResolver = this.getContentResolver();

                        // make screen brightness mode as manual
                        Settings.System.putInt(cResolver,
                                Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                        Log.d("!MapActivity!", "Screen brightness mode is set to manual");


                        // set brightness level of screen \todo hardcoded array/rrcProbe_brightness_position indexes
                        if (config.SCREEN_BRIGHTNESS_POSITION != 4) {
                            // set screen brightness to maximum
                            WindowManager.LayoutParams layout = this.getWindow().getAttributes();

                            if (config.SCREEN_BRIGHTNESS_POSITION == 0) {
                                // max
                                layout.screenBrightness = 1F;
                            } else if (MapActivity.rrcProbe_brightness_position == 1) {
                                // 50%
                                layout.screenBrightness = (255 / 2) / (float) 255;
                            } else if (MapActivity.rrcProbe_brightness_position == 2) {
                                // min
                                layout.screenBrightness = 0;
                            }

                            this.getWindow().setAttributes(layout);
                            Log.d("!MapActivity!", "Screen brightness set to maximum");
                        }
                    }
                }
                break;
            case R.id.edit_tracer:
                if (!running) {
                    TracerDialog tracerDialog = new TracerDialog();
                    FragmentManager fm = getSupportFragmentManager();
                    tracerDialog.show(fm, "Trace Config.");
                    Log.i("MapActivity", "Tracer Dialog opened");
                } else {
                    Log.i("MapActivity", "Will not open TracerDialog as tracer is running");
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static void verifyStoragePermission(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }


}
