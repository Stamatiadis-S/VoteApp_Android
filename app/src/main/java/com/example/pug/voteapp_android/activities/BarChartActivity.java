package com.example.pug.voteapp_android.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.pug.voteapp_android.models.Option;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.R;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BarChartActivity extends AppCompatActivity {

    final int[] BAR_CHART_COLOR_PALETTE = {
            ColorTemplate.rgb("#ef5350"), ColorTemplate.rgb("#5c6bc0"), ColorTemplate.rgb("#26a69a"), ColorTemplate.rgb("#ffee58"),
            ColorTemplate.rgb("#8d6e63"), ColorTemplate.rgb("#ec407a"), ColorTemplate.rgb("#42a5f5"), ColorTemplate.rgb("#66bb6a"),
            ColorTemplate.rgb("#ffca28"), ColorTemplate.rgb("#bdbdbd"), ColorTemplate.rgb("#ab47bc"), ColorTemplate.rgb("#29b6f6"),
            ColorTemplate.rgb("#9ccc65"), ColorTemplate.rgb("#ffa726"), ColorTemplate.rgb("#78909c"), ColorTemplate.rgb("#7e57c2"),
            ColorTemplate.rgb("#26c6da"), ColorTemplate.rgb("#d4e157"), ColorTemplate.rgb("#ff7043") };

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private HorizontalBarChart barChart;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            barChart.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private Poll poll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bar_chart);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        barChart = (HorizontalBarChart) findViewById(R.id.bar_chart);

        // Set up the user interaction to manually show or hide the system UI.
        barChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        //Unpack poll data.
        Intent intent = getIntent();
        poll = intent.getParcelableExtra("poll");
        //Create a data set and render the pie chart.
        setBarData(poll.getOptions().size());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        barChart.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void setBarData(int count) {
        List<Option> options = poll.getOptions();
        List<BarEntry> yValues = new ArrayList<>();
        float barWidth = 3f;
        float spaceForBar = 4f;

        for (int i = 0; i < count; i++) {
            if(options.get(i).getVotes() > 0)
                yValues.add(new BarEntry((spaceForBar * i), options.get(i).getVotes()));
        }

        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(false);

        YAxis topAxis = barChart.getAxisLeft();
        topAxis.setAxisMinimum(0);
        topAxis.setEnabled(false);

        YAxis bottomAxis = barChart.getAxisRight();
        bottomAxis.setAxisMinimum(0);
        bottomAxis.setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setTextSize(16f);
        legend.setWordWrapEnabled(true);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

        //Prepare data set.
        BarDataSet barDataSet = new BarDataSet(yValues, "");
        barDataSet.setValueTextSize(16f);
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setColors(BAR_CHART_COLOR_PALETTE);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        //Configure horizontal bar chart view.
        BarData barData = new BarData(dataSets);
        barData.setDrawValues(true);
        barData.setValueTextSize(16f);
        barData.setValueTypeface(Typeface.DEFAULT_BOLD);
        barData.setBarWidth(barWidth);
        barData.setValueFormatter(new DefaultValueFormatter(0));
        barChart.setDrawBorders(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawGridBackground(false);
        barChart.setHighlightPerTapEnabled(false);
        barChart.setHighlightPerDragEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setData(barData);

        //Refresh bar chart and animate.
        barChart.invalidate();
        barChart.animateY(3000);
    }
}
