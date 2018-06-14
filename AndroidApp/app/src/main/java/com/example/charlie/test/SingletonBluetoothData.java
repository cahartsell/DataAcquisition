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
    private OutputStream btOutStream = null;
    private InputStream btInStream = null;

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
        if(btSocket.isConnected()) {
            try {
                this.btOutStream = btSocket.getOutputStream();
                this.btInStream = btSocket.getInputStream();
            } catch (IOException e) {
                this.btInStream = null;
                this.btOutStream = null;
            }
        }
    }

    public boolean socketConnected() {
        return this.btSocket.isConnected();
    }

    public OutputStream getBtOutStream() {
        return btOutStream;
    }

    public InputStream getBtInStream() {
        return btInStream;
    }
}
