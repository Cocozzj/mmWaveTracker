package com.example.mmwavetracker.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.mmwavetracker.core.config;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class DetectedActivitiesIntentService  extends IntentService {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();
    public ArrayList<DetectedActivity> detectedActivities;


    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super("DetectedActivityService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        detectedActivities = (ArrayList) result.getProbableActivities();

        for (DetectedActivity activity : detectedActivities) {
            Log.i("!AN!DetectedActivity", "Details: " + activity.getType() + ", " + activity.getConfidence());
        }

        Intent outputIntent = new Intent(config.BROADCAST_DETECTED_ACTIVITY);
        outputIntent.setAction(config.BROADCAST_DETECTED_ACTIVITY);
        outputIntent.putExtra("all_activities", detectedActivities);
        sendBroadcast(outputIntent);
    }


}

