package com.mz.segiu.mvp.presenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.annotation.RequiresApi;

import com.amap.api.location.AMapLocationClient;
import com.example.sca100.scalerSDK;
import com.google.gson.internal.LinkedTreeMap;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.download.DownloadListener;
import com.jess.arms.http.download.DownloadManager;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.utils.Convert;
import com.jess.arms.utils.DataCleanUtils;
import com.jess.arms.utils.DeviceUtils;
import com.jess.arms.utils.FileUtils;
import com.jess.arms.utils.PermissionUtil;
import com.jess.arms.utils.PhotoBitmapUtils;
import com.jess.arms.utils.RxLifecycleUtils;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.mz.segiu.R;
import com.mz.segiu.api.js.JsCode;
import com.mz.segiu.api.js.JsMethodApi;
import com.mz.segiu.ble.DeviceConnFactoryManager;
import com.mz.segiu.ble.JiaBoSendData;
import com.mz.segiu.mvp.contract.MzWebContract;
import com.mz.segiu.mvp.model.entity.BaseJSParams;
import com.mz.segiu.mvp.model.entity.DeviceEntity;
import com.mz.segiu.mvp.model.entity.PdaPrintEntity;
import com.mz.segiu.mvp.model.entity.UpFileEntity;
import com.mz.segiu.services.BluetoothLeService;
import com.mz.segiu.utils.SpUtils;
import com.mz.segiu.widget.webview.MzWebView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import me.jessyan.rxerrorhandler.handler.RetryWithDelay;
import retrofit2.HttpException;
import timber.log.Timber;
import top.zibin.luban.Luban;


@ActivityScope
public class MzWebPresenter extends BasePresenter<MzWebContract.Model, MzWebContract.View> {
    @Inject
    RxErrorHandler mErrorHandler;
    @Inject
    Application mApplication;
    @Inject
    ImageLoader mImageLoader;
    @Inject
    AppManager mAppManager;
    @Inject
    RxPermissions mRxPermissions;
    @Inject
    AMapLocationClient mlocationClient;
    @Inject
    @Named("listData")
    ArrayList<Map<String, Object>> mListData;
    @Inject
    @Named("lastListData")
    ArrayList<Map<String, Object>> mListLastData;
    @Inject
    DownloadManager mDownloadManager;
    @Inject
    public HashMap<String, Object> mJsResMap;

    public int mUsbId;

    private MediaRecorder mMediaRecorder;
    private String mMediaRecordPath;
    private BluetoothLeService mBluetoothLeService;
    private String tempRotatePath;

    @Inject
    public MzWebPresenter(MzWebContract.Model model, MzWebContract.View rootView) {
        super(model, rootView);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLeService.LocalBinder localBinder = (BluetoothLeService.LocalBinder) service;
            mBluetoothLeService = localBinder.getService();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!mBluetoothLeService.initialize()) {

                    Timber.e("Unable to initialize Bluetooth");
                    mRootView.killMyself();
                    return;
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mBluetoothLeService = null;
        }

    };


    public void initPremission() {
        //请求外部存储权限用于适配android6.0的权限管理机制
        PermissionUtil.requestPermission(
                new PermissionUtil.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {

                    }

                    @Override
                    public void onRequestPermissionFailure(List<String> permissions) {
                        mRootView.showMessage("申请权限失败");
                    }

                    @Override
                    public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                        mRootView.showMessage(mApplication.getString(R.string.go_to_setting_open_permission));
                    }

                }, mRxPermissions, mErrorHandler,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO);
    }

    public void initToken(String json) {
        BaseJSParams params = Convert.fromJson(json, BaseJSParams.class);
        if (params.parameters != null && params.parameters.token != null) {
            SpUtils.getInstance(mApplication).putString("token", params.parameters.token);
        }
    }


    /**
     * @des 获取高德地图定位
     */
    public void appGetGaoDeLocation() {

        //请求外部存储权限用于适配android6.0的权限管理机制
        PermissionUtil.requestPermission(
                new PermissionUtil.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        mlocationClient.startLocation();
                    }

                    @Override
                    public void onRequestPermissionFailure(List<String> permissions) {
                        mRootView.showMessage("申请权限失败");
                    }

                    @Override
                    public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                        mRootView.showMessage(mApplication.getString(R.string.go_to_setting_open_permission));
                    }

                },
                mRxPermissions, mErrorHandler,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void closeport() {
        if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[mUsbId] != null && DeviceConnFactoryManager.getDeviceConnFactoryManagers()[mUsbId].mPort != null) {
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[mUsbId].mPort.closePort();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[mUsbId].mPort = null;
        }
    }

    /**
     * usb连接
     *
     * @param usbDevice
     */
    public void usbConn(UsbDevice usbDevice) {
        new DeviceConnFactoryManager.Build()
                .setId(mUsbId)
                .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.USB)
                .setUsbDevice(usbDevice)
                .setContext(mRootView.getMzWebViewActivity())
                .build();
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[mUsbId].openPort();
//        ArmsUtils.makeText("usb连接")
    }

    public boolean bindBleService() {
        Intent gattServiceIntent = new Intent(mRootView.getMzWebViewActivity(), BluetoothLeService.class);
        return mRootView.getMzWebViewActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @SuppressLint("CheckResult")
    public void uploadFile(String path, String callBack, String requestCode, Boolean isPic) {
        if (TextUtils.isEmpty(path)) {
            mRootView.showMessage("路径不存在");
            return;
        }
        Observable.just(path)
                .map(s -> {
                    if (isPic) {
                        tempRotatePath = PhotoBitmapUtils.amendRotatePhoto(path, mApplication, false);
                    }
                    if (isPic) {

                        return tempRotatePath;
                    } else {
                        return path;

                    }
                })
                .subscribeOn(Schedulers.io())
                .map(it -> {
                    if (isPic) {
                        return Luban.with(mApplication).load(it).ignoreBy(50).get().get(0);
                    } else {
                        return new File(it);

                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable ->
                        mRootView.showFileLoading() //显示下拉刷新的进度条
                ).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
                .subscribe(new ErrorHandleSubscriber<File>(mErrorHandler) {
                    @Override
                    public void onNext(File it) {
                        if (!it.exists()) {
                            mRootView.showMessage("文件不存在");
                            return;
                        }
                        Timber.i("图片大小：" + it.length());
                        mModel.uploadFiles(it)
                                .subscribeOn(Schedulers.io())
                                .retryWhen(new RetryWithDelay(3, 2)) //遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔
                                .observeOn(AndroidSchedulers.mainThread())
                                .doFinally(() -> mRootView.hideLoading())
                                .compose(RxLifecycleUtils.bindToLifecycle(mRootView)) //使用 Rxlifecycle,使 Disposable 和 Activity 一起销毁
                                .subscribe(new ErrorHandleSubscriber<UpFileEntity>(mErrorHandler) {
                                    @Override
                                    public void onNext(UpFileEntity upFileEntity) {
                                        if (isPic) {
                                            if (tempRotatePath != null) {
                                                File file = new File(tempRotatePath);
                                                if (file.exists()) {
                                                    file.delete();
                                                }
                                            }
                                        }
                                        PictureFileUtils.deleteAllCacheDirFile(mApplication);
                                        upFileEntity.requestCode = requestCode == null ? "" : requestCode;
                                        mRootView.resUpload(Convert.toJson(upFileEntity), callBack);
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        if (t instanceof HttpException) {
                                            int code = ((HttpException) t).code();
                                            mRootView.resUpload("{'code':" + code + ",'message':'token 过期'}", callBack);
                                        } else {
                                            mRootView.
                                                    resUpload("{'code':400,'message':'上传失败','requestCode':$requestCode}", callBack);
                                        }
                                    }

                                });
                    }

                });


    }

    public void uploadCamera(LocalMedia localMedia, String callBack, String requestCode) {
        String path = "";
        if (localMedia != null) {
            if (!TextUtils.isEmpty(localMedia.getPath())) {
                path = localMedia.getPath();
            }
            if (!TextUtils.isEmpty(localMedia.getAndroidQToPath())) {
                path = localMedia.getAndroidQToPath();
            }
        }
        if (TextUtils.isEmpty(path)) {
            mRootView.showMessage("路径不存在");
            return;
        }
        Observable.just(path)
                .map(it -> {
                    return Luban.with(mApplication).load(it).ignoreBy(50).get().get(0);
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable ->
                        mRootView.showFileLoading() //显示下拉刷新的进度条
                ).subscribeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
                .subscribe(new ErrorHandleSubscriber<File>(mErrorHandler) {
                    @Override
                    public void onNext(File it) {
                        if (!it.exists()) {
                            mRootView.showMessage("文件不存在");
                            return;
                        }
                        Timber.i("图片大小：" + it.length());
                        mModel.uploadFiles(it)
                                .subscribeOn(Schedulers.io())
                                .retryWhen(new RetryWithDelay(3, 2)) //遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔
                                .observeOn(AndroidSchedulers.mainThread())
                                .doFinally(() -> mRootView.hideLoading())
                                .compose(RxLifecycleUtils.bindToLifecycle(mRootView)) //使用 Rxlifecycle,使 Disposable 和 Activity 一起销毁
                                .subscribe(new ErrorHandleSubscriber<UpFileEntity>(mErrorHandler) {

                                    @Override
                                    public void onNext(UpFileEntity upFileEntity) {
                                        it.delete();
                                        PictureFileUtils.deleteAllCacheDirFile(mApplication);
                                        upFileEntity.requestCode = requestCode == null ? "" : requestCode;
                                        mRootView.resUpload(Convert.toJson(upFileEntity), callBack);
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        if (t instanceof HttpException) {
                                            int code = ((HttpException) t).code();
                                            mRootView.resUpload("{'code':" + code + ",'message':'token 过期'}", callBack);
                                        } else {
                                            mRootView.resUpload("{'code':400,'message':'上传失败','requestCode':$requestCode}", callBack);
                                        }
                                    }
                                });
                    }

                });


    }

    public String getFileBase64(LocalMedia localMedia) {
        final String[] path = {""};
        if (localMedia != null) {
            if (!TextUtils.isEmpty(localMedia.getPath())) {
                path[0] = localMedia.getPath();
            }
            if (!TextUtils.isEmpty(localMedia.getAndroidQToPath())) {
                path[0] = localMedia.getAndroidQToPath();
            }
        }
        Observable.just(path[0])
                .map(it ->
                        FileUtils.fileToBase64(Luban.with(mApplication).load(it).ignoreBy(80).get().get(0))
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
                .subscribe(new ErrorHandleSubscriber<String>(mErrorHandler) {
                    @Override
                    public void onNext(String s) {
                        path[0] = s;
                    }

                });

        return path[0];
    }

    /**
     * @param callBack
     * @des 文件下载
     */
    @SuppressLint("CheckResult")
    public void download(String callBack, LinkedTreeMap<String, Object> params) {
        String url = (String) params.get("filePath");
        String fileName = (String) params.get("fileName");
        HashMap map = new HashMap<String, Object>();
        mDownloadManager.download(url,
                TextUtils.isEmpty(fileName) ? url.substring(url.lastIndexOf("/") + 1) : fileName,
                new DownloadListener() {

                    @Override
                    public void onCheckerDownloading(int progress) {

                    }

                    @Override
                    public void onCheckerDownloadSuccess(File file) {
                        map.clear();
                        map.put("code", 200);
                        map.put("message", file.getAbsolutePath().replace(file.getName(), ""));
                        mRootView.resDownload(callBack, Convert.toJson(map));
                    }

                    @Override
                    public void onCheckerDownloadFail() {
                        map.clear();
                        map.put("code", 400);
                        map.put("message","下载失败");
                        mRootView.resDownload(callBack, Convert.toJson(map));
                    }

                    @Override
                    public void onCheckerStartDownload() {

                    }

                });
    }

    /**
     * @desc 清理缓存
     */
    public void clearCache(String callBack) {
        DataCleanUtils.cleanApplicationData(mApplication,
                mApplication.getCacheDir().getAbsolutePath(), mApplication.getFilesDir().getAbsolutePath());
        CookieSyncManager.createInstance(mApplication);
        CookieSyncManager.getInstance().startSync();
        CookieManager.getInstance().removeSessionCookie();
        mRootView.clearCacheFinish(callBack);
    }

    @SuppressLint("CheckResult")
    public void startRecord() {
        Flowable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    //请求外部存储权限用于适配android6.0的权限管理机制
                    PermissionUtil.requestPermission(
                            new PermissionUtil.RequestPermission() {
                                @Override
                                public void onRequestPermissionSuccess() {
                                    //request permission success, do something.
                                    // 开始录音
                                    /* ①Initial：实例化MediaRecorder对象 */
                                    Vibrator vibrator = (Vibrator) mApplication.getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator.vibrate(100);
                                    if (mMediaRecorder == null) {
                                        mMediaRecorder = new MediaRecorder();
                                    }
                                    try {
                                        /* ②setAudioSource/setVedioSource */
                                        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 设置麦克风
                                        /*
                                         * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
                                         * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
                                         */
                                        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                                        /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
                                        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                                        String fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)).toString() + ".wav";
                                        //            if (!FileUtils.isFolderExists(mApplication.getExternalCacheDir())) {
//                FileUtils.makeDirectory(audioSaveDir);
//            }
                                        mMediaRecordPath = mApplication.getExternalCacheDir().toString() + fileName;
                                        /* ③准备 */
                                        mMediaRecorder.setOutputFile(mMediaRecordPath);
                                        mMediaRecorder.prepare();
                                        /* ④开始 */
                                        mMediaRecorder.start();
                                    } catch (IllegalStateException e) {
//                                    mRootView.showMessage("录音失败请联系管理员");
                                        Timber.i("call startAmr(File mRecAudioFile) failed!" + e.getMessage());
                                    } catch (IOException e) {
                                        mRootView.showMessage("录音失败，请检查手机内存是否充足");
                                        Timber.i("call startAmr(File mRecAudioFile) failed!" + e.getMessage());
                                    }
                                }

                                @Override
                                public void onRequestPermissionFailure(List<String> permissions) {
                                    mRootView.showMessage("申请权限失败");
                                }

                                @Override
                                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                                    mRootView.showMessage(mApplication.getString(R.string.go_to_setting_open_permission));
                                }

                            },
                            mRxPermissions, mErrorHandler, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
                });
    }

    public void stopRecord(String callBack) {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (mMediaRecordPath != null) {
                uploadFile(mMediaRecordPath, callBack, "", false);
                mMediaRecordPath = null;
            }
        } catch (RuntimeException e) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void getDeviceInfo(String callBack) {
        String deviceId = DeviceUtils.getDeviceId(mApplication);
        int versionCode = DeviceUtils.getVersionCode(mApplication);
        String versionName = DeviceUtils.getVersionName(mApplication);
        mRootView.resDeviceInfo(callBack, Convert.toJson(new DeviceEntity(deviceId, versionCode, versionName)));
    }


    public void pdaPrint(MzWebView webView, LinkedTreeMap<String, Object> params, String requestCode) {
        String template = (String) params.get("template");
        if (!TextUtils.isEmpty(template)) {

            try {
                JiaBoSendData.sendLabel(mApplication, Convert.fromJson(template, PdaPrintEntity.class), mUsbId);
                JsMethodApi.webLoadUrl(webView, JsMethodApi.APP_RES_PDA_PRINT, JsCode.PRINT_SUCCESS.getCode(), JsCode.PRINT_SUCCESS.getMsg(), requestCode);
                mRootView.showMessage("响应pda打印" + JsCode.PRINT_SUCCESS.getMsg());
            } catch (Exception e) {
//                JsMethodApi.webLoadUrl(webView, JsMethodApi.APP_RES_PDA_PRINT, JsCode.PRINT_FAILED.code, JsCode.PRINT_FAILED.msg, requestCode)
            }
        } else {
            JsMethodApi.webLoadUrl(webView, JsMethodApi.APP_RES_PDA_PRINT, JsCode.PRINT_DATA_NULL.getCode(), JsCode.PRINT_DATA_NULL.getMsg(), requestCode);
            mRootView.showMessage("响应pda打印" + JsCode.PRINT_DATA_NULL.getMsg());
        }
    }

    public void thPrint(LinkedTreeMap<String, Object> params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String template = (String) params.get("template");
            if (!TextUtils.isEmpty(template)) {

                mBluetoothLeService.writeValue(template);
            }
        }
    }

    public void appResUsbConnected(MzWebView webView, Boolean isConnected, String requestCode) {
        mJsResMap.put("isConnected", isConnected);
        JsMethodApi.webLoadUrl(webView, JsMethodApi.APP_RES_USB_CONNECTED, mJsResMap, requestCode);
    }

    public void appResConnectUSB(MzWebView webView, Boolean isConnected, String requestCode) {
        mJsResMap.put("isConnected", isConnected);
        JsMethodApi.webLoadUrl(webView, JsMethodApi.APP_RES_CONNECT_USB, mJsResMap, requestCode);
    }

    public void releaseBle(scalerSDK mScale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothLeService.mIsConnected = false;
        }

        if (mScale != null) {
            mScale.disconnect();
            mScale.clearDevicelist();
        }
        if (mBluetoothLeService != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBluetoothLeService.disconnect();
                mBluetoothLeService.close();
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void bleConnect(String address) {
        mBluetoothLeService.connect(address);
    }

    /**
     * @des 解绑蓝牙服务
     */
    public void unbindService() {
        if (mServiceConnection != null) {

            mRootView.getMzWebViewActivity().unbindService(mServiceConnection);
        }
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothLeService.mIsConnected = false;
        }
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
    }
}
