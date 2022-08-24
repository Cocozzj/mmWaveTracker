//
// Created by Herman Cheung on 27/2/2017.
//

#include <jni.h>
#include <stdio.h>
#include <sys/select.h>
#include <iperf_api.h>
#include <android/log.h>
#include <iperf.h>
//#include "iperf_api.h"

//int run(struct iperf_test *test);
int main(int argc, char **argv);


JNIEXPORT jstring JNICALL
                  Java_hkhc_iperfwrapper_HelloJni_stringFromJNI( JNIEnv* env,
                                                                    jobject thiz )
{

    struct iperf_test *test;

    test = iperf_new_test();

    if (!test)
        iperf_errexit(NULL, "create new test error - %s", iperf_strerror(i_errno));
    iperf_defaults(test);	/* sets defaults */

    iperf_set_test_role(test, 'c');
//    iperf_set_test_server_hostname(test, "192.168.202.250");
    iperf_set_test_server_hostname(test, "192.168.51.55");

    iperf_set_test_template(test, "/data/data/hkhc.iperfwrapper.sample/cache/iperftmpXXXXXX");
//    iperf_set_test_server_hostname(test, "192.168.51.55");
//    iperf_set_test_server_port(test, 5001);
    iperf_set_test_duration(test, 10);
    test->outfile = fopen("/data/data/hkhc.iperfwrapper.sample/cache/output.txt","a+");

    __android_log_print(ANDROID_LOG_DEBUG, "IPERF", "Start client");

    if (iperf_run_client(test) < 0) {
        iperf_errexit(test, "error - %s", iperf_strerror(i_errno));
        __android_log_print(ANDROID_LOG_ERROR, "IPERF", "client error");
    }

    __android_log_print(ANDROID_LOG_DEBUG, "IPERF", "client finished");

//
//    if (iperf_parse_arguments(test, sizeof(argv)/sizeof(char*), argv) < 0) {
//        iperf_err(test, "parameter error - %s", iperf_strerror(i_errno));
//        fprintf(stderr, "\n");
//        usage_long();
//        return (*env)->NewStringUTF(env, "Hello from JNI !  Compiled with ABI. error");
//    }
//
//    if (run(test) < 0)
//        iperf_errexit(test, "error - %s", iperf_strerror(i_errno));
//
//    iperf_free_test(test);


    return (*env)->NewStringUTF(env, "Hello from JNI !  Compiled with ABI.");
}