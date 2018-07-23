//
// Created by Charlie on 7/21/2018.
//

#ifndef DAS_LOGGER_H
#define DAS_LOGGER_H

#include <fstream>

class Logger {
public:
    Logger();
    ~Logger();
    bool open(std::string fileName);
    bool write(std::string str);
    bool writeln(std::string str);
private:
    std::fstream logStream;
    std::string logFile;
};

#endif //DAS_LOGGER_H
