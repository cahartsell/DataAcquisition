#include "Arduino.h"
#include "SENSORFUNC.h"
#include "i2cmaster.h"

//Constructor
SENSORFUNC::SENSORFUNC(){}

//Destructor
SENSORFUNC::~SENSORFUNC(){}



float SENSORFUNC::RPM(volatile int * flagpoint){
	
	//volatile int flag = false;
	unsigned long time[21] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	unsigned long del_t = 0;
	int i = 0;
	float freq = 0;
	float del_tavg = 0;
	unsigned long timer1= micros();
	
	while( i < 21 ){
        
		// if interrupt occurs
		//flag = *flagpoint;
		if ( *flagpoint ) {
            
			//measure time of interrupt
			time[i] = micros();
			//pflag = false;
			*flagpoint = false;
			i++;
		}
		
		//if timeout occurs
		if (micros()-timer1>1000000) {

			break;
			
		}
	}
	
	//if at least two interrupts occured
	if(i > 1){
		//calculate frequency
		for (int n = 0; n < i-1; n++){
	  
			del_t = time[n+1]  - time[n];
			del_tavg = del_tavg + del_t;
			
		} 
		
		del_tavg = del_tavg/((i-1)*1E6);    //Divide by 10^6 for microseonds, 
											//divide by (i-1) for avg
	
		freq = 1.0/del_tavg;
		return freq*60;
		
	} else{
		
		// //return -1 on a timeout
		return -1;
		
	}	
}

// //measurement() global interrupt flag
// volatile int sflag;
// volatile int pflag;

// //interrupt function smeasurement()
// void smeasurement()
// {
	
	// sflag = true;
	
// }

// //interrupt function pmeasurement()
// void pmeasurement()
// {

	// pflag = true;

// }

// //Secondary RPM Calculation
// float SENSORFUNC::SRPM(int ipin)
// {
	// //Insure interrupt sflag is reset
	// sflag = false;
	
	// //Local Variable Declaration
	// unsigned long time[11];
	// unsigned long del_t[10];
	// int i = 0;
	// float freq;
	// float del_tavg;
	// unsigned long timer1= micros();
	
	// //Interrupt assignment to measurement()
	// attachInterrupt(ipin,smeasurement,RISING);
	
	// //Loop that waits for interrupts to occur
	// while( i < 11 ){
        
		// //if interrupt occurs
		// if ( sflag == true ) {
            
			// //measure time of interrupt
			// time[i] = micros();
			// sflag = false;
			// i++;
		// }
		
		// //if timeout occurs
		// if (micros()-timer1>1000000) {
		
			// break;
			
		// }
	// }
	
	// //if at least two interrupts occured
	// if(i > 1){
		// //calculate frequency
		// for (int n = 0; n < i-1; n++){
	  
			// del_t[n] = time[n+1]  - time[n];
			// del_tavg = del_tavg + del_t[n];
			
		// } 
		
		// del_tavg = del_tavg/((i-1)*1E6);    //Divide by 10^6 for microseonds 
											// //divide by (i-1) for avg
	
		// freq = 1.0/del_tavg;
		// return freq*60;
		
	// } else{
		
		// //return -1 on a timeout
		// return -1;
	// }
// }

// //Primary RPM Calculation
// float SENSORFUNC::PRPM(int ipin)
// {
	// //Insure interrupt flag is reset
	// pflag = false;
	
	// //Local Variable Declaration
	// unsigned long time[11];
	// unsigned long del_t[10];
	// int i = 0;
	// float freq;
	// float del_tavg;
	// unsigned long timer1= micros();
	
	// //Interrupt assignment to measurement()
	// attachInterrupt(ipin,pmeasurement,RISING);
	
	// //Loop that waits for interrupts to occur
	// while( i < 11 ){
        
		// //if interrupt occurs
		// if ( pflag == true ) {
            
			// //measure time of interrupt
			// time[i] = micros();
			// pflag = false;
			// i++;
		// }
		
		// //if timeout occurs
		// if (micros()-timer1>1000000) {
		
			// break;
			
		// }
	// }
	
	// //if at least two interrupts occured
	// if(i > 1){
		// //calculate frequency
		// for (int n = 0; n < i-1; n++){
	  
			// del_t[n] = time[n+1]  - time[n];
			// del_tavg = del_tavg + del_t[n];
			
		// } 
		
		// del_tavg = del_tavg/((i-1)*1E6);    //Divide by 10^6 for microseonds 
											// //divide by (i-1) for avg
	
		// freq = 1.0/del_tavg;
		// return freq*60;
		
	// } else{
		
		// //return -1 on a timeout
		// return -1;
	// }
// }


//TPS Sensor (potentiometer)
/*float SENSORFUNC::potentiometer()
{
  // read the input on analog pin 8:
 int sensorValue = analogRead(A8);
 
  //Convert the analog reading (which goes from 0 - 1023) to a voltage (0 - 5V):
 float voltage = sensorValue * (5.0 / 1023.0);

 return voltage;
}

//Infrared Temperature Sensor
float SENSORFUNC::irsensor()
{
	// i2c_init(); 								//Initialise the i2c bus
	// PORTC = (1 << PORTC4) | (1 << PORTC5);		//enable pullups

    int dev = 0x5A<<1;
    int data_low = 0;
    int data_high = 0;
    int pec = 0;
    
    i2c_start_wait(dev+I2C_WRITE);
    i2c_write(0x07);
    
    // read
    i2c_rep_start(dev+I2C_READ);
    data_low = i2c_readAck(); 					//Read 1 byte and then send ack
    data_high = i2c_readAck(); 					//Read 1 byte and then send ack
    pec = i2c_readNak();
    i2c_stop();
    
    //This converts high and low bytes together and processes temperature 
	//MSB is a error bit and is ignored for temps
    double tempFactor = 0.02; 					// 0.02 degrees per LSB (measurement resolution of the MLX90614)
    double tempData = 0x0000; 					// zero out the data
    int frac; 									// data past the decimal point
    
    // This masks off the error bit of the high byte, 
	//then moves it left 8 bits and adds the low byte.
    tempData = (double)(((data_high & 0x007F) << 8) + data_low);
    tempData = (tempData * tempFactor)-0.01;
    
    float celcius = tempData - 273.15;
    float fahrenheit = (celcius*1.8) + 32;

    return fahrenheit;
}
*/