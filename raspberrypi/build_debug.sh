rm -rf build_d/
mkdir build_d
cd build_d
cmake -DCMAKE_BUILD_TYPE=Debug -DCMAKE_TOOLCHAIN_FILE=/home/vagrant/raspberrypi/pi.cmake  ../
make -j4
