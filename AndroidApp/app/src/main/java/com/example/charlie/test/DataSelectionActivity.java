package com.example.charlie.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataSelectionActivity extends AppCompatActivity
        implements ItemListFragment.OnListFragmentInteractionListener{
    FileDataPagerAdapter mFileDataPagerAdapter;
    ViewPager mViewPager;
    Intent intent;
    Toolbar toolbar;
    String filename, x_data_name, y_data_name;
    Map<String, Integer> headerMap;
    List<CSVRecord> records;
    ArrayList<String> datasetNames;
    CSVParser csvParser;
    TextView statusText;

    // Constants
    private static final String TAG = "DataSelectionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_selection);

        // Misc. init
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        y_data_name = null;
        x_data_name = null;

        // Get info from calling activity
        intent = getIntent();
        filename = intent.getStringExtra("filename");

        // Load and parse datafile
        // FIXME: Does this need to be done async?
        File csvData = new File(getExternalFilesDir(null), filename);
        try {
            // Set CSV File to be parsed, character set file is encoded with, and format of the file
            csvParser = CSVParser.parse(csvData, StandardCharsets.US_ASCII, CSVFormat.DEFAULT.withHeader());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            finish();
        }
        headerMap = csvParser.getHeaderMap();
        try {
            // Get list of records
            records = csvParser.getRecords();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            finish();
        }


        // Read data set names into an ArrayList for compatability with Recycler View
        datasetNames = new ArrayList<String>();
        if (headerMap != null) {
            datasetNames.addAll(headerMap.keySet());
        } else {
            Log.e(TAG, "CSVParser returned null header map for datafile: ".concat(filename));
            finish();
        }

        // View pager setup
        mFileDataPagerAdapter = new DataSelectionActivity.FileDataPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.files_view_pager);
        mViewPager.setAdapter(mFileDataPagerAdapter);
        mFileDataPagerAdapter.notifyDataSetChanged();

        // Give the user some instructions
        statusText = (TextView) findViewById(R.id.status_text);
        statusText.setText("Select Y-axis data set");

//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void startGraphActivity() {
        Intent intent = new Intent(DataSelectionActivity.this, GraphViewActivity.class);
        intent.putExtra("x_title", this.x_data_name);
        intent.putExtra("y_title", this.y_data_name);
        intent.putExtra("x_data", this.getDataFromCSV(this.x_data_name));
        intent.putExtra("y_data", this.getDataFromCSV(this.y_data_name));
        startActivity(intent);
    }

    private float[] getDataFromCSV(String data_name) {
        int record_index = this.headerMap.get(data_name);
        float[] data = new float[this.records.size()];
        CSVRecord record;

        for (int i = 0; i < this.records.size(); i++) {
            record = this.records.get(i);
            data[i] = Float.parseFloat(record.get(record_index));
        }

        return data;
    }

    // User should select Y-axis data set followed by X-axis data set.
    // User can toggle select/deselect Y-axis data set by selecting the same item again
    public void onListFragmentInteraction(Integer position, String datasetName){
        if (y_data_name == null) {
            y_data_name = datasetName;
            statusText.setText("Select X-axis data set");
        } else if (datasetName.equals(y_data_name)) {
            y_data_name = null;
            statusText.setText("Select Y-axis data set");
        } else {
            x_data_name = datasetName;
            this.startGraphActivity();
        }
    }

    public class FileDataPagerAdapter extends FragmentPagerAdapter {
        ArrayList<ItemListFragment> listFragments;
        ArrayList<String> pageTitles;
        private final int COLUMN_COUNT = 1;


        public FileDataPagerAdapter(FragmentManager fm) {
            super(fm);

            listFragments = new ArrayList<ItemListFragment>();
            listFragments.add(ItemListFragment.newInstance(COLUMN_COUNT, datasetNames));

            pageTitles = new ArrayList<String>();
            pageTitles.add("Available data sets");
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
