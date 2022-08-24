# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# set flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread")

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

## UDP RECEIVER
add_subdirectory(src/main/udprecvlib)

## RRC PROBE
include_directories(src/main/rrclibcpp)
add_library(client_udp_down SHARED src/main/rrclibcpp/client_udp_down.cpp)
target_link_libraries(client_udp_down android log)