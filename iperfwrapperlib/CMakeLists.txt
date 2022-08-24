##
cmake_minimum_required(VERSION 3.4.1)


include_directories(iperf/src)

add_library( jni SHARED
               src/main/cpp/hello.c
               src/main/cpp/test_run.c
               )

add_library( iperf SHARED
             iperf/src/cjson.c
             iperf/src/dscp.c
             iperf/src/iperf_api.c
             iperf/src/iperf_auth.c
             iperf/src/iperf_client_api.c
             iperf/src/iperf_error.c
             iperf/src/iperf_locale.c
             iperf/src/iperf_sctp.c
             iperf/src/iperf_tcp.c
             iperf/src/iperf_time.c
             iperf/src/iperf_udp.c
             iperf/src/iperf_util.c
             iperf/src/iperf_server_api.c
             iperf/src/main.c
             iperf/src/net.c
             iperf/src/tcp_info.c
             iperf/src/timer.c
             iperf/src/units.c
             )

target_link_libraries(iperf log)

target_link_libraries(jni iperf log)