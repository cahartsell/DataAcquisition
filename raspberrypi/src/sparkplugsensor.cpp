//
// Created by Charlie on 7/21/2018.
//

#define SPARKPLUGSENSOR_LOG_PREFIX  "SPARK_PLUG_SENSOR: "

#include "pthread.h"
#include <time.h>
#include "sparkplugsensor.h"
#include "wiringPi.h"

// Declare instance of SparkPlugSensor
SparkPlugSensor *SparkPlugSensor::instance;

// SparkPlugSensor class
SparkPlugSensor::SparkPlugSensor(Logger *logger, int pin) {
    // Copy args to member variables
    this->_logger = logger;
    this->inputPin = pin;

    // Set this as the sole instance of SparkPlugSensor
    instance = this;
}

SparkPlugSensor::~SparkPlugSensor() {
    // Destroy mutex
    pthread_mutex_destroy(&lock);
}

int SparkPlugSensor::setup() {
    // Setup pin mode and ISR function
    pinMode(inputPin, INPUT);
    wiringPiISR(inputPin, INT_EDGE_FALLING, _isr);

    // Create mutex - default settings
    int result = pthread_mutex_init(&(this->lock), NULL);
    if (result != 0) {
        this->log("Failed to create pthread mutex");
    }

    return 0;
}

void SparkPlugSensor::isr(void) {
    // MUTEX inside an ISR is not the greatest idea, but don't know how to disable interrupts with Linux/WiringPi
    // Try to lock mutex, but don't wait
    // FIXME: Using trylock means a pulse may occasionally be missed. Possibly short-delay timed lock instead?
    if (pthread_mutex_trylock(&lock) != 0) {
        return;
    }

    // Update counter and store time of update
    // FIXME: don't use clock_gettime in ISR
    cnt++;
    timespec tv;
    clock_gettime(CLOCK_MONOTONIC, &tv);
    endTime.tv_sec = tv.tv_sec;
    endTime.tv_nsec = tv.tv_nsec;
    pthread_mutex_unlock(&lock);
}

int SparkPlugSensor::update() {
    // TODO: Write this
    // Calculate engine
    if (cnt > MIN_UPDATE_CNT) {

    }

    return true;
}

// Wrapper function for using logger. Checks if logger exists and prepends LOG_PREFIX to any message.
int SparkPlugSensor::log(std::string _msg) {
    if(this->_logger != NULL) {
        std::string msg = SPARKPLUGSENSOR_LOG_PREFIX;
        msg.append(_msg);

        return this->_logger->writeln(msg);
    } else {
        return false;
    }
}

void SparkPlugSensor::_isr() {
    instance->isr();
}
