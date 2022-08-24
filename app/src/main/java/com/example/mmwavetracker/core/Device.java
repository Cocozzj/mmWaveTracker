package com.example.mmwavetracker.core;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Device {

    public static String carrier;
    public static String phoneModel;
    public static String network_service;
    public static String network_type,connect_type;
    public static String phoneName;

    public Context context;
    public ConnectivityManager connMgr;
    public TelephonyManager tm;
    public DisplayPhoneState displayPhoneState;

    public boolean is5Gverified,is5GSA,is5GNSA;

    public String getCarrierName() {
        carrier = tm.getNetworkOperatorName();
        return carrier;
    }

    public String getPhoneModel() {
        phoneModel = Build.MODEL;
        return phoneModel;
    }

    public Device(Context cont) {
        context= cont;
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        displayPhoneState = new DisplayPhoneState(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            tm.listen(displayPhoneState, PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED);
        }

        // clearDevice();
        getPhoneModel();
        getPhoneName();
        getCarrierName();
        getNetworkType();
        getNetworkService();

    }

    @SuppressLint("MissingPermission")
    public String getConnectType() {
        NetworkCapabilities networkInfo = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
        if (networkInfo != null) {
            if (networkInfo.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                connect_type = config.NETWORK_TYPE_WIFI;
            } else if (networkInfo.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                connect_type = config.NETWORK_TYPE_MOBILE;
            }
        } else {
            connect_type = config.NETWORK_NOT_CONNECTED;
        }
        return connect_type;
    }

    @SuppressLint("MissingPermission")

    public String getNetworkService() {
        String connect_type = getConnectType();
        if (config.lacksPermission(this.context, Manifest.permission.READ_PHONE_STATE)) {
            Log.e("Permission", "Device: READ_PHONE_STATE not set!");
        }
        if (connect_type.equals(config.NETWORK_TYPE_MOBILE)) {
            switch (tm.getDataNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_NR:
                    network_service = config.NETWORK_TYPE_5GSA;
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    network_service = config.NETWORK_TYPE_LTE;
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    break;
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    break;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    break;
                case TelephonyManager.NETWORK_TYPE_GSM:
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    break;
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    break;
                case TelephonyManager.NETWORK_TYPE_IWLAN:
                    break;
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    break;
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    break;
            }
        } else if (connect_type.equals(config.NETWORK_TYPE_WIFI)){
            network_service = config.NETWORK_TYPE_WIFI;
        } else {
            network_service = config.NETWORK_TYPE_UNKNOWN;
        }
//        Log.i("!MapActivity!", "network_service/"+network_service);

        return network_service;
    }


    @SuppressLint({"MissingPermission", "SwitchIntDef"})
    public String getNetworkType() {
        String connect_type = getConnectType();
        if (config.lacksPermission(this.context, Manifest.permission.READ_PHONE_STATE)) {
            Log.e("Permission", "Device: READ_PHONE_STATE not set!");
        }
        if (connect_type.equals(config.NETWORK_TYPE_MOBILE)) {
            switch (tm.getDataNetworkType()){
                case TelephonyManager.NETWORK_TYPE_NR:
                    network_type = config.NETWORK_TYPE_5GSA;
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    network_type= displayPhoneState.netDisplayType;
                    break;
                default:
                    network_type = config.NETWORK_TYPE_MOBILE;
            }
        } else if (connect_type.equals(config.NETWORK_TYPE_WIFI)){
            network_type = config.NETWORK_TYPE_WIFI;
        } else {
            network_type = config.NETWORK_TYPE_UNKNOWN;
        }
        // Log.i("!MapActivity!", "network_type/"+network_type);
        return network_type;

    }

    public boolean check5GStatus() {
        this.is5Gverified= getIs5GSA() || getIs5GNSA();
        return this.is5Gverified;
    }

    public boolean getIs5GSA(){
        this.is5GSA= getNetworkType().equals(config.NETWORK_TYPE_5GSA);
        return this.is5GSA;
    }
    public boolean getIs5GNSA(){
        this.is5GNSA= getNetworkType().equals(config.NETWORK_TYPE_MMWAVE) || getNetworkType().equals(config.NETWORK_TYPE_5GNSA) || getNetworkType().equals(config.NETWORK_TYPE_NR_ADVANCED);
        return this.is5GNSA;
    }

    public void clearDevice() {
        carrier = "";
        phoneModel = "";
        phoneName = "";
        network_type="";
        network_service="";
    }

//    public boolean checkDeviceVerified() {
//        return ((this.carrier!=null) && (this.phoneModel!=null) && (this.is5Gverified));
//    }

    public String getPhoneName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            phoneName=capitalize(model);
        } else {
            phoneName=capitalize(manufacturer) + " " + model;
        }
        return phoneName;
    }
    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}

