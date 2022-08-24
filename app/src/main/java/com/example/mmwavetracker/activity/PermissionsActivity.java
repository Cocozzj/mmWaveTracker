package com.example.mmwavetracker.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mmwavetracker.R;
import com.example.mmwavetracker.core.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PermissionsActivity extends AppCompatActivity {

    private Button permBt;
    private ArrayAdapter<String> adapter;
    private int permsLeft;
    private HashMap<String, Boolean> permsSet;
    private Context context;
    private View[] lvElements;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = getApplicationContext().getSharedPreferences("perm-activity", 0);
        permsLeft = sharedPref.getInt("perms-left", config.NUM_PERMISSIONS_NEEDED);
        Log.d("permsLeft", permsLeft+"");
        //if all permissions already set go to next activity
        if(permsLeft == 0){
            Intent intent = new Intent(PermissionsActivity.this, DeviceActivity.class);
            startActivity(intent);
        }

        permsLeft = config.NUM_PERMISSIONS_NEEDED;

        setContentView(R.layout.activity_permissions);

        ListView permLv = findViewById(R.id.perm_permissionsLv);
        permBt = findViewById(R.id.perm_bt);

        lvElements = new View[config.NUM_PERMISSIONS_NEEDED];


        permsSet = new HashMap<>();
        for(String s: config.PERMISSION_NAMES_FULL){
            permsSet.put(s, false);
        }


        //populate and display in permLv descriptions of permissions that we need
        context = permLv.getContext().getApplicationContext();
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, config.PERMISSION_NAMES);

        adapter = new ArrayAdapter<>(context, android.R.layout.simple_expandable_list_item_1, list);

        permLv.setAdapter(adapter);

        //when a user clicks a list element, launch the intent to request that permission
        permLv.setOnItemClickListener((adapterView, view, i, l) -> {

            String perm = (String) adapterView.getItemAtPosition(i);

            //save view of element for later to access from handlePermGrant()
            lvElements[i] = view;

            requestPermission(perm);
        });
        //TODO: set permBt onclick to continue to validation activity
        permBt.setOnClickListener(v -> {
            Intent intent = new Intent(PermissionsActivity.this, DeviceActivity.class);
            startActivity(intent);
        });
    }

    private void handlePermGrant(String permName){

        //getting index of perm in config names array, assuming
        //they maintain the same order
        int indexOfPerm = -1;
        for(int i = 0; i < config.PERMISSION_NAMES_FULL.length; i++){
            if(config.PERMISSION_NAMES_FULL[i].equals(permName)){
                indexOfPerm = i;
                break;
            }
        }

        //get corresponding view to permission
        View view = lvElements[indexOfPerm];

        if(Boolean.FALSE.equals(permsSet.get(permName))){
            //perm has just been set for first time, color views bg and set in hashmap
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            permsSet.replace(permName, true);
            permsLeft--;
        }
        if(permsLeft <= 0){
            //keep sharedpref consistent with permsLeft
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("perms-left", 0);
            editor.apply();
            permBt.setEnabled(true);
        }
        adapter.notifyDataSetChanged();
    }

    //launches an activity to request perm
    private void requestPermission(String perm){
        Intent intent;
        switch(perm){
            case "Coarse Location":
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                break;
            case "Fine Location":
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                break;
            case "Location Extra":
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS}, 1);
                break;
            case "Background Location":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                }
                break;
            case "Write External":
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                break;
            case "Read External":
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                break;
            case "Read Phone State":
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                break;
            case "Internet":
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
                break;
            case "Activity Recognition":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
                }
                break;
            case "Write Settings":
                intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(intent);

                //handle this separately because we use a different method
                //TODO: use startActivityForResult to check if set afterwards
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED){
                    handlePermGrant("WRITE_SETTINGS");
                } else {
                    Toast.makeText(context, "Permission was not granted", Toast.LENGTH_LONG).show();
                }
                break;
            case "Usage Access Settings":
                intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
//
//                //handle this separately because we use a different method
//                //TODO: use startActivityForResult to check if set afterwards
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS) != PackageManager.PERMISSION_GRANTED){
                    handlePermGrant("USAGE_ACCESS_SETTINGS");
                } else {
                    Toast.makeText(context, "Permission was not granted", Toast.LENGTH_LONG).show();
                }
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.PACKAGE_USAGE_STATS}, 1);
                break;
            default:
                break;
        }
    }

    public void onRequestPermissionsResult (int requestCode,
                                            @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission was not granted", Toast.LENGTH_LONG).show();
        } else {
            String[] parts = permissions[0].split("\\.", 3);
            handlePermGrant(parts[2]);
        }
    }
}

