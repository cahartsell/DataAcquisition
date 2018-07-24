//
// Created by Charlie on 7/21/2018.
//

#ifndef DAS_SPARKPLUGSENSOR_H
#define DAS_SPARKPLUGSENSOR_H

#define MIN_UPDATE_CNT          3
#define MIN_RPM                 600
#define UPDATE_CNT_TIMEOUT_MS   (60000 / MIN_RPM)  // Allowed time between pulses before considering the engine off

#include <time.h>
#include <pthread.h>
#include "sensorbase.h"

class SparkPlugSensor: public SensorBase {
public:
    // Public functions
    SparkPlugSensor(Logger *_logger, int pin);
    ~SparkPlugSensor();
    int setup();   // overrides base class
    void isr();
    int update();  // overrides base class

    // Black magic ISR trickery
    // This limits class to only one instance which is annoying.
    // TODO: Add vector of instances to solve one instance limitation
    static SparkPlugSensor *instance;
    static void _isr();

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
