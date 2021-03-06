package com.mobilis.bt_tabletgauge.bo;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.*;

import com.mobilis.bt_tabletgauge.R;
import com.mobilis.bt_tabletgauge.ui.widgets.gauges.IBaseGpsListener;
import com.mobilis.bt_tabletgauge.ui.widgets.gauges.IGauge;
import com.mobilis.bt_tabletgauge.ui.widgets.gauges.IGaugeUI;



public class AccelGauge implements IGauge {

    private static final int GAUGE_MODE_DEMO = 1;
    private static final int GAUGE_MODE_BLUETOOTH = 0;

    private static final int gSpeed = 1;
    private static final int gBattery = 2;
    private static final int gCurrent = 3;

    private static float OFFSET;

    private static final int MAX_GAUGE_VALUE = 50;
    private static final int MAX_GAUGE_VALUE_BATTERY = 100;
    private static final int MAX_GAUGE_VALUE_CURRENT = 300;
    private static final int MIN_GAUGE_VALUE_CURRENT = -200;

    private float nCORRENTE = 0;

    private static IGaugeUI mIGaugeUI;
    private Context mContext;

    private float[] mSpeedValues = new float[2];

    private GaugeTimerTask mGaugeTimerTask;
    private Timer mTimerUpdate;
    private int mGaugeMode;

    private float mDemoSpeedValue;
    private boolean mDemoDecrement;
    private boolean mDemoCurrentDecrement;


    public AccelGauge(IGaugeUI iGaugeUI, Context context) {
        mIGaugeUI = iGaugeUI;
        mContext = context;
        init();
    }

    private void init() {
        mGaugeMode = GAUGE_MODE_BLUETOOTH; //GAUGE_MODE_ADDOFFSET;


        mIGaugeUI.setIGauge(this);
        setDisplayMode();
        startTimerTask();

        mSpeedValues[0] = 0;
        mSpeedValues[1] = 0;
    }

    @Override
    public void onClick() {
        mGaugeMode = mGaugeMode == GAUGE_MODE_DEMO ? GAUGE_MODE_BLUETOOTH : GAUGE_MODE_DEMO;
        setDisplayMode();
    }

    private void setDisplayMode() {
        mIGaugeUI.set7SegmentLabelBattery("%");
        mIGaugeUI.set7SegmentLabelCurrent("A");
        mIGaugeUI.set7SegmentLabelSpeed(mContext.getResources().getString(R.string.m_per_second));

        switch (mGaugeMode) {


            case GAUGE_MODE_BLUETOOTH:
                mIGaugeUI.setMajorLabel(mContext.getResources().getString(R.string.bt));
                break;

            case GAUGE_MODE_DEMO:
                mIGaugeUI.setMajorLabel(mContext.getResources().getString(R.string.demo));
                break;
        }
        updateGauge();
    }

    public static void updateGaugeSpeed(float value){
      //  Log.i("UpdateGaugeSpeed", String.valueOf(value));
        updateDisplayValue(value, gSpeed);
        setGaugePointerValue(value, gSpeed);
    }

    public static void updateGaugeBattery(float value){
        setGaugePointerValue(value, gBattery);
        updateDisplayValue(value, gBattery);
    }

    public static void updateGaugeCurrent(float value){
        setGaugePointerValue(value, gCurrent);
        updateDisplayValue(value, gCurrent);
    }

    private static void updateDisplayValue(float value, int gauge) {
        DecimalFormat df;
        String text;

        switch (gauge) {
            case gSpeed:
                if (value < 1)  df = new DecimalFormat("0.0");
                else            df = new DecimalFormat("###.0");
                text = df.format(value);
                mIGaugeUI.set7SegmentSpeed(text);
                break;

            case gBattery:
                if (value < 1)  df = new DecimalFormat("0");
                else            df = new DecimalFormat("###");
                text = df.format(value);
                mIGaugeUI.set7SegmentBattery(text);
                break;

            case gCurrent:
                if (value < 1)  df = new DecimalFormat("0");
                else            df = new DecimalFormat("###");
                text = df.format(value);
                mIGaugeUI.set7SegmentCurrent(text);
                break;
        }
    }

    private static void setGaugePointerValue(float value, int gauge) {
        switch (gauge) {
            case gSpeed:
                if (value > MAX_GAUGE_VALUE) mIGaugeUI.setPointerSpeed(MAX_GAUGE_VALUE);
                else mIGaugeUI.setPointerSpeed(value);
                break;

            case gBattery:
                if (value > MAX_GAUGE_VALUE_BATTERY) mIGaugeUI.setPointerBattery(MAX_GAUGE_VALUE_BATTERY);
                else mIGaugeUI.setPointerBattery(value);
                break;

            case gCurrent:
                if (value > 0 && value > MAX_GAUGE_VALUE_CURRENT) mIGaugeUI.setPointerCurrent(MAX_GAUGE_VALUE_CURRENT);
                else if(value < 0 && value < MIN_GAUGE_VALUE_CURRENT) mIGaugeUI.setPointerCurrent(MIN_GAUGE_VALUE_CURRENT);
                else if (value == 0) mIGaugeUI.setPointerCurrent(value);
                else mIGaugeUI.setPointerCurrent(value);
                break;
        }
    }

    private void updateGauge() {

        switch (mGaugeMode) {

            case GAUGE_MODE_BLUETOOTH:
                updateGaugeBluetooth();
                break;

            case GAUGE_MODE_DEMO:
                if (!mDemoDecrement && (mDemoSpeedValue >= MAX_GAUGE_VALUE))
                    mDemoDecrement = true;
                else if (mDemoDecrement && (mDemoSpeedValue <= 0))
                    mDemoDecrement = false;

                if (mDemoDecrement)
                    mDemoSpeedValue = mDemoSpeedValue - (50f / 300f);
                else
                    mDemoSpeedValue = mDemoSpeedValue + (50f / 300f);

             //   updateGaugeSpeed(mDemoSpeedValue);

               /* if (!mDemoCurrentDecrement && (nCORRENTE >= MAX_GAUGE_VALUE_CURRENT))
                    mDemoCurrentDecrement = true;
                else if (mDemoCurrentDecrement && (nCORRENTE <= MIN_GAUGE_VALUE_CURRENT))
                    mDemoCurrentDecrement = false;

                if(mDemoCurrentDecrement)
                    nCORRENTE = nCORRENTE - (100f / 150f);
                else
                    nCORRENTE = nCORRENTE + (100f / 150f);

               updateGaugeCurrent(nCORRENTE);
               */

               mSpeedValues[1] = mDemoSpeedValue;

              /*  Log.i("--- speed 1: ", String.valueOf(mSpeedValues[1]));
                Log.i("--- speed 0: ", String.valueOf(mSpeedValues[0]));
                Log.i("--- speed diff : ", String.valueOf(mSpeedValues[0] - mSpeedValues[1]));
                Log.i("-----", "-----");
*/
               if(mSpeedValues[0] < mSpeedValues[1]){
                    updateGaugeSpeed(mSpeedValues[0]+OFFSET);
                    updateGaugeCurrent(mSpeedValues[0]*6);
                    mSpeedValues[0] = mSpeedValues[0] + (50f/300f);
                } /*else if(Math.abs(mSpeedValues[1] - mSpeedValues[0]) < .5){
                    //updateGaugeSpeed(mDemoSpeedValue+OFFSET);
                   //updateGaugeCurrent(mDemoSpeedValue*-4);
                   // updateGaugeCurrent(0);
                    mSpeedValues[0] = mSpeedValues[1];
                } */else if (mSpeedValues[0] > mSpeedValues[1]){
                    updateGaugeSpeed(mSpeedValues[0]+OFFSET);
                    updateGaugeCurrent(mSpeedValues[0]*-4);
                    mSpeedValues[0] = mSpeedValues[0] - (50f/300f);
                }

                if(Math.abs(mSpeedValues[1] - mSpeedValues[0]) < .5)
                    mSpeedValues[0] = mSpeedValues[1];


                break;
        }
    }


    private void updateGaugeBluetooth(){

    }


    /** *********************** Funções Gauge UI ***************************/
    private void startTimerTask() {
        mGaugeTimerTask = new GaugeTimerTask();
        mTimerUpdate = new Timer();
        int SENSOR_READ_RATE = 200;
        mTimerUpdate.scheduleAtFixedRate(mGaugeTimerTask, 0, SENSOR_READ_RATE);
    }

    private class GaugeTimerTask extends TimerTask {
        @Override
        public void run() {
            updateGauge();
        }
    }

    @Override
    public void onAppDestroy() {
        shutdown();
    }

    @Override
    public void onAppStop() {
    }

    @Override
    public void onAppPause() {
    }

    @Override
    public void onAppResume() {
    }

    private void shutdown() {
        mTimerUpdate.cancel();
    }
}
