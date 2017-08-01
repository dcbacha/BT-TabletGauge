package com.mobilis.bt_tabletgauge;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobilis.bt_tabletgauge.bluetooth.BluetoothChatService;
import com.mobilis.bt_tabletgauge.bluetooth.Constants;
import com.mobilis.bt_tabletgauge.bluetooth.DeviceListActivity;
import com.mobilis.bt_tabletgauge.bo.AccelGauge;
import com.mobilis.bt_tabletgauge.ui.widgets.gauges.MultiColoredScaleGauge;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public AccelGauge mAccelGauge;
    public MultiColoredScaleGauge mGauge;
    private Context mContext;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_SEARCH_BT = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private static final int CONECTADO = 10;
    private static final int DESCONECTADO = 11;

    private BluetoothAdapter mBluetoothAdapter = null;
    private StringBuffer mOutStringBuffer;
    private BluetoothChatService mChatService = null;
    private ProgressDialog progress = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /** *************************************** **/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mChatService = new BluetoothChatService(this, mHandler);

        mOutStringBuffer = new StringBuffer("");



        /** *************************************** **/

        ImageView red_button = (ImageView) findViewById(R.id.red_button);
        ImageView blue_button = (ImageView) findViewById(R.id.blue_button);



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

        searchBT();


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

    /** ******************** Bluetooth ***************************/
    public void searchBT(){
        // Log.i(TAG, "searchBT()");
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_SEARCH_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        progress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
        if (resultCode != 0) {
            if (requestCode == REQUEST_SEARCH_BT) {

                connectDevice(data, true);
                progress.setTitle(R.string.connecting);
                progress.setMessage(getResources().getText(R.string.waiting).toString());
                progress.setCancelable(true);
                progress.show();
            } else if (requestCode == REQUEST_CONNECT_DEVICE_SECURE) {

                //sendMessage("asd");
                //setConnectedStatus(CONECTADO);
                progress.setTitle(R.string.configuring);
                progress.setMessage(getResources().getText(R.string.waiting).toString());
                progress.show();

            }
        }
    }

    private void sendMessage(String message) {
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);
        }
    }

    private final Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            progress.dismiss();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            progress.show();
                            break;
                        case BluetoothChatService.STATE_NONE:
                            progress.dismiss();
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // Log.i(TAG, String.valueOf(readMessage));

                    processData(readMessage);
                    break;
            }
        }
    };

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    private void processData(String mensagem){
        String[] gauge = mensagem.split("\n");

        String bat = gauge[0];
        String v = gauge[1];
        String i = gauge[2];

        float value_bat = Float.parseFloat(bat.split(":")[1]);
        float value_v = Float.parseFloat(v.split(":")[1]);
        float value_i = Float.parseFloat(i.split(":")[1]);

        /*Log.i("BAT", String.valueOf(value_bat));
        Log.i("VELO", String.valueOf(value_v));
        Log.i("CORR", String.valueOf(value_i));
*/
        AccelGauge.updateGaugeBattery(value_bat);
        AccelGauge.updateGaugeSpeed(value_v);
        AccelGauge.updateGaugeCurrent(value_i);

    }


}
