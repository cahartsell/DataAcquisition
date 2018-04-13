package com.example.charlie.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    // Class variables
    Toolbar toolbar;
    TextView tv;
    FloatingActionButton fab;

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

        // Dope ass title
        // FIXME: getSupportActionBar can return NULL
        getSupportActionBar().setTitle("Swagger Central");

        fab = (FloatingActionButton)  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            View.OnClickListener snackbarOnClick = new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    String tempStr = "Good jorb";
                    tv.setText(tempStr);
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
        Intent intent = new Intent(this, BluetoothList.class);
        startActivityForResult(intent, CONNECT_TO_BT_DEVICE);
    }

    public void startBluetoothListActivity(){
        // Start List activity
        // FIXME: This don't work
        Intent intent = new Intent(this, BluetoothList.class);
        startActivity(intent);
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
