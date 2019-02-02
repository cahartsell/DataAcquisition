//
// Created by charlie on 7/29/18.
//

#ifndef DAS_THERMISTOR_H
#define DAS_THERMISTOR_H


#include "logger.h"
#include "sensorbase.h"

class Thermistor: public SensorBase {
public:
    Thermistor(Logger *logger, int input_pin);
    ~Thermistor();
    int setup();
    int update();

private:
    int inputPin;
};


#endif //DAS_THERMISTOR_H
