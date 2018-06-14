////////////////////////////////////////////////////////////////////////////////////////////////////
// Simple test of seven segment display library
// CHARLES HARTSELL
//
// 7SegDisplay.ino
// LAST EDIT:    5-2-2018
////////////////////////////////////////////////////////////////////////////////////////////////////

#include <Arduino.h>
#include "SevenSegDisplay.h"

SevenSegDisplay disp = SevenSegDisplay(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

void setup(){
  disp.setupPins();
  delay(50);
}


void loop(){
  
  // Display "1234" for a little while
  for(int i = 0; i < 15; i++){
    disp.setDisplayValue(1);
    disp.turnOnDigit(0);
    delay(10);
    disp.turnOffDigit(0);

    disp.setDisplayValue(2);
    disp.turnOnDigit(1);
    delay(10);
    disp.turnOffDigit(1);

    disp.setDisplayValue(3);
    disp.turnOnDigit(2);
    delay(10);
    disp.turnOffDigit(2);
    
    disp.setDisplayValue(4);
    disp.turnOnDigit(3);
    delay(10);
    disp.turnOffDigit(3);
  }

  // Wait a bit. Display should clear
  delay(500);

  // Display "8.8.8.8", then "8888"
  disp.setDisplayValue(8);
  disp.setDecimalPoint();
  disp.turnOnDigit(-1);
  delay(500);
  disp.clearDecimalPoint();
  delay(500);

  // Clear display. Wait a bit to make sure it worked
  disp.clearDisplayValue();
  delay(500);
  disp.turnOffDigit(-1);
  delay(500);
}
