package com.example.charlie.test;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements ItemListFragment.OnListFragmentInteractionListener{
    // Class variables
    Toolbar toolbar;
    TextView tv;
    FloatingActionButton fab;
    ImageButton refreshButton, viewButton, shareButton, downloadButton;
    LineChart chart;
    ArrayList<String> localFileNames = new ArrayList<String>();
    BluetoothInterface btInterface;
    Thread btThread;
    Runnable btThreadRunnable;
    FileNamesPagerAdapter mFileNamesPagerAdapter;
    ViewPager mViewPager;

    // Constants
    private static final String TAG = "MainActivity";
    int THREAD_JOIN_TIMEOUT_MS = 10;
    int CONNECT_TO_BT_DEVICE = 1354;
    int CHOOSE_DOWNLOAD_FILE = 56498;
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1920;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set view and find various UI elements
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.status_text);

        // Register for broadcasts from Bluetooth Interface
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothInterface.ACTION_LISTENER_STOPPED);
        filter.addAction(BluetoothInterface.ACTION_FILE_DOWNLOAD_COMPLETE);
        filter.addAction(BluetoothActivity.ACTION_BLUETOOTH_SOCKET_CONNECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(btInterfaceReceiver, filter);

        // Request any necessary permissions from user
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        // Configure toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Set ultra dope title
        if (toolbar != null) {
            toolbar.setTitle("Swagger Central");
        }

        // View pager setup
        mFileNamesPagerAdapter = new FileNamesPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.files_view_pager);
        mViewPager.setAdapter(mFileNamesPagerAdapter);
        refreshLocalFileNames(); 

        // Find and configure refresh button
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshLocalFileNames();
            }
        });

        // Find and configure FAB click event
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
//            View.OnClickListener snackbarOnClick = new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    startBluetoothActivity();
//                }
//            };

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivityForResult(intent, CONNECT_TO_BT_DEVICE);

//                Snackbar.make(view, "Find and connect to device", Snackbar.LENGTH_LONG)
//                        .setAction("Bluetooth", snackbarOnClick).show();
            }
        });

        // Find and configure view file button
        viewButton = (ImageButton) findViewById(R.id.view_button);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: This button should tell activity which file user selected (or files if multiple)
                Intent intent = new Intent(MainActivity.this, DataSelectionActivity.class);
                startActivity(intent);
            }
        });

        // Find and configure download file button
        downloadButton = (ImageButton) findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadFileActivity.class);
                startActivityForResult(intent, CHOOSE_DOWNLOAD_FILE);
            }
        });

        // Find and configure share button
        shareButton = (ImageButton) findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Share button should share things
            }
        });

        // Setup bluetooth interface and thread for running listener loop
        btInterface = BluetoothInterface.getInstance();
        btThreadRunnable = new Runnable() {
            public void run() {
                btInterface.listen();
            }
        };
    }

//    @Override
//    protected void onCreate() {
//        // Register for broadcasts from Bluetooth Interface
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothInterface.ACTION_LISTENER_STOPPED);
//        filter.addAction(BluetoothInterface.ACTION_FILE_DOWNLOAD_COMPLETE);
//        filter.addAction(BluetoothActivity.ACTION_BLUETOOTH_SOCKET_CONNECTED);
//        LocalBroadcastManager.getInstance(this).registerReceiver(btInterfaceReceiver, filter);
//
//        super.onCreate();
//    }

    @Override
    protected void onDestroy() {
        // Unregister for broadcasts from Bluetooth Interface
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btInterfaceReceiver);

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONNECT_TO_BT_DEVICE) {
            if (resultCode == RESULT_OK) {
                // BT Enable succeeded. Start bluetooth thread
                String tempStr = "Bluetooth connected.";
                Log.i(TAG, tempStr);
                tv.setText(tempStr);
            } else if (resultCode == RESULT_CANCELED) {
                // BT Enable failed
                // FIXME: This could also be the user exiting
                String tempStr = "Failed to connect Bluetooth.";
                Log.w(TAG, tempStr);
                tv.setText(tempStr);
            } else {
                String tempStr = "Unknown result code when connecting Bluetooth.";
                Log.e(TAG, tempStr);
                tv.setText(tempStr);
            }
        }

        if (requestCode == CHOOSE_DOWNLOAD_FILE) {
            if (resultCode == RESULT_OK) {
                String filename = data.getStringExtra("filename");
                Log.i(TAG, "User selected file for download: ".concat(filename));
                btInterface.requestFileContent(filename);
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Choose download file activity was canceled.");
            } else {
                String tempStr = "Unknown result code when selecting a file for download.";
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

    // BroadcastReceiver for result Intents from bluetooth interface class
    private final BroadcastReceiver btInterfaceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothInterface.ACTION_LISTENER_STOPPED.equals(action)) {
                Log.w(TAG, "Bluetooth listener shutdown");
                try {
                    btThread.join(THREAD_JOIN_TIMEOUT_MS);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            if (BluetoothInterface.ACTION_FILE_DOWNLOAD_COMPLETE.equals(action)) {
                refreshLocalFileNames();
            }

            if (BluetoothActivity.ACTION_BLUETOOTH_SOCKET_CONNECTED.equals(action)) {
                // BT Enable succeeded. Start bluetooth thread
                String tempStr = "Bluetooth connected.";
                Log.i(TAG, tempStr);
                tv.setText(tempStr);
                btThread = new Thread(btThreadRunnable);
                btThread.start();
                // TODO: Does android require any special thread cleanup? And when exactly is this thread destroyed?
            }
        }
    };

    private void refreshLocalFileNames() {
        File[] files;
        File dir;

        // Find directory where app data is stored
        try {
            dir = MyApplication.get_instance().getExternalFilesDir(null);
            if (dir == null) {
                Log.e(TAG, "getExternalFilesDir returned null when refreshing local file names.");
                return;
            }
            files = dir.listFiles();
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        // Add all non-directory files to file names list
        this.localFileNames.clear();
        for (File file : files) {
            if (!file.isDirectory()) {
                this.localFileNames.add(file.getName());
            }
        }

        // Refresh recycler view
        mFileNamesPagerAdapter.notifyDataSetChanged();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void onListFragmentInteraction(Integer position, String filename){
        if (filename.toLowerCase().endsWith(".dat")) {
            Intent intent = new Intent(MainActivity.this, DataSelectionActivity.class);
            intent.putExtra("filename", filename);
            startActivity(intent);
        }
        /* TODO: Write handler for when other local file type is selected */
    }

    public class FileNamesPagerAdapter extends FragmentPagerAdapter {
        ArrayList<ItemListFragment> listFragments;
        ArrayList<String> pageTitles;
        private final int COLUMN_COUNT = 1;

        public FileNamesPagerAdapter(FragmentManager fm) {
            super(fm);

            listFragments = new ArrayList<ItemListFragment>();
            listFragments.add(ItemListFragment.newInstance(COLUMN_COUNT, localFileNames));

            pageTitles = new ArrayList<String>();
            pageTitles.add("Local Files");
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
