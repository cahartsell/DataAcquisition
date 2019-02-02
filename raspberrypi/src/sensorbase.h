//
// Created by Charlie on 7/21/2018.
//

#ifndef DAS_SENSORBASE_H
#define DAS_SENSORBASE_H

#include <vector>
#include "logger.h"

/********************************************************
 * Simple base class for sensors.
 *  - Setup function is called once at program init
 *  - Update function is called at a set time interval
 ********************************************************/
class SensorBase {
public:
    SensorBase() {}; // Default constructor

    //SensorBase(Logger *_logger); // No pin constructor
    //SensorBase(Logger *_logger, int pin_number); // Single pin constructor
    //SensorBase(Logger *_logger, std::vector<int> pins); // Multiple pin constructor

    virtual int setup() {};
    virtual int update() {};

protected:
    Logger *_logger;
};

#endif //DAS_SENSORBASE_H
