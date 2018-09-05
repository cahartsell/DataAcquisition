package com.example.charlie.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Class variables
    Toolbar toolbar;
    TextView tv;
    FloatingActionButton fab;
    TextInputEditText inputMsg;
    Button sendButton;
    LineChart chart;
    List<String> dataFileNames, logFileNames, pyLogFileNames;
    BluetoothInterface btInterface;
    Thread btThread;

    // Constants
    int CONNECT_TO_BT_DEVICE = 1354;
    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find UI elements
        inputMsg = (TextInputEditText) findViewById(R.id.inputMsg);
        tv = (TextView) findViewById(R.id.sample_text);

        // Set ultra dope title
        if(toolbar != null) {
            toolbar.setTitle("Swagger Central");
        }

        // Find and configure message send button
        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View view){
                if (!btInterface.connected()) {
                    String tempStr = "Bluetooth not connected!";
                    tv.setText(tempStr);
                } else {
                    btInterface.sendString( inputMsg.getText().toString() );
                }
            }
        });

        // Find and configure FAB click event
        fab = (FloatingActionButton)  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            View.OnClickListener snackbarOnClick = new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    startBluetoothActivity();
                }
            };

            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Bluetooth", snackbarOnClick).show();
            }
        });

        // Setup bluetooth interface and thread
        btInterface = new BluetoothInterface();
        btThread = new Thread( new Runnable() {
            public void run() {
               btInterface.listen();
            }
        });
    }

    @Override
    protected void onResume(){
        // Register for broadcasts from Bluetooth Interface
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothInterface.ACTION_FILENAMES_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(btInterfaceReceiver, filter);

        super.onResume();
    }

    @Override
    protected void onPause(){
        // Unregister for broadcasts from Bluetooth Interface
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btInterfaceReceiver);

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECT_TO_BT_DEVICE){
            if (resultCode == RESULT_OK){
                // BT Enable succeeded
                String tempStr = "Bluetooth connected.";
                Log.i(TAG, tempStr);
                tv.setText(tempStr);
                btThread.start();
                // TODO: Recv file names
            }
            else if (resultCode == RESULT_CANCELED){
                // BT Enable failed
                String tempStr = "Failed to connect Bluetooth.";
                Log.w(TAG, tempStr);
                tv.setText(tempStr);
            }
            else{
                String tempStr = "Unknown result code when connecting Bluetooth.";
                Log.e(TAG, tempStr);
                tv.setText(tempStr);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Convenience function for starting BluetoothActivity
    public void startBluetoothActivity(){
        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
        startActivityForResult(intent, CONNECT_TO_BT_DEVICE);
    }

    // BroadcastReceiver for Intents from bluetooth interface class
    private final BroadcastReceiver btInterfaceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothInterface.ACTION_FILENAMES_UPDATED.equals(action)) {
                //******** DEBUG *************/
                Log.w(TAG, "GOT FILENAMES UPDATE");
                // Filenames have been refreshed. Update and display.
            }
        }
    };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

//    private boolean recvFileNames(){
//        if (sBTData.socketConnected()) {
//            try {
//                if (inStream.available() > 0) {
//                    byte[] buf = new byte[1024];
//                    int size;
//                    size = inStream.read(buf);
//                    buf[size] = '\n';
//                    String str = new String(buf, "UTF-8");
//                    Log.d(TAG, str);
//                    String temp = "GOT: " + str;
//                    tv.setText(temp);
//                    return true;
//                }
//                else{
//                    Log.w(TAG, "No data to read.");
//                    return false;
//                }
//            } catch (IOException e) {
//                tv.setText("Failed reading message.");
//                Log.e(TAG, "Failed reading BT message.");
//                return false;
//            }
//        } else {
//            tv.setText("NOPE");
//            return true;
//        }
//    }
}
