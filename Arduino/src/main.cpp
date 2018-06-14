////////////////////////////////////////////////////////////////////////////////////////////////////
// UNIVERSITY OF ALABAMA SAE MINI BAJA TEAM
// BAMA RACING
// IN-VEHICLE MICROCONTROLLER SOURCE CODE
// CHARLES HARTSELL, JOHN MITCHELL, MICHAEL FLUEGEMANN
//
// Baja_v0.1.0
// LAST EDIT:    APRIL 29 2018
////////////////////////////////////////////////////////////////////////////////////////////////////


#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_MCP23017.h>
#include <Adafruit_RGBLCDShield.h>
#include <SFE_MMA8452Q.h>

// Create instances of LCD Screen and Accelerometer
Adafruit_RGBLCDShield lcd = Adafruit_RGBLCDShield();
MMA8452Q accelSen;

// Backlight color codes for LCD
#define OFF 0x0
#define RED 0x1
#define YELLOW 0x3
#define GREEN 0x2
#define TEAL 0x6
#define BLUE 0x4
#define VIOLET 0x5
#define WHITE 0x7

// Accelerometer Definitions
#define G_SCALE SCALE_2G              // accel-range (2,4, or 8 g's)
#define DATARATE ODR_50              // 0=800Hz, 1=400, 2=200, 3=100, 4=50, 5=12.5, 6=6.25, 7=1.56
//#define MMA8452_ADDRESS 0x1D          // Default i2c address. Changes if SA0 jumper on bottom of board is set

// EEPROM Map
#define MILEAGE_IDX 0
#define MILEAGE_SIZE sizeof(long)
#define MINUTES_IDX (MILEAGE_IDX + MILAGE_SIZE)
#define MINUTES_SIZE sizeof(long)

// Fixed point math scale factors
#define MILEAGE_SCALE_FACTOR 1000
#define MINUTES_SCALE_FACTOR 1000

// Maximum limits
#define MAX_SPEED 40
#define MAX_RPM 4000

// Setup color array and default color selection
uint8_t colorArray[8] = {OFF, RED, YELLOW, GREEN, TEAL, BLUE, VIOLET, WHITE};
int backlightSelection = 1;

/*
   RPM Sensors Pin Declarations
   Both pins require interrupts. To attach an interrupt, a specific
   Int.N value is required, and this varies from chip to chip, based
   on which pins can handle interrupt attachments.
*/
static int engineRPM_pin = 18;
static int engineRPM_int = 5;
static int axleRPM_pin = 19;
static int axleRPM_int = 4;

// Accelerometer Variables
//int accelInt1Pin = 5;
int accelInitSuccess = 0;

// RPM Sensors Interrupt Function variables
// Defined as long integers to avoid overflow during speed and engine RPM calculations
volatile long wheelRevCount = 0;
volatile long engineRevCount = 0;

// Custom Character Arrays
byte lineOne[8] = {0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0};
byte lineTwo[8] = {0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8};
byte lineThree[8] = {0xFC, 0xFC, 0xFC, 0xFC, 0xFC, 0xFC, 0xFC, 0xFC};
byte lineFour[8] = {0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE};


// Variable Declarations
unsigned long scaledWheelRPM = 0;
unsigned long scaledSpeed = 0;
static int WheelDia = 2;  // in feet
int unsigned roundSpeed = 0;
int unsigned lastRoundSpeed = 0;
unsigned long roundEngineRPM = 0;

// Long-term variables. Need to be stored in/loaded from EEPROM
long totalMiles = 0;
long totalMinutes = 0;


// Time variables for calculating RPM
// Variables modified by interrupts should be declared "volatile"
unsigned long startTime_axle;
volatile unsigned long lastTime_axle;
unsigned long startTime_engine;
volatile unsigned long lastTime_engine;
unsigned long lastTime_button = 0;

// Function Declarations
void displayOdo(long scaledMiles, long scaledMinutes);
void displayPlainText(int MPH, int ERPM);
void displaySlidingBar(float currentVal, float maxVal, char displayChar);
int logData(unsigned long logTime, unsigned int logSpeed, unsigned int logEngineRPM, unsigned long logAxleRPM);


// Interrupt Function
// Increases wheel revolution count and updates time of last pulse
void axleRpmMeasurement()
{  
  wheelRevCount += 1;
  lastTime_axle = millis();
}
  
// Interrupt Function
// Increases engine revolution count and updates time of la t pulse
void engineRpmMeasurement()
{  
  engineRevCount += 1;
  lastTime_engine = millis();  
}



/////////////////////
// SETUP FUNCTION  //
/////////////////////
void setup(){
  
  // Open serial communications and wait for port to open:
  // Initialize SD OpenLog
  // ######## IMPORTANT NOTE ##############
  // OpenLog does not seem to recognize newline character "\n"
  // Use Serial.println() when a newline is needed instead of Serial.print("\n")
  Serial.begin(115200);
  Serial.flush();
  Serial.println("//Serial Connection Established.");
  
  
  // Initalize accelerometer
  if((accelSen.init(G_SCALE, DATARATE)) == 1) {
    Serial.println("//Accelerometer connection successful.");
    accelInitSuccess = 1;}
  else {
    Serial.println("//Accelerometer connection failed.");
    accelInitSuccess = 0;}


  // Initalize LCD screen
  lcd.begin(16, 2);
  lcd.setBacklight(RED);
  lcd.clear();
  lcd.setCursor(2,0);
  lcd.noAutoscroll();
  lcd.noBlink();
  lcd.print("BAMA RACING");
  lcd.setCursor(3,1);
  lcd.print("ROLL TIDE");
  
  // Set axleRPM_int pin to call axleRpmMeasurement function
  // FALLING pulse triggered
  // Define first startTime_axle
  pinMode(axleRPM_pin, INPUT_PULLUP);
  attachInterrupt(axleRPM_int, axleRpmMeasurement, FALLING);
  startTime_axle = millis();
  
  // Set engineRPM_int pin to call engineRpmMeasurement function
  // FALLING pulse triggered
  // Define first startTime_engine
  pinMode(engineRPM_pin, INPUT_PULLUP);
  attachInterrupt(engineRPM_int, engineRpmMeasurement, FALLING);
  startTime_engine = millis();
  
  // Create custom characters for smooth display bar
  // Each character is one, two, three, or four solid columns
  lcd.createChar(1, lineOne);
  lcd.createChar(2, lineTwo);
  lcd.createChar(3, lineThree);
  lcd.createChar(4, lineFour);
  
  Serial.println("//");
  Serial.println("//Time  Speed   E-RPM   A-RPM");
  
  // Delay to allow all setup to be completed before beginning loop
  delay(50);
}



//////////////////////////
// MAIN LOOP FUNCTION   //
//////////////////////////
void loop(){
  // Display selection variable. 0 = Splash screen, 1 = Slider MPH, 2 = Slider RPM, 3 = Plain Text MPH & RPM, 4 = Odometer & Hourmeter
  int displaySel = 0;
  
  // Display info
  switch(displaySel){
    case 0:
      lcd.setCursor(2,0);
      lcd.print("BAMA RACING");
      lcd.setCursor(3,1);
      lcd.print("ROLL TIDE");
      break;
    case 1:
      displaySlidingBar(scaledSpeed/1000.0, MAX_SPEED, 'S');
      break;
    case 2:
      displaySlidingBar(roundEngineRPM, MAX_RPM, 'T');
      break;
    case 3:
      displayPlainText(roundSpeed, roundEngineRPM);
      break;
    case 4:
      displayOdo(totalMiles, totalMinutes);
      break;
    default:
      break;
  }
  
  // If at least 3 wheel pulses (1/2 revolution), calculate speed and reset variables
  if ( wheelRevCount >= 3){
    // ########################################################################################
    // ### SPEED IS DETERMINED USING FIXED POINT MATH TO AVOID FLOATING POINT CLALCULATIONS ###
    // ### SCALED RPM AND SPEED VARIABLES ARE A FACTOR OF 1000 GREATER THAN THE TRUE VALUE  ###
    // ########################################################################################
    scaledWheelRPM = ((wheelRevCount*10000))*1000/(lastTime_axle-startTime_axle);  //Calculate axle rpm. Constant for conversion from milliseconds to minutes and division by 6 pulses per revolution
    scaledSpeed = scaledWheelRPM*WheelDia*100/2801;                      //Calculate speed. Constant for conversion to MPH
    roundSpeed = scaledSpeed/1000;      //Scale down by factor of 1000 for true speed (result is truncated integer)
    if( (scaledSpeed%1000) >= 500 ) {   
      roundSpeed = roundSpeed + 1;
    } //roundSpeed is now a true rounded value instead of truncated
    wheelRevCount = 0;
    startTime_axle = millis();
  }
  
  // If 0.5 seconds passes with no axle revolution count, restart revolution count and startTime
  // Also set Speed to 0.0 and roundSpeed to 0
  if ( (millis()-lastTime_axle) > 500 ){
    wheelRevCount = 0;
    scaledWheelRPM = 0;
    startTime_axle = millis();
    lastTime_axle = millis();
    scaledSpeed = 0;
    roundSpeed = 0;
  }
  
  // Calculate engine RPM after 3 rev counts
  if( engineRevCount >= 3){
    roundEngineRPM = (engineRevCount*60000)/(lastTime_engine-startTime_engine); //Calculate truncated engine rpm. Constant for conversion from ms to min
    engineRevCount = 0;
    startTime_engine = millis();
    logData(millis(), roundSpeed, roundEngineRPM, scaledWheelRPM);
  }
  
  // Restart engine revolution count after 200 ms with no engine rpm count
  if ( (millis()-lastTime_engine) > 200 ){
    engineRevCount = 0;
    startTime_engine = millis();
    lastTime_engine = millis(); 
    roundEngineRPM = 0;
    logData(millis(), roundSpeed, roundEngineRPM, scaledWheelRPM);
  }
  

////////////////////////////////////////////////////////////////////
// ACCELEROMETER CODE SECTION. NOT CURRENTLY USED
/*  
  // If new data is available from accelerometer, then read G values into accelGValues array.
  // Also logs new data to SD card. Prevents Data from logging too often
  if (accelInitSuccess && accelSen.available()){
    accelSen.read();
    Serial.print(millis());
    Serial.print("\t");
    Serial.print (analogRead(19));
    Serial.print("\t");
    Serial.print(accelSen.cx, 3);
    Serial.print("\t");
    Serial.print(accelSen.cy, 3);
    Serial.print("\t");
    Serial.print(accelSen.cz, 3);
    Serial.print("\n");
  }
 */
////////////////////////////////////////////////////////////////////////////  
  
  // If up botton is pressed, then change backlight color
  // Must wait 0.3 s between presses
  if((millis()-lastTime_button) > 300){
    // Read LCD button presses. Returns 0x00 if no buttons are pressed.
    // Otherwise, sets a bit between 0x01 and 0x10 for each button pressed.
    uint8_t buttons = lcd.readButtons();

    // If at least one button was pressed
    if (buttons & 0xFF) {
      lastTime_button = millis();

      if (buttons & BUTTON_UP){
        backlightSelection++;
        if(backlightSelection > 7) backlightSelection = 1;
        lcd.setBacklight(colorArray[backlightSelection]);
      }

      if (buttons & BUTTON_RIGHT) {
        displaySel++;
        if(displaySel > 4) displaySel = 0;
        lcd.clear();
      }

      if (buttons & BUTTON_LEFT) {
        displaySel--;
        if(displaySel < 0) displaySel = 4;
        lcd.clear();
      }

      if (buttons & BUTTON_DOWN) {
        // Unused
      }

      if (buttons & BUTTON_SELECT) {
        // Unused
      }
    }

  } // end if
}; // end loop()



////////////////////////////////////////////////////////////////////
// Function to log Time, Speed, and Engine RPM data
// Takes 3 arguments: Time of log, Vehicle speed, engine RPM
// Sends data over standard Serial link to be logged onto SD card by OpenLog
/////////////////////////////////////////////////////////////////////
int logData(unsigned long logTime, unsigned int logSpeed, unsigned int logEngineRPM, unsigned long logAxleRPM){
  Serial.print(logTime);
  Serial.print("\t");
  Serial.print(logSpeed);
  Serial.print("\t");
  Serial.print(logEngineRPM);
  Serial.print("\t");
  Serial.println(logAxleRPM);
  
  return 1;
} // end logData()


////////////////////////////////////////////////////////////////////
// Function to display speed in MPH as a sliding bar
// Takes a float speed value
// Range is 0-40 MPH. If argument is >40, 40 MPH is displayed
/////////////////////////////////////////////////////////////////////
void displaySlidingBar(float currentVal, float maxVal, char displayChar){
  lcd.setCursor(0,0);
  lcd.print(displayChar);
  lcd.setCursor(1,0);
  lcd.print("  1   2   3   4");
  lcd.setCursor(0,1);
  float ppc = 100.0/80.0;                     // each column of pixels is 1.25% of max speed
  float pom = (currentVal/maxVal) * 100.0;               // percent of max speed
  int cnt_column = round(pom / ppc) ;         // percent of speed / percent per column = # of columns
  int num_full_block = cnt_column / 5;        // number of solid blocks
  int partial_char = cnt_column % 5;           // partial block character (custom chars 1-4 created in setup function)
  
  // Set sliding bar to full if over maximum
  if (currentVal > maxVal){
    num_full_block = 16;
    partial_char = 0;
  }
  // print desired number of full blocks
  for (int i = 0; i < num_full_block; i++){
    lcd.write(char(255));
  }
  // print remainder partial block
  if (partial_char != 0) lcd.write(partial_char);
  // Clear residual full blocks
  for (int i = num_full_block + 1; i <= 16; i++){
    lcd.print(" ");
  } 
} // end displayMPH()


////////////////////////////////////////////////////////////////////
// Function to display speed and engine RPM as plain text
// Takes an integer MPH value and an integer engine RPM value
/////////////////////////////////////////////////////////////////////
void displayPlainText(int MPH, int ERPM){
  lcd.setCursor(0,0);
  lcd.print("Speed: ");
  lcd.print(MPH);
  lcd.setCursor(0,1);
  lcd.print("RPM: ");
  lcd.print(ERPM);
} // end displayPlainText();


////////////////////////////////////////////////////////////////////
// Function to display miles driven and hours with engine running as plain text
/////////////////////////////////////////////////////////////////////
void displayOdo(long scaledMiles, long scaledMinutes){
  long miles = scaledMiles / MILEAGE_SCALE_FACTOR;
  long scaledHours = scaledMinutes / 60;
  long hours = scaledHours / MINUTES_SCALE_FACTOR;
  int tenthMiles, tenthHours;

  tenthMiles = miles % (10 * MILEAGE_SCALE_FACTOR);
  tenthMiles = tenthMiles / (MILEAGE_SCALE_FACTOR/10);

  tenthHours = hours % (10 * MINUTES_SCALE_FACTOR);
  tenthHours = tenthHours / (MINUTES_SCALE_FACTOR/10);

  lcd.setCursor(0,0);
  lcd.print("Miles: ");
  lcd.print(round(miles));
  lcd.print(".");
  lcd.print(tenthMiles);
  lcd.setCursor(0,1);
  lcd.print("Hours: ");
  lcd.print(hours);
  lcd.print(".");
  lcd.print(tenthHours);
}
  
