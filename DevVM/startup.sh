# Install some prereqs
apt-get update
apt-get -y install git cmake lib32z1 g++ lib32stdc++6

# Set password for user "vagrant" to "vagrant"
echo -e "vagrant\nvagrant\n" | passwd vagrant

# Make folders and clone data aquisition and raspberrypi tools repos
if [ ! -d "/home/vagrant/data-aquisition/" ]; then
	mkdir /home/vagrant/data-aquisition/
	git clone https://github.com/cahartsell/DataAquisition.git /home/vagrant/data-aquisition
	chown -R vagrant:vagrant /home/vagrant/data-aquisition/
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