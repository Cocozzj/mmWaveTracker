package com.example.mmwavetracker.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.mmwavetracker.core.Device;
import com.example.mmwavetracker.core.DisplayPhoneState;
import com.example.mmwavetracker.core.config;
import com.google.android.gms.location.DetectedActivity;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Report {
    private Context context;
    private ConnectivityManager ConnectManager;
    private TelephonyManager tm;
    private ServiceState servicestate;

    // Reporting fields
    public Instant TimeStamp;
    public List<CellInfo> CellInfos;
    public Network ActiveNetwork;
    public NetworkCapabilities networkCapabilities;
    public LinkProperties ActiveNetworkProperties;
    public Network[] AllNets;
    public Location LocationData;
    public SignalStrength SignalStrength;
    public Device device;

    // Reporting Fields, primitives
    public ServiceState RawServiceState;

    // Handoff type
    public int HandoffType = 0;
    public int HorizontalHandoff = 0;
    public int VerticalHandoff = 0;

    // data transmission
    public long RxMobileBytes = 0;
    public long TxMobileBytes = 0;
    public long RxMobilePackets = 0;
    public long TxMobilePackets = 0;
    public long RxMobileBytesUid = 0;
    public long TxMobileBytesUid = 0;
    public long RxMobilePacketsUid = 0;
    public long TxMobilePacketsUid = 0;

    public int DataNetworkType = -1;
    public int NetworkType = -1;

    public int DataActivity = -1;
    public int DataState = -1;

    public String nrStatus = "";
    public String mCid = "";
    public boolean is5GNSA = false;
    public boolean is5GSA = false;
    public DisplayPhoneState displayPhoneState;
    public static String accessNetworkTech = "";
    public String networkType = "";


    private String nr_CIs="",nr_PCIs="",lte_CIs="",lte_PCIs="",lte_Bwds="",nr_Signal="",lte_Signal="";
    private String allNrCells="",allLteCells="",allNrCellsIdentities="",allLteCellIdentities="";
    private String lte_ci, lte_dbm, lte_level, lte_rsrp, lte_rsrq, lte_rssi, lte_rssnr, lte_pci, lte_tac, lte_bwd, lte_mccstring, lte_mncstring,lte_AsuLevel,lte_earfcn,lte_operater;
    private String nr_dbm, nr_level, nr_ssrsrp, nr_sssinr, nr_ssrsrq, nr_Asulevel, nr_csirsrp, nr_csirsrq, nr_csisinr, nr_ci, nr_bds, nr_arfcn, nr_pci, nr_tac,nr_mccstring,nr_mncstring;


    private String[] activities = new String[10];
    private String mostProbableActivityType;
    private int mostProbableActivityConfidence;
    private int mobilityLabel, mobilityLabelv2; // old approach 101-still, 102-walking, 103-driving
    private float distTraveledFromLoc;

    // speed
    public long mobileRx_diff_Mbps = 0;
    public long mobileTx_diff_Mbps = 0;


    // Nr Signal Strength
    public String getNr_CIS() {
        return nr_CIs;
    }
    public String getNr_PCIS() {
        return nr_PCIs;
    }
    public String getNr_ssrsrp() {
        return nr_ssrsrp;
    }
    public String getNr_ssrsrq() {
        return nr_ssrsrq;
    }
    public String getNr_sssinr() {
        return nr_sssinr;
    }
    public String getNr_level() {
        return nr_level;
    }
    public String getNrStatus() {
        return nrStatus;
    }
    public String get5GSignalStrength(){
        return getNr_ssrsrp();
    }

    // 4G Signal Strength
    public String getLte_CIS() {
        return lte_CIs;
    }
    public String getLte_PCIS() {
        return lte_PCIs;
    }
    public String getLte_rsrp() {return lte_rsrp;}
    public String getLte_rsrq() {
        return lte_rsrq;
    }
    public String getLte_rssi() {
        return lte_rssi;
    }
    public String getLte_rssnr() {
        return lte_rssnr;
    }
    public String getLte_level() {
        return lte_level;
    }
    public String get4GSignalStrength() {
        return getLte_rsrp();
    }


    public Instant getTimeStamp() {
        return TimeStamp;
    }

    public Location getLocation() {
        return LocationData;
    }

    public int getHandoffType() {
        return HandoffType;
    }

    public int getHorizontalHandoff() {
        return HorizontalHandoff;
    }

    public int getVerticalHandoff() {
        return VerticalHandoff;
    }

    public String getMobilityActivityDesc() {
        String descMobileActivity;

        if (mostProbableActivityType == null) {
            return "NONE";
        }

        switch (mostProbableActivityType) {
            case "0":
                descMobileActivity = "IN_VEHICLE";
                break;
            case "1":
                descMobileActivity = "ON_BICYCLE";
                break;
            case "2":
                descMobileActivity = "ON_FOOT";
                break;
            case "3":
                descMobileActivity = "STILL";
                break;
            case "4":
                descMobileActivity = "UNKNOWN";
                break;
            case "5":
                descMobileActivity = "TILTING";
                break;
            case "7":
                descMobileActivity = "WALKING";
                break;
            case "8":
                descMobileActivity = "RUNNING";
                break;
            default:
                descMobileActivity = "NONE";
                break;
        }

        return descMobileActivity;
    }

    public String getMobilityLabelDesc() {
        if (mobilityLabel == config.MOBILITYLABEL_WALKING) {
            return "WALKING";
        } else if (mobilityLabel == config.MOBILITYLABEL_STATIONARY) {
            return "STILL";
        } else if (mobilityLabel == config.MOBILITYLABEL_DRIVING) {
            return "IN_VEHICLE";
        }
        return "UNKNOWN";
    }

    public String getMobilityLabelv2Desc() {
        if (mobilityLabelv2 == config.MOBILITYLABEL_WALKING) {
            return "WALKING";
        } else if (mobilityLabelv2 == config.MOBILITYLABEL_STATIONARY) {
            return "STILL";
        } else if (mobilityLabelv2 == config.MOBILITYLABEL_DRIVING) {
            return "IN_VEHICLE";
        }
        return "UNKNOWN";
    }

    public String getMobilityActivity() {
        return mostProbableActivityType;
    }

    public int getMobilityActivityConfidence() {
        return mostProbableActivityConfidence;
    }

    public float getSpeed() {
        return LocationData.getSpeed();
    }

    public boolean setSpeeds(long rx_bytes, long tx_bytes, Instant prev_timestamp) {

        try {

            float timedif = 0;

            if (prev_timestamp == null) {
                return true;
            }

            // get difference of time between two sample epochs; 10 is just to handle timedif not=0
            timedif = Math.max(Duration.between(prev_timestamp, this.getTimeStamp()).toMillis(), 1000) / 1000;

            // set RX speed using difference between previous value
            mobileRx_diff_Mbps = Math.round((((RxMobileBytes - rx_bytes) * 8) / 1000000) / (timedif));

            if (mobileRx_diff_Mbps < 0)
                mobileRx_diff_Mbps = 0;

            // set TX speed using difference between previous value
            mobileTx_diff_Mbps = Math.round((((TxMobileBytes - tx_bytes) * 8) / 1000000) / (timedif));

            if (mobileTx_diff_Mbps < 0)
                mobileTx_diff_Mbps = 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return true;
    }

    public String getRxSpeed() {
        return String.valueOf(mobileRx_diff_Mbps);
    }

    public String getTxSpeed() {
        return String.valueOf(mobileTx_diff_Mbps);
    }


    public boolean setActivities(ArrayList<DetectedActivity> detectedActivities) {
        for (DetectedActivity detectedActivity : detectedActivities) {
            activities[detectedActivity.getType()] = String.valueOf(detectedActivity.getConfidence());
        }

        mostProbableActivityType = String.valueOf(detectedActivities.get(0).getType());
        mostProbableActivityConfidence = detectedActivities.get(0).getConfidence();

        mobilityLabelv2 = config.MOBILITYLABEL_INVALID; // invalid
        if (mostProbableActivityType.equals("3")) {
            mobilityLabelv2 = config.MOBILITYLABEL_STATIONARY;
        } else if (mostProbableActivityType.equals("2") || mostProbableActivityType.equals("7") || mostProbableActivityType.equals("8")) {
            // this covers on foot, walking and running
            mobilityLabelv2 = config.MOBILITYLABEL_WALKING;
        } else if (mostProbableActivityType.equals("0")) {
            // this covers driving
            mobilityLabelv2 = config.MOBILITYLABEL_DRIVING;
        }

        return true;
    }

    public boolean setMobilityLabelv1(int i) {
        mobilityLabel = i;
        return true;
    }

    public float getMobilityOrientation() {
        return this.LocationData.getBearing();
    }

    public boolean setMobilityActivityv1(Location loc) {
        try {
            distTraveledFromLoc = LocationData.distanceTo(loc);

            if (distTraveledFromLoc <= config.MOBILITYLABEL_STATIONARY_MAX_THRESHOLD_METERS) {
                mobilityLabel = config.MOBILITYLABEL_STATIONARY;
            } else if (config.MOBILITYLABEL_STATIONARY_MAX_THRESHOLD_METERS < distTraveledFromLoc && distTraveledFromLoc <= config.MOBILITYLABEL_WALKING_MAX_THRESHOLD_METERS) {
                mobilityLabel = config.MOBILITYLABEL_WALKING;
            } else if (distTraveledFromLoc > config.MOBILITYLABEL_WALKING_MAX_THRESHOLD_METERS) {
                mobilityLabel = config.MOBILITYLABEL_DRIVING;
            } else {
                mobilityLabel = config.MOBILITYLABEL_INVALID;
            }

        } catch (Exception e) {
            e.printStackTrace();
            mobilityLabel = config.MOBILITYLABEL_INVALID;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void setContext(Context cont) {
        //TimeStamp = Instant.now();
        context= cont;
        ConnectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //device = new Device(context);
        CellInfos = tm.getAllCellInfo();
        RawServiceState = tm.getServiceState();
        SignalStrength = tm.getSignalStrength();
        networkCapabilities=ConnectManager.getNetworkCapabilities(ConnectManager.getActiveNetwork());
        //displayPhoneState = new DisplayPhoneState(context);
        getIs5GNSA();
        getIs5GNSA();
        getAccessNetworkTech();
        getDetailNetworkType();
        getCellinfoData();

    }
    public String setNrStatus() {
//        //Parse the Text for the 5G flag
        nrStatus = "";

        try{
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//                List<NetworkRegistrationInfo> reginfoList= RawServiceState.getNetworkRegistrationInfoList();
//                for (NetworkRegistrationInfo reginfo : reginfoList) {
//
//                }
//
//            }


            Log.i("!Report!","SDK_INT/1");
            Method[]methods = new Method[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                methods = ServiceState.class.getMethods();
            }
            for(Method method: methods){
                Log.i("!Report!", method.getName());
            }

//
            Class reportHelper = Class.forName("com.example.mmwavetracker.data.Report");
            Log.i("!Report!","SDK_INT/2");
            @SuppressLint("BlockedPrivateApi") Method getNetworkRegistrationInfo = ServiceState.class.getMethod("getNrState");
            Log.i("!Report!","SDK_INT/3");
            getNetworkRegistrationInfo.setAccessible(true);
            //getNetworkRegistrationInfo.set(reportHelper,null);
            Log.i("!Report!","Method/"+getNetworkRegistrationInfo);
//            Method getNetworkRegistrationInfo = ServiceState.class.getMethod("getNetworkRegistrationInfoList");
//
//            Log.i("!Report!","SDK_INT/2");
////            Log.i("!Report!","Method/"+ServiceState.class.getMethod("getNetworkRegistrationInfoList"));
////
//            getNetworkRegistrationInfo.setAccessible(true);
//            Log.i("!Report!","SDK_INT/3");
           //int result = (int)getNetworkRegistrationInfo.invoke(RawServiceState);
//            Log.i("!Report!","Method/"+result);
//            switch(result){
//                case NetworkRegistrationInfo.NR_STATE_CONNECTED:
//                    nrStatus = config.NR_CONNECTED_KEYWORD;
//                    break;
//                case NetworkRegistrationInfo.NR_STATE_RESTRICTED:
//                    nrStatus = config.NR_RESTRICTED_KEYWORD;
//                    break;
//                case NetworkRegistrationInfo.NR_STATE_NOT_RESTRICTED:
//                    nrStatus = config.NR_NOT_RESTRICTED_KEYWORD;
//                    break;
//                case NetworkRegistrationInfo.NR_STATE_NONE:
//                    nrStatus = config.NRC_NONE_KEYWORD;
//                    break;
//            }
        }catch (Exception e){
            Log.i("!Report!","error");
            e.printStackTrace();
        }
        return nrStatus;
    }


    public String getAccessNetworkTech() {
        accessNetworkTech=device.getNetworkService();
        return accessNetworkTech;
    }

    public String getDetailNetworkType()
    {
        networkType=device.getNetworkType();
        return networkType;
    }
    public int setHandoffType(String prevNrStatus, String prevMCid, String prevAccessNetworkTech) {

        this.HandoffType = 0;
        this.HorizontalHandoff = 0;
        this.VerticalHandoff = 0;

        boolean towerChanged = !this.mCid.equals(prevMCid);
        boolean nrStatusChanged = !this.nrStatus.equals(prevNrStatus);

        //add the SA situation
        if (prevAccessNetworkTech.equals(config.NETWORK_TYPE_5GSA)) {
            if (accessNetworkTech.equals(config.NETWORK_TYPE_5GSA)) {
                if (towerChanged) {
                    this.HandoffType = 15;
                    this.HorizontalHandoff = 1;
                }
            } else {
                //HH+VH
                if (towerChanged) {
                    this.HandoffType = 18;
                    this.HorizontalHandoff = 1;
                    this.VerticalHandoff = 1;
                } else {
                    this.HandoffType = 16;
                    this.VerticalHandoff = 1;
                }
            }
        } else if (accessNetworkTech.equals(config.NETWORK_TYPE_5GSA)) {
            if (!towerChanged) {
                this.HandoffType = 17;
                this.VerticalHandoff = 1;
            } else if (prevNrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD)) {
                this.HandoffType = 19;
                this.VerticalHandoff = 1;
                this.HorizontalHandoff = 1;
            }
        } else {
            if (!towerChanged && nrStatusChanged) {
                if (prevNrStatus.equals(config.NR_CONNECTED_KEYWORD) && this.nrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD)) {
                    this.HandoffType = 1;
                    this.VerticalHandoff = 1;
                } else if (prevNrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD) && this.nrStatus.equals(config.NR_CONNECTED_KEYWORD)) {
                    this.HandoffType = 2;
                    this.VerticalHandoff = 1;
                }
            } else if (towerChanged && !nrStatusChanged) {
                if (this.nrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD)) {
                    this.HandoffType = 4;
                    this.HorizontalHandoff = 1;
                } else if (this.nrStatus.equals(config.NRC_NONE_KEYWORD)) {
                    this.HandoffType = 9;
                    this.HorizontalHandoff = 1;
                }
            } else if (towerChanged && nrStatusChanged) {
                if (prevNrStatus.equals(config.NR_CONNECTED_KEYWORD) && this.nrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD)) {
                    this.HandoffType = 3;
                    this.VerticalHandoff = 1;
                    this.HorizontalHandoff = 1;
                } else if (prevNrStatus.equals(config.NR_CONNECTED_KEYWORD) && this.nrStatus.equals(config.NRC_NONE_KEYWORD)) {
                    this.HandoffType = 10;
                    this.VerticalHandoff = 1;
                    this.HorizontalHandoff = 1;
                } else if (prevNrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD) && this.nrStatus.equals(config.NR_CONNECTED_KEYWORD)) {
                    this.HandoffType = 13;
                    this.VerticalHandoff = 1;
                    this.HorizontalHandoff = 1;
                } else if (prevNrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD) && this.nrStatus.equals(config.NRC_NONE_KEYWORD)) {
                    this.HandoffType = 11;
                    this.HorizontalHandoff = 1;
                } else if (prevNrStatus.equals(config.NRC_NONE_KEYWORD) && this.nrStatus.equals(config.NR_NOT_RESTRICTED_KEYWORD)) {
                    this.HandoffType = 12;
                    this.HorizontalHandoff = 1;
                } else if (prevNrStatus.equals(config.NRC_NONE_KEYWORD) && this.nrStatus.equals(config.NR_CONNECTED_KEYWORD)) {
                    this.HandoffType = 14;
                    this.VerticalHandoff = 1;
                    this.HorizontalHandoff = 1;
                }
            }
        }
        return HandoffType;
    }


    public static final String REPORTHEADER = "timestamp"
            + "," + "mobileDataActivity"
            + "," + "mobileDataState"
            + "," + "mobileRx"
            + "," + "mobileTx"
            + "," + "mobileRxPackets"
            + "," + "mobileTxPackets"
            + "," + "mobileRxUid"
            + "," + "mobileTxUid"
            + "," + "mobileRxPacketsUid"
            + "," + "mobileTxPacketsUid"
            + "," + "mobileRx_diff_Mbps"
            + "," + "mobileTx_diff_Mbps";

    public static final String LOCATIONHEADER = "," + "locationProvider"
            + "," + "latitude"
            + "," + "longitude"
            + "," + "locationAccuracy"
            + "," + "movingSpeed"
            + "," + "movingSpeedAccuracyMPS"
            + "," + "compassDirection"
            + "," + "compassAccuracy"
// Activities
            + "," + "activity0"
            + "," + "activity1"
            + "," + "activity2"
            + "," + "activity3"
            + "," + "activity4"
            + "," + "activity5"
            + "," + "activity6"
            + "," + "activity7"
            + "," + "activity8"
            + "," + "activity9"
            + "," + "mostProbableActivityType"
            + "," + "mobilityLabel"
            + "," + "mobilityLabelv2";

    public static final String SIGNALFEATURE = "," + "ActiveNetwork"
            + "," + "NetworkService"
            + "," + "NetworkType"
            +"," + "displayNetworkState"
            + "," + "linkUpKbps"
            + "," + "linkDownKbps"
            + "," + "SignalStrLevel"
            + "," + "Carrier"
            + "," + "nrStatus"
// 4G
            + "," + "ltePCIs"
            + "," + "lteCIs"
//            + "," + "ltersrp"
//            + "," + "ltersrq"
//            + "," + "lterssi"
//            + "," + "lterssnr"
            + "," + "lteBwds"
            + "," + "lteSignal(rsrp,rsrq,rssi,rssnr)"
//            + "," + "lteTac"
//            + "," + "lteAsuLevel"
//            + "," + "lteDbm"
// 5G
            + "," + "is5GSAMode"
            + "," + "is5GNSAMode"
            + "," + "nrPCIs"
            + "," + "nrCIs"
            + "," + "nrSignal(ssrsrp,ssrsrq,sssinr,csirsrp,csirsrq,csisinr)"
//            + "," + "nr_ssRsrp"
//            + "," + "nr_ssRsrq"
//            + "," + "nr_CsiRsrp"
//            + "," + "nr_CsiRsrq"
//            + "," + "nr_CsiSinr"
//            + "," + "nr_Bds"
//            + "," + "nr_Tac"
//            + "," + "nr_level"
//            + "," + "nr_Asulevel"
//            + "," + "nr_Dbm"
            + "," + "handoffType"
            + "," + "horizontalHandoff"
            + "," + "verticalHandoff";

    public static final String REPORTHEADER_VERBOSE_ADDITIONAL = "," + "RawServiceState"
            + "," + "LengthRawCellInfos"
            + "," + "RawCellInfos"
            + "," + "RawSignalStrengths"
            + "," + "RawNetworkCapabilities"
            + "," + "NrCellInfos"
            + "," + "LteCellInfos"
            + "," + "NrCellIdentities"
            + "," + "LteCellIdentities"
            + "," + "HasCapabilityNETCAPABILITYNOTMETERED"
            + "," + "HasCapabilityNETCAPABILITYNOTCONGESTED"
            + "," + "DNSservers"
            + "," + "LinkAddresses"
            + "," + "allInterfaces"
            ;
//            + "," + "primaryLteCellInfo"
//            + "," + "secondaryLteCellInfo"
//            + "," + "primaryNrCellInfo"
//            + "," + "secondaryNrCellInfo"
//            + "," + "primaryCellIdentity"
//            + "," + "secondaryNrCellIdentity"

    public void getCellinfoData(){
        StringJoiner allLte_Cells = new StringJoiner("===");
        StringJoiner allNr_Cells = new StringJoiner("===");

        //Put all of the cell infos together
        StringJoiner allLte_CellIdentities = new StringJoiner("===");
        StringJoiner allNr_CellsIdentities = new StringJoiner("===");

        StringBuilder ltePCIs = new StringBuilder();
        StringBuilder lteCIs = new StringBuilder();
        StringBuilder lteBwds = new StringBuilder();
        StringBuilder lteSignal = new StringBuilder();
        StringBuilder nrPCIs = new StringBuilder();
        StringBuilder nrCIs = new StringBuilder();
        StringBuilder nrSignal = new StringBuilder();

        for (CellInfo cinfo : this.CellInfos) {

            StringJoiner cellGroup = new StringJoiner(config.CSV_DELIMIETER);
            StringJoiner cellIdentityGroup = new StringJoiner(config.CSV_DELIMIETER);

            cellGroup.add(cinfo.toString());

            // check if lte primary
            if (cinfo instanceof CellInfoLte) {
                CellInfoLte lte = (CellInfoLte) cinfo;
                CellIdentityLte lteid = lte.getCellIdentity();
                CellSignalStrengthLte sigStrLte = lte.getCellSignalStrength();
                // add signal strength info

                lte_dbm = String.valueOf(sigStrLte.getDbm());
                lte_AsuLevel= String.valueOf(sigStrLte.getAsuLevel());
                lte_level = String.valueOf(sigStrLte.getLevel());
                lte_rsrp = String.valueOf(sigStrLte.getRsrp());
                lte_rsrq = String.valueOf(sigStrLte.getRsrq());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    lte_rssi = String.valueOf(sigStrLte.getRssi());
                }
                lte_rssnr = String.valueOf(sigStrLte.getRssnr());
                lte_pci = String.valueOf(lteid.getPci());
                lte_tac = String.valueOf(lteid.getTac());
                lte_bwd = String.valueOf(lteid.getBandwidth());
                lte_ci = String.valueOf(lteid.getCi());
                lte_mccstring = lteid.getMccString();
                lte_mncstring = lteid.getMncString();
                lte_earfcn = String.valueOf(lteid.getEarfcn());
                lte_operater = String.valueOf(lteid.getMobileNetworkOperator());

                cellGroup.add(lte_rsrp);
                cellGroup.add(lte_rsrq);
                cellGroup.add(lte_rssi);
                cellGroup.add(lte_rssnr);
                cellGroup.add(lte_level);
                cellGroup.add(lte_AsuLevel);
                cellGroup.add(lte_dbm);

                cellIdentityGroup.add(lte_ci);
                cellIdentityGroup.add(lte_pci);
                cellIdentityGroup.add(lte_bwd);
                cellIdentityGroup.add(lte_tac);
                cellIdentityGroup.add(lte_earfcn);
                cellIdentityGroup.add(lte_operater);
                cellIdentityGroup.add(lte_mccstring);
                cellIdentityGroup.add(lte_mncstring);

                ltePCIs.append(lte_pci).append(";");
                lteBwds.append(lte_bwd).append(";");
                lteCIs.append(lte_ci).append(";");
                lteSignal.append(lte_rsrp).append(",").append(lte_rsrq).append(",").append(lte_rssi).append(",").append(lte_rssnr).append(";");

//                    if (cinfo.getCellConnectionStatus() == CellInfo.CONNECTION_PRIMARY_SERVING) {
//                        hasPrimaryLteCell = true;
//                        primaryCell.add(cellGroup.toString());
//                        primaryCellIdentity.add(cellIdentityGroup.toString());
//                    }else if (cinfo.getCellConnectionStatus() == CellInfo.CONNECTION_SECONDARY_SERVING){
//                        hasSecondaryLteCell = true;
//                        secondaryCell.add(cellGroup.toString());
//                        primaryCellIdentity.add(cellIdentityGroup.toString());
//                    }

                allLte_Cells.add(cellGroup.toString());
                allLte_CellIdentities.add(cellIdentityGroup.toString());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (cinfo instanceof CellInfoNr) {
                    CellInfoNr nr = (CellInfoNr) cinfo;
                    CellIdentityNr nrid = (CellIdentityNr) nr.getCellIdentity();
                    CellSignalStrengthNr sigStrNr = (CellSignalStrengthNr) nr.getCellSignalStrength();

                    nr_dbm = String.valueOf(sigStrNr.getDbm());
                    nr_level = String.valueOf(sigStrNr.getLevel());
                    nr_Asulevel = String.valueOf(sigStrNr.getLevel());
                    nr_csirsrp = String.valueOf(sigStrNr.getCsiRsrp());
                    nr_csirsrq = String.valueOf(sigStrNr.getCsiRsrq());
                    nr_csisinr = String.valueOf(sigStrNr.getCsiSinr());
                    nr_ssrsrp = String.valueOf(sigStrNr.getSsRsrp());
                    nr_ssrsrq = String.valueOf(sigStrNr.getSsRsrq());
                    nr_sssinr = String.valueOf(sigStrNr.getSsSinr());
                    nr_ci = String.valueOf(nrid.getNci());
                    nr_arfcn = String.valueOf(nrid.getNci());
                    nr_pci = String.valueOf(nrid.getPci());
                    nr_tac = String.valueOf(nrid.getTac());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        nr_bds = String.valueOf(nrid.getBands());
                    }
                    nr_mccstring = nrid.getMccString();
                    nr_mncstring = nrid.getMncString();

                    cellGroup.add(nr_ssrsrp);
                    cellGroup.add(nr_ssrsrq);
                    cellGroup.add(nr_sssinr);
                    cellGroup.add(nr_csirsrp);
                    cellGroup.add(nr_csirsrq);
                    cellGroup.add(nr_csisinr);
                    cellGroup.add(nr_level);
                    cellGroup.add(nr_Asulevel);
                    cellGroup.add(nr_dbm);

                    cellIdentityGroup.add(nr_ci);
                    cellIdentityGroup.add(nr_pci);
                    cellIdentityGroup.add(nr_tac);
                    cellIdentityGroup.add(nr_arfcn);

                    nrPCIs.append(nr_pci).append(";");
                    nrCIs.append(nr_ci).append(";");
                    nrSignal.append(nr_ssrsrp).append(",").append(nr_ssrsrq).append(",").append(nr_sssinr).append(",").append(nr_csirsrp).append(",").append(nr_csirsrq).append(",").append(nr_csisinr).append(";");


                    //                    if (cinfo.getCellConnectionStatus() == CellInfo.CONNECTION_SECONDARY_SERVING) {
                    //                        hasSecondaryNrCell = true;
                    //                        secondaryNrCell.add(cellGroup.toString());
                    //                        secondaryNrCellIdentity.add(cellIdentityGroup.toString());
                    //                    }
                    allNr_Cells.add(cellGroup.toString());
                    allNr_CellsIdentities.add(cellIdentityGroup.toString());
                }
            }
        }
        nr_CIs= nrCIs.toString();
        nr_PCIs=nrPCIs.toString();
        lte_CIs= lteCIs.toString();
        nr_Signal = nrSignal.toString();
        lte_PCIs= ltePCIs.toString();
        lte_Bwds= lteBwds.toString();
        lte_Signal = lteSignal.toString();
        allLteCells=allLte_Cells.toString();
        allNrCells=allNr_Cells.toString();
        allLteCellIdentities=allLte_CellIdentities.toString();
        allNrCellsIdentities=allNr_CellsIdentities.toString();
    }

    @Override
    public String toString() {
        StringJoiner strJoiner = new StringJoiner(config.CSV_DELIMIETER);
        try {
            //REPORTHEADER
            strJoiner.add(config.SampleDateFormat.format(TimeStamp));

            strJoiner.add(String.valueOf(this.DataActivity));
            strJoiner.add(String.valueOf(this.DataState));
            strJoiner.add(String.valueOf(this.RxMobileBytes));
            strJoiner.add(String.valueOf(this.TxMobileBytes));
            strJoiner.add(String.valueOf(this.RxMobilePackets));
            strJoiner.add(String.valueOf(this.TxMobilePackets));
            strJoiner.add(String.valueOf(this.RxMobileBytesUid));
            strJoiner.add(String.valueOf(this.TxMobileBytesUid));
            strJoiner.add(String.valueOf(this.RxMobilePacketsUid));
            strJoiner.add(String.valueOf(this.TxMobilePacketsUid));
            strJoiner.add(String.valueOf(this.mobileRx_diff_Mbps));
            strJoiner.add(String.valueOf(this.mobileTx_diff_Mbps));

            //LOCATIONHEADER
            if ((this.LocationData != null) ) {
                strJoiner.add(this.LocationData.getProvider());
                strJoiner.add(String.valueOf(this.LocationData.getLatitude()));
                strJoiner.add(String.valueOf(this.LocationData.getLongitude()));
                strJoiner.add(String.valueOf(this.LocationData.getAccuracy()));

                strJoiner.add(String.valueOf(this.LocationData.getSpeed()));
                strJoiner.add(String.valueOf(this.LocationData.getSpeedAccuracyMetersPerSecond()));
                strJoiner.add(String.valueOf(this.LocationData.getBearing()));
                strJoiner.add(String.valueOf(this.LocationData.getBearingAccuracyDegrees()));
            } else {
                strJoiner.add("-1");
                strJoiner.add("-1");
                strJoiner.add("-1");
                strJoiner.add("-1");
                strJoiner.add("-1");
                strJoiner.add("-1");
                strJoiner.add("-1");
                strJoiner.add("-1");
            }

            // append activities

            for (int i = 0; i < activities.length; i++)
                strJoiner.add(activities[i]);

            strJoiner.add(mostProbableActivityType);
            strJoiner.add(String.valueOf(mobilityLabel));
            strJoiner.add(String.valueOf(mobilityLabelv2));

            //Put all of the cell infos together


            // SIGNALFEATURE

            strJoiner.add(ConnectManager.getActiveNetwork().toString());
            strJoiner.add(device.getNetworkService());
            strJoiner.add(device.getNetworkType());
            strJoiner.add(String.valueOf(displayPhoneState.netDisplayType));
            strJoiner.add(String.valueOf(networkCapabilities.getLinkUpstreamBandwidthKbps()));
            strJoiner.add(String.valueOf(networkCapabilities.getLinkDownstreamBandwidthKbps()));
            strJoiner.add(String.valueOf(SignalStrength.getLevel()));
            strJoiner.add(device.carrier);
            //strJoiner.add(nrStatus);

            StringBuilder allNetsString = new StringBuilder();
            for (Network net : this.AllNets) {

                NetworkCapabilities netinfo = ConnectManager.getNetworkCapabilities(net);

                allNetsString.append("\"");
                allNetsString.append(net.toString());
                allNetsString.append("\"");
                allNetsString.append(device.getNetworkType());
                allNetsString.append("\"");
                allNetsString.append(netinfo != null);
                allNetsString.append("\"");
            }


            // 4G

            strJoiner.add(lte_PCIs);
            strJoiner.add(lte_CIs);
            strJoiner.add(lte_bwd);
            strJoiner.add(lte_Signal);
//            strJoiner.add(lte_pci);
//            strJoiner.add(lte_ci);
//            strJoiner.add(lte_rsrp);
//            strJoiner.add(lte_rsrq);
//            strJoiner.add(lte_rssi);
//            strJoiner.add(lte_rssnr);
//            strJoiner.add(lte_bwd);
//            strJoiner.add(lte_tac);
//            strJoiner.add(lte_AsuLevel);
//            strJoiner.add(lte_dbm);
            //5G
            strJoiner.add(nr_PCIs);
            strJoiner.add(nr_CIs);
            strJoiner.add(nr_Signal);
//            strJoiner.add(String.valueOf(device.getIs5GSA()));
//            strJoiner.add(String.valueOf(device.getIs5GNSA()));
//            strJoiner.add(nr_pci);
//            strJoiner.add(nr_ci);
//            strJoiner.add(nr_ssrsrp);
//            strJoiner.add(nr_ssrsrq);
//            strJoiner.add(nr_csirsrp);
//            strJoiner.add(nr_csirsrq);
//            strJoiner.add(nr_csisinr);
//            strJoiner.add(nr_bds);
//            strJoiner.add(nr_tac);
//            strJoiner.add(nr_level);
//            strJoiner.add(nr_Asulevel);
//            strJoiner.add(nr_dbm);

            strJoiner.add(String.valueOf(this.HandoffType));
            strJoiner.add(String.valueOf(this.HorizontalHandoff));
            strJoiner.add(String.valueOf(this.VerticalHandoff));
            //

            strJoiner.add("\"" + RawServiceState.toString() + "\"");
            strJoiner.add("" + this.CellInfos.size());
            strJoiner.add("\"" + this.CellInfos.toString() + "\"");
            strJoiner.add("\"" + this.SignalStrength.toString() + "\"");
            strJoiner.add("\"" + networkCapabilities.toString() + "\"");
            strJoiner.add("\"" + allNrCells + "\"");
            strJoiner.add("\"" + allLteCells + "\"");
            strJoiner.add("\"" + allNrCellsIdentities + "\"");
            strJoiner.add("\"" + allLteCellIdentities + "\"");
            strJoiner.add("" + networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED));
            strJoiner.add("" + networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED));
            strJoiner.add("\"" + ActiveNetworkProperties.getDnsServers().toString() + "\"");
            strJoiner.add("\"" + ActiveNetworkProperties.getLinkAddresses().toString() + "\"");
            strJoiner.add("\"" + allNetsString.toString());


        }catch (Exception ex) {
            Log.e("EXCEPTION", ex.toString());
        }

        return strJoiner.toString();
    }


    public String setMCid(){
        for (CellInfo info : this.CellInfos) {
            String cellInfoStr = info.toString();
            if (cellInfoStr.contains("mRegistered=YES")) {
                if (info instanceof CellInfoLte) {
                    CellInfoLte lteg = (CellInfoLte) info;
                    CellIdentityLte lteidg = lteg.getCellIdentity();
                    mCid = String.valueOf(lteidg.getCi());
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (info instanceof CellInfoNr) {
                        CellInfoNr nrg = (CellInfoNr) info;
                        CellIdentityNr nridg = (CellIdentityNr) nrg.getCellIdentity();
                        mCid = String.valueOf(nridg.getNci());
                    }
                }

            }
        }
    return mCid;
    }

    public String getMCid(){
        return mCid;
    }
    public boolean getIs5GNSA(){
        this.is5GNSA=device.getIs5GNSA();
        return is5GNSA;
    }
    public boolean getIs5GSA(){
        this.is5GSA=device.getIs5GSA();
        return is5GSA;
    }

    public void setSignalStrength(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            List<CellSignalStrength> sigstrName = SignalStrength.getCellSignalStrengths();
            Log.i("Report","+sigstrName+"+sigstrName);
        }
    }
}
