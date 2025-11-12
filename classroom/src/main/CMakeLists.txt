cmake_minimum_required(VERSION 3.18.1)


project("rtmptester")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")#设置RTMP不适用OpenSSL

include_directories(
        cpp/librtmp
)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        cpp/native-lib.cpp
        cpp/librtmp/amf.c
        cpp/librtmp/hashswf.c
        cpp/librtmp/log.c
        cpp/librtmp/parseurl.c
        cpp/librtmp/rtmp.c
        )

find_library(
        log-lib
        log)

target_link_libraries(
        native-lib

        ${log-lib})