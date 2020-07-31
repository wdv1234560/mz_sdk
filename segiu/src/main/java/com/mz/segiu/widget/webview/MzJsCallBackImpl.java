package com.mz.segiu.widget.webview;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.sca100.IGetBLEScaleDevice;
import com.example.sca100.ScaleDevice;
import com.example.sca100.scalerSDK;
import com.google.gson.internal.LinkedTreeMap;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mz.segiu.app.GlideEngine;
import com.jess.arms.integration.EventBusManager;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.Convert;
import com.jess.arms.utils.FileUtils;
import com.jess.arms.utils.LocationUtlils;
import com.mz.segiu.utils.PictureSelectorUtils;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.mz.segiu.R;
import com.mz.segiu.api.js.JsApi;
import com.mz.segiu.api.js.JsCode;
import com.mz.segiu.api.js.JsMethodApi;
import com.mz.segiu.ble.Constant;
import com.mz.segiu.ble.DeviceConnFactoryManager;
import com.mz.segiu.ble.DeviceListActivity;
import com.mz.segiu.ble.UsbDeviceList;
import com.mz.segiu.mvp.model.entity.BaseFormJsEntity;
import com.mz.segiu.mvp.model.entity.LocationEntity;
import com.mz.segiu.mvp.presenter.MzWebPresenter;
import com.mz.segiu.mvp.ui.activity.MzWebActivity;
import com.mz.segiu.mvp.ui.activity.ScanActivity;
import com.mz.segiu.services.BluetoothLeService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simple.eventbus.Subscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MzJsCallBackImpl implements AMapLocationListener {
    private MzWebView mWebView;
    private MzWebActivity mActivity;
    private MzWebPresenter mPresenter;
    private HashMap<String, Object> mJsResMap = new HashMap();
    private boolean mUsbIsConnected;
    private scalerSDK mScale;
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private String mThWeight;
    private String mLocationCallBackName;
    private String mCamerUploadCallBackName;
    private String mGalleryUploadCallBackName;
    private String mFileUploadCallBackName;
    private String mBlueMethodName;
    private String mRequestCode = "";
    private int mConnectType;
    private File mImageFile;
    private AMapLocationClient mLocationClient;
    private ScaleDevice mScaleDevice = new ScaleDevice();
    public static final int BLUETOOTH_REQUEST_CODE = 1;
    public static final int USB_REQUEST_CODE = 2;
    public static final int CAMERA_REQUEST_CODE = 3;
    public static final int GALLERY_REQUEST_CODE = 4;
    public static final int FILE_REQUEST_CODE = 5;
    public static final int IMAGE_EDIT_REQUEST_CODE = 6;
    public static final int IMAGE_FILE_REQUEST_CODE = 7;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(@NotNull Context context, @NotNull Intent intent) {
            String action = intent.getAction();
            //usb权限
            if (Constant.ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) { //用户点击授权
                        mPresenter.usbConn(device);
                    }
                } else { //用户点击不授权,则无权限访问USB
                    Timber.e("No access to USB");
                }

            }
            //usb断开连接
            else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                mUsbIsConnected = false;
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice == DeviceConnFactoryManager.getDeviceConnFactoryManagers()[mPresenter.mUsbId].usbDevice()) {
                    Timber.d("断开连接");
                }
            }
            //usb连接状态
            else if (DeviceConnFactoryManager.ACTION_CONN_STATE.equals(action)) {
                int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                int deviceId = intent.getIntExtra(DeviceConnFactoryManager.DEVICE_ID, -1);
                switch (state) {
                    case DeviceConnFactoryManager.CONN_STATE_DISCONNECT: {
                        if (mPresenter.mUsbId == deviceId) {
                            mUsbIsConnected = false;
                            JsMethodApi.webLoadUrl(mWebView, JsMethodApi.APP_RES_CONNECT_USB, JsCode.USB_DISCONNECT.code, JsCode.USB_DISCONNECT.msg, mRequestCode);
                        }
                        break;
                    }
                    case DeviceConnFactoryManager.CONN_STATE_CONNECTING: {
                        mUsbIsConnected = false;
                        ArmsUtils.makeText(mActivity, mActivity.getString(R.string.str_conn_state_connecting));
                        JsMethodApi.webLoadUrl(mWebView, JsMethodApi.APP_RES_CONNECT_USB, JsCode.USB_CONNECTING.code, JsCode.USB_CONNECTING.msg, mRequestCode);
                        break;
                    }
                    case DeviceConnFactoryManager.CONN_STATE_CONNECTED: {
                        mUsbIsConnected = true;
                        JsMethodApi.webLoadUrl(mWebView, JsMethodApi.APP_RES_CONNECT_USB, JsCode.USB_SUCCESS.code, JsCode.USB_SUCCESS.msg, mRequestCode);
                        break;
                    }
                    case DeviceConnFactoryManager.CONN_STATE_FAILED: {
                        mUsbIsConnected = false;
                        JsMethodApi.webLoadUrl(mWebView, JsMethodApi.APP_RES_CONNECT_USB, JsCode.USB_FAILED.code, JsCode.USB_FAILED.msg, mRequestCode);
                        break;
                    }
                }
            }
            //蓝牙打印机状态
            else if (DeviceConnFactoryManager.ACTION_PRINTER_STATUS.equals(action)) {

            }
        }
    };

    public MzJsCallBackImpl(@NotNull MzWebActivity mzWebActivity, @Nullable MzWebPresenter presenter, @NotNull MzWebView webView) {
        this.mActivity = mzWebActivity;
        this.mPresenter = presenter;
        this.mWebView = webView;
        this.init();
    }

    private final void init() {
        EventBusManager.getInstance().register(this);
        this.initAMap();
        this.initBroadcast();

        if (mActivity != null) {
            this.mUsbManager = (UsbManager) mActivity.getSystemService(Context.USB_SERVICE);

            mScale = new scalerSDK(mActivity, new IGetBLEScaleDevice() {
                @Override
                public void onGetBluetoothDevice(ArrayList<ScaleDevice> arrayList) {

                }

                @Override
                public void onBluetoothState(ScaleDevice scaleDevice, int state) {
                    //蓝牙秤连接状态变化的回调函数
                    //state=8  表示蓝牙秤断开连接
                    //state=2  表示蓝牙秤连上
                    if (state == 8) {
                        ArmsUtils.makeText("蓝牙断开连接");
                        mScale.disconnect();
                        mScale.clearDevicelist();
                        BluetoothLeService.mIsConnected = false;
                    }
                    if (state == 2) {
                        BluetoothLeService.mIsConnected = true;
                        ArmsUtils.makeText("蓝牙连接成功");
                    }
                    mScaleDevice = scaleDevice;
                }
            });

            mPresenter.bindBleService();
        }
    }

    private final void initAMap() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        option.setInterval(2000L);
        if (mActivity != null) {
            mLocationClient = new AMapLocationClient(mActivity);
            mLocationClient.setLocationOption(option);
            mLocationClient.setLocationListener(this);
        }

    }

    private final void initBroadcast() {
        IntentFilter filter = new IntentFilter(Constant.ACTION_USB_PERMISSION); //USB访问权限广播
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED); //USB线拔出
        filter.addAction(DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE); //查询打印机缓冲区状态广播，用于一票一控
        filter.addAction(DeviceConnFactoryManager.ACTION_CONN_STATE); //与打印机连接状态
        filter.addAction(DeviceConnFactoryManager.ACTION_PRINTER_STATUS); //与打印机连接状态
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); //USB线插入
        mActivity.registerReceiver(receiver, filter);
    }

    @Subscriber(tag = "scanCode")
    public final void onScanEvent(@Nullable String[] data) {
        if (data != null) {
            String callBack = data[0];
            String code = data[1];
            if (mWebView != null) {
                mWebView.loadUrl("javascript:" + callBack + "('" + code + "')");
            }

        }

    }

    @Subscriber(tag = "scanCode")
    public final void onInputCodeEvent(@Nullable String[] data) {
        if (data != null) {
            this.mJsResMap.clear();
            String callBack = data[0];
            String equipmentNo = data[1];
            String id = data[2];
            mJsResMap.put("equipmentNo", equipmentNo);
            mJsResMap.put("id", id);
            if (mWebView != null) {
                JsMethodApi.webLoadUrl(mWebView, callBack, this.mJsResMap, this.mRequestCode);
            }
        }

    }

    @Subscriber(tag = "thWeight")
    public final void onThWeightEvent(@Nullable String weight) {
        this.mThWeight = weight;
    }

    @Subscriber(tag = "xmgScannerCode")
    public final void onXmgEvent(@NotNull String code) {
        if (this.mRequestCode != null) {
            HashMap map = new HashMap();

            map.put("requestCode", mRequestCode);
            //TODO Kotlin String.trim{ it <= ' '}转Java
            map.put("scanCode", code.trim());

            mWebView.loadUrl(JsMethodApi.doJsByMap("appResXmgCode", map));
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void callMz(@Nullable String data) {
        BaseFormJsEntity json = Convert.fromJson(data, BaseFormJsEntity.class);
        if (json.parameters != null) {
            LinkedTreeMap params = (LinkedTreeMap) json.parameters;
            if (TextUtils.isEmpty(json.requestCode)) {
                this.mRequestCode = json.requestCode;
            }
            mPresenter.initToken(data);
            String methodName = json.methodName;

            if (JsApi.GET_GAODE_LOCATION.equals(methodName)) {
                mLocationCallBackName = json.callBack;
                if (mLocationClient != null) {
                    mLocationClient.startLocation();
                }
            } else if (JsApi.BARCODESCANNER_SCAN.equals(methodName)) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(mActivity);
                if (params.get("codeType") != null) {
                    int codeType = (int) params.get("codeType");
                    intentIntegrator.addExtra("type", codeType);
                }
                intentIntegrator.addExtra("callBack", json.callBack);
                intentIntegrator.setOrientationLocked(false).setCaptureActivity(ScanActivity.class).initiateScan();
            } else if (JsApi.PDA_SCAN.equals(methodName)) {
                mRequestCode = json.requestCode;
            } else if (JsApi.APP_GET_FILE_BASE64.equals(methodName)) {
                PictureSelector.create(mActivity)
                        .openGallery(PictureMimeType.ofImage())
                        .isCamera(false)
                        .maxSelectNum(1)
                        .loadImageEngine(GlideEngine.createGlideEngine()) // Please refer to the Demo GlideEngine.java
                        .forResult(IMAGE_FILE_REQUEST_CODE);
            } else if (JsApi.CAMERA_UPLOAD.equals(methodName)) {
                mCamerUploadCallBackName = json.callBack;
                PictureSelector.create(mActivity)
                        .openCamera(PictureMimeType.ofImage())
                        .setOutputCameraPath(FileUtils.createRootPath(mActivity))
                        .isUseCustomCamera(PictureSelectorUtils.isUserCustomCamera())//PDA打印机使用系统拍照&&"MI 9 SE"==Build.MODEL
                        .loadImageEngine(GlideEngine.createGlideEngine()) // Please refer to the Demo GlideEngine.java
                        .forResult(CAMERA_REQUEST_CODE);
            } else if (JsApi.GALLERY_UPLOAD.equals(methodName)) {
                mGalleryUploadCallBackName = json.callBack;
                PictureSelector.create(mActivity)
                        .openGallery(PictureMimeType.ofImage())
                        .isCamera(false)
                        .maxSelectNum(1)
                        .loadImageEngine(GlideEngine.createGlideEngine()) // Please refer to the Demo GlideEngine.java
                        .forResult(GALLERY_REQUEST_CODE);
            } else if (JsApi.FILE_UPLOAD.equals(methodName)) {
                mFileUploadCallBackName = json.callBack;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                mActivity.startActivityForResult(intent, FILE_REQUEST_CODE);
            } else if (JsApi.SCAN_BLUETOOTH.equals(methodName)) {
                if (!LocationUtlils.isLocationEnabled(mActivity)) {
                    LocationUtlils.toOpenGPS(mActivity);
                    return;
                }
                try {
                    mConnectType = ((Double) params.get("connectType")).intValue();
                } catch (Exception e) {
                    Timber.e(e);
                }
                Intent intent = new Intent(mActivity, DeviceListActivity.class);
                intent.putExtra("connectType", mConnectType);
                mActivity.startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
            } else if (JsApi.GET_TH_WEIGHT.equals(methodName)) {
                mBlueMethodName = json.methodName;
                if (!TextUtils.isEmpty(mThWeight)) {
                    mJsResMap.put("weight", mThWeight);
                    JsMethodApi.webLoadUrl(mWebView, json.callBack, mJsResMap, mRequestCode);
                }
            } else if (JsApi.TH_PRINT.equals(methodName)) {
                mPresenter.thPrint(params);
            } else if (JsApi.GET_SJ_WEIGHT.equals(methodName)) {
                mBlueMethodName = json.methodName;
                mJsResMap.put("weight", mScaleDevice.scalevalue);
                JsMethodApi.webLoadUrl(mWebView, json.callBack, mJsResMap, mRequestCode);
            } else if (JsApi.CLEAR_CACHE.equals(methodName)) {
                mPresenter.clearCache(json.callBack);
            } else if (JsApi.DOWNLOAD_FILE.equals(methodName)) {
                mPresenter.download(json.callBack, params);
            } else if (JsApi.MEDIA_START_RECORD.equals(methodName)) {
                mPresenter.startRecord();
            } else if (JsApi.MEDIA_STOP_RECORD.equals(methodName)) {
                mPresenter.stopRecord(json.callBack);
            } else if (JsApi.APP_BLE_CONNECTED.equals(methodName)) {
                mJsResMap.clear();
                mJsResMap.put("isConnected", BluetoothLeService.mIsConnected);
                if (TextUtils.isEmpty(json.callBack)) {
                    json.callBack = JsMethodApi.APP_RES_BLE_CONNECTED;
                }
                JsMethodApi.webLoadUrl(mWebView, json.callBack, mJsResMap, mRequestCode);
            } else if (JsApi.APP_DISCONNECT_BLE.equals(methodName)) {
                mPresenter.releaseBle(mScale);
            } else if (JsApi.APP_USB_CONNECTED.equals(methodName)) {
                mPresenter.appResUsbConnected(mWebView, mUsbIsConnected, mRequestCode);
            } else if (JsApi.APP_CONNECT_USB.equals(methodName)) {
                mPresenter.closeport();
                mActivity.startActivityForResult(new Intent(mActivity, UsbDeviceList.class), USB_REQUEST_CODE);
            } else if (JsApi.PDA_PRINT.equals(methodName)) {
                mPresenter.pdaPrint(mWebView, params, mRequestCode);
            }

        }
    }

    public final void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                if (data != null) {
                    data.getIntExtra("type", 0);
                }
                String callBack = data != null ? data.getStringExtra("callback") : "";
                mWebView.loadUrl(JsMethodApi.BASE_URL + callBack + "('" + result.getContents() + "')");
            }

            if (resultCode == Activity.RESULT_OK) {
                switch (requestCode) {
                    case BLUETOOTH_REQUEST_CODE: {
                        String btAddress = data.getStringExtra("device_address");
                        String btName = data.getStringExtra("device_Name");
                        btName = (TextUtils.isEmpty(btName)) ? btAddress : btName;
                        if (!TextUtils.isEmpty(btAddress)) {
                            if (JsApi.GET_SJ_WEIGHT == mBlueMethodName || mConnectType == 1) {
                                mScaleDevice.devicename = btName;
                                mScaleDevice.deviceaddr = btAddress;
                                mScale.connect(mScaleDevice);
                            } else {

                                mPresenter.bleConnect(btAddress);
                            }
                        }
                        break;
                    }
                    case USB_REQUEST_CODE: {
                        /* 获取USB设备名 */
                        String usbName = data.getStringExtra(UsbDeviceList.USB_NAME);
                        //通过USB设备名找到USB设备
                        UsbDevice usbDevice = ArmsUtils.getUsbDeviceFromName(mActivity, usbName);
//                    showMessage("USB设备==$usbName")
                        /* 判断USB设备是否有权限 */
                        if (usbDevice == null) {
                            JsMethodApi.webLoadUrl(mWebView, JsMethodApi.APP_RES_CONNECT_USB, JsCode.NO_USB.code, JsCode.NO_USB.msg, mRequestCode);
                        } else {

                            //判断USB设备是否有权限
                            if (mUsbManager.hasPermission(usbDevice)) {
                                if (DeviceConnFactoryManager.mConnectState == DeviceConnFactoryManager.CONN_STATE_CONNECTED) {
                                    //usb已连接,可以进行打印
                                } else {
                                    mPresenter.usbConn(usbDevice);
                                }
                            } else { //请求权限
                                mPermissionIntent = PendingIntent.getBroadcast(mActivity, 0, new Intent(Constant.ACTION_USB_PERMISSION), 0);
                                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                            }
                        }
                        break;
                    }
                    case IMAGE_FILE_REQUEST_CODE: {
                        List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                        LocalMedia localMedia = selectList.get(0);
                        mJsResMap.clear();
                        mJsResMap.put("file", mPresenter.getFileBase64(localMedia));
                        JsMethodApi.webLoadUrl(mWebView, JsMethodApi.APP_RES_FILE_BASE64, mJsResMap, mRequestCode);
                        break;
                    }
                    case CAMERA_REQUEST_CODE: {
                        List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                        LocalMedia localMedia = selectList.get(0);
                        mPresenter.uploadCamera(localMedia, mCamerUploadCallBackName, mRequestCode);
                        break;
                    }
                    case GALLERY_REQUEST_CODE: {
                        List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                        String path = "";
                        if (!selectList.isEmpty()) {
                            LocalMedia localMedia = selectList.get(0);
                            if (localMedia != null) {
                                if (!TextUtils.isEmpty(localMedia.getPath())) {
                                    path = localMedia.getPath();
                                }
                                if (!TextUtils.isEmpty(localMedia.getAndroidQToPath())) {
                                    path = localMedia.getAndroidQToPath();
                                }
                                mPresenter.uploadFile(path, mGalleryUploadCallBackName, mRequestCode, true);
                            }
                        }
                    }
                    case IMAGE_EDIT_REQUEST_CODE: {
                        mPresenter.uploadFile(mImageFile.getAbsolutePath(), mGalleryUploadCallBackName, mRequestCode, true);
                    }
                    case FILE_REQUEST_CODE: {
                        try {
                            Uri uri = data.getData();
                            mPresenter.uploadFile(FileUtils.getUriPath(mActivity, uri), mFileUploadCallBackName, mRequestCode, false);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception var10) {
        }

    }

    @Override
    public void onLocationChanged(@Nullable AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                aMapLocation.getLocationType();
                double latitude = aMapLocation.getLatitude();
                double longitude = aMapLocation.getLongitude();
                String address = aMapLocation.getAddress();
                String city = aMapLocation.getCity();
                String province = aMapLocation.getProvince();

                mWebView.loadUrl(JsMethodApi.doJsByJson(this.mLocationCallBackName, Convert.toJson(new LocationEntity(latitude, longitude, address, city, province))));
                Timber.d("lat===" + latitude + "----longitude===" + longitude);
                if (mLocationClient != null) {
                    mLocationClient.stopLocation();
                }
            } else {
                Timber.e("location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());
            }
        }

    }

    public final void onDestroy() {
        if (mPresenter != null) {
            mPresenter.releaseBle(this.mScale);
            mPresenter.unbindService();
        }

        if (this.mUsbManager != null) {
            this.mUsbManager = null;
        }

        if (mActivity != null) {
            mActivity.unregisterReceiver(this.receiver);
        }

        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }

        DeviceConnFactoryManager.closeAllPort();
    }

}
