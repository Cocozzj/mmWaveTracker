#include <fstream>
#include <cstring>
#include <sys/socket.h>
#include <netinet/in.h>
#include <cstdarg>
#include <pthread.h>
#include <csignal>
#include <unistd.h>
#include <cmath>
#include <thread>

#include "packet.h"
#include "config.h"

#include <jni.h>
#include <android/log.h>

#define LOG_TAG    "Net-Mashup (Native)"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

int client_fd;
std::ofstream log_file_handler;

struct sockaddr_in peer_addr;

bool SENDER_RUNNING;
uint64_t milliseconds_to_sleep, pkts_to_send, duration;

bool DEBUG = false;

/* keep receiving packets and send acks (used on receiving side) */
void recv_packets_and_send_ack(int fd);

/* use this function to send packets over a socket. */
void *send_udp_packets(void* fd_ptr);

/* use this function to receive packets over a socket.
   the socket must already be connected to an address.*/
void *recv_udp_packets(void* fd_ptr);

void signalHandler(int signum) {
    shutdown(client_fd, SHUT_RDWR);
    log_file_handler.close();
    exit(signum);
}

int run_client(const char* server_ip, const char* server_port, const char* log_file_name,
               double sending_rate_mbps, int time_to_run, bool downlink=true) {

    SENDER_RUNNING = true;

    // set up runtime parameters
    duration = int(time_to_run * 1000);  // in ms
    double pkts_per_ms = sending_rate_mbps * SENDING_RATE_CONST;

    if ((1.0 / SENDING_RATE_CONST) > sending_rate_mbps) {  // if sending rate < 1 pkt/ms
        milliseconds_to_sleep = std::max(1, int(std::round(1.0 / pkts_per_ms)));
        pkts_to_send = 1;
    }
    else {  // if sending rate >= 1 pkt/ms
        milliseconds_to_sleep = 1;
        pkts_to_send = std::max(1, int(std::round(pkts_per_ms)));
    }
    int port = std::atoi(server_port);
    if (downlink)
        LOGI("Server -> Client");
    else {
        LOGI("Client -> Server");
        LOGI("sleep time %llu; pkts to send %llu", milliseconds_to_sleep, pkts_to_send);
    }


    // initialize signal handler and open log file
    signal(SIGINT, signalHandler);
    log_file_handler.open(log_file_name);


    // initialize server address
    memset(&peer_addr, 0, sizeof(struct sockaddr_in));
    peer_addr.sin_family = AF_INET;
    peer_addr.sin_port = htons(port);
    if (inet_pton(AF_INET, server_ip, &peer_addr.sin_addr) <= 0) {
        LOGE("Invalid address or address not supported");
        pthread_exit(nullptr);
    }

    // initialize UDP socket
    client_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (client_fd < 0) {
        LOGE("Socket creation error"); // replace with android logging
        pthread_exit(nullptr);
    }
    set_timestamps(client_fd);

    // send first packet to server and wait for 2 packets to establish communication
    LOGI("Sending message to the server");
    char* recv_data = (char*)malloc(RECV_BUFFER_LEN);
    sendto(client_fd, "Test1\n", strlen("Test1\n"), 0, (struct sockaddr *) &peer_addr, sizeof(peer_addr));
    socklen_t server_addr_len = sizeof(peer_addr);
    recvfrom(client_fd, recv_data, RECV_BUFFER_LEN, 0, (struct sockaddr *) &peer_addr, &server_addr_len);
    recvfrom(client_fd, recv_data, RECV_BUFFER_LEN, 0, (struct sockaddr *) &peer_addr, &server_addr_len);
    sendto(client_fd, "Test2_ACK\n", strlen("Test2_ACK\n"), 0, (struct sockaddr *) &peer_addr, sizeof(peer_addr));
    LOGI("Communication established with server...");

    // connect socket to the server address
    connect_socket_to_address(client_fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr));
    if (downlink) {
        // start receiving packets and send acks
        recv_packets_and_send_ack(client_fd);
    }
    else {
        // create two threads: one for sending packets and one for receiving
        pthread_t send_thread, recv_thread;
        pthread_create(&send_thread, nullptr, send_udp_packets, (void*) &client_fd);
        pthread_create(&recv_thread, nullptr, recv_udp_packets, (void*) &client_fd);

        pthread_join(send_thread, nullptr);
        LOGI("Sender thread returned");
        pthread_join(recv_thread, nullptr);
        LOGI("Recv thread returned");
    }

    shutdown(client_fd, SHUT_RDWR);
    log_file_handler.close();

    return 1;
}

/* keep receiving packets and send acks (used on receiving side) */
void recv_packets_and_send_ack(int fd) {
    int client_seq_no = 1;

    while (true) {
        received_datagram message = recv_packet(fd);
        Packet packet = message.payload;
        if (DEBUG) {
            LOGD("Custom message received ==> %d, %llu, %llu, %llu, %llu, %llu, %llu, %llu",
                packet.is_ack(),
                packet.header.sequence_number,
                packet.header.send_timestamp,
                packet.header.ack_sequence_number,
                packet.header.ack_send_timestamp,
                packet.header.ack_recv_timestamp,
                message.timestamp,
                packet.header.ack_payload_length);
        }
        std::string packet_info = string_format("%d, %d, %d, %d, %d, %d, %d, %d, %d\n",
                                                packet.is_ack(),
                                                packet.header.sequence_number,
                                                packet.header.send_timestamp,
                                                packet.header.ack_sequence_number,
                                                packet.header.ack_send_timestamp,
                                                packet.header.ack_recv_timestamp,
                                                message.timestamp,
                                                packet.header.ack_payload_length,
                                                get_current_timestamp());
        log_file_handler << packet_info;
        if (packet.header.sequence_number <= 0) {
            break;
        }
        packet.transform_into_ack(client_seq_no++, message.timestamp);
        packet.set_send_timestamp();
        send_packet(fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr), packet.to_string());
    }
}

/* use this function to send packets over a socket. */
void *send_udp_packets(void* fd_ptr)
{
    int server_seq_no = 1;
    int socket_fd = *((int*) fd_ptr);

    int count = 0;
    uint64_t start_time_ms = timestamp_ms();
    while ((timestamp_ms() - start_time_ms) <= duration and SENDER_RUNNING) {
        while (count++ < pkts_to_send) {
            // send packets
            std::string message = create_packet(server_seq_no++);
            send_packet(socket_fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr), message);
            if (DEBUG)
                LOGD("Custom message sent");
        }
        std::this_thread::sleep_for(std::chrono::milliseconds(milliseconds_to_sleep));
        count = 0;
    }
    std::string message = create_packet(0);
    // send a few times just in case
    send_packet(socket_fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr), message);
    send_packet(socket_fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr), message);
    send_packet(socket_fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr), message);
    send_packet(socket_fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr), message);
    send_packet(socket_fd, (struct sockaddr *) &peer_addr, sizeof(peer_addr), message);
    if (DEBUG)
        LOGI("Last Custom message sent");
    SENDER_RUNNING = false;
    return nullptr;
}

/* use this function to receive packets over a socket.
   the socket must already be connected to an address.*/
void *recv_udp_packets(void* fd_ptr)
{
    int socket_fd = *((int*) fd_ptr);
    set_socket_timeout(socket_fd, SERVER_RECV_MSG_TIMEOUT);

    // recv acks and log them
    while (SENDER_RUNNING) {
        received_datagram recv_message = recv_packet(socket_fd); // this will exit the thread if timeout
        Packet packet = recv_message.payload;
        if (DEBUG) {
            LOGD("Custom message received ==> %d, %llu, %llu, %llu, %llu, %llu, %llu, %llu",
                packet.is_ack(),
                packet.header.sequence_number,
                packet.header.send_timestamp,
                packet.header.ack_sequence_number,
                packet.header.ack_send_timestamp,
                packet.header.ack_recv_timestamp,
                recv_message.timestamp,
                packet.header.ack_payload_length);
        }
        std::string packet_info = string_format("%d, %d, %d, %d, %d, %d, %d, %d, %d\n",
                                                packet.is_ack(),
                                                packet.header.sequence_number,
                                                packet.header.send_timestamp,
                                                packet.header.ack_sequence_number,
                                                packet.header.ack_send_timestamp,
                                                packet.header.ack_recv_timestamp,
                                                recv_message.timestamp,
                                                packet.header.ack_payload_length,
                                                get_current_timestamp());
        log_file_handler << packet_info;
    }
    return nullptr;
}

void runClientJNI(JNIEnv *env, jobject *thiz,
                  jstring server_address, jstring server_port, jstring logfile,
                  jdouble sending_rate_mbps, jint time_to_run, jboolean downlink) {
    const char *serverAddress = env->GetStringUTFChars(server_address, nullptr);
    const char *serverPort = env->GetStringUTFChars(server_port, nullptr);
    const char *logFile = env->GetStringUTFChars(logfile, nullptr);
    LOGI("Calling run_client()");
    run_client(serverAddress, serverPort, logFile, sending_rate_mbps, time_to_run, downlink);
    LOGI("run_client() exited");

}

extern "C" JNIEXPORT jstring JNICALL Java_edu_umn_fivegtracker_UI_UdpClientAppFragment_clientFromJNI(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jboolean flag_client,
                                                                             jstring server_ip,
                                                                             jstring server_port,
                                                                             jstring logfile,
                                                                             jdouble sending_rate_mbps,
                                                                             jint time_to_run,
                                                                             jboolean downlink) {
    if(!flag_client) {
        LOGI("Start client()");
        runClientJNI(env, &thiz, server_ip, server_port, logfile, sending_rate_mbps, time_to_run, downlink);
        return env->NewStringUTF("Client running.");
    }
    else {
        shutdown(client_fd, SHUT_RDWR);
        close(client_fd);
        LOGI("Closed");
        return env->NewStringUTF("Client closed.");
    }
}
