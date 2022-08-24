package com.example.mmwavetracker.data;

import android.location.Location;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.example.mmwavetracker.core.config;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

@Entity
public class SessionSummary {

    protected int session_id_sys;

    @ColumnInfo(name = "session_id")
    protected String sessionId;       // R: (indicated included in report)

    public SessionSummary(String sessionId) {
        this.sessionStarted = false;
        this.sessionId = sessionId;
        this.samplingIntervalms = config.SAMPLING_RATE_IN_MS;
        if (config.PING_ENABLE_FLAG) {
            this.pingEnabled = "Yes";
        } else {
            this.pingEnabled = "No";
        }

        this.tcpDumpCommand = null;
        this.nTrackerParts = 0;
        this.nIperfRuns = 0;
        this.nTowers = 0;
        this.nHandoffs = 0;
        this.nVerticalHandoffs = 0;
        this.nHorizontalHandoffs = 0;
        this.nSecondsIn5G = 0;
        this.distanceCoveredInMeters = 0;
        this.uploaded = false;
        this.uploadProgressStatus = 0;
        this.currentNrStatus = null;
        this.runNumbers = "";
        this.pingBatch = config.MAX_PING_LINES_PER_BATCH;
        this.pingInterval = config.PING_DELAY_IN_MS;
        this.trackerBatch = config.MAX_LINES_PER_BATCH;
        this.screenBrightnessPosition = config.SCREEN_BRIGHTNESS_POSITION;

    }

    public String getLastTime5GSeen() {
        if (this.currentNrStatus.equals(config.NR_CONNECTED_KEYWORD))
            return "In 5G Zone";
        if (this.lastTime5GSeen == null)
            return "5G Not Seen Yet";
        else {
            // return the difference between the current time and the timestamp of the last 5G Connected
            // in minutes and seconds
            Instant curTime = Instant.now();
            long diff = Duration.between(this.lastTime5GSeen, curTime).getSeconds();
            int minutes = ((int) diff) / 60;
            int secs = ((int) diff) % 60;
            return String.format("%02d", minutes) + ":" + String.format("%02d", secs);
        }
    }

    public double getPercentageOf5G() {
        Instant curTime = Instant.now();
        Instant sTime = LocalDateTime.parse(this.startTime, config.SampleDateFormat).atZone(ZoneId.systemDefault()).toInstant();
        long diff = Duration.between(sTime, curTime).getSeconds();
        if (diff != 0) {
            if (this.nSecondsIn5G >= diff || (this.nSecondsIn5G / diff) > 0.99)
                return 100;
            else
                return (this.nSecondsIn5G / diff) * 100;
        } else {
            //return this.nSecondsIn5G;
            if (this.currentNrStatus.equals(config.NR_CONNECTED_KEYWORD))
                return 100;
            if (this.lastTime5GSeen == null)
                return 0;
        }
        return this.nSecondsIn5G;
    }

    public double getDistanceCovered() {
        // TODO: Check the unit of the distance meters or miles and convert this number accordingly
        double distanceCovered_miles = this.distanceCoveredInMeters * 0.00062137119;
        return distanceCovered_miles;
    }

    public void incrementTotalNumberIperfParts() {

    }

    public void appendIperfRunNumber(String run_number) {
        this.runNumbers += " " + run_number;
        this.nIperfRuns++;
    }

    public boolean endSession(int parts, String tcpDumpCommand) {
        this.endTime = config.SampleDateFormat.format(Instant.now());
        this.nTrackerParts = parts;
        this.tcpDumpCommand = tcpDumpCommand;
        return true;
    }

    public void processNetworkReport(String sessionId, Report report) {

        if (!this.sessionStarted) {
            this.sessionStarted = true;
            this.sessionId = sessionId;
            this.startTime = config.SampleDateFormat.format(report.getTimeStamp());
            this.prevLocation = report.getLocation();
        }

        this.curMCid = report.getMCid();
        this.towers.add(report.getMCid());
        this.nTowers = towers.size();

        if (report.getHandoffType() != 0)
            this.nHandoffs++;
        if (report.getVerticalHandoff() == 1)
            this.nVerticalHandoffs++;
        if (report.getHorizontalHandoff() == 1)
            this.nHorizontalHandoffs++;

        if (report.getHandoffType() == 0)
            this.curHandoffOrientation = config.NO_HANDOFF;
        else if (report.getVerticalHandoff() == 1 && report.getHorizontalHandoff() == 1)
            this.curHandoffOrientation = config.VERTICAL_HORIZONTANL_HANDOFF;
        else if (report.getVerticalHandoff() == 1)
            this.curHandoffOrientation = config.VERTICAL_HANDOFF;
        else if (report.getHorizontalHandoff() == 1)
            this.curHandoffOrientation = config.HORIZONTAL_HANDOFF;

        this.currentNrStatus = report.getNrStatus();
        if (report.getNrStatus().equals(config.NR_CONNECTED_KEYWORD)) {
            this.lastTime5GSeen = report.getTimeStamp();
            this.nSecondsIn5G += config.SAMPLING_RATE_IN_MS * 1.0 / 1000;
        }

        // get the distance covered in meters

        this.curLocation = report.getLocation();
        this.distanceCoveredInMeters += prevLocation.distanceTo(this.curLocation);
        this.prevLocation = this.curLocation;


            // get current mobility fields
        this.curMobilityActivity = report.getMobilityActivityDesc();
        this.curMobilityLabelActivity = report.getMobilityLabelDesc();
        this.curMobilityConfidence = report.getMobilityActivityConfidence();
        this.curMobilitySpeed = report.getSpeed();
        this.curMobilityOrientation = report.getMobilityOrientation();


        // get temperature stats
//        this.cpuTemperature = report.GetCPUTemperature();
//        this.batteryTemperature = report.GetBatteryTemperature();

        // get rx/tx speeds
        this.rxSpeed = report.getRxSpeed();
        this.txSpeed = report.getTxSpeed();

        // get 4G, 5G signal strength
        this.signalStrength4G = report.get4GSignalStrength();
        this.signalStrength5G = report.get5GSignalStrength();

        // get NSA or SA
        this.isNSA5G = report.getIs5GNSA();
        this.isSA5G = report.getIs5GSA();

        this.networkService=report.getAccessNetworkTech();
        this.networkType=report.getDetailNetworkType();

        this.lteCi=report.getLte_CIS();
        this.ltePci=report.getLte_PCIS();
        this.nrCi=report.getNr_CIS();
        this.nrPci=report.getNr_PCIS();
    }


    // Report header
    private static final String SESSION_SUMMARY_HEADER = "startTimestamp"
            + "," + "endTimestamp"
            + "," + "sessionId"
            + "," + "samplingIntervalms"
            + "," + "pingEnabled"
            + "," + "totalNumberOfTrackerParts"
            + "," + "totalHandoffs"
            + "," + "totalVerticalHandoffs"
            + "," + "totalHorizontalHandoffs"
            + "," + "secondsIn5G"
            + "," + "distanceCoveredMeters"
            + "," + "totalNumberTowers"
            + "," + "percentage5G"
            + "," + "uploadedStatus"
            + "," + "tcpdumpCommand"
            + "," + "runNumbers"
            + "," + "screenBrightnessSetting"
            + "," + "trackerBatch"
            + "," + "pingBatch"
            + "," + "pingInterval"
            + "," + "rrcProbeFlag";

    public String getHeader() {
        return SESSION_SUMMARY_HEADER;
    }

    @Override
    public String toString() {
        StringJoiner strJoiner = new StringJoiner(config.CSV_DELIMIETER);

        try {
            strJoiner.add(startTime);
            strJoiner.add(endTime);
            strJoiner.add(sessionId);
            strJoiner.add(Integer.toString(samplingIntervalms));
            strJoiner.add(pingEnabled);
            strJoiner.add(String.valueOf(getNTrackerParts()));
            strJoiner.add(String.valueOf(getNHandoffs()));
            strJoiner.add(String.valueOf(getNVerticalHandoffs()));
            strJoiner.add(String.valueOf(getNHorizontalHandoffs()));
            strJoiner.add(String.valueOf(getNSecondsIn5G()));
            strJoiner.add(String.valueOf(getDistanceCovered()));
            strJoiner.add(String.valueOf(getNTowers()));
            strJoiner.add(String.valueOf(getPercentageOf5G()));
            strJoiner.add(Boolean.toString(uploaded));
            strJoiner.add(tcpDumpCommand);
            strJoiner.add(runNumbers);
            strJoiner.add(config.SCREEN_BRIGHTNESS_OPTS[screenBrightnessPosition]);
            strJoiner.add(Integer.toString(trackerBatch));
            strJoiner.add(Integer.toString(pingBatch));
            strJoiner.add(Integer.toString(pingInterval));
            strJoiner.add(Boolean.toString(rrcProbeFlag));
        } catch (Exception ex) {
            Log.e("EXCEPTION", ex.toString());
        }

        return strJoiner.toString();
    }

    public int getSession_id_sys() {
        return session_id_sys;
    }

    public void setSession_id_sys(int session_id_sys) {
        this.session_id_sys = session_id_sys;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getNTrackerParts() {
        return nTrackerParts;
    }

    public void setNTrackerParts(int nTrackerParts) {
        this.nTrackerParts = nTrackerParts;
    }

    public int getNIperfRuns() {
        return nIperfRuns;
    }

    public void setNIperfRuns(int nIperfRuns) {
        this.nIperfRuns = nIperfRuns;
    }

    public int getNTowers() {
        return nTowers;
    }

    public void setNTowers(int nTowers) {
        this.nTowers = nTowers;
    }

    public int getNHandoffs() {
        return nHandoffs;
    }

    public void setNHandoffs(int nHandoffs) {
        this.nHandoffs = nHandoffs;
    }

    public int getNVerticalHandoffs() {
        return nVerticalHandoffs;
    }

    public void setNVerticalHandoffs(int nVerticalHandoffs) {
        this.nVerticalHandoffs = nVerticalHandoffs;
    }

    public int getNHorizontalHandoffs() {
        return nHorizontalHandoffs;
    }

    public void setNHorizontalHandoffs(int nHorizontalHandoffs) {
        this.nHorizontalHandoffs = nHorizontalHandoffs;
    }

    public double getNSecondsIn5G() {
        return nSecondsIn5G;
    }

    public void setNSecondsIn5G(double nSecondsIn5G) {
        this.nSecondsIn5G = nSecondsIn5G;
    }

    public double getDistanceCoveredInMeters() {
        return distanceCoveredInMeters;
    }

    public void setDistanceCoveredInMeters(double distanceCoveredInMeters) {
        this.distanceCoveredInMeters = distanceCoveredInMeters;
    }


    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public int getUploadProgressStatus() {
        return uploadProgressStatus;
    }

    public void setUploadProgressStatus(int uploadProgressStatus) {
        this.uploadProgressStatus = uploadProgressStatus;
    }

    public String getCurrentNrStatus() {
        return currentNrStatus;
    }

    public String getRxSpeed() {
        return rxSpeed;
    }

    public String getTxSpeed() {
        return txSpeed;
    }

    public float getCpuTemperature() {
        return cpuTemperature;
    }

    public double getBatteryTemperature() {
        return batteryTemperature;
    }

    public Location getPrevLocation() {
        return prevLocation;
    }

    public Location getCurLocation() {
        return curLocation;
    }

    public int getCurHandoffOrientation() {
        return curHandoffOrientation;
    }

    public String getCurMobilityActivity() {
        return curMobilityActivity;
    }

    public String getCurMobilityLabelActivity() {
        return curMobilityLabelActivity;
    }

    public int getCurMobilityConfidence() {
        return curMobilityConfidence;
    }

    public float getCurMobilitySpeed() {
        return curMobilitySpeed;
    }

    public String getCurMCid() {
        return curMCid;
    }

    public float getCurMobilityOrientation() {
        return curMobilityOrientation;
    }

    public String getSignalStrength4G() {
        return signalStrength4G;
    }

    public String getSignalStrength5G() {
        return signalStrength5G;
    }

    public String getPingEnabled() {
        return pingEnabled;
    }

    public void setPingEnabled(String pingEnabled) {
        this.pingEnabled = pingEnabled;
    }

    public int getSamplingIntervalms() {
        return samplingIntervalms;
    }

    public void setSamplingIntervalms(int samplingIntervalms) {
        this.samplingIntervalms = samplingIntervalms;
    }

    public String getTcpDumpCommand() {
        return tcpDumpCommand;
    }

    public void setTcpDumpCommand(String tcpDumpCommand) {
        this.tcpDumpCommand = tcpDumpCommand;
    }

    public String getRunNumbers() {
        return runNumbers;
    }

    public void setRunNumbers(String runNumbers) {
        this.runNumbers = runNumbers;
    }

    public boolean isRrcProbeFlag() {
        return rrcProbeFlag;
    }

    public void setRrcProbeFlag(boolean rrcProbeFlag) {
        this.rrcProbeFlag = rrcProbeFlag;
    }

    public boolean getIsNSA5G() {return isNSA5G;}
    public void setIsNSA5G(boolean flag) {this.isNSA5G = flag;}
    public boolean getIsSA5G() {return isSA5G;}
    public void setIsSA5G(boolean flag) {this.isSA5G = flag;}

    public String getNetworkService() {
        return networkService;
    }
    public String getNetworkType() {
        return networkType;
    }

    @Ignore
    private boolean sessionStarted;


    private String pingEnabled;

    private int samplingIntervalms;

    private String startTime;

    private String endTime;

    private int nTrackerParts;

    private int nIperfRuns;

    private String runNumbers;

    private int nTowers;

    @Ignore
    private Set<String> towers = new HashSet<>();

    private int nHandoffs;           // R: total number of primitive handoffs vh+hh handoffs

    private int nVerticalHandoffs;   // R: number of vertical handoffs (between diff technologies)

    private int nHorizontalHandoffs; // R: number of horizontal handoffs (between diff towers)

    private double nSecondsIn5G;

    private double distanceCoveredInMeters;

    private boolean uploaded;

    private int uploadProgressStatus;

    private String tcpDumpCommand;

    private boolean rrcProbeFlag;

    public int getScreenBrightnessPosition() {
        return screenBrightnessPosition;
    }

    public void setScreenBrightnessPosition(int screenBrightnessPosition) {
        this.screenBrightnessPosition = screenBrightnessPosition;
    }

    public int getTrackerBatch() {
        return trackerBatch;
    }

    public void setTrackerBatch(int trackerBatch) {
        this.trackerBatch = trackerBatch;
    }

    public int getPingBatch() {
        return pingBatch;
    }

    public void setPingBatch(int pingBatch) {
        this.pingBatch = pingBatch;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    public String getLteCi() {return this.lteCi;}
    public String getLtePci() {return this.ltePci;}
    public String getNrCi() {return this.nrCi;}
    public String getNrPci() {return this.nrPci;}

    private int screenBrightnessPosition;

    private int trackerBatch;

    private int pingBatch;

    private int pingInterval;

    private boolean isNSA5G;

    private boolean isSA5G;


    @Ignore
    private Instant lastTime5GSeen;

    @Ignore
    private String currentNrStatus;

    @Ignore
    private String rxSpeed;

    @Ignore
    private String txSpeed;

    @Ignore
    private float cpuTemperature;

    @Ignore
    private double batteryTemperature;

    @Ignore
    private Location prevLocation;

    @Ignore
    private Location curLocation;

    @Ignore
    private int curHandoffOrientation;     // none(0), vertical(1), horizontal(2), or both (3)

    @Ignore
    private String curMobilityActivity;      // set by the Google API

    @Ignore
    private String curMobilityLabelActivity; // manual setting

    @Ignore
    private int curMobilityConfidence;

    @Ignore
    private float curMobilitySpeed;

    @Ignore
    private String curMCid;

    @Ignore
    private float curMobilityOrientation;

    @Ignore
    private String signalStrength4G;

    @Ignore
    private String signalStrength5G;

    @Ignore
    private String networkType;

    @Ignore
    private String lteCi;
    @Ignore
    private String ltePci;
    @Ignore
    private String nrCi;
    @Ignore
    private String nrPci;
    @Ignore
    private String networkService;


}
