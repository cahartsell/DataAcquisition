# Install some prereqs
apt-get update
apt-get -y install git cmake lib32z1 g++ lib32stdc++6

# Set password for user "vagrant" to "vagrant"
echo -e "vagrant\nvagrant\n" | passwd vagrant

# Make folders and clone data aquisition and raspberrypi tools repos
if [ ! -d "/home/vagrant/data-acquisition/" ]; then
	mkdir /home/vagrant/data-acquisition/
	git clone https://github.com/cahartsell/DataAcquisition.git /home/vagrant/data-acquisition
	chown -R vagrant:vagrant /home/vagrant/data-acquisition/
	cd /home/vagrant/data-acquisition/
	git submodule init
	git submodule update
fi

if [ ! -d "/home/vagrant/raspberrypi/" ]; then
	mkdir /home/vagrant/raspberrypi/
	git clone git://github.com/raspberrypi/tools.git /home/vagrant/raspberrypi/tools
	chown -R vagrant:vagrant /home/vagrant/raspberrypi/
fi

# Add cross compiler to PATH
if [[ ":$PATH:" == *":/home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin:"* ]]; then
  echo "Your path is correctly set"
else
  echo "PATH=$PATH:/home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin" >> /home/vagrant/.bashrc
fi

# CMAKE config for cross compilation
echo "SET(CMAKE_SYSTEM_NAME Linux)
SET(CMAKE_SYSTEM_VERSION 1)
SET(CMAKE_C_COMPILER /home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin/arm-linux-gnueabihf-gcc)
SET(CMAKE_CXX_COMPILER /home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin/arm-linux-gnueabihf-g++)
SET(CMAKE_FIND_ROOT_PATH /home/vagrant/raspberrypi/rootfs)
SET(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
SET(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
SET(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)" > /home/vagrant/raspberrypi/pi.cmake

# Build wiringPi shared library for cross-compilation to raspberrypi
if [ -d "/home/vagrant/data-acquisition/deps/wiringPi/wiringPi/" ]; then
  cd /home/vagrant/data-acquisition/deps/wiringPi/wiringPi/
  # delete any old build files
  rm libwiringPi*
  rm *.o
  # build with cross compiler and make symbolic link (w/o version number)
  make CC=/home/vagrant/raspberrypi/tools/arm-bcm2708/gcc-linaro-arm-linux-gnueabihf-raspbian/bin/arm-linux-gnueabihf-gcc
  ln -s libwiringPi.so.* libwiringPi.so
fi
