/********************************************************************************
  Main project file for data acquisition system.

  Charles Hartsell    JULY 9 2018
********************************************************************************/

// Include every file ever made
#include <sys/types.h>
#include <sys/stat.h>
#include <pthread.h>
#include <ctime>
#include <cstdlib>
#include <cerrno>
#include <string>
#include <iostream>

#include "wiringPi.h"
#include "logger.h"
#include "sensorbase.h"
#include "sparkplugsensor.h"

#define BASE_DIR  "/var/lib/das/"
#define LOG_DIR   "/var/lib/das/logs/"

#define WRITE_PIN 0
#define SPARK_PLUG_PIN 1

enum sensorIds {
    sparkPlug,
    sensorCnt
};

const std::string getCurrentDateTime();

int main (int argc, char *argv[])
{
  // Local variables
  int result, i, j;
  Logger logger;

  // Make directories for storing data files. Owner has read, write, execute permission. Everyone else has read permission.
  result = mkdir(BASE_DIR, S_IRWXU | S_IRGRP | S_IROTH);
  if (result != 0) {
    if (errno != EEXIST) {
      // Something weird happened
      std::cout << "Failed to make directory: " << BASE_DIR << ". ERRNO: " << errno << std::endl;
    }
    if (errno == EACCES) {
      // Don't have permission to access directory.
      std::cout << "Could not access " << BASE_DIR << " (Permission Denied)." << std::endl;
      std::cout << "Change ownership or access permissions of directory to appropriate user." << std::endl;
    }
  }

  result = mkdir(LOG_DIR, S_IRWXU | S_IRGRP | S_IROTH);
  if (result != 0) {
    if (errno != EEXIST) {
      // Something weird happened
    }
  }

  // Get today's date/time and init logger.
  // Logger should create file if it does not already exist.
  std::string currentDate, logFile;
  currentDate = getCurrentDateTime();
  // CLion complained when this was on one line
  logFile = LOG_DIR;
  logFile.append(currentDate);
  logFile.append(".log");
  logger.open(logFile);
  logger.writeln("Log File Created");

  // Init wiringPi
  wiringPiSetup();

  // Construct and setup sensor classes
  SensorBase* sensors[sensorCnt];
  SparkPlugSensor *sparkPlugSensor = new SparkPlugSensor(&logger, SPARK_PLUG_PIN);
  sensors[sparkPlug] = sparkPlugSensor;

  // FIXME: Some way to shutdown cleanly would be good
  timespec lastTime, curTime;
  j = 0;
  while (j < 10) {
      j++;
      std::cout << "Looping..." << std::endl;
      logger.writeln("Logging...");
      // Update all sensors
      for (i = 0; i < sensorCnt; i++) {
          sensors[i]->update();
      }
  }

  std::cout << "Cleanup." << std::endl;
  logger.writeln("Cleanup.");

  // Cleanup sensor classes
  for (i = 0; i < sensorCnt; i++) {
    delete sensors[i];
  }

  return 0;
}

// Copied from stackoverflow thread:
// https://stackoverflow.com/questions/997946/how-to-get-current-time-and-date-in-c
// Get current date/time, format is YYYY-MM-DD_HH-mm-ss
const std::string getCurrentDateTime() {
  time_t        now = time(NULL);
  struct tm     tstruct;
  char          buf[80];
  std::string   result;

  tstruct = *localtime(&now);
  // Visit http://en.cppreference.com/w/cpp/chrono/c/strftime
  // for more information about date/time format
  strftime(buf, sizeof(buf), "%Y-%m-%d_%H-%M-%S", &tstruct);

  result = buf;
  return result;
}
