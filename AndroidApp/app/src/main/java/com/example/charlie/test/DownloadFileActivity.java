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
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class DownloadFileActivity extends AppCompatActivity
        implements ItemListFragment.OnListFragmentInteractionListener{
    // Class variables
    Toolbar toolbar;
    TextView tv;
    FloatingActionButton fab;
    ImageButton refreshButton;
    ArrayList<String> dataFileNames = new ArrayList<String>();
    ArrayList<String> logFileNames = new ArrayList<String>();
    ArrayList<String> pyLogFileNames = new ArrayList<String>();
    BluetoothInterface btInterface;
    FileNamesPagerAdapter mFileNamesPagerAdapter;
    ViewPager mViewPager;
    Intent ret_intent = new Intent();

    // Constants
    int CONNECT_TO_BT_DEVICE = 1354;
    private static final String TAG = "DownloadFileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);

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

        // Find and configure FAB click event
        fab = (FloatingActionButton) findViewById(R.id.bt_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBluetoothActivity();
            }
        });

        // Setup bluetooth interface and thread
        btInterface = BluetoothInterface.getInstance();
    }

    @Override
    protected void onResume() {
        // Register for broadcasts from Bluetooth Interface
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothInterface.ACTION_FILENAMES_UPDATED);
        filter.addAction(BluetoothInterface.ACTION_LISTENER_STOPPED);
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
        Intent intent = new Intent(DownloadFileActivity.this, BluetoothActivity.class);
        startActivityForResult(intent, CONNECT_TO_BT_DEVICE);
    }

    // BroadcastReceiver for Intents from bluetooth interface class
    private final BroadcastReceiver btInterfaceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothInterface.ACTION_FILENAMES_UPDATED.equals(action)) {
                Log.i(TAG, "Got filenames update");
                // Filenames have been refreshed. Update and display.
                dataFileNames.clear();
                logFileNames.clear();
                pyLogFileNames.clear();
                btInterface.getDataFileNames(dataFileNames);
                btInterface.getLogFileNames(logFileNames);
                btInterface.getPyLogFileNames(pyLogFileNames);
                mFileNamesPagerAdapter.notifyDataSetChanged();
            }

            if (BluetoothInterface.ACTION_LISTENER_STOPPED.equals(action)) {
                // Return FAILURE intent to calling activity
                // TODO: Should notify user that bluetooth connection was lost
                Log.w(TAG, "Bluetooth listener shutdown");
                DownloadFileActivity.this.setResult(RESULT_CANCELED, ret_intent);
                finish();
            }
        }
    };

    public void onListFragmentInteraction(String filename){
        /* Return filename to calling activity as intent */
        String tempStr = "User selected filename: ".concat(filename);
        Log.i(TAG, tempStr);
        ret_intent.putExtra("filename", filename);
        setResult(RESULT_OK, ret_intent);
        finish();
    }

    public class FileNamesPagerAdapter extends FragmentPagerAdapter {
        ArrayList<ItemListFragment> listFragments;
        ArrayList<String> pageTitles;
        private final int COLUMN_COUNT = 1;

        public FileNamesPagerAdapter(FragmentManager fm) {
            super(fm);

            listFragments = new ArrayList<ItemListFragment>();
            listFragments.add(ItemListFragment.newInstance(COLUMN_COUNT, dataFileNames));
            listFragments.add(ItemListFragment.newInstance(COLUMN_COUNT, logFileNames));
            listFragments.add(ItemListFragment.newInstance(COLUMN_COUNT, pyLogFileNames));

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
