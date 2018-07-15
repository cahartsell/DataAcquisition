/********************************************************************************
  Main project file for data acquisition system.

  Charles Hartsell    JULY 9 2018
********************************************************************************/

#include <sys/types.h>
#include <sys/stat.h>

#define LOG_DIRECTORY_STR "/var/log/das/"

int main (int argc, char *argv[])
{
  // make directory for storing data files. Owner has read, write, execute permission. Everyone else has read permission.
  mkdir(LOG_DIRECTORY_STR, S_IRWXU & S_IRGRP & S_IROTH);

  return 0;
}

