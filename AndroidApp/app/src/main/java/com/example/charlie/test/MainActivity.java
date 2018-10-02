package com.example.charlie.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener{
    // Class variables
    Toolbar toolbar;
    TextView tv;
    FloatingActionButton fab;
    Button sendButton;
    ImageButton refreshButton;
    LineChart chart;
    RecyclerView filesList;
    RecyclerView.LayoutManager listLayoutManager;
    RecyclerView.Adapter listAdapter;
    //private ArrayAdapter<String> filesListAdapter;
    ArrayList<String> dataFileNames = new ArrayList<String>();
    ArrayList<String> logFileNames = new ArrayList<String>();
    ArrayList<String> pyLogFileNames = new ArrayList<String>();
    BluetoothInterface btInterface;
    Thread btThread;

    FileNamesPagerAdapter mFileNamesPagerAdapter;
    ViewPager mViewPager;

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

        // Find various UI elements
        tv = (TextView) findViewById(R.id.status_text);

        // Configure toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set ultra dope title
        if (toolbar != null) {
            toolbar.setTitle("Swagger Central");
        }

        // View pager setup
        mFileNamesPagerAdapter = new FileNamesPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.files_view_pager);
        mViewPager.setAdapter(mFileNamesPagerAdapter);

        // Find and configure refresh button
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btInterface.connected()) {
                    String tempStr = "Bluetooth not connected!";
                    tv.setText(tempStr);
                } else {
                    btInterface.requestFilenames();
                }
            }
        });

        // Find and configure message send button
//        sendButton = (Button) findViewById(R.id.sendButton);
//        sendButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public  void onClick(View view){
//                if (!btInterface.connected()) {
//                    String tempStr = "Bluetooth not connected!";
//                    tv.setText(tempStr);
//                } else {
//                    btInterface.sendString( inputMsg.getText().toString() );
//                }
//            }
//        });

        // Find and configure FAB click event
        fab = (FloatingActionButton) findViewById(R.id.bt_fab);
        fab.setOnClickListener(new View.OnClickListener() {
//            View.OnClickListener snackbarOnClick = new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    startBluetoothActivity();
//                }
//            };

            @Override
            public void onClick(View view) {
                startBluetoothActivity();
//                Snackbar.make(view, "Find and connect to device", Snackbar.LENGTH_LONG)
//                        .setAction("Bluetooth", snackbarOnClick).show();
            }
        });

        // Setup bluetooth interface and thread
        btInterface = new BluetoothInterface();
        btThread = new Thread(new Runnable() {
            public void run() {
                btInterface.listen();
            }
        });
    }

    @Override
    protected void onResume() {
        // Register for broadcasts from Bluetooth Interface
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothInterface.ACTION_FILENAMES_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(btInterfaceReceiver, filter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        // Unregister for broadcasts from Bluetooth Interface
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btInterfaceReceiver);

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECT_TO_BT_DEVICE) {
            if (resultCode == RESULT_OK) {
                // BT Enable succeeded
                String tempStr = "Bluetooth connected.";
                Log.i(TAG, tempStr);
                tv.setText(tempStr);
                btThread.start();
                // TODO: Recv file names
            } else if (resultCode == RESULT_CANCELED) {
                // BT Enable failed
                String tempStr = "Failed to connect Bluetooth.";
                Log.w(TAG, tempStr);
                tv.setText(tempStr);
            } else {
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
            tv.setText("There are no settings you stupid idiot.");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Convenience function for starting BluetoothActivity
    public void startBluetoothActivity() {
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
                dataFileNames.clear();
                logFileNames.clear();
                pyLogFileNames.clear();
                btInterface.getDataFileNames(dataFileNames);
                btInterface.getLogFileNames(logFileNames);
                btInterface.getPyLogFileNames(pyLogFileNames);
                mFileNamesPagerAdapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void onListFragmentInteraction(String filename){
        /* TODO: Write code to handle when item is selected in fragment */
    }

    public class FileNamesPagerAdapter extends FragmentPagerAdapter {
        ArrayList<ItemFragment> listFragments;
        ArrayList<String> pageTitles;
        private final int COLUMN_COUNT = 1;

        public FileNamesPagerAdapter(FragmentManager fm) {
            super(fm);

            listFragments = new ArrayList<ItemFragment>();
            listFragments.add(ItemFragment.newInstance(COLUMN_COUNT, dataFileNames));
            listFragments.add(ItemFragment.newInstance(COLUMN_COUNT, logFileNames));
            listFragments.add(ItemFragment.newInstance(COLUMN_COUNT, pyLogFileNames));

            pageTitles = new ArrayList<String>();
            pageTitles.add("Data Files");
            pageTitles.add("Log Files");
            pageTitles.add("Python Log Files");
        }

        @Override
        public Fragment getItem(int i) {
            return listFragments.get(i);
        }

        @Override
        public int getCount() {
            return listFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            // Causes adapter to reload all Fragments when notifyDataSetChanged is called.
            // Seems inefficient, but it'll ride
            return POSITION_NONE;
        }
    }
}
