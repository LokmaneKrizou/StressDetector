package com.choosemuse.example.libmuse.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.choosemuse.example.libmuse.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

// Created by Lokmane Krizou on 6/6/2018.
public class homeScreen extends Fragment{
    private final String TAG = "homeScreen";
    public static String ACTION_MUSE_CONNECTED = "ACTION_MUSE_CONNECTED";
    public static String ACTION_MUSE_DISCONNECTED = "ACTION_MUSE_DISCONNECTED";
    public static String ACTION_MUSE_ALPHA = "ACTION_MUSE_ALPHA";
    public static String ACTION_MUSE_WAVES = "ACTION_MUSE_WAVES";
    public static String ACTION_MUSE_BETHA = "ACTION_MUSE_BETHA";
    public static String ACTION_MUSE_GAMMA = "ACTION_MUSE_GAMMA";
    public static String ACTION_MUSE_THETA = "ACTION_MUSE_THETA";
    public static String EXTRA_DATA = "EXTRA_DATA";
    public double[] waves =new double[4];
    private SharedPreferences preferences;
    private LineChart mChart;
    private Thread thread;
    private boolean plotData = true;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        android.view.View rootView = inflater.inflate(R.layout.fragment_homescreen, container, false);
        Log.d("Start Fragment",TAG);
        mChart = (LineChart) rootView.findViewById(R.id.chart1);

        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        LineData data1 = new LineData();
        LineData data2 = new LineData();
        LineData data3 = new LineData();
        LineData data4 = new LineData();
        data1.setValueTextColor(Color.BLUE);

        // add empty data
        mChart.setData(data1);
        mChart.setData(data2);
        mChart.setData(data3);
        mChart.setData(data4);
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(1);
        leftAxis.setAxisMinimum(-1);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.getXAxis().setDrawGridLines(true);
        mChart.setDrawBorders(false);

        feedMultiple();

        preferences = getContext().getSharedPreferences("settings", MODE_PRIVATE);

        if (getSharedValue("action_type") == 0)
            showImageFeedback();

        IntentFilter Fillter = new IntentFilter();

        //intent filter that checks whether bluetooth state changed.
        Fillter.addAction(ACTION_MUSE_CONNECTED);
        //intent filter that checks new bluetooth devices.
        Fillter.addAction(ACTION_MUSE_DISCONNECTED);
        Fillter.addAction(ACTION_MUSE_ALPHA);
        Fillter.addAction(ACTION_MUSE_BETHA);
        Fillter.addAction(ACTION_MUSE_GAMMA);
        Fillter.addAction(ACTION_MUSE_THETA);
        Fillter.addAction(ACTION_MUSE_WAVES);

        getActivity().registerReceiver(mReceiver, Fillter);
        return rootView;
    }

// TODO add Graph view (MpChartAndroid)
// TODO add Stress level and animate the circle view(change the color according to the stress level)
// TODO start the feedback whenever there is problem with the stress

    void showImageFeedback() {

        AlertDialog.Builder alert_add = new AlertDialog.Builder(getContext());
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View view = factory.inflate(R.layout.image_dialog, null);
        view.findViewById(R.id.image_feedback).setBackgroundResource(R.drawable.meme_1);
        alert_add.setView(view);
        alert_add.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int clicked_id) {}
        });

        alert_add.show();
    }

    public Integer getSharedValue(String key) {
            return preferences.getInt(key, 0);
    }

    Drawable getRandomDrawable() {
        Field[] drawablesFields = com.choosemuse.example.libmuse.R.drawable.class.getFields();
        ArrayList<Drawable> drawables = new ArrayList<>();

        for (Field field : drawablesFields) {
            try {
                Log.i("LOG_TAG", "com.choosemuse.example.libmuse.R.drawable." + field.getName());
                drawables.add(getResources().getDrawable(field.getInt(null)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return drawables.get(new Random().nextInt(drawables.size() + 1));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(ACTION_MUSE_WAVES)){
                waves =intent.getDoubleArrayExtra(EXTRA_DATA);
                addEntry(waves);
                Log.i("receivedAlpha", String.valueOf(waves[1]));
            }
            }


    };

    private void addEntry(double[] event) {

        LineData data = mChart.getData();
        LineData data2 = mChart.getData();
        LineData data3 = mChart.getData();
        LineData data4 = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data2.getDataSetByIndex(0);
            ILineDataSet set3 = data.getDataSetByIndex(0);
            ILineDataSet set4 = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet(2);
                set2 = createSet(1);
                set3 = createSet(3);
                set4= createSet(4);
                data.addDataSet(set);
                data.addDataSet(set2);
                data.addDataSet(set3);
                data.addDataSet(set4);
            }

//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 80) + 10f), 0);
            data.addEntry(new Entry(set.getEntryCount(), (float)event[0]), 0);
            data.addEntry(new Entry(set2.getEntryCount(), (float)event[1]), 1);
            data.addEntry(new Entry(set3.getEntryCount(), (float)event[2]), 2);
            data.addEntry(new Entry(set4.getEntryCount(), (float)event[3]), 3);

            data.notifyDataChanged();
            data2.notifyDataChanged();
            data3.notifyDataChanged();
            data4.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(100);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

        }

    }

    private LineDataSet createSet(int color) {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(1f);
       if(color==1)
        set.setColor(Color.MAGENTA);
       else if(color==2){
           set.setColor(Color.BLUE);

       }
       else if(color==4){
           set.setColor(Color.GREEN);

       }else if(color==3){
           set.setColor(Color.YELLOW);

       }
       set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }


    @Override
    public void onResume() {
        super.onResume();
     }

    @Override
    public void onDestroy() {
        thread.interrupt();
        super.onDestroy();
    }
}
