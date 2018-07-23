#ifndef SENSORFUNC_H
#define SENSORFUNC_H

#include "Arduino.h"

class SENSORFUNC
{
  public:
    SENSORFUNC();
    ~SENSORFUNC();
    //float potentiometer();
    //float irsensor();
	float RPM(volatile int * flagpoint);
    //float SRPM( int ipin );
    //float PRPM( int ipin );
};

#endif
