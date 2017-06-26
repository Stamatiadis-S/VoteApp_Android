package com.example.pug.voteapp_android.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.pug.voteapp_android.models.Option;
import com.example.pug.voteapp_android.models.Poll;
import com.example.pug.voteapp_android.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PieChartActivity extends AppCompatActivity {

    final int[] PIE_CHART_COLOR_PALETTE = {
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
    private PieChart pieChart;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            pieChart.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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

        setContentView(R.layout.activity_pie_chart);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        pieChart = (PieChart) findViewById(R.id.pie_chart);

        // Set up the user interaction to manually show or hide the system UI.
        pieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        //Unpack poll data.
        Intent intent = getIntent();
        poll = intent.getParcelableExtra("poll");
        //Create a data set and render the pie chart.
        setPieData(poll.getOptions().size());
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
        pieChart.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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

    private void setPieData(int count) {
        List<Option> options = poll.getOptions();
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        int totalVotes = 0;

        //Count total votes.
        for (Option option : options)
            totalVotes += option.getVotes();

        //Add entries.
        if(totalVotes != 0) {
            for(int i = 0; i < count; i++)
                if(options.get(i).getVotes() > 0)
                    pieEntries.add(new PieEntry((options.get(i).getVotes()), options.get(i).getOption()));
        }

        //Create the data set.
        PieDataSet pieDataset = new PieDataSet(pieEntries, "");
        pieDataset.setSliceSpace(2f);

        //Add multiple colors.
        pieDataset.setColors(PIE_CHART_COLOR_PALETTE);

        PieData pieData = new PieData(pieDataset);
        pieData.setValueTextSize(16f);
        pieData.setValueFormatter(new DefaultValueFormatter(0));

        //Configure pie chart view.
        pieChart.setData(pieData);
        pieChart.setHoleRadius(4f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setTransparentCircleRadius(5f);
        pieChart.getDescription().setText("Total Votes: " + totalVotes);
        pieChart.getDescription().setTextSize(16f);
        pieChart.getDescription().setTextAlign(Paint.Align.RIGHT);
        pieChart.getDescription().setTypeface(Typeface.DEFAULT_BOLD);
        pieChart.getLegend().setTextSize(16f);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.getLegend().setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);

        //Refresh pie chart and animate.
        pieChart.invalidate();
        pieChart.animateY(3500 , Easing.EasingOption.EaseInOutExpo);
    }
}
