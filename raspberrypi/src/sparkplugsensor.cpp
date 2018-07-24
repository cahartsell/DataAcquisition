//
// Created by Charlie on 7/21/2018.
//

#define LOG_PREFIX  "SPARK_PLUG_SENSOR: "

#include <pthread.h>
#include <time.h>
#include "sparkplugsensor.h"
#include "wiringPi.h"

// SparkPlugSensor class
SparkPlugSensor::SparkPlugSensor(Logger *_logger, int pin) {
    // Copy args to member variables
    logger = _logger;
    inputPin = pin;

    // Create mutex - default settings
    int result = pthread_mutex_init(&lock, nullptr);
    if (result != 0) {
        lock = nullptr;
    }

    // Set this as the sole instance of SparkPlugSensor
    instance = this;
}

SparkPlugSensor::~SparkPlugSensor() {
    // Destroy mutex if it exists
    if (lock != nullptr) {
        pthread_mutex_destroy(&lock);
    }
}

int SparkPlugSensor::setup() {
    // Setup pin mode and ISR function
    pinMode(inputPin, INPUT);
    wiringPiISR(inputPin, INT_EDGE_FALLING, _isr);
}

void SparkPlugSensor::isr(void) {
    // MUTEX inside an ISR is not the greatest idea, but don't know how to disable interrupts with Linux/WiringPi
    // Try to lock mutex, but don't wait
    // FIXME: Using trylock means a pulse may occasionally be missed. Possibly short-delay timed lock instead?
    if (pthread_mutex_trylock(&lock) != 0) {
        return;
    }

    // Update counter and store time of update
    // FIXME: dont use clock_gettime in ISR
    cnt++;
    clock_gettime(CLOCK_MONOTONIC, &endTime);
    pthread_mutex_unlock(&lock);
}

int SparkPlugSensor::update() {
    if (cnt > MIN_UPDATE_CNT) {

    }

    return true;
}

// Wrapper function for using logger. Checks if logger exists and prepends LOG_PREFIX to any message.
bool SparkPlugSensor::log(std::string _msg) {
    if(logger != nullptr) {
        std::string msg = LOG_PREFIX;
        msg.append(_msg);

        return logger->writeln(msg);
    } else {
        return false;
    }
}

static void SparkPlugSensor::_isr(void) {
    instance->isr;
}