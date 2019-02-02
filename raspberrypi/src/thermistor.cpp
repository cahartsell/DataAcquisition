//
// Created by charlie on 7/29/18.
//

#include <wiringPi/wiringPi.h>
#include "thermistor.h"

Thermistor::Thermistor(Logger *logger, int input_pin) {
    this->_logger = logger;
    this->inputPin = input_pin;
}

Thermistor::~Thermistor() {
    // Destructor
}

int Thermistor::setup() {
    // Setup pin mode
    pinMode(inputPin, INPUT);
    return 0;
}

int Thermistor::update() {

}