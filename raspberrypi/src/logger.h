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
    int open(std::string fileName);
    int write(std::string str);
    int writeln(std::string str);
    int writeBin(char *data, size_t data_size);
private:
    std::fstream logStream;
    std::string logFile;
};

#endif //DAS_LOGGER_H
