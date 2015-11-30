package com.example.shubhankar.pillboxlogic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class ScanActivity extends AppCompatActivity {

    private static final String LOG_TAG = "PILL BOX scan";
    public BluetoothManager mBluetoothManager = null;
    protected BluetoothAdapter mBluetoothAdapter = null;
    public BluetoothGatt mBluetoothGatt;
    public ArrayList<BluetoothClass.Device> deviceList = new ArrayList<BluetoothClass.Device>();
    private String deviceAddr;
    Handler mHandler = new Handler();
    public ProgressBar scanning;
    public TextView scanresult;
    public Button action;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scanning = (ProgressBar) findViewById(R.id.progressBar);
        scanresult = (TextView) findViewById(R.id.scanresult);
        action = (Button) findViewById(R.id.action);

        mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mHandler.post(startScanning);


    }

    protected Runnable startScanning = new Runnable() {
        @Override
        public void run() {
            action.setText("STOP");
            scanning.setVisibility(View.VISIBLE);

            Log.d("Starting scan", "now");
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();

            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                scanLeDevice(true);
                mHandler.removeCallbacks(startScanning);
                mHandler.postDelayed(stopScanning, 15000);
            }

            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mHandler.removeCallbacks(startScanning);
                    mHandler.post(stopScanning);
                }
            });
        }
    };

    private Runnable stopScanning = new Runnable() {
        @Override
        public void run() {
            Log.d("Stopping scan", "now");
            Log.d("No. of devices found",""+BLEDeviceList.getCount());
            action.setText("RETRY");
            scanning.setVisibility(View.INVISIBLE);
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mHandler.removeCallbacks(stopScanning);
                scanLeDevice(false);
            }
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mHandler.removeCallbacks(stopScanning);
                    mHandler.postDelayed(startScanning, 100);
                    scanresult.setText("");
                }
            });
        }
    };

    public void scanLeDevice(final boolean enable) {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (enable) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("Device found", "" + device.getName());
            new AnotherParser().printScanRecord(scanRecord);
            mHandler.post(stopScanning);

            DeviceHolder deviceHolder = new DeviceHolder(device, scanRecord, rssi);
            runOnUiThread(new DeviceAddTask(deviceHolder));
        }
    };

    public DeviceList BLEDeviceList=new DeviceList();

    class DeviceAddTask implements Runnable {
        DeviceHolder deviceHolder;

        public DeviceAddTask(DeviceHolder deviceHolder) {
            this.deviceHolder = deviceHolder;
        }

        public void run() {
            BLEDeviceList.addDevice(deviceHolder);
            BLEDeviceList.notifyDataSetChanged();
        }
    }

    class DeviceHolder {
        BluetoothDevice device;
        byte[] additionalData;
        int rssi;

        public DeviceHolder(BluetoothDevice device, byte[] additionalData, int rssi) {
            this.device = device;
            this.additionalData = additionalData;
            this.rssi = rssi;
        }
    }

    private class DeviceList extends BaseAdapter {
        //    private class DeviceList {
        private ArrayList<BluetoothDevice> BLEDevices;
        private ArrayList<DeviceHolder> BLEHolders;

        public DeviceList() {
            super();
            BLEDevices = new ArrayList<BluetoothDevice>();
            BLEHolders = new ArrayList<DeviceHolder>();
        }

        public void addDevice(DeviceHolder deviceHolder) {
            if (!BLEDevices.contains(deviceHolder.device)) {
                BLEDevices.add(deviceHolder.device);
                BLEHolders.add(deviceHolder);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return BLEDevices.get(position);
        }

        public void clear() {
            BLEDevices.clear();
            BLEHolders.clear();
        }

        @Override
        public int getCount() {
            return BLEDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return BLEDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }


//    private void EvaluateData(BluetoothDevice device, int rssi, byte[] scanRecord) {
//        String deviceName = device.getName();
//        StringBuffer b = new StringBuffer();
//        int byteCtr = 0;
//        for (int i = 0; i < scanRecord.length; ++i) {
//            if (byteCtr > 0)
//                b.append(" ");
//            b.append(Integer.toHexString(((int) scanRecord[i]) & 0xFF));
//            ++byteCtr;
//            if (byteCtr == 8) {
//                Log.d(LOG_TAG, new String(b));
//                byteCtr = 0;
//                b = new StringBuffer();
//            }
//        }
//        ArrayList<AdElement> ads = AdParser.parseAdData(scanRecord);
//
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < ads.size(); ++i) {
//            AdElement e = ads.get(i);
//            if (i > 0)
//                sb.append(" ; ");
//            sb.append(e.toString());
//        }
//        String additionalData = new String(sb);
//        Log.d(LOG_TAG, "additionalData: " + additionalData);
//        scanresult.setText("" + additionalData);
//        mHandler.post(stopScanning);
//    }
}
