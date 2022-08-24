package com.example.mmwavetracker.UI;

import static com.example.mmwavetracker.UI.FragmentMain1.curTowerTV;
import static com.example.mmwavetracker.UI.FragmentMain1.horizontalHandoffTV;
import static com.example.mmwavetracker.UI.FragmentMain1.percentage5GTV;
import static com.example.mmwavetracker.UI.FragmentMain1.signalStrength4GTV;
import static com.example.mmwavetracker.UI.FragmentMain1.signalStrength5GTV;
import static com.example.mmwavetracker.UI.FragmentMain1.towersNumTV;
import static com.example.mmwavetracker.UI.FragmentMain1.verticalHandoffTV;
import static com.example.mmwavetracker.UI.FragmentMain2.ConnectTypetv;
import static com.example.mmwavetracker.UI.FragmentMain2.NetworkTypetv;
import static com.example.mmwavetracker.UI.FragmentMain2.distanceTV;
import static com.example.mmwavetracker.UI.FragmentMain2.isNSA5GTv;
import static com.example.mmwavetracker.UI.FragmentMain2.isSA5GTv;
import static com.example.mmwavetracker.UI.FragmentMain2.lteCitv;
import static com.example.mmwavetracker.UI.FragmentMain2.ltePcitv;
import static com.example.mmwavetracker.UI.FragmentMain2.nrCitv;
import static com.example.mmwavetracker.UI.FragmentMain2.nrPcitv;
import static com.example.mmwavetracker.activity.MapActivity.bikeM;
import static com.example.mmwavetracker.activity.MapActivity.bikeT;
import static com.example.mmwavetracker.activity.MapActivity.bottomsheet;
import static com.example.mmwavetracker.activity.MapActivity.carM;
import static com.example.mmwavetracker.activity.MapActivity.carT;
import static com.example.mmwavetracker.activity.MapActivity.curMobilityOrientationTV;
import static com.example.mmwavetracker.activity.MapActivity.mobilitySpeedTV;
import static com.example.mmwavetracker.activity.MapActivity.mobilitySpeedUnitTV;
import static com.example.mmwavetracker.activity.MapActivity.runM;
import static com.example.mmwavetracker.activity.MapActivity.runT;
import static com.example.mmwavetracker.activity.MapActivity.standM;
import static com.example.mmwavetracker.activity.MapActivity.standT;
import static com.example.mmwavetracker.activity.MapActivity.walkM;
import static com.example.mmwavetracker.activity.MapActivity.walkT;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.mmwavetracker.R;
import com.example.mmwavetracker.activity.MapActivity;
import com.example.mmwavetracker.core.config;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentParent extends Fragment {

    //view related
    Context context;
    ViewGroup rootView;
    TabLayout tabLayout, tabLayoutApp;

    public static ProgressBar iperfProgressPB;
    public static TextView iperfProgressTV;

    static WrapContentViewPager wrapContentViewPager, wrapContentViewPagerApp;


    Location curDataLocation;
    public static TextView rxSpeedTV,txSpeedTV;

    public static ToggleButton toggle;

    private static final int CONNECTED_COLOR = Color.GREEN;
    private static final int DIS_CONNECTED_COLOR = Color.RED;
    private static final int LTE_COLOR = Color.BLACK;

    public static BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_parent, container, false);

        toggle = rootView.findViewById(R.id.expand_toggle);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                bottomsheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            }else{
                bottomsheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // main pager
        wrapContentViewPager = rootView.findViewById(R.id.pager);
        wrapContentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                wrapContentViewPager.reMeasureCurrentPage(wrapContentViewPager.getCurrentItem());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // app pager
        wrapContentViewPagerApp = (WrapContentViewPager) rootView.findViewById(R.id.app_pager);
        wrapContentViewPagerApp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                wrapContentViewPagerApp.reMeasureCurrentPage(wrapContentViewPagerApp.getCurrentItem());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // upload download speed
        rxSpeedTV = rootView.findViewById(R.id.download_speed_tv);
        txSpeedTV = rootView.findViewById(R.id.upload_speed_tv);

        // progress bar of iPerf
        iperfProgressPB = rootView.findViewById(R.id.remaining_pb);
        iperfProgressTV = rootView.findViewById(R.id.remaing_iperf_tv);

        //initialize variables
        tabLayout = rootView.findViewById(R.id.tabs);
        ScreenSlidePagerAdapter tabAdapter = new ScreenSlidePagerAdapter(getParentFragmentManager());
        tabAdapter.addFragment(new FragmentMain1());
        tabAdapter.addFragment(new FragmentMain2());
        tabAdapter.addFragment(new FragmentMain3());
        wrapContentViewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(wrapContentViewPager,true);

        tabLayoutApp = rootView.findViewById(R.id.app_tabs);
        ScreenSlidePagerAdapter tabAdapterApp = new ScreenSlidePagerAdapter(getParentFragmentManager());
        tabAdapterApp.addFragment(new FragmentAppIperf());
//        tabAdapterApp.addFragment(new FragmentAppCC());
//        tabAdapterApp.addFragment(new UdpClientAppFragment());
//        tabAdapterApp.addFragment(new FragmentAppRRCProbe());
        wrapContentViewPagerApp.setAdapter(tabAdapterApp);
        tabLayoutApp.setupWithViewPager(wrapContentViewPagerApp,true);

        broadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(config.BROADCAST_SESSION_INFO)) {

                    //get the attached bundle from the intent
                    Bundle extras = intent.getExtras();

                    towersNumTV.setText(Integer.toString(extras.getInt("Session_TowersNum")));
                    DecimalFormat df = new DecimalFormat("#.##");
                    Double distance_formatted = extras.getDouble("Session_Distance");
                    String distance = "";
                    if (config.CURRENT_UNIT.equals("mile")) {
                        distance = df.format(distance_formatted) + " mi";

                    } else {
                        distance = df.format((distance_formatted * 1.609344)) + " km";
                    }
                    distanceTV.setText(distance);
                    DecimalFormat pf = new DecimalFormat("#");
                    String percentage_formatted = pf.format(extras.getDouble("Session_PercentageOf5G"));
                    percentage5GTV.setText(percentage_formatted + " %");
                    verticalHandoffTV.setText(Integer.toString(extras.getInt("Session_VerticalHandoffs")));
                    horizontalHandoffTV.setText(Integer.toString(extras.getInt("Session_HorizontalHandoffs")));

                    walkT.setVisibility(View.INVISIBLE);
                    carT.setVisibility(View.INVISIBLE);
                    standT.setVisibility(View.INVISIBLE);
                    bikeT.setVisibility(View.INVISIBLE);
                    runT.setVisibility(View.INVISIBLE);
                    switch (extras.getString("Session_currentMobilityActivity")) {
                        case "IN_VEHICLE":
                            carT.setVisibility(View.VISIBLE);
                            break;
                        case "ON_BICYCLE":
                            bikeT.setVisibility(View.VISIBLE);
                            break;
                        case "STILL":
                            standT.setVisibility(View.VISIBLE);
                            break;
                        case "WALKING":
                            walkT.setVisibility(View.VISIBLE);
                            break;
                        case "RUNNING":
                            runT.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
                    }
                    walkM.setVisibility(View.INVISIBLE);
                    carM.setVisibility(View.INVISIBLE);
                    standM.setVisibility(View.INVISIBLE);
                    bikeM.setVisibility(View.INVISIBLE);
                    runM.setVisibility(View.INVISIBLE);
                    switch (extras.getString("Session_currentMobilityLabelActivity")) {
                        case "IN_VEHICLE":
                            carM.setVisibility(View.VISIBLE);
                            break;
                        case "ON_BICYCLE":
                            bikeM.setVisibility(View.VISIBLE);
                            break;
                        case "STILL":
                            standM.setVisibility(View.VISIBLE);
                            break;
                        case "WALKING":
                            walkM.setVisibility(View.VISIBLE);
                            break;
                        case "RUNNING":
                            runM.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
                    }
                    double mobility_speed_formatted = extras.getFloat("Session_currentMobilitySpeed");
                    String speed = "";
                    if (!config.CURRENT_UNIT.equals("mile")) {
                        speed = df.format(mobility_speed_formatted);
                        mobilitySpeedUnitTV.setText("m/s");
                    } else {
                        DecimalFormat sf = new DecimalFormat("#.#");
                        speed = sf.format(mobility_speed_formatted * 2.23694);
                        mobilitySpeedUnitTV.setText("mph");
                    }
                    mobilitySpeedTV.setText(speed);
                    curTowerTV.setText(extras.getString("Session_currentTower"));

                    String curNrStatus = extras.getString("Session_currentNrStatus");
                    curDataLocation = extras.getParcelable("Session_currentLocation");
                    int curHandoffOrientation = extras.getInt("Session_currentHandoffOrientation", 0);
                    String mobility_orientation_formatted = df.format((int) extras.getFloat("Session_currentMobilityOrientation"));
                    curMobilityOrientationTV.setText(mobility_orientation_formatted);

 //                   String cpu_temperature_formatted = df.format(extras.getFloat("Session_currentCPUTemperature"));
//                    cpuTemperatureTV.setText(cpu_temperature_formatted);

//                    String battery_temperature_formatted = df.format(extras.getDouble("Session_currentBatteryTemperature"));
//                    batteryTemperatureTV.setText(battery_temperature_formatted);

                    // TODO check if always in Mb/s or could be Kb/s as well
                    //rxSpeedTV.setText(extras.getString("Session_rxSpeed") + " Mb/s");
                    //txSpeedTV.setText(extras.getString("Session_txSpeed") + " Mb/s");

                    String rxSpeed = extras.getString("Session_rxSpeed");
//                    int cur_rxspeed = 0;
//                    cur_rxspeed = Integer.parseInt(rxSpeed);
//                    //if (cur_rxspeed <= 3000)
                    rxSpeedTV.setText(extras.getString("Session_rxSpeed") + " Mb/s");

                    String txSpeed = extras.getString("Session_txSpeed");
//                    int cur_txSpeed = 0;
//                    cur_txSpeed = Integer.parseInt(txSpeed);
//                    //if (cur_txSpeed <= 3000)
                    txSpeedTV.setText(extras.getString("Session_txSpeed") + " Mb/s");

                    drawCoverageMap(curNrStatus, rxSpeed, curDataLocation, curHandoffOrientation);

                    signalStrength4GTV.setText(extras.getString("Session_4GSignalStrength"));
                    signalStrength5GTV.setText(extras.getString("Session_5GSignalStrength"));
                    if (extras.getBoolean("Session_isNSA5G")) {
                        isNSA5GTv.setText("True");
                    }
                    else if (!extras.getBoolean("Session_isNSA5G")) {
                        isNSA5GTv.setText("False");
                    }
                    else {
                        isNSA5GTv.setText("Unknown");
                    }
                    if (extras.getBoolean("Session_isSA5G")) {
                        isSA5GTv.setText("True");
                    }
                    else if (!extras.getBoolean("Session_isSA5G")) {
                        isSA5GTv.setText("False");
                    }
                    else {
                        isSA5GTv.setText("Unknown");
                    }
                    ConnectTypetv.setText(extras.getString("Session_networkService"));
                    NetworkTypetv.setText(extras.getString("Session_networkType"));
                    lteCitv.setText(extras.getString("Session_lteCi"));
                    ltePcitv.setText(extras.getString("Session_ltePci"));
                    nrCitv.setText(extras.getString("Session_nrCi"));
                    nrPcitv.setText(extras.getString("Session_nrPci"));
                }
            }
        };

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(config.BROADCAST_SESSION_INFO));


        return rootView;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }

    public class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mList = new ArrayList<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
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

            return "";
        }

    }



    private void drawCoverageMap(String curNrStatus, String rxSpeed_str, Location curDataLocation, int curHandoffOrientation) {

        if (MapActivity.lastDataLocation != null && MapActivity.lastDataLocation.getLatitude() != curDataLocation.getLatitude() && MapActivity.lastDataLocation.getLongitude() != curDataLocation.getLongitude()) {
            Polyline polyline = MapActivity.map.addPolyline(new PolylineOptions()
                    .add(
                            new LatLng(MapActivity.lastDataLocation.getLatitude(), MapActivity.lastDataLocation.getLongitude()),
                            new LatLng(curDataLocation.getLatitude(), curDataLocation.getLongitude())
                    ));
            polyline.setStartCap(new RoundCap());
            polyline.setJointType(JointType.ROUND);
            polyline.setEndCap(new RoundCap());

            // bar colors as per throughput
            // zindex = higher number is kept in front
            if (curNrStatus.equals(config.NR_CONNECTED_KEYWORD)) {
                // connected to 5G

                int rxSpeed = Integer.parseInt(rxSpeed_str);

                if (rxSpeed < config.POOR_THROUGHPUT_RANGE) {
                    polyline.setColor(config.POOR_THROUGHPUT_COLOR);
                    polyline.setWidth(10);
                    polyline.setZIndex(7);
                } else if (rxSpeed < config.LTE_THROUGHPUT_RANGE) {
                    polyline.setColor(config.LTE_THROUGHPUT_COLOR);
                    polyline.setWidth(13);
                    polyline.setZIndex(6);
                } else if (rxSpeed < config.LTECA_THROUGHPUT_RANGE) {
                    polyline.setColor(config.LTECA_THROUGHPUT_COLOR);
                    polyline.setWidth(16);
                    polyline.setZIndex(5);
                } else if (rxSpeed < config.LOW_THROUGHPUT_RANGE) {
                    polyline.setColor(config.LOW_THROUGHPUT_COLOR);
                    polyline.setWidth(19);
                    polyline.setZIndex(4);
                } else if (rxSpeed < config.GOOD_THROUGHPUT_RANGE) {
                    polyline.setColor(config.GOOD_THROUGHPUT_COLOR);
                    polyline.setWidth(22);
                    polyline.setZIndex(3);
                } else {
                    polyline.setColor(config.EXCELLENT_THROUGHPUT_COLOR);
                    polyline.setWidth(25);
                    polyline.setZIndex(2);
                }
            } else {
                // connected to 4G
                polyline.setColor(LTE_COLOR);
                polyline.setWidth(8);
                polyline.setZIndex(8);
            }

        }
        if (MapActivity.lastDataLocation == null) {
            // Add a circle to mark the start
            Circle circle = MapActivity.map.addCircle(new CircleOptions()
                    .center(new LatLng(curDataLocation.getLatitude(), curDataLocation.getLongitude()))
                    .radius(1)
                    .zIndex(1)
                    .fillColor(Color.BLACK));
        }

        MapActivity.lastDataLocation = curDataLocation;
        if (MapActivity.reCenter.getVisibility() == View.GONE) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(curDataLocation.getLatitude(), curDataLocation.getLongitude()));
//            MapActivity.map.moveCamera(cameraUpdate);
            MapActivity.map.animateCamera(cameraUpdate);
        }


        if (curHandoffOrientation == config.HORIZONTAL_HANDOFF) {
            Circle circle = MapActivity.map.addCircle(new CircleOptions()
                    .center(new LatLng(curDataLocation.getLatitude(), curDataLocation.getLongitude()))
                    .radius(3)
                    .zIndex(1)
                    .strokeColor(Color.TRANSPARENT)
                    .fillColor(Color.GRAY));
        } else if (curHandoffOrientation == config.VERTICAL_HANDOFF) {
            Circle circle = MapActivity.map.addCircle(new CircleOptions()
                    .center(new LatLng(curDataLocation.getLatitude(), curDataLocation.getLongitude()))
                    .radius(3)
                    .zIndex(1)
                    .strokeColor(Color.TRANSPARENT)
                    .fillColor(Color.MAGENTA));
        } else if (curHandoffOrientation == config.VERTICAL_HORIZONTANL_HANDOFF) {
            Circle circle = MapActivity.map.addCircle(new CircleOptions()
                    .center(new LatLng(curDataLocation.getLatitude(), curDataLocation.getLongitude()))
                    .radius(3)
                    .zIndex(1)
                    .strokeColor(Color.TRANSPARENT)
                    .fillColor(Color.YELLOW));
        }
    }
}
