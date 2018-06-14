package com.example.charlie.test;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class BluetoothList extends AppCompatActivity {
    private ListView BTListView;
    private ArrayAdapter<String> listAdapter;
    SingletonBluetoothData sBTData = SingletonBluetoothData.getInstance();
    BluetoothAdapter btAdapter;
    BluetoothSocket btSocket;
    Set<BluetoothDevice> pairedDevices;
    ArrayList<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
    ArrayList<String> deviceNames = new ArrayList<String>();
    ArrayList<String> deviceAddresses = new ArrayList<String>();
    TextView statusTV;
    Button bottomLeftButton, bottomRightButton;

    // Constants. This seems like the wrong place
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 41568;
    int REQUEST_ENABLE_BT = 777;
    private static final String TAG = "BluetoothListActivity";
    String SERVER_UUID_STRING = "94f39d29-7d6d-435d-973b-fba39e49d4ee";
    UUID SERVER_UUID = UUID.fromString(SERVER_UUID_STRING);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title
        getSupportActionBar().setTitle("Discovered Bluetooth Devices");

        // Find status notification text view
        statusTV = (TextView) findViewById(R.id.statusTextView);
        BTListView = (ListView) findViewById( R.id.listview );

        // Configure refresh button
        bottomRightButton = (Button) findViewById(R.id.bottomRightButton);
        String brText = "Refresh List";
        bottomRightButton.setText(brText);
        bottomRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear lists
                Log.d(TAG, "Restart discovery button pressed.");
                discoveredDevices.clear();
                deviceAddresses.clear();
                deviceNames.clear();
                listAdapter.notifyDataSetChanged();

                // Discover nearby devices
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                btAdapter.startDiscovery();
            }
        });

        // Setup Bluetooth
        btAdapter = enableBluetooth(statusTV);
        sBTData.setBtAdapter(btAdapter);
        if (btAdapter == null) {
            Log.e(TAG, "Failed to enable bluetooth adapter");
        }
        pairedDevices = btAdapter.getBondedDevices();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(btReceiver, filter);

        // I have no idea why this is required to make BT discovery work, but it is
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        // Discover nearby devices
        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }
        boolean result = btAdapter.startDiscovery();
        if (result == false) {
            String tempStr = "Failed to start bluetooth discovery.";
            statusTV.setText(tempStr);
            Log.e(TAG, tempStr);
        }

        // Display paired device names
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceNames);
        BTListView.setAdapter( listAdapter );

        // Handler for when user clicks on a bluetooth device in list
        BTListView.setClickable(true);
        BTListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /* Can skip pairing and use an insecure connection instead.
                   This is suitable for microcontroller since no user interface easily available.
                if ( !pairedDevices.contains(discoveredDevices.get(i)) ) {
                    // Chosen device has not been paired yet. Have to do that first.
                    Intent enableBtIntent = new Intent(BluetoothAdapter.);
                    Log.d(TAG, "Starting activity for result: BluetoothAdapter.ACTION_REQUEST_ENABLE");
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } */

                BluetoothDevice selectedDevice = discoveredDevices.get(i);
                /*ParcelUuid uuids[] = selectedDevice.getUuids();
                if(uuids == null) {
                    Log.w(TAG, "No UUIDs found on selected device.");
                    return;
                }
                for (i = 0; i < uuids.length; i++) {
                    if (uuids[i].getUuid() == SERVER_UUID) break;
                }*/

                // FIXME: This is always triggered for some reason, even if UUID is advertised correctly
                /*if (i == uuids.length){
                    // Did not find desired UUID advertised by this device - Not hosting desired service
                    String tempStr = "Selected device has incorrect UUID";
                    Log.w(TAG, tempStr);
                    statusTV.setText(tempStr);
                    return;
                }*/

                // Check if open BT Socket already exists. Close it if so.
                // TODO: Add some logging and make this not broken
                if (sBTData.getBtSocket() != null){
                    try{
                        sBTData.getBtSocket().close();
                    } catch (IOException e) {
                        sBTData.setBtSocket(null);
                    }
                }

                try {
                    // Try to create Insecure RFCOMM socket to selected device on SERVER_UUID
                    btSocket = selectedDevice.createInsecureRfcommSocketToServiceRecord(SERVER_UUID);
                } catch (IOException e) {
                    btSocket = null;
                }

                if (btSocket == null){
                    String tempStr = "Failed creating RFCOMM Socket to device: ";
                    tempStr = tempStr.concat(selectedDevice.getAddress());
                    Log.w(TAG, tempStr);
                    tempStr = "BT socket creation failed";
                    statusTV.setText(tempStr);
                    return;
                }

                try{
                    btSocket.connect();
                } catch (IOException e) {
                    String tempStr = "Failed when connecting socket to device: ";
                    tempStr = tempStr.concat(selectedDevice.getAddress());
                    Log.w(TAG, tempStr);
                    tempStr = "BT socket connection failed";
                    statusTV.setText(tempStr);
                    return;
                }

                if (btSocket.isConnected()){
                    // If we successfully connected, set BT Socket and return to Main Activity
                    sBTData.setBtSocket(btSocket);
                    Log.d(TAG, "BT Socket connected successfully");
                    finish();
                }
                else{
                    Log.d(TAG, "BT Socket connecting failed");
                    statusTV.setText("Failed to connect.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (btAdapter.isDiscovering()) {
            btAdapter.cancelDiscovery();
        }

        unregisterReceiver(btReceiver);
    }

    // Create a BroadcastReceiver
    private final android.content.BroadcastReceiver btReceiver = new android.content.BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(device);
                deviceNames.add(device.getName());
                deviceAddresses.add(device.getAddress()); // MAC address
                listAdapter.notifyDataSetChanged();
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                statusTV.setText("Discovering...");
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                statusTV.setText("Discovery finished.");
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON) {
                    statusTV.setText("Bluetooth adapter ON.");
                }
            }
        }
    };

    // If bluetooth device available, start activity to enable it
    // I can't make this a standalone function because JAVA is disgusting.
    public BluetoothAdapter enableBluetooth(TextView tv) {
        BluetoothAdapter adapter;
        Log.d(TAG, "enableBluetooth() called.");
        // I don't think this null check even works because Android programming makes no sense
        if (tv != null) {
            tv.setText("Trying to enable BT...");
        }

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            // No bluetooth adapter found
            Log.w(TAG, "No bluetooth adapter found.");
            if (tv != null) {
                tv.setText("No BT adapter found.");
            }
            return null;
        }

        if (!adapter.isEnabled()) {
            // Enable adapter
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (tv != null) {
                tv.setText("Starting activity to enable BT...");
            }
            Log.d(TAG, "Starting activity for result: BluetoothAdapter.ACTION_REQUEST_ENABLE");
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Log.d(TAG, "Bluetooth already enabled.");
            if (tv != null) {
                tv.setText("Bluetooth enabled.");
            }
        }

        return adapter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT){
            if (requestCode == RESULT_OK){
                // BT Enable succeeded
                statusTV.setText("BT Enabled.");
            }
            else if (requestCode == RESULT_CANCELED){
                // BT Enable failed
                Log.w(TAG, "Failed to enable bluetooth.");
                statusTV.setText("Failed to enable BT.");
            }
            else{
                // Unknown result
            }
        }
    }

    public ArrayList<String> stringifyBtDevices(Set<BluetoothDevice> devices) {
        ArrayList<String> deviceNames = new ArrayList<String>();

        if (devices == null){
            Log.e(TAG, "stringifyBtDevices received null device set.");
            return deviceNames;
        }

        if (devices.size() > 0){
            for(BluetoothDevice device : devices){
                deviceNames.add(device.getName());
            }
        }

        return deviceNames;
    }
}

