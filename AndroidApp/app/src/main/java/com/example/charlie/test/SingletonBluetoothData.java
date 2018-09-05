package com.example.charlie.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    public OutputStream outStream = null;
    public InputStream inStream = null;

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
        if (btSocket != null && btSocket.isConnected()) {
            try {
                this.outStream = btSocket.getOutputStream();
                this.inStream = btSocket.getInputStream();
            } catch (IOException e) {
                this.inStream = null;
                this.outStream = null;
            }
        } else {
            this.inStream = null;
            this.outStream = null;
        }
    }

    public boolean socketConnected() {
        if (this.btSocket == null)
            return false;
        else
            return this.btSocket.isConnected();
    }

    public OutputStream getOutStream() {
        return outStream;
    }

    public InputStream getInStream() {
        return inStream;
    }
}
