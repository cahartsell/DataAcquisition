////////////////////////////////////////////////////////////////////////////////////////////////////
// UNIVERSITY OF ALABAMA SAE MINI BAJA TEAM
// BAMA RACING
// TACHOMETER CIRCUIT TEST CODE
// CHARLES HARTSELL
//
// RPM_Test
// LAST EDIT:    JUN 24 2016
////////////////////////////////////////////////////////////////////////////////////////////////////

#include <Arduino.h>
#include <SEVEN_SEGMENT_DRIVER.h>
#include <math.h>

static int outputPin = 8;
static int inputPin = 19;
static int inputInt = 4;

volatile int flag;
volatile long count;
int outputVal;
int inputVal;
int tempERPM;
int highERPM;
int ERPM;
int i;

long lastTime;
long pulseTime = 0;
long lastTimeDisp;
long lastTimeOut;


// Interrupt function
void inputIntFunc(){
  count += 1;
  flag = 1;
}


void setup(){
  
  Serial.begin(115200);
  Serial.flush();
  
  pinMode( outputPin , OUTPUT);
  digitalWrite(outputPin, LOW);
  outputVal = 0;
  pinMode(inputPin, INPUT_PULLUP);
  inputVal = 1;
  
  attachInterrupt(inputInt, inputIntFunc, FALLING);
  flag = 0;
  count = 0;
  highERPM = 0;
  lastTime = millis();
  lastTimeDisp = lastTime;
  lastTimeOut = lastTime;
  i = 4;
  
  displayInit();
  displayHex(0);
  turnOnDigit(4);
  
  Serial.println("Begin RPM Test:");
  
  delay(50);
}



void loop(){
  
/*
  //SIMULATED ENGINE RPM CODE (for code verification)
  if(millis()-lastTimeOut > 16){
    digitalWrite(outputPin, HIGH);
    delayMicroseconds(500);
    digitalWrite(outputPin, LOW);
    lastTimeOut = millis();
  }
*/
  
  //DISPLAY RPM TO 7-SEGMENT DISPLAY CODE
  if( count > 20 ){
    count = count*60000;
    ERPM = count/(millis()-lastTime);
    count = 0;
    lastTime = millis();
    if(ERPM > highERPM){
      highERPM = ERPM;
    }
    Serial.println(ERPM);
  }
  if(ERPM > 9999){
    displayHex(16);
    turnOnDigit(4);
  }
  else if(millis()-lastTimeDisp > 5){
    turnOffDigit(3-i);
    i--;
    if(i<0){i=3;}
    tempERPM = ERPM/(pow(10, i));
    displayHex(tempERPM%10);
    turnOnDigit(3-i);
    lastTimeDisp = millis();
  }
  
  
/*
  // BLINK LED CODE
  if( flag == 1){
    flag = 0;
    digitalWrite(outputPin, HIGH);
    delay(10);
    digitalWrite(outputPin, LOW);
  }
*/
}

