//
// Created by harry on 8/13/19.
//

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <errno.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <netinet/tcp.h>
#include <fcntl.h>
#include <stdarg.h>
#include <time.h>
#include <sys/time.h>
#include <fstream>
#include <sstream>

#include <pthread.h>

#include <jni.h>
#include <android/log.h>

#define LOG_TAG    "client"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define BUFFER_SIZE 50000
//#define DEBUG

#define NET_UNKNOWN 0
#define NET_WIFI 1
#define NET_2G 2
#define NET_3G 3
#define NET_4G 4
#define NET_5G 5


//#define DEBUG

using namespace std;

typedef unsigned char BYTE;
typedef unsigned int DWORD;     // 4 bytes
typedef unsigned short WORD;   // 2 bytes
typedef struct network_info
{
  int recv_bytes = 0;
  int recv_pkt = 0;
  int send_bytes = 0;
  int send_pkt = 0;
} network_info;

int s; // file descriptor
void *client(void *x);
pthread_t clientThread;

int networkStat(struct network_info &n)
{
    ifstream fileStat("/proc/net/dev");
    string line;
    while (getline(fileStat, line))
    {
        int para[16];
        int count = 0;
#ifdef DEBUG // assuming debug will use WiFi network
        if (line.find("wlan0") != string::npos)
#else
        if (line.find("rmnet_data0") != string::npos)
#endif
        {
            istringstream tmp(line);
            string word;
            tmp >> word;// char* token = strtok(line, ' ');
            while (tmp)
            {
                tmp >> para[count++]; // token = strtok(NULL, ' ')
            }
            n.recv_bytes = para[0];
            n.recv_pkt = para[1];
            n.send_bytes = para[8];
            n.send_pkt = para[9];
            return 1;
        }
    }
    return 1;
}

void client(JNIEnv * env, jobject * thiz, jstring server_ip) //int, char * * argv
{
    int nRecv = 0; // number of bytes received
    int nPing = 0; // number of pings received (start from 0)
    int seq_recv = 0;
    int seq_now = -1;
    struct timeval start, curr;




    struct sockaddr_in serverAddr;
    memset(&serverAddr, 0, sizeof(struct sockaddr_in));
    socklen_t serverAddrLen = sizeof(serverAddr);

    jclass cls = env->GetObjectClass(*thiz);
    jmethodID getNetType = env->GetMethodID(cls, "getNetTypeEnum", "()I");

    jmethodID getRxId = env->GetMethodID(cls, "getRxPackets_wrapper", "()J");
    jmethodID getTxId = env->GetMethodID(cls, "getTxPackets_wrapper", "()J");
    jmethodID updateCliUI = env->GetMethodID(cls, "updateProbeResult", "(ID)I");
    if (getRxId == nullptr || getTxId == nullptr) {
        LOGE("Not able to get Rx/Tx Packets through JNI!");
    }

    //socket

    if ((s = socket(AF_INET, SOCK_DGRAM, 0)) < 0)
    {
        LOGD("Socket creation error"); // printf("Socket creation error \n");
        pthread_exit(NULL); // return -1;
        return;
    }
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(4000);

    const char *serverIP = env->GetStringUTFChars(server_ip, 0);
    LOGD("Server IP: %s", serverIP);

    if(inet_pton(AF_INET, serverIP, &serverAddr.sin_addr) <= 0)
    {
        LOGD("Invalid address/ Address not supported"); // printf("\nInvalid address/ Address not supported \n");
        pthread_exit(NULL); // return -1;
        return;
    }


    BYTE * data = (BYTE *)malloc(BUFFER_SIZE);
    memset(data, '\0', BUFFER_SIZE);

    sendto(s, "Test1\n", strlen("Test1\n"), 0, (struct sockaddr *) &serverAddr, serverAddrLen);
    recvfrom(s, data, BUFFER_SIZE, 0, (struct sockaddr *) &serverAddr, &serverAddrLen);
    recvfrom(s, data, BUFFER_SIZE, 0, (struct sockaddr *) &serverAddr, &serverAddrLen);
    sendto(s, "Test2_ACK\n", strlen("Test2_ACK\n"), 0, (struct sockaddr *) &serverAddr, serverAddrLen);

    network_info n1, n2;
    networkStat(n1);
    // printf("init: %d %d \n", n1.recv_bytes, n2.recv_bytes);
    long TxBefore = env->CallLongMethod(*thiz, getTxId), TxAfter, RxBefore = env->CallLongMethod(*thiz, getRxId), RxAfter;
    gettimeofday(&start, NULL);

    while(true)
    {
        if (nRecv < BUFFER_SIZE)
        {
            int n = recvfrom(s, data + nRecv, BUFFER_SIZE - nRecv, 0, (struct sockaddr *) &serverAddr, &serverAddrLen);
            char* seq = (char*) data + nRecv;
            char seq_str[11];
            memcpy(seq_str, seq, n+1);
            seq_recv = atoi(seq_str);
            networkStat(n2); // end
            RxAfter = env->CallLongMethod(*thiz, getRxId);
            TxAfter = env->CallLongMethod(*thiz, getTxId);
            gettimeofday(&curr, NULL);
            double elapsed_time = (curr.tv_sec) * 1000 + (curr.tv_usec)/1000;
            LOGI("Seq recved: %d, Seq now: %d\n", seq_recv, seq_now);

            if (n < 0)
            {
                LOGD("Receive error"); // printf("Connection closed.");
                return;
            }
            else if (n == 0)
            {
                LOGD("Socket closed"); // printf("Connection closed.");
                return;
            }
            else
            {
                jint netType = env->CallIntMethod(*thiz, getNetType);
                char echo[17];
                memset(echo, ' ', 17);
                sprintf(echo, "%d", seq_recv);
                echo[n] = ' ';
                sprintf(echo+n+1, "%d", netType);
                echo[n+2] = ' ';
                echo[16] = '\0';

                if (seq_recv < seq_now) {
                    // something bad happened here
                    LOGI("Our of order pkt!");
                }

                if (((n2.recv_pkt == n1.recv_pkt + 1 && n2.send_pkt == n1.send_pkt) ||
                    (RxAfter == RxBefore + 1 && TxAfter == TxBefore)) && seq_recv > seq_now) // only receive one from the server, send nothing
                {
                    sprintf(echo+n+3, "YEP\n");
                    if (sendto(s, echo, strlen(echo), 0, (struct sockaddr *) &serverAddr, serverAddrLen) == -1) {
                        LOGD("Send error"); // perror("send");
                        int status = env->CallIntMethod(*thiz, updateCliUI, 2, elapsed_time);
                        return;
                    }
                    LOGI("#%d:\tgood: recv %ld %ld, send %ld %ld \n", nPing++, RxBefore, RxAfter, TxBefore, TxAfter);
//                    LOGI("#%d:\tgood: recv %d %d %d %d, send %d %d %d %d \n", nPing++, n1.recv_bytes, n2.recv_bytes, n1.recv_pkt, n2.recv_pkt, n1.send_bytes, n2.send_bytes, n1.send_pkt, n2.send_pkt);
                    int status = env->CallIntMethod(*thiz, updateCliUI, 1, elapsed_time);
                }
                else
                {
                    sprintf(echo+n+3, "BAD\n");
                    if (sendto(s, echo, strlen(echo), 0, (struct sockaddr *) &serverAddr, serverAddrLen) == -1) {
                        LOGD("Send error"); // perror("send");
                        int status = env->CallIntMethod(*thiz, updateCliUI, 2, elapsed_time);
                        return;
                    }

                    LOGI("#%d:\tbad: recv %ld %ld, send %ld %ld \n", nPing++, RxBefore, RxAfter, TxBefore, TxAfter);
//                    LOGI("#%d:\tbad: recv %d %d %d %d, send %d %d %d %d \n", nPing++, n1.recv_bytes, n2.recv_bytes, n1.recv_pkt, n2.recv_pkt, n1.send_bytes, n2.send_bytes, n1.send_pkt, n2.send_pkt);
                    int status = env->CallIntMethod(*thiz, updateCliUI, 0, elapsed_time);
                }
                networkStat(n1); // begin
                RxBefore = env->CallLongMethod(*thiz, getRxId);
                TxBefore = env->CallLongMethod(*thiz, getTxId);
            }
            if (seq_recv > seq_now) {
                seq_now = seq_recv;
            }
            gettimeofday(&start, NULL);
        }
        else
        {
            printf("nRecv: %d\n", nRecv);
            LOGD("Buffer is full.");
            break;
        }
    }

    close(s);
    LOGD("Socket closed (end of code)");
    pthread_exit(NULL); // return 0;
}

extern "C" {
JNIEXPORT jstring JNICALL Java_edu_umn_fivegtracker_UI_FragmentAppRRCProbe_clientFromJNI (JNIEnv *env, jobject thiz, jboolean flag_client, jstring server_ip);
}

extern "C" JNIEXPORT jstring JNICALL Java_edu_umn_fivegtracker_UI_FragmentAppRRCProbe_clientFromJNI (JNIEnv *env, jobject thiz,  jboolean flag_client, jstring server_ip) {
    if(!flag_client) {
        LOGI("Start client()");
        client(env, &thiz, server_ip);
        return env->NewStringUTF("Client running.");
    }
    else {
        shutdown(s, SHUT_RDWR);
        close(s);
        LOGI("Closed");
        return env->NewStringUTF("Client closed.");
    }
}


