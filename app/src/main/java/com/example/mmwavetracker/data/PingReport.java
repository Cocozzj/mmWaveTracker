package com.example.mmwavetracker.data;

import com.example.mmwavetracker.core.config;

import java.time.Instant;
import java.util.StringJoiner;

public class PingReport {

    public static final String PINGFILEHEADER = "timestamp,payload,targetIP,targetHostName,rtt,destIP,destName";

    // Ping stats
    public Instant Timestamp;
    private int Payload = -1;
    public String TargetIP;
    public String TargetHost;
    public float Rtt = -1;
    public String DestinationIP = "";
    public String DestinationHost = "";

    // raw primitive
    public String RawHeader;

    public String RawData;


    private void Parse(){

        // Parse out pieces
        if(RawHeader != null){
            String[] headerPieces = RawHeader.split(" ");

            TargetHost = headerPieces[1];
            TargetIP = headerPieces[2];
            // clean off the ()
            TargetIP = TargetIP.replace("(","");
            TargetIP = TargetIP.replace(")","");


            String payload = headerPieces[3];
            // trim off the ()
            Payload = Integer.parseInt(payload.substring(0,payload.indexOf("(")));

            String[] dataPieces = RawData.split(" ");

            // is target responding? look for "from"
            if(dataPieces.length >= 2 && dataPieces[2].contains("from")){

                Rtt = Float.parseFloat(dataPieces[7].split("=")[1]);

                DestinationIP = dataPieces[4];
                // clean off the ()
                DestinationIP = DestinationIP.replace("(","");
                DestinationIP = DestinationIP.replace(")","");
                DestinationIP = DestinationIP.replace(":","");

                DestinationHost = dataPieces[3];

            }

        }

    }

    @Override
    public String toString(){

        try {
            this.Parse();

            StringJoiner stringJoiner = new StringJoiner(config.CSV_DELIMIETER);

            stringJoiner.add(config.SampleDateFormat.format(Timestamp));
            stringJoiner.add(String.valueOf(Payload));
            stringJoiner.add(TargetIP);
            stringJoiner.add(TargetHost);
            stringJoiner.add(String.valueOf(Rtt));
            stringJoiner.add(DestinationIP);
            stringJoiner.add(DestinationHost);

            return stringJoiner.toString();
        }catch (Exception ex){
            //  add exception handle here
            ex.printStackTrace();
        }

        return null;
    }
}
