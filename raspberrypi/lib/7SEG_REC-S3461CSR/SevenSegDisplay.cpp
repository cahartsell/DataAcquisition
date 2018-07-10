/*
 * SevenSegDisplay.cpp
 *
 *  Created on: Apr 29, 2018
 *      Author: Charlie Hartsell
 */

#include <Arduino.h>
#include "SevenSegDisplay.h"

SevenSegDisplay::SevenSegDisplay( int pinA, int pinB, int pinC, int pinD,
                                  int pinE, int pinF, int pinG, int pinDP,
                                  int pinGnd0, int pinGnd1, int pinGnd2, int pinGnd3)
{
  segPins[segA] = pinA;
  segPins[segB] = pinB;
  segPins[segC] = pinC;
  segPins[segD] = pinD;
  segPins[segE] = pinE;
  segPins[segF] = pinF;
  segPins[segG] = pinG;
  segPins[segDP] = pinDP;

  digitGndPin[0] = pinGnd0;
  digitGndPin[1] = pinGnd1;
  digitGndPin[2] = pinGnd2;
  digitGndPin[3] = pinGnd3;
}

SevenSegDisplay::~SevenSegDisplay() {
  // TODO Auto-generated destructor stub
}

void SevenSegDisplay::setupPins()
{
  // Configure the ground pins as outputs and set to high (ie. digit OFF)
  for (int i = 0; i < NUM_DIGITS; i++){
    pinMode(digitGndPin[i], OUTPUT);
    digitalWrite(digitGndPin[i], HIGH);
  }

  // Configure all segment pins as outputs and set to low (ie. segment OFF)
  for (int i = 0; i < NUM_SEGMENTS; i++){
    pinMode(segPins[i], OUTPUT);
    digitalWrite(segPins[i], LOW);
  }
}

void SevenSegDisplay::clearDisplayValue()
{
  // Turn off all segments
  for (int i = 0; i < NUM_SEGMENTS; i++){
    digitalWrite(segPins[i], LOW);
  }
}

void SevenSegDisplay::setDecimalPoint()
{
  digitalWrite(segPins[segDP], HIGH);
}

void SevenSegDisplay::clearDecimalPoint()
{
  digitalWrite(segPins[segDP], LOW);
}

bool SevenSegDisplay::turnOnDigit(int digit)
{
  if (digit < 0){
    if (digit == -1){
      // Special case to turn on all digits
      for(int i = 0; i < NUM_DIGITS; i++){
        digitalWrite(digitGndPin[i], LOW);
      }
      return true;
    }

    // Otherwise, input is invalid
    else return false;
  }

  else if (digit >= NUM_DIGITS){
    return false;
  }

  digitalWrite(digitGndPin[digit], LOW);
  return true;
}

bool SevenSegDisplay::turnOffDigit(int digit)
{
  if (digit < 0){
    if (digit == -1){
      // Special case to turn on all digits
      for(int i = 0; i < NUM_DIGITS; i++){
        digitalWrite(digitGndPin[i], HIGH);
      }
      return true;
    }

    // Otherwise, input is invalid
    else return false;
  }

  else if (digit >= NUM_DIGITS){
    return false;
  }

  digitalWrite(digitGndPin[digit], HIGH);
  return true;
}

bool SevenSegDisplay::setDisplayValue(int value)
{
  // Can only display integers in range [0,9]
  if ((value < 0) || (value > 9)){
    return false;
  }

  switch (value){
    case 0:
      // Turn on all segments except segment G and DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segG) || (i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    case 1:
      // Turn on segments B and C
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segB) || (i == segC)) {digitalWrite(segPins[i], HIGH);}
        else {digitalWrite(segPins[i], LOW);}
      }
      break;

    case 2:
      // Turn on all segments except segment C, F, and DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segF) || (i == segC) || (i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    case 3:
      // Turn on all segments except segment F, E, and DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segF) || (i == segE) || (i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    case 4:
      // Turn on all segments except segment A, E, D and DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segA) || (i == segE) || (i == segD) || (i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    case 5:
      // Turn on all segments except segment B, E and DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segB) || (i == segE) || (i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    case 6:
      // Turn on all segments except segment B and DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segB) || (i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    case 7:
      // Turn on segments A, B, and C
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segA) || (i == segB) || (i == segC)) {digitalWrite(segPins[i], HIGH);}
        else {digitalWrite(segPins[i], LOW);}
      }
      break;

    case 8:
      // Turn on all segments except DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    case 9:
      // Turn on all segments except E, D, and DP (decimal point)
      for (int i = 0; i < NUM_SEGMENTS; i++){
        if ((i == segE) || (i == segD) || (i == segDP)) {digitalWrite(segPins[i], LOW);}
        else {digitalWrite(segPins[i], HIGH);}
      }
      break;

    default:
      for (int i = 0; i < NUM_SEGMENTS; i++){
        digitalWrite(segPins[i], LOW);
      }
      return false;
  }

  return true;
}
