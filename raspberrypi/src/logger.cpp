//
// Created by Charlie on 7/21/2018.
//

#include <fstream>
#include "logger.h"

using namespace std::fstream;

class Logger {
    // Constructor
    Logger() {
    }

    // Destructor
    ~Logger() {
        if (logStream.is_open()){
            logStream.close();
        }
    }

    bool open(std::string fileName) {
        // Open file for input/output/append. Check that file is open
        logStream.open(fileName.c_str(), in | out | app);
        return logStream.is_open();
    }

    bool write(std::string str) {
        if (logStream.is_open()) {
            logStream << str;
            return 0;
        }
        return -1;
    }

    bool writeln(std::string str) {
        if (logStream.is_open()) {
            logStream << str;
            return 0;
        }
        return -1;
    }
};