package com.mz.segiu.mvp.ui.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.RxUtils;
import com.jess.arms.widget.ProgresDialog;
import com.mz.segiu.R;
import com.mz.segiu.api.js.JsApi;
import com.mz.segiu.api.js.JsMethodApi;
import com.mz.segiu.api.js.JsUrl;
import com.mz.segiu.app.MzWebViewActivity;
import com.mz.segiu.ble.BleProtocol;
import com.mz.segiu.di.component.DaggerMzWebComponent;
import com.mz.segiu.mvp.contract.MzWebContract;
import com.mz.segiu.mvp.presenter.MzWebPresenter;
import com.mz.segiu.widget.webview.MzJsCallBackImpl;
import com.mz.segiu.widget.webview.OnJsCallListener;

import org.simple.eventbus.Subscriber;

import javax.inject.Inject;

import static com.jess.arms.utils.Preconditions.checkNotNull;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MzWebActivity extends MzWebViewActivity<MzWebPresenter> implements MzWebContract.View, OnJsCallListener {
    private MzJsCallBackImpl mMzJsCallBackImpl;

    @Inject
    Application mApplication;

    @Inject
    BleProtocol mBleProtocol;

    @Inject
    ProgresDialog mProgresDialog;

    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {
        DaggerMzWebComponent //如找不到该类,请编译一下项目
                .builder()
                .appComponent(appComponent)
                .view(this)
                .build()
                .inject(this);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_mz_webview; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }


    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mPresenter.initPremission();
        mMzJsCallBackImpl = new MzJsCallBackImpl(this, mPresenter, mWebView);
        initWebView();
    }

    @Override
    public void initListener(@Nullable Bundle savedInstanceState) {
        mWebView.setOnJsCallListener(this);
    }

    private void initWebView() {
        mWebView.initConfig(this);
        String url = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(url)) {
        }
        mWebView.loadUrl("http://192.168.0.215:8080/#/medicalMenu");
    }

    @Subscriber(tag = JsApi.APP_TO_BACKLOG)
    public void onToBacklogEvent(boolean b) {
        killMyself();
    }

    @Override
    public void showLoading(String msg) {
        mProgresDialog.setText("加载中...");
        mProgresDialog.show();
    }

    @Override
    public void hideLoading() {
        mProgresDialog.dismiss();
    }

    @Override
    public void showMessage(@NonNull String message) {
        checkNotNull(message);
        ArmsUtils.makeText(this, message);
    }

    @Override
    public void launchActivity(@NonNull Intent intent) {
        checkNotNull(intent);
        ArmsUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {
        finish();
    }

    @Override
    public Activity getMzWebViewActivity() {
        return this;
    }

    @Override
    public void resUpload(String toJson, String callBack) {
        mWebView.loadUrl(JsMethodApi.doJsByJson(callBack, toJson));
    }

    @Override
    public void clearCacheFinish(String callBack) {
        JsMethodApi.doJsByStr(callBack, "");
    }

    @Override
    public void resDownload(String callBack, String result) {
        mWebView.loadUrl(JsMethodApi.doJsByJson(callBack, result));
    }

    @Override
    public void resDeviceInfo(String callBack, String toJson) {
        RxUtils.runOnMainThread(o ->
                mWebView.loadUrl(JsMethodApi.doJsByJson(callBack, toJson))
        );
    }

    @Override
    public void showFileLoading() {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMzJsCallBackImpl.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        mMzJsCallBackImpl.onDestroy();
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onJsCall(String data) {
        mMzJsCallBackImpl.callMz(data);
    }

    @Override
    public void pageStarted(String var1) {

    }

    @Override
    public void pageFinished(String var1) {

    }
}
