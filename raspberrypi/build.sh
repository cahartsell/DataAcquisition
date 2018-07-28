rm -rf build/
mkdir build
cd build
cmake -D CMAKE_TOOLCHAIN_FILE=/home/vagrant/raspberrypi/pi.cmake ../
#cmake -DCMAKE_C_COMPILER=/usr/bin/arm-linux-gnueabihf-gcc -DCMAKE_CXX_COMPILER=/usr/bin/arm-linux-gnueabihf-g++ ../
make -j4
