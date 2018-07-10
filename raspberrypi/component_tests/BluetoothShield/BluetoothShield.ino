#include <Arduino.h>
#include <SoftwareSerial.h>

#define RxD 2
#define TxD 3
SoftwareSerial mySerial(RxD,TxD);

void setup()
{
    pinMode(RxD, INPUT);
    pinMode(TxD, OUTPUT);
    mySerial.begin(115200);               // the ble4.0 baud rate
    Serial.begin(115200);                 // the terminal baud rate
    delay(500);
}

void loop()
{
    if(Serial.available())
    {
       char val = Serial.read();
       Serial.print(val);
       mySerial.print(val);
    }

    if(mySerial.available())
    {
       Serial.print((char)mySerial.read());
    }
}
