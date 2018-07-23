cmake_minimum_required (VERSION 3.0)
project(DAS)

# Set version number
set (DAS_VERSION_MAJOR 0)
set (DAS_VERSION_MINOR 1)

# This is wrong. Should be fixed
include_directories("include/" "deps/wiringPi/wiringPi/")
LINK_DIRECTORIES("deps/wiringPi/wiringPi/")

#SET(CMAKE_C_FLAGS "-I/usr/local/include -L/usr/local/lib")

# Find source files
file(GLOB_RECURSE SOURCES RELATIVE ${CMAKE_SOURCE_DIR} "src/*.c" "src/*.cpp")

# Main executable
add_executable(DAS ${SOURCES})
# Component test executables
add_executable(GPIO_test component_tests/raspberrypi_GPIO/gpio_test.c)
add_executable(GPIO_speed_test component_tests/raspberrypi_GPIO/speed.c)

# Set libraries to link
#TARGET_LINK_LIBRARIES(DAS wiringPi)
TARGET_LINK_LIBRARIES(GPIO_test wiringPi)
TARGET_LINK_LIBRARIES(GPIO_speed_test wiringPi)
