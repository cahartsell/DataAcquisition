rm -rf build/
mkdir build
cd build
cmake -D CMAKE_TOOLCHAIN_FILE=/home/vagrant/raspberrypi/pi.cmake ../
make -j4
