# create variable for common sources
set(sources timestamp.cpp packet.cpp)

# add executables
add_library(udp_custom_client SHARED ${sources} client.cpp)

# link libraries
target_link_libraries(udp_custom_client android log)