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
#define DATA_DIR  "/var/lib/das/data/"

#define WRITE_PIN 0
#define SPARK_PLUG_PIN 1

enum sensorIds {
    sparkPlug,
    sensorCnt
};

const std::string getCurrentDateTime();
int mkdir_s (std::string dirName);

int main (int argc, char *argv[])
{
  // Local variables
  int result, i, j;
  Logger eventLogger, dataLogger;

  // Make directories for storing data files. Owner has read, write, execute permission. Everyone else has read permission.
  result = mkdir_s(BASE_DIR);
  if (result != 0) { return result; }
  result = mkdir_s(LOG_DIR);
  if (result != 0) { return result; }
  result = mkdir_s(DATA_DIR);
  if (result != 0) { return result; }

  // Get today's date/time and init logger.
  // Logger should create file if it does not already exist.
  // Create logger instance for event log and data file.
  std::string currentDate, logFile, dataFile;
  currentDate = getCurrentDateTime();

  logFile = LOG_DIR;
  logFile.append(currentDate);
  logFile.append(".log");
  eventLogger.open(logFile);
  eventLogger.writeln("Log file created.");

  dataFile = DATA_DIR;
  dataFile.append(currentDate);
  dataFile.append(".dat");
  dataLogger.open(dataFile);
  eventLogger.writeln("Data file created.");

  // Init wiringPi
  wiringPiSetupGpio();
  eventLogger.writeln("Wiring Pi setup complete.");

  // Construct and setup sensor classes
  SensorBase *sensors[sensorCnt];
  SparkPlugSensor *sparkPlugSensor = new SparkPlugSensor(&eventLogger, SPARK_PLUG_PIN);
  sensors[sparkPlug] = sparkPlugSensor;

  // FIXME: Some way to shutdown cleanly would be good
  timespec lastTime, curTime;
  j = 0;
  while (j < 10) {
      j++;
      // Update all sensors
      for (i = 0; i < sensorCnt; i++) {
          sensors[i]->update();
      }
  }

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

int mkdir_s (std::string dirName) {
  int result = mkdir(dirName.c_str(), S_IRWXU | S_IRGRP | S_IROTH);
  if (result != 0) {
    if (errno != EEXIST) {
      // Typical case since directory will normally already exist
    } else if (errno == EACCES) {
      // Don't have permission to access directory.
      std::cout << "Could not access " << dirName << " (Permission Denied)." << std::endl;
      std::cout << "Change ownership or access permissions of directory to appropriate user." << std::endl;
      return -1;
    } else {
      // Something other error
      std::cout << "Failed to make directory: " << dirName << ". ERRNO: " << errno << std::endl;
    }
  }
  return 0;
}