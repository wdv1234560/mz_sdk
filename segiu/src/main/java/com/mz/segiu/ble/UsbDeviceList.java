package com.mz.segiu.ble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by Administrator
 *
 * @author 猿史森林
 * Date: 2017/11/3
 * Class description:
 */
public class UsbDeviceList extends Activity {
    /**
     * Debugging
     */
    private static final String DEBUG_TAG = "DeviceListActivity";
    public static final String USB_NAME = "usb_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUsbDeviceList();
    }

    boolean checkUsbDevicePidVid(UsbDevice dev) {
        int pid = dev.getProductId();
        int vid = dev.getVendorId();
        return ((vid == 34918 && pid == 256) || (vid == 1137 && pid == 85)
                || (vid == 6790 && pid == 30084)
                || (vid == 26728 && pid == 256) || (vid == 26728 && pid == 512)
                || (vid == 26728 && pid == 256) || (vid == 26728 && pid == 768)
                || (vid == 26728 && pid == 1024) || (vid == 26728 && pid == 1280)
                || (vid == 26728 && pid == 1536));
    }

    public void getUsbDeviceList() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        // Get the list of attached devices
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = devices.values().iterator();
        int count = devices.size();
        Log.d(DEBUG_TAG, "count " + count);
        Intent intent = new Intent();
        if (count > 0) {
            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                String devicename = device.getDeviceName();
                if (checkUsbDevicePidVid(device)) {
                    intent.putExtra(USB_NAME, devicename);
                    break;
                }
            }
        } else {
//            String noDevices = getResources().getText(R.string.none_usb_device)
//                    .toString();
//            ArmsUtils.makeTextSafely(noDevices);
            intent.putExtra(USB_NAME, "0");
        }
        setResult(RESULT_OK, intent);
        finish();
    }

}
