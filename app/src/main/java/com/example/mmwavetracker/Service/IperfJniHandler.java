package com.example.mmwavetracker.Service;

import android.util.Log;

import com.example.mmwavetracker.core.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IperfJniHandler {

    private static Path iperfFilePath;
    private static boolean iperfJSON;
    private static boolean iperfTimeStampAdded;
    private static String iperfLabel;

    static public void initializeIperfSettings(String iperfFile, boolean iperfJSONFlag,
                                               String iperfTitle) {
        iperfFilePath = Paths.get(iperfFile);
        iperfJSON = iperfJSONFlag;
        iperfLabel = iperfTitle;
        iperfTimeStampAdded = false;
    }

    static public void recvIperfData(String msg) {
        try {
            // check if the last char is a newline, then delete it because it will be added
            // after encryption
            int msg_len = msg.length();
            if (msg_len > 0 && msg.charAt(msg_len - 1) == '\n')
                msg = msg.substring(0, msg_len - 1);

            // Convert the timestamp received from the server from GMT time zone to the local zone
            // this is required to be able to merge the 5GTracker file with iperf files
            if (!iperfJSON) {
                // Handle iperf txt files
                if (msg.contains("Time: ")) {
                    // extract the timestamp when iperf started
                    int time_index = msg.lastIndexOf("Time: ");

                    String iperfTimeStr = msg.substring(time_index + 6);

                    String convertedTimeStamp = convertDateTimeToLocalZone(iperfTimeStr);

                    msg = msg.substring(0, time_index + 6) + convertedTimeStamp;
                    iperfTimeStampAdded = true;
                }
                if (msg.contains("Starting Test: ") && (!iperfTimeStampAdded)) {
                    // Add the timestamp before the current message in case it was never sent
                    // by server
                    String currentTimeStamp = config.iperfDateFormat.format(Instant.now());

                    String time_msg = iperfLabel + ":  Time: " + currentTimeStamp;

                    Files.write(iperfFilePath, (time_msg + "\n").getBytes(), StandardOpenOption.APPEND);
                    // uncomment the following line to save unencrypted file
                    iperfTimeStampAdded = true;
                }
            } else {
                // Handle iperf JSON files
                // find the indices of these two strings which enclose the timestamp
                int indexOfTimeStart = msg.indexOf("\"time\":");
                int indexofTimeEnd = msg.indexOf("\"timesecs\":");
                if (indexOfTimeStart != -1 && indexofTimeEnd != -1) {
                    // extract the timestamp when iperf started
                    String iperfTimeStr = msg.substring(indexOfTimeStart + 9, indexofTimeEnd - 6);

                    String convertedTimeStamp = convertDateTimeToLocalZone(iperfTimeStr);

                    // add the converted timestamp back to the JSON string in its correct position
                    msg = msg.substring(0, indexOfTimeStart + 9) + convertedTimeStamp +
                            msg.substring(indexofTimeEnd - 6);
                }
            }

            // Write the received iperf string to the file
           Files.write(iperfFilePath, (msg + "\n").getBytes(), StandardOpenOption.APPEND);
            // uncomment the following line to save unencrypted file
            //Files.write(iperfFilePath, msg.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static private String convertDateTimeToLocalZone(String iperfTimeStr) {
        Log.i("IperfJniHandler", "ORIGINAL IPERF TIME TXT: " + iperfTimeStr);

        // Convert the timestamp from string to date object
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss z");
        Instant iperfTimeInstant = ZonedDateTime.parse(iperfTimeStr, formatter).toInstant();

        // convert the timestamp from GMT to the current timezone
        String convertedTimeStamp = config.iperfDateFormat.format(iperfTimeInstant);
        Log.i("IperfJniHandler", "Converted IPERF TIME JSON: " + convertedTimeStamp);

        return convertedTimeStamp;
    }

}
