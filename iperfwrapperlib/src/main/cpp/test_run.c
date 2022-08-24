//
// Created by Herman Cheung on 6/3/2017.
//

#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <sys/select.h>
#include <iperf_api.h>
#include <android/log.h>
#include <iperf.h>
#include <assert.h>


// processing callback to handler class
typedef struct iperf_context {
    JavaVM *javaVM;              // Used to cache the VM
    jclass iperfjJiHelperClz;    // edu/umn/fivegtracker/Service/IperfJniHandler Class
    jobject iperfJniHelperObj;   // edu/umn/fivegtracker/Service/IperfJniHandler Instance
} IperfContext;
IperfContext g_ctx;


/*
 *  A helper function to show how to call
 *     java static function IperfJniHandler::recvIperfData(String msg)
 *  The implementation for this function (recvIperfData) is inside file
 *     edu/umn/fivegtracker/Service/IperfJniHandler.java
 */
void sendIperfDataToJavaClass(JNIEnv *env, jclass java_class, char *msg) {
    jstring javaMsg = (*env)->NewStringUTF(env, msg);
    jmethodID statusId = (*env)->GetStaticMethodID(env, java_class,
                                                   "recvIperfData",
                                                   "(Ljava/lang/String;)V");
    (*env)->CallStaticVoidMethod(env, java_class, statusId, javaMsg);
    (*env)->DeleteLocalRef(env, javaMsg);
}

/*
 * A helper function which can be called from the C code to pass
 * a message to a Java method using a JNI call.
 */
void recv_iperf_data_from_C(char *msg) {
    JNIEnv *env;

    if ((*(g_ctx.javaVM))->GetEnv(g_ctx.javaVM, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return; // JNI version not supported.
    }

    sendIperfDataToJavaClass(env, g_ctx.iperfjJiHelperClz, msg);
}

/*
 * processing one time initialization:
 *     Cache the javaVM into our context
 *     Find class ID for IperfJniHandler: used to call static methods
 *     Create an instance of IperfJniHandler: used to call non-static methods
 *     Make global reference since we are using them from a native thread
 * Note:
 *     All resources allocated here are never released by application
 *     we rely on system to free all global refs when it goes away;
 *     the pairing function JNI_OnUnload() never gets called at all.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "IPERF", "JNI_OnLoad JNI is called");
    JNIEnv *env;
    memset(&g_ctx, 0, sizeof(g_ctx));

    g_ctx.javaVM = vm;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR; // JNI version not supported.
    }

    jclass clz = (*env)->FindClass(env,
                                   "edu/umn/fivegtracker/Service/IperfJniHandler");
    g_ctx.iperfjJiHelperClz = (*env)->NewGlobalRef(env, clz);

    jmethodID jniHelperCtor = (*env)->GetMethodID(env, g_ctx.iperfjJiHelperClz,
                                                  "<init>", "()V");
    jobject handler = (*env)->NewObject(env, g_ctx.iperfjJiHelperClz,
                                        jniHelperCtor);
    g_ctx.iperfJniHelperObj = (*env)->NewGlobalRef(env, handler);

    return JNI_VERSION_1_6;
}

JNIEXPORT jlong JNICALL
Java_hkhc_iperfwrapper_Iperf3_newTestImpl( JNIEnv* env, jobject thiz )
{

    struct iperf_test *test;
    test = iperf_new_test();
    __android_log_print(ANDROID_LOG_DEBUG, "IPERF", "new test %lld", (long long)test);

    return (jlong)test;

}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_freeTestImpl( JNIEnv* env, jobject thiz, jlong ref)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_free_test(test);

}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_endTestImpl(JNIEnv *env, jobject thiz, jlong ref) {

    struct iperf_test *test;
    test = (struct iperf_test *) ref;

    iperf_client_end(test);

}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_testRoleImpl( JNIEnv* env, jobject thiz, jlong ref, jchar role)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    __android_log_print(ANDROID_LOG_DEBUG, "IPERF", "set role %c %lld", (char)role, 'c');

    iperf_set_test_role(test, (char)role );

}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_defaultsImpl( JNIEnv* env, jobject thiz, jlong ref)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_defaults(test);

}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_hostnameImpl( JNIEnv* env, jobject thiz, jlong ref, jstring j_hostname)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    const char *hostname = (*env)->GetStringUTFChars(env, j_hostname, 0);

    iperf_set_test_server_hostname(test, hostname);

    (*env)->ReleaseStringUTFChars(env, j_hostname, hostname);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_protocolImpl( JNIEnv* env, jobject thiz, jlong ref, jchar protocol)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    __android_log_print(ANDROID_LOG_DEBUG, "IPERF", "set protocol %c ", (char)protocol);

    iperf_set_test_protocol(test, protocol);
}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_tempFileTemplateImpl( JNIEnv* env, jobject thiz, jlong ref, jstring j_template)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    const char *template = (*env)->GetStringUTFChars(env, j_template, 0);

    iperf_set_test_template(test, template);

    (*env)->ReleaseStringUTFChars(env, j_template, template);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_durationImpl( JNIEnv* env, jobject thiz, jlong ref, jint duration)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_set_test_duration(test, duration);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_intervalImpl( JNIEnv* env, jobject thiz, jlong ref, jint interval_ms)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_set_test_interval(test, interval_ms);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_numStreamImpl( JNIEnv* env, jobject thiz, jlong ref, jint num_streams)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_set_test_num_streams(test, num_streams);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_serverPortImpl( JNIEnv* env, jobject thiz, jlong ref, jint server_port)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_set_test_server_port(test, server_port);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_reverseImpl( JNIEnv* env, jobject thiz, jlong ref, jint reverse)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_set_test_reverse(test, reverse);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_verboseImpl( JNIEnv* env, jobject thiz, jlong ref, jint verbose)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_set_verbose(test, verbose);


}

JNIEXPORT jint JNICALL
Java_hkhc_iperfwrapper_Iperf3_runClientImpl( JNIEnv* env, jobject thiz, jlong ref)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    test->outfile = fopen(test->logfile, "a+");
    // start the iperf test
    iperf_run_client(test);

    // get iperf test error code and error message
    int result = i_errno;
    test->iperf_error_message = iperf_strerror(i_errno);
    __android_log_print(ANDROID_LOG_DEBUG, "IPERF", "TEST is FINISHED Result: %d Error Code: %d "
                                                    "Error Message %s", test->done, i_errno,
                        test->iperf_error_message);
    return (jint) result;

}

JNIEXPORT jstring JNICALL
Java_hkhc_iperfwrapper_Iperf3_getTestResultImpl(JNIEnv *env, jobject thiz, jlong ref) {

    struct iperf_test *test;
    test = (struct iperf_test *) ref;

    // return iperf test result error message
    const char *cStr = test->iperf_error_message;

    return (jstring) ((*env)->NewStringUTF(env, cStr));

}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_outputJsonImpl( JNIEnv* env, jobject thiz, jlong ref, jboolean useJson)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    iperf_set_test_json_output(ref, (int)useJson);

}

/*
 * Command line options
 */


// OPT_LOG

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_bandwidthImpl( JNIEnv* env, jobject thiz, jlong ref, jstring j_bandwidth)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    const char *bandwidth = (*env)->GetStringUTFChars(env, j_bandwidth, 0);

    iperf_set_test_udp_bandwidth(test, bandwidth);

    (*env)->ReleaseStringUTFChars(env, j_bandwidth, bandwidth);


}


JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_logFileImpl( JNIEnv* env, jobject thiz, jlong ref, jstring j_logfile)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    const char *logfile = (*env)->GetStringUTFChars(env, j_logfile, 0);

    test->logfile = strdup(logfile);

    (*env)->ReleaseStringUTFChars(env, j_logfile, logfile);


}

JNIEXPORT void JNICALL
Java_hkhc_iperfwrapper_Iperf3_titleImpl( JNIEnv* env, jobject thiz, jlong ref, jstring j_title)
{

    struct iperf_test *test;
    test = (struct iperf_test *)ref;

    const char *title = (*env)->GetStringUTFChars(env, j_title, 0);

    test->title = strdup(title);

    (*env)->ReleaseStringUTFChars(env, j_title, title);


}

