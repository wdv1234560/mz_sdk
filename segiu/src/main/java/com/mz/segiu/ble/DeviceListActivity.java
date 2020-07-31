package com.mz.segiu.ble;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.mvp.IPresenter;
import com.jess.arms.utils.ArmsUtils;
import com.mz.segiu.R;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceListActivity extends BaseActivity<IPresenter> {
    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    ListView DeviceListView;
    private final static String TAG = DeviceListActivity.class.getSimpleName();

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;


    private boolean mScanning;
    private Handler mHandler;

    public int BLUETOOTH_CONNECT_CODE = 1111;

    private Runnable runnable;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 3000;
    private int mConnectType;

    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {

    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.dialog_bluetooth_list;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mConnectType = getIntent().getIntExtra("connectType", 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 检查设备上是否支持蓝牙
        if (mBluetoothAdapter == null) {
            ArmsUtils.makeText(this,getString(R.string.error_bluetooth_not_supported));
            finish();
            return;
        }
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }

        mHandler = new Handler();
//        final BluetoothManager bluetoothManager =
//                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent intent = new Intent();
//            setResult(0, intent);
//            finish(); //开启蓝牙
//            mBluetoothAdapter.enable();
//            return;
//        }
        DeviceListView = findViewById(R.id.lvPairedDevices);
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            ArmsUtils.makeText(this,getString(R.string.error_bluetooth_not_supported));
            finish();
            return;
        }
    }

    @Override
    public void initListener(@Nullable Bundle savedInstanceState) {
        DeviceListView.setOnItemClickListener(new OnItemClickListener() {


            @SuppressWarnings("deprecation")
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mHandler.removeCallbacks(runnable);
                    mScanning = false;
                }
                if (mConnectType == Constant.CONNECT_TYPE_SJ) {
                    if (device.getName().contains("蓝牙秤")) {
                        resBleInfo(device);
                    } else {
                        ArmsUtils.makeText(DeviceListActivity.this,getString(R.string.please_select_triangle_scale));
                    }
                } else if (mConnectType == Constant.CONNECT_TYPE_TH) {
                    if (device.getName().contains("sztscale")) {
                        resBleInfo(device);
                    } else {
                        ArmsUtils.makeText(DeviceListActivity.this,getString(R.string.please_select_th_scale));
                    }

                } else {
                    ArmsUtils.makeText(DeviceListActivity.this,getString(R.string.invalid_device));
                }

            }

        });
    }

    private void resBleInfo(BluetoothDevice device) {
        // Create the result Intent and include the MAC address
        Intent intent = new Intent();
        intent.putExtra("device_address", device.getAddress());
        intent.putExtra("device_Name", device.getName());
        // Set result and finish this Activity
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success

                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initializes list view adapter.
        if (mBluetoothAdapter.isEnabled()) {
            mLeDeviceListAdapter = new LeDeviceListAdapter(this);
            DeviceListView.setAdapter(mLeDeviceListAdapter);
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        if (null != mLeDeviceListAdapter) {
            mLeDeviceListAdapter.clear();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(runnable = new Runnable() {
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {    //scanRecord为广播数据，可以根据自己需要进行判断是否连接
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    public class LeDeviceListAdapter extends BaseAdapter {

        // Adapter for holding devices found through scanning.

        private ArrayList<BluetoothDevice> mLeDevices;
        int count = 0;
        private LayoutInflater mInflator;
        private Activity mContext;

        public LeDeviceListAdapter(Activity c) {
            super();
            mContext = c;
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = mContext.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            System.out.println(device.getName());
            if (device!=null&&device.getName()!=null&&(device.getName().contains("蓝牙秤") || device.getName().contains("sztscale"))) {

            }
                if (!mLeDevices.contains(device)) {
                    mLeDevices.add(device);
                }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        public int getCount() {
            return mLeDevices.size();
        }

        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        public long getItemId(int i) {
            return i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.item_ble_scale, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view
                        .findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view
                        .findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }

        class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
        }
    }
}
