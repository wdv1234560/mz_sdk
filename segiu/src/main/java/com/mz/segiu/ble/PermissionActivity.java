package com.mz.segiu.ble;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.example.sca100.scalerSDK;

import java.util.ArrayList;
import java.util.List;

import static com.mz.segiu.ble.Constant.ACTION_USB_PERMISSION;


//import com.example.weightscaler.scalerSDK;

public class PermissionActivity extends Activity {
  private scalerSDK scale;

  public static final int GET_RECODE_AUDIO = 1;
  public static String[] PERMISSION_AUDIO = {
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.CAMERA
  };
  List<String> mPermissionList = new ArrayList<>();

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    /* 申请USB授权 */
    UsbManager mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
    for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
      if (!mUsbManager.hasPermission(usbDevice)) {
        mUsbManager.requestPermission(usbDevice, mPermissionIntent);
      }
    }
    /* 蓝牙权限授权 */

//    if (null == scale) {
//      scale = new scalerSDK(this);
//    }
//    if (!scale.bleIsEnabled()) {
//      final BluetoothManager bluetoothManager =
//        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//      BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
//      if (!mBluetoothAdapter.isEnabled()) {
//        mBluetoothAdapter.enable();
//      }
//    }

    /* 权限申请 */
//    mPermissionList.clear();                                    //清空已经允许的没有通过的权限
    for (int i = 0; i < PERMISSION_AUDIO.length; i++) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {     //逐个判断是否还有未通过的权限
        if (ActivityCompat.checkSelfPermission(this, PERMISSION_AUDIO[i]) != PackageManager.PERMISSION_GRANTED) {
          mPermissionList.add(PERMISSION_AUDIO[i]);
        }
      }
    }
    if(mPermissionList.size() > 0){
      ActivityCompat.requestPermissions(this, PERMISSION_AUDIO, GET_RECODE_AUDIO);
    }
//    int permission = ActivityCompat.checkSelfPermission(this,
//      Manifest.permission.RECORD_AUDIO);
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      if ((permission != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//        ActivityCompat.requestPermissions(this, PERMISSION_AUDIO,
//          1);
//      }
//    }
    finish();
  }
}
