////////////////////////////////////////////////////////////////////////////////////////////////////
// Program to wipe EEPROM memory
// DANGER: WILL LOST ALL LONG TERM DATA STORED ON ARDUINO ITSELF
// 
// LAST EDIT:    APRIL 28 2018
////////////////////////////////////////////////////////////////////////////////////////////////////

#include <EEPROM.h>

void setup(){
  for (int i = 0; i < EEPROM.length(); i++){
    EEPROM.write(i,0);
  }
}

void loop(){
 // Do a whole lot of nothing
 while(1){
  delay(1000);
 }
}
