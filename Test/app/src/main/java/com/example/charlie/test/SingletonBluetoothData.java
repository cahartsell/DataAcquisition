package com.example.charlie.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

// Class that contains bluetooth objects which can be shared across Activities/Views
// This entire class is embarrassing and I am ashamed. Android programming is awful
public class SingletonBluetoothData{

    private static volatile SingletonBluetoothData sBluetoothData = new SingletonBluetoothData();

    // Private constructor
    private SingletonBluetoothData(){}

    public static SingletonBluetoothData getInstance() {
        return sBluetoothData;
    }

    // Bluetooth objects
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    // Get and Set functions for BT Adapter
    public BluetoothAdapter getBtAdapter() {
        return btAdapter;
    }

    public void setBtAdapter(BluetoothAdapter btAdapter) {
        this.btAdapter = btAdapter;
    }

    // Get and Set functions for BT Socket
    public BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }
}
