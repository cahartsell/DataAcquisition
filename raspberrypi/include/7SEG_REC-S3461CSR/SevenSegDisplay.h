/*
 * SevenSegDisplay.h
 *
 *  Created on: Apr 29, 2018
 *      Author: Charlie
 */

#ifndef LIB_7SEG_REC_S3461CSR_SEVENSEGDISPLAY_H_
#define LIB_7SEG_REC_S3461CSR_SEVENSEGDISPLAY_H_

#define NUM_DIGITS 4

enum pinID{
  segA = 0,
  segB,
  segC,
  segD,
  segE,
  segF,
  segG,
  segDP, // Decimal Point
  NUM_SEGMENTS
};

class SevenSegDisplay {
public:
  SevenSegDisplay(int pinA, int pinB, int pinC, int pinD,
                  int pinE, int pinF, int pinG, int pinDP,
                  int pinGnd0, int pinGnd1, int pinGnd2, int pinGnd3);
  virtual ~SevenSegDisplay();

  void setupPins();
  bool setDisplayValue(int value);
  void clearDisplayValue();
  void setDecimalPoint();
  void clearDecimalPoint();
  bool turnOnDigit(int digit);
  bool turnOffDigit(int digit);

private:
  int digitGndPin[NUM_DIGITS], segPins[NUM_SEGMENTS];
};

#endif /* LIB_7SEG_REC_S3461CSR_SEVENSEGDISPLAY_H_ */
