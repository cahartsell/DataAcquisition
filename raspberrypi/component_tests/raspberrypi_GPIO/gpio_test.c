//
// Created by Charlie on 7/15/2018.
// Basic test of Raspberry PI GPIO using "WiringPi" library
//

#include <wiringPi.h>
#include <stdio.h>
#include <time.h>
#include <string.h>
#include <stdlib.h>

#define LOOP_COUNT 100
#define WRITE_STABILIZE_DELAY_NANO_S 100000
#define WRITE_PIN 0
#define READ_PIN 1

int main (void)
{
    int i, wrote[LOOP_COUNT], read[LOOP_COUNT], rv, nextWrite, nextRead;
    struct timespec tv;

    // Zero arrays
    memset(wrote, 0, sizeof(int) * LOOP_COUNT);
    memset(read, 0, sizeof(int) * LOOP_COUNT);

    // Init wiringPi and GPIO pins
    wiringPiSetup();
    pinMode(WRITE_PIN, OUTPUT);
    pinMode(READ_PIN, INPUT);

    // Seed RNG
    srand(time(0));

    // Fill timespec
    // This assumes stabilize delay is always less than 1 second
    tv.tv_nsec = WRITE_STABILIZE_DELAY_NANO_S;
    tv.tv_sec = 0;

    // Write random value to one pin, wait, then read the value at another pin.
    for(i = 0; i < LOOP_COUNT; i++) {
        nextWrite = rand() % 2;
        digitalWrite(WRITE_PIN, nextWrite);
        nanosleep(&tv, NULL);
        nextRead = digitalRead(READ_PIN);
        wrote[i] = nextWrite;
        read[i] = nextRead;
    }

    // Print results
    printf("Wrote | Read\n");
    for(i = 0; i < LOOP_COUNT; i++) {
        printf("%d\t%d\n", wrote[i], read[i]);
    }

    return 0;
}
