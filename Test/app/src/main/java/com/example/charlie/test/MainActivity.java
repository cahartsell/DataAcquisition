package com.example.charlie.test;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Class variables
    Toolbar toolbar;
    TextView tv;
    FloatingActionButton fab;
    TextInputEditText inputMsg;
    Button sendButton;
    SingletonBluetoothData sBTData = SingletonBluetoothData.getInstance();
    LineChart chart;

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

        inputMsg = (TextInputEditText) findViewById(R.id.inputMsg);

        // Testing chart functinality
        chart = (LineChart) findViewById(R.id.chart);
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(0,0));
        entries.add(new Entry(1,1));
        entries.add(new Entry(2,4));
        entries.add(new Entry(4,4));
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLUE);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();

        if(toolbar != null) {
            // Dope title
            toolbar.setTitle("Swagger Central");
        }

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View view){
                if (sBTData.socketConnected()){
                    OutputStream outStream = sBTData.getBtOutStream();
                    String inputStr = inputMsg.getText().toString();
                    try {
                        outStream.write(inputStr.getBytes());
                    } catch (IOException e){
                        tv.setText("Failed sending message.");
                        Log.w(TAG, "Failed sending input message.");
                    }
                }
            }
        });

        fab = (FloatingActionButton)  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            View.OnClickListener snackbarOnClick = new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    startBluetoothListActivity();
                }
            };

            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", snackbarOnClick).show();
            }
        });

        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        // Start List activity
        /*Intent intent = new Intent(this, BluetoothList.class);
        startActivityForResult(intent, CONNECT_TO_BT_DEVICE);*/
    }

    public void startBluetoothListActivity(){
        // Start List activity
        // FIXME: This don't work
        Intent intent = new Intent(this, BluetoothList.class);
        startActivityForResult(intent, CONNECT_TO_BT_DEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECT_TO_BT_DEVICE){
            if (requestCode == RESULT_OK){
                // BT Enable succeeded
                tv.setText("BT Enabled.");
            }
            else if (requestCode == RESULT_CANCELED){
                // BT Enable failed
                Log.w(TAG, "Failed to enable bluetooth.");
                tv.setText("Failed to enable BT.");
            }
            else{
                // Unknown result
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

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
