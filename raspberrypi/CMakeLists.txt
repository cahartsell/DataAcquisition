cmake_minimum_required (VERSION 2.6)
project(DAS)

# Set version number
set (DAS_VERSION_MAJOR 0)
set (DAS_VERSION_MINOR 1)

include_directories("libs/")

add_executable(DAS src/main.cpp)
