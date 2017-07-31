package com.mobilis.bt_tabletgauge;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.mobilis.bt_tabletgauge.bo.AccelGauge;
import com.mobilis.bt_tabletgauge.ui.widgets.gauges.MultiColoredScaleGauge;

public class MainActivity extends AppCompatActivity {

    public AccelGauge mAccelGauge;
    public MultiColoredScaleGauge mGauge;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = this;
        mGauge = (MultiColoredScaleGauge) findViewById(R.id.gauge);
        mGauge.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                mGauge.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mAccelGauge = new AccelGauge(mGauge, mContext);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAccelGauge != null) {
            mAccelGauge.onAppDestroy();
            mAccelGauge = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
