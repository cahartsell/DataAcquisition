////////////////////////////////////////////////////////////////////////////////////////////////////
// Program to read analog value and print the value as fast as possible
// CHARLES HARTSELL
//
// AnalogRead
// LAST EDIT:    4-29-2018
////////////////////////////////////////////////////////////////////////////////////////////////////

#include <Arduino.h>

static int analogIn = 0; // Analog Pin 0 (A0)

void setup(){
  
  Serial.begin(115200);
  
  delay(50);
  
  Serial.println("Begin AnalogRead Test:");
}



void loop(){

  int analogVal;

  analogVal = analogRead(analogIn);
  Serial.println(analogVal);
}
