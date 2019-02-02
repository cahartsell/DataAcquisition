//
// Created by Charlie on 7/21/2018.
//

#include <fstream>
#include "logger.h"

// FIXME: Make this thread-safe just in case. Mutex around logStream should be fine.
// Constructor
Logger::Logger() {
}

// Destructor
Logger::~Logger() {
    if (logStream.is_open()){
        logStream.close();
    }
}

int Logger::open(std::string fileName) {
    // Open file for input/output/append. Check that file is open
    logStream.open(fileName.c_str(), std::fstream::in | std::fstream::out | std::fstream::app);
    return logStream.is_open();
}

int Logger::write(std::string str) {
    if (logStream.is_open()) {
        logStream << str;
        return 0;
    }
    return -1;
}

int Logger::writeln(std::string str) {
    if (logStream.is_open()) {
        logStream << str << '\n';
        return 0;
    }
    return -1;
}

int Logger::writeBin(char *data, size_t data_size) {
    if (logStream.is_open()) {
        logStream.write(data, data_size);
        return 0;
    }
    return -1;
}
