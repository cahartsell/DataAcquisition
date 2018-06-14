#include <Arduino.h>
#include <Wire.h>
#include <math.h>
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


int colorArray[8] = {OFF, RED, YELLOW, GREEN, TEAL, BLUE, VIOLET, WHITE};

/*
   RPM Sensors Pin Declarations
   Both pins require interrupts. To attach an interrupt, a specific
   Int.N value is required, and this varies from chip to chip, based
   on which pins can handle interrupt attachments.
*/
//int prpm_pin = 18;
//int prpm_int = 5;
int axleRPM_pin = 19;
int axleRPM_int = 4;

// Accelerometer Variables
//int accelInt1Pin = 5;

// RPM Sensors Interrupt Function variables
volatile int wheelRevCount = 0;

// Custom Character Arrays
byte lineOne[8] = {0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xF0};
byte lineTwo[8] = {0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8, 0xF8};
byte lineThree[8] = {0xFC, 0xFC, 0xFC, 0xFC, 0xFC, 0xFC, 0xFC, 0xFC};
byte lineFour[8] = {0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE};


// Variable Declarations
float wheelRPM = 0.0;
float Speed = 0.0;
int j = 1;
int WheelDia = 2;
int roundSpeed = 0;
int lastRoundSpeed = 0;


// Time variables for calculating RPM
// Variables modified by interrupts should be declared "volatile"
unsigned long startTime;
unsigned long endTime;
volatile unsigned long lastTime;
unsigned long lastButton = 0;


// Interrupt Function
// Increases revolution count and updates time of last pulse
void rpmMeasurement(){
  
  wheelRevCount += 1;
  lastTime = millis();
  
  }
  
  

// Setup Function
void setup(){
  
  // Open serial communications and wait for port to open:
  // Initialize SD OpenLog
  Serial.begin(115200);
  Serial.flush();
  
  // Axle RPM pin floats with no sensor attached and causes problems
  // Set as OUTPUT and set to LOW to stop floating problems
  pinMode(axleRPM_pin, OUTPUT);
  digitalWrite(axleRPM_pin, LOW);
  
  // Initalize accelerometer
  if((accelSen.init(G_SCALE, DATARATE)) == 1) Serial.print("Accelerometer connection successful.\n");
  else Serial.print("Accelerometer connection failed.\n");


  // Initalize LCD screen
  lcd.begin(16, 2);
  lcd.setBacklight(RED);
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.noAutoscroll();
  lcd.print("Bama Racing");
  
  // Set axleRPM_int pin to call rpmMeasurement function on every rising pulse
  // Define first start time
  attachInterrupt(axleRPM_int, rpmMeasurement, RISING);
  startTime = millis();
  
  // Create custom characters for smooth display bar
  // Each character is one, two, three, or four solid columns
  lcd.createChar(1, lineOne);
  lcd.createChar(2, lineTwo);
  lcd.createChar(3, lineThree);
  lcd.createChar(4, lineFour);
  
  Serial.println("\nTime    Speed    X-acc    Y-acc    Z-acc");
  
  // Delay to allow all setup to be completed before beginning loop
  delay(50);
}

// Main Function
void loop(){
  
  // Set up LCD buttons and Print Data
  uint8_t buttons = lcd.readButtons();
  
  // If speed has changed, update display
  
  displayMPH(Speed);
  lastRoundSpeed = roundSpeed;
  
  
  // If atleast 3 revolutions, calculate speed and reset variables
  
    endTime = lastTime;
    wheelRPM = (wheelRevCount*60000)/(endTime-startTime);  //Calculate rpm. Constant for conversion from milliseconds to minutes
    Speed = wheelRPM*WheelDia*0.0357;                      //Calculate speed. Constant for conversion to MPH
    roundSpeed = round(Speed);
    wheelRevCount = 0;
    startTime = millis();
 
  
 
};


// Function to display speed in MPH as a sliding bar
// Takes a float speed value
// Range is 0-40 MPH. If argument is >40, 40 MPH is displayed
void displayMPH(float MPH){
  lcd.setCursor(0,0);
  lcd.print("   1   2   3   4");
  lcd.setCursor(0,1);
  float ppc = 100.0/80.0;                     // each column of pixels is 1.25% of max speed
  float pom = MPH/40.0 * 100.0;               // percent of max speed
  int cnt_column = round(pom / ppc) ;         // percent of speed / percent per column = # of columns
  int num_full_block = cnt_column / 5;        // number of solid blocks
  int partial_char = cnt_column %5;           // partial block character (custom chars 1-4 created in setup function)
  
  // Set sliding bar to full if MPH > 40
  if (MPH > 40.0){
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
    
}



