Component tests are still in .ino format for simplicity.
PlatformIO is picky about configuring multiple, seperate programs as build targets.

Just compile and run these tests with the standard Arduino IDE.
Note that some tests import libraries.
With the standard Arduino IDE, these libraries must either be in the same directory, or they must be placed into the Arduino sketchbook location.
The default location for Arduino sketchbook on Windows is "C:\<User_documents_folder>\Arduino\libraries".