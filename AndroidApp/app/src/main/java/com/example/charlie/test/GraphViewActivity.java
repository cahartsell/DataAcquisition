package com.example.charlie.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class GraphViewActivity extends AppCompatActivity {
    Intent intent;
    String x_title, y_title;
    float[] x_data, y_data;

    String TAG = "GraphViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_view);

        // Get info from calling activity
        intent = getIntent();
        x_title = intent.getStringExtra("x_title");
        y_title = intent.getStringExtra("y_title");
        x_data = intent.getFloatArrayExtra("x_data");
        y_data = intent.getFloatArrayExtra("y_data");
        LineChart chart = (LineChart) findViewById(R.id.chart);

        // Logging
        Log.i(TAG, "Plotting Y-axis data set ".concat(y_title).concat(" vs X-axis data set ").concat(x_title));

        // Sanity checks on data
        if (x_data.length != y_data.length) {
            Log.e(TAG, "Provided X and Y data sets have different number of elements.");
            finish();
        }

        // Create entry list for graph
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < x_data.length; i++) {
            Log.d(TAG, "X_data: ".concat(Float.toString(x_data[i])).concat(" Y_data: ").concat(Float.toString(y_data[i])));
            entries.add(new Entry(x_data[i], y_data[i]));
        }

        // Display plot
        LineDataSet dataSet = new LineDataSet(entries, "Data");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }
}
