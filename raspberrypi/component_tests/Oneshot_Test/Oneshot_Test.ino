////////////////////////////////////////////////////////////////////////////////////////////////////
// UNIVERSITY OF ALABAMA SAE MINI BAJA TEAM
// BAMA RACING
// SN74221 MONOSTABLE-MULTIVIBRATOR (ONESHOT) TEST CODE
// CHARLES HARTSELL
//
// Oneshot_Test
// LAST EDIT:    SEPT 30 2015
////////////////////////////////////////////////////////////////////////////////////////////////////

#include <Arduino.h>

static int outputPin = 18;
static int inputPin = 19;
static int inputInt = 4;

volatile int count;
int outputVal;
int inputVal;

unsigned long lastTime;
unsigned long pulseTime = 0;


// Interrupt function
void inputIntFunc(){
  count += 1;
}


void setup(){
  
  Serial.begin(115200);
  Serial.flush();
  
  pinMode( outputPin , OUTPUT);
  digitalWrite(outputPin, HIGH);
  outputVal = 0;
  pinMode(inputPin, INPUT_PULLUP);
  inputVal = 1;
  
  attachInterrupt(inputInt, inputIntFunc, FALLING);
  count = 0;
  lastTime = millis();
  
  delay(50);
  
  Serial.println("Begin OneShot Test:");
}



void loop(){

  if((micros()-lastTime) > 2000000){
    Serial.println("////");
    digitalWrite(outputPin, LOW);
    lastTime = micros();
    delay(1);
    digitalWrite(outputPin, HIGH);
    inputVal = digitalRead(inputPin);
    while(inputVal == 0){
      inputVal = digitalRead(inputPin);
    }
    pulseTime = micros()-lastTime;
    Serial.println(pulseTime);
  }
  
  
  
// BASIC I/O PIN FUNCTION TEST
/*  
  if((millis()-lastTime) >= 1000){
    switch(outputVal){
      case 1:
        digitalWrite(outputPin, LOW);
        outputVal = 0;
        break;
      case 0:
        digitalWrite(outputPin, HIGH);
        outputVal = 1;
        break;
    }
    lastTime = millis();
  }
  
  Serial.println(digitalRead(inputPin));
  
  delay(10);
*/
}
