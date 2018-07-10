### Development VM

Debian VM for developing data aquisiton system software for Raspberry Pi.
Requires **vagrant** (https://www.vagrantup.com/) and an internet connection. 
Start machine with following command:
	
```
	vagrant up
```

On first boot, this process may take 5-10 minutes and should install all required tools.
Once the VM finishes booting, login with the command (enter 'yes' if asked about host key authenticity):

```
	vagrant ssh
	
username: 'vagrant'
password: 'vagrant'
```

Once logged in, the inital setup should have already downloaded the Data Acquisition and Raspberry Pi Tools git repositories.
Check that the folders 'data-acquistion' and 'raspberrypi' are in the HOME directory (/home/vagrant/).

To compile for the raspberry pi:

```
cd data-acquistion
chmod +x build.sh   /* Only required first time */
./build.sh
```

If the build is successful, this will produce the 'DAS' executable within the build directory. 
Copy this executable over to the raspberry pi. 
Recommended method is SCP:

```
scp ./build/DAS pi@<rpi_IP>:/home/pi/    /* Replace rpi_IP with IP address of raspberry pi */
```

Binary should now be availble in the HOME directory of the raspberry pi.