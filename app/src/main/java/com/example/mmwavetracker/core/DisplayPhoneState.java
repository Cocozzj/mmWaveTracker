package com.example.mmwavetracker.core;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyDisplayInfo;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class DisplayPhoneState extends PhoneStateListener {
    public static String netDisplayType = "";
    private final Context appContext;

    public DisplayPhoneState(Context appCon) {
        this.appContext = appCon;
    }


    @Override
    public void onDisplayInfoChanged(TelephonyDisplayInfo telephonyDisplayInfo) {
        if (ActivityCompat.checkSelfPermission(this.appContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // permission should already been granted
            Log.e("Permission", "onServiceStateChanged: READ_PHONE_STATE not set! Should already be granted");
        }

        super.onDisplayInfoChanged(telephonyDisplayInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            switch (telephonyDisplayInfo.getOverrideNetworkType()) {
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE:
                    netDisplayType = config.NETWORK_TYPE_MMWAVE;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA:
                    netDisplayType= config.NETWORK_TYPE_5GNSA;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO:
                    netDisplayType = config.NETWORK_TYPE_LTEPRO;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA:
                    netDisplayType = config.NETWORK_TYPE_LTECA;
                    break;
                case TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED:
                    netDisplayType = config.NETWORK_TYPE_NR_ADVANCED;
                    break;
                default:
                    netDisplayType = config.NETWORK_TYPE_LTE;
            }
        }
    }
}
