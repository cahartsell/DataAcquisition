//
// Created by Charlie on 7/21/2018.
//

#ifndef DAS_SPARKPLUGSENSOR_H
#define DAS_SPARKPLUGSENSOR_H

#define MIN_UPDATE_CNT          3
#define MIN_RPM                 600
#define UPDATE_CNT_TIMEOUT_MS   (60000 / MIN_RPM)  // Allowed time between pulses before considering the engine off

#include "sensorbase.h"

class SparkPlugSensor: public SensorBase {
public:
    // Public functions
    SparkPlugSensor(Logger *_logger, int pin);
    bool setup();   // overrides base class
    void isr(void);
    bool update();  // overrides base class

private:
    // Private functions
    bool log(std::string msg);

    // Private variables
    timespec startTime;
    volatile timespec endTime;
    volatile int cnt;
    pthread_mutex_t lock;
    int inputPin;
};


#endif //DAS_SPARKPLUGSENSOR_H
