package com.example.mmwavetracker.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mmwavetracker.R;
import com.example.mmwavetracker.core.Device;
import com.example.mmwavetracker.core.config;

public class DeviceActivity extends AppCompatActivity implements View.OnClickListener {

    TextView model_tv, carrier_tv, fiveg_tv,permission_tv;
    ImageButton reverify_btn;
    Button proceed_btn;


    private Integer verifiedStages;
//    private ConnectivityManager connMgr;
//    private TelephonyManager tm;
    Context context;
    private Device device;
    private boolean isDeviceVerified;
//    private boolean permissionFail,phoneFail,carrierFail,fivegFail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        context = getApplicationContext();
//        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        device = new Device(context);

        permission_tv =  findViewById(R.id.permission_check_flag);
        model_tv =  findViewById(R.id.phone_model_flag);
        carrier_tv =  findViewById(R.id.carrier_flag);
        fiveg_tv = findViewById(R.id.fiveg_status);
        reverify_btn = findViewById(R.id.reverify_device);
        proceed_btn = findViewById(R.id.proceed);


        reverify_btn.setOnClickListener(this);
        proceed_btn.setOnClickListener(this);
    }

    public void verification(){
        if (verifiedStages >= config.DEVICE_VERIFIED_NUMBER) {
            isDeviceVerified=true;
            Toast.makeText(getApplicationContext(), "All Checks passed!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Checks not passed!", Toast.LENGTH_LONG).show();
        }

        if (!reverify_btn.isEnabled()) {
            reverify_btn.setEnabled(true);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reverify_device:
                reverify_btn.setEnabled(false);
                verifyDevice();
                reverify_btn.setEnabled(true);
                verification();// do your code
                break;
            case R.id.proceed:
                if (isDeviceVerified){
                    Intent intent = new Intent(DeviceActivity.this, MapActivity.class);
                    startActivity(intent);
                }
                break;
            default:
                Toast.makeText(getApplicationContext(), "Please Check device first!", Toast.LENGTH_LONG).show();
                break;
        }

    }

    @SuppressLint("SetTextI18n")
    public void verifyDevice() {
        verifiedStages = 0;
        // check Permissions

        if (!config.hasMissingPermissions(this.context)) {
            // incompatible phone model
            permission_tv.setText("Pass");
            verifiedStages++;
        } else {
            permission_tv.setText("Failed");
            //permissionFail=true;
        }

        if (device.getPhoneName()!=null) {
            // incompatible phone model
            //model_tv.setText("Pass");
            model_tv.setText(device.getPhoneName());
            verifiedStages++;
        } else {
            model_tv.setText("Failed");
            //phoneFail=true;
        }

        // check if carrier supported
        if (device.getCarrierName()!=null) {
            //carrier_tv.setText("Pass");
            carrier_tv.setText(device.getCarrierName());
            verifiedStages++;
        } else {
            carrier_tv.setText("Failed");
            // carrierFail = true;
        }

        // are we connected to 5G
        if (device.check5GStatus()) {
            fiveg_tv.setText("Pass");
            //fiveg_tv.setText(device.getNetworkService());
            verifiedStages++;
        } else {
            fiveg_tv.setText("Failed");
            // fivegFail=true;
        }

    }

}
