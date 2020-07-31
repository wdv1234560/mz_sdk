/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mz.segiu.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jess.arms.base.BaseApplication;
import com.jess.arms.utils.ArmsUtils;
import com.mz.segiu.ble.BleProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mz.segiu.api.js.JsCode;
import timber.log.Timber;

import static com.mz.segiu.ble.DeviceConnFactoryManager.ACTION_PRINTER_STATUS;
import static com.mz.segiu.ble.DeviceConnFactoryManager.PRINTER_STATUS;
import static com.mz.segiu.ble.DeviceConnFactoryManager.PRINTER_STATUS_MSG;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    public static boolean mIsConnected = false;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BleProtocol mBleProtocol = new BleProtocol();
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    /**
     * -------------连接成功-------------
     **/
    public final static String ACTION_GATT_CONNECTED =
            "jianxun.com.hrssipad.ACTION_GATT_CONNECTED";

    /**
     * -------------断开连接-------------
     **/
    public final static String ACTION_GATT_DISCONNECTED =
            "jianxun.com.hrssipad.ACTION_GATT_DISCONNECTED";

    /**
     * -------------没发现服务特征-------------
     **/
    public static final String ACTION_GATT_SERVICES_UNDISCOVERED =
            "jianxun.com.hrssipad.ACTION_GATT_SERVICES_UNDISCOVERED";

    /**
     * -------------发现服务特征-------------
     **/
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "jianxun.com.hrssipad.ACTION_GATT_SERVICES_DISCOVERED";

    /**
     * -------------接收到数据-------------
     **/
    public final static String ACTION_DATA_AVAILABLE =
            "jianxun.com.hrssipad.ACTION_DATA_AVAILABLE";

    /**
     * -------------收到的数据-------------
     **/
    public final static String EXTRA_DATA =
            "jianxun.com.hrssipad.EXTRA_DATA";

    public final static UUID UUID_NOTIFY =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    public BluetoothGattCharacteristic mGattCharacteristic;
    private String mBluetoothDeviceAddress;

    /**
     * @param str
     * @des 台衡打印，蓝牙写入
     */
    public void writeValue(String str) {
//        byte[] value = stringToBytes(str.replaceAll("\\\\r\\\\n", "\r\n"));
        Log.i(TAG, "BuletoothGatt=======" + mBluetoothGatt);
        str="\02"+str;//厂商规定，头加十六进制02
        str = str.replaceAll("\\\\r\\\\n", "\r\n");
        List<String> l = splitStr(str);
        try {
            for (String s : l
            ) {
                mGattCharacteristic.setValue(s.getBytes("GBK"));
                boolean b = mBluetoothGatt.writeCharacteristic(mGattCharacteristic);
                Timber.d("蓝牙写入writeCharacteristic：" + b);
                Thread.sleep(25);
            }

            mGattCharacteristic.setValue(str.getBytes("GBK"));
            boolean b = mBluetoothGatt.writeCharacteristic(mGattCharacteristic);
            Timber.d("蓝牙写入writeCharacteristic：" + b);
            sendPrintStatusBroadcast(JsCode.SEND_BLE_PRINT_DATA_SUCCESS.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            sendPrintStatusBroadcast(JsCode.SEND_BLE_PRINT_DATA_FAILED.getCode());
            Timber.e("转码识别");
        }
    }

    public static List<String> splitStr(String str) {
        int k = 20;
        List<String> list = new ArrayList<>();
        String temp = "";
        String b = "";
        for (int i = 0; i < str.length(); i++) {
            if (k > 0) {
                b = String.valueOf(str.charAt(i));
                temp += b;
                k = k - b.getBytes().length;
            } else {
                list.add(temp);
                temp = "";
                k = 20;
                i--;
            }

            if (i == str.length() - 1) {
                list.add(temp);
            }


        }
        return list;
    }

    /**
     * 发送广播
     *
     * @param status
     */
    private void sendPrintStatusBroadcast(int status) {
        Intent intent = new Intent(ACTION_PRINTER_STATUS);
        intent.putExtra(PRINTER_STATUS, status);
        intent.putExtra(PRINTER_STATUS_MSG, JsCode.getMsg(status));
        BaseApplication.getContext().sendBroadcast(intent);//此处若报空指针错误，需要在清单文件application标签里注册此类，参考demo
    }

    public static byte[] stringToBytes(String text) {
        int len = text.length();
        byte[] bytes = new byte[(len + 1) / 2];
        for (int i = 0; i < len; i += 2) {
            int size = Math.min(2, len - i);
            String sub = text.substring(i, i + size);
            bytes[i / 2] = (byte) Integer.parseInt(sub, 16);
        }
        return bytes;
    }

    //// TODO: 2017/8/1 蓝牙连接成功数
    public void findService(List<BluetoothGattService> gattServices) {
        Log.i(TAG, "Services Count is:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices) {
            Log.i(TAG, gattService.getUuid().toString());
            Log.i(TAG, UUID_SERVICE.toString());
            if (gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString())) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                Log.i(TAG, "Character Count is:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {
                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString())) {
                        Log.i(TAG, gattCharacteristic.getUuid().toString());
                        Log.i(TAG, UUID_NOTIFY.toString());
                        mGattCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(gattCharacteristic, true);
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                        //// TODO: 2017/8/1 蓝牙连接log打印
                        return;
                    }
                }
            }
        }

        Log.d(TAG, "找不到蓝牙服务特征，连接失败...");

    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.i(TAG, "oldStatus=" + status + " NewStates=" + newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "蓝牙已连接");
                    mIsConnected = true;
                    ArmsUtils.makeText("蓝牙已连接");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    //// TODO: 2017/7/28 正在断开连接
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    ArmsUtils.makeText("蓝牙断开连接");
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    mIsConnected = false;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                findService(gatt.getServices());
            } else {
                if (mBluetoothGatt.getDevice().getUuids() == null) {

                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
                //TODO 没有发现对应的服务

                broadcastUpdate(ACTION_GATT_SERVICES_UNDISCOVERED);

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            Log.e(TAG, "OnCharacteristicWrite");
            mBleProtocol.onCharacteristicWrite(gatt, characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                          int status) {
            Timber.d("OnCharacteristicWrite");

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor bd,
                                     int status) {
            Timber.e("onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor bd,
                                      int status) {
            Timber.e("onDescriptorWrite");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b) {
            Timber.e("onReadRemoteRssi");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int a) {
            Timber.e("onReliableWriteCompleted");
        }

    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            //final StringBuilder stringBuilder = new StringBuilder(data.length);
            //for(byte byteChar : data)
            //    stringBuilder.append(String.format("%02X ", byteChar));
            //intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            intent.putExtra(EXTRA_DATA, ByteUtils.byteToString(data));
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
    }


    public static boolean isEmpty(byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        try {

            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
        } catch (Exception e) {

        }

        //这里依旧是一个延时100ms的任务，是因为怕用户断开之后又很快连接时，BLE设备端没有很好的释放资源，所以这里延时一段时间，
        //给BLE充分释放资源的时间，这个时间也是可以根据具体需求情况调整
        SystemClock.sleep(300);
        mBluetoothGatt = device.connectGatt(BluetoothLeService.this, false, mGattCallback);

        // TODO: 2017/7/28  RECONNECT断开自动重连
        //这里最好 Sleep 300毫秒，测试发现有时候三星手机断线之后立马调用connect会容易蓝牙奔溃
//        mBluetoothGatt.connect();
        Log.w(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    private boolean checkIsSamsung() { //此方法是我自行使用众多三星手机总结出来，不一定很准确
        String brand = Build.BRAND;
        Log.e("", " brand:" + brand);
        if (brand.toLowerCase().equals("samsung")) {
            return true;
        }
        return false;
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        try {
            Thread.sleep(10L);
            mBluetoothGatt.disconnect();
        } catch (InterruptedException var5) {
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        try {
            if (mBluetoothGatt != null) {
                Thread.sleep(10L);
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
/*
        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        */
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
