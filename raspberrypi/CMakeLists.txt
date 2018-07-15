cmake_minimum_required (VERSION 2.6)
project(DAS)

# Set version number
set (DAS_VERSION_MAJOR 0)
set (DAS_VERSION_MINOR 1)

include_directories("lib/" "lib/wiringPi/wiringPi")

add_executable(DAS src/main.cpp)

# Component test executables
add_executable(GPIO_test component_tests/raspberrypi_GPIO/gpio_test.c)