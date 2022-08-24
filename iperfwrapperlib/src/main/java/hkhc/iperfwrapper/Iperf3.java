package hkhc.iperfwrapper;

/**
 * Created by herman on 6/3/2017.
 */

public class Iperf3 {

    static {
        System.loadLibrary("jni");
    }

    public static final char ROLE_SERVER = 's';
    public static final char ROLE_CLIENT = 'c';

    private long testRef = 0;
    public int testResult = 0;      // test error code
    public String testResultMessage = "";    // test error message

    private native long newTestImpl();
    private native void freeTestImpl(long ref);

    private native void endTestImpl(long ref);
    private native void testRoleImpl(long ref, char role);
    private native void defaultsImpl(long ref);
    private native void hostnameImpl(long ref, String host);
    private native void protocolImpl(long ref, char protocol);
    private native void tempFileTemplateImpl(long ref, String template);
    private native void durationImpl(long ref, int duration);
    private native void intervalImpl(long ref, int interval_ms);
    private native void numStreamImpl(long ref, int num_streams);
    private native void serverPortImpl(long ref, int server_port);
    private native void reverseImpl(long ref, int reverse);
    private native void bandwidthImpl(long ref, String bandwidth);
    private native void verboseImpl(long ref, int verbose);
    private native void logFileImpl(long ref, String logfile);
    private native void titleImpl(long ref, String title);

    private native int runClientImpl(long ref);

    private native String getTestResultImpl(long ref);
    private native void outputJsonImpl(long ref, boolean useJson);

    public Iperf3 newTest() throws IperfException {
        if (testRef!=0) {
            freeTest();
        }
        testRef = newTestImpl();
        if (testRef==0) {
            throw new IperfException("Failed to initialize test");
        }
        return this;
    }

    public void endTest() {
        if (testRef != 0) {
            endTestImpl(testRef);
            testRef = 0;
        }
    }


    public void freeTest() {
        if (testRef!=0) {
            freeTestImpl(testRef);
            testRef=0;
        }
    }

    public Iperf3 testRole(char role) {
        testRoleImpl(testRef, role);
        return this;
    }

    public Iperf3 defaults() {
        defaultsImpl(testRef);
        return this;
    }

    public Iperf3 defaults(DefaultConfig config) {
        defaultsImpl(testRef);
        config.defaults(this);
        return this;
    }

    public Iperf3 hostname(String host) {
        hostnameImpl(testRef, host);
        return this;
    }

    public Iperf3 protocol(char protocol) {
        protocolImpl(testRef, protocol);
        return this;
    }

    public Iperf3 tempFileTemplate(String template) {
        tempFileTemplateImpl(testRef, template);
        return this;
    }

    public Iperf3 durationInSeconds(int duration) {
        durationImpl(testRef, duration);
        return this;
    }

    public Iperf3 intervalInMilliSeconds(int interval_ms) {
        intervalImpl(testRef, interval_ms);
        return this;
    }

    public Iperf3 numParallelStreams(int num_streams) {
        numStreamImpl(testRef, num_streams);
        return this;
    }

    public Iperf3 serverPort(int server_port) {
        serverPortImpl(testRef, server_port);
        return this;
    }

    public Iperf3 reverse(int reverse) {
        reverseImpl(testRef, reverse);
        return this;
    }

    public Iperf3 udp_bandwidth(String bandwidth) {
        bandwidthImpl(testRef, bandwidth);
        return this;
    }

    public Iperf3 verbose(int verbose) {
        verboseImpl(testRef, verbose);
        return this;
    }

    public Iperf3 logfile(String file) {
        logFileImpl(testRef, file);
        return this;
    }

    public Iperf3 title(String title) {
        titleImpl(testRef, title);
        return this;
    }

    public Iperf3 runClient() {
        testResult = runClientImpl(testRef);
        return this;
    }

    public Iperf3 getTestResultMessage() {
        if (testRef != 0) {
            testResultMessage = getTestResultImpl(testRef);
        }
        return this;
    }

    public Iperf3 outputJson(boolean useJson) {
        outputJsonImpl(testRef, useJson);
        return this;
    }

}
