# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.7

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/vagrant/data-aquisition/raspberrypi

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/vagrant/data-aquisition/raspberrypi/build

# Include any dependencies generated for this target.
include CMakeFiles/DAS.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/DAS.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/DAS.dir/flags.make

CMakeFiles/DAS.dir/src/main.cpp.o: CMakeFiles/DAS.dir/flags.make
CMakeFiles/DAS.dir/src/main.cpp.o: ../src/main.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/vagrant/data-aquisition/raspberrypi/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object CMakeFiles/DAS.dir/src/main.cpp.o"
	/home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin/arm-linux-gnueabihf-g++   $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/DAS.dir/src/main.cpp.o -c /home/vagrant/data-aquisition/raspberrypi/src/main.cpp

CMakeFiles/DAS.dir/src/main.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/DAS.dir/src/main.cpp.i"
	/home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin/arm-linux-gnueabihf-g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/vagrant/data-aquisition/raspberrypi/src/main.cpp > CMakeFiles/DAS.dir/src/main.cpp.i

CMakeFiles/DAS.dir/src/main.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/DAS.dir/src/main.cpp.s"
	/home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin/arm-linux-gnueabihf-g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/vagrant/data-aquisition/raspberrypi/src/main.cpp -o CMakeFiles/DAS.dir/src/main.cpp.s

CMakeFiles/DAS.dir/src/main.cpp.o.requires:

.PHONY : CMakeFiles/DAS.dir/src/main.cpp.o.requires

CMakeFiles/DAS.dir/src/main.cpp.o.provides: CMakeFiles/DAS.dir/src/main.cpp.o.requires
	$(MAKE) -f CMakeFiles/DAS.dir/build.make CMakeFiles/DAS.dir/src/main.cpp.o.provides.build
.PHONY : CMakeFiles/DAS.dir/src/main.cpp.o.provides

CMakeFiles/DAS.dir/src/main.cpp.o.provides.build: CMakeFiles/DAS.dir/src/main.cpp.o


# Object files for target DAS
DAS_OBJECTS = \
"CMakeFiles/DAS.dir/src/main.cpp.o"

# External object files for target DAS
DAS_EXTERNAL_OBJECTS =

DAS: CMakeFiles/DAS.dir/src/main.cpp.o
DAS: CMakeFiles/DAS.dir/build.make
DAS: CMakeFiles/DAS.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/vagrant/data-aquisition/raspberrypi/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX executable DAS"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/DAS.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/DAS.dir/build: DAS

.PHONY : CMakeFiles/DAS.dir/build

CMakeFiles/DAS.dir/requires: CMakeFiles/DAS.dir/src/main.cpp.o.requires

.PHONY : CMakeFiles/DAS.dir/requires

CMakeFiles/DAS.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/DAS.dir/cmake_clean.cmake
.PHONY : CMakeFiles/DAS.dir/clean

CMakeFiles/DAS.dir/depend:
	cd /home/vagrant/data-aquisition/raspberrypi/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/vagrant/data-aquisition/raspberrypi /home/vagrant/data-aquisition/raspberrypi /home/vagrant/data-aquisition/raspberrypi/build /home/vagrant/data-aquisition/raspberrypi/build /home/vagrant/data-aquisition/raspberrypi/build/CMakeFiles/DAS.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/DAS.dir/depend

