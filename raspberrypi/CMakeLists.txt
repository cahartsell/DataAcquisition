cmake_minimum_required (VERSION 3.0)
project(DAS)

# Set version number
set (DAS_VERSION_MAJOR 0)
set (DAS_VERSION_MINOR 1)

# This is wrong. Should be fixed
include_directories("include/" "include/wiringPi")
LINK_DIRECTORIES("lib/")

#SET(CMAKE_C_FLAGS "-I/usr/local/include -L/usr/local/lib")

# Find source files
file(GLOB_RECURSE SOURCES RELATIVE ${CMAKE_SOURCE_DIR} "src/*.c" "src/*.cpp")

#SET(THREADTEST_COMPILE_FLAG "-std=c++11 -pthread")
#SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${THREADTEST_COMPILE_FLAG}")
#SET(CMAKE_EXE_LINKER_FLAGS "-Wl, --no-as-needed ${THREADTEST_COMPILE_FLAG}")

# Main executable
add_executable(DAS ${SOURCES})
# Component test executables
add_executable(GPIO_test component_tests/raspberrypi_GPIO/gpio_test.c)
add_executable(GPIO_speed_test component_tests/raspberrypi_GPIO/speed.c)

# Set libraries to link
TARGET_LINK_LIBRARIES(DAS wiringPi pthread rt)
TARGET_LINK_LIBRARIES(GPIO_test wiringPi)
TARGET_LINK_LIBRARIES(GPIO_speed_test wiringPi)
