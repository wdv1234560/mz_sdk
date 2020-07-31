package com.mz.segiu.mvp.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.integration.EventBusManager;
import com.jess.arms.utils.ArmsUtils;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.mz.segiu.R;
import com.mz.segiu.api.js.JsMethodApi;
import com.mz.segiu.di.component.DaggerScanComponent;
import com.mz.segiu.mvp.contract.ScanContract;
import com.mz.segiu.mvp.presenter.ScanPresenter;
import com.mz.segiu.widget.ZxingViewFinderView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import javax.inject.Inject;

import static com.jess.arms.utils.Preconditions.checkNotNull;


public class ScanActivity extends BaseActivity<ScanPresenter> implements ScanContract.View, RadioGroup.OnCheckedChangeListener, TextWatcher {
    private static final int SCAN_TYPE = 100;
    private static final int INPUT_TYPE = 200;
    private String mCallBacks = "";
    private int mType;
    @Inject
    HashMap<String,Object> mMap;
    private CaptureManager mCapture;
    private RadioGroup mRgScan;
    private DecoratedBarcodeView mZxingScanner;
    private TextView mTvTitle;
    private ZxingViewFinderView mZxingViewFinderView;
    private EditText mEtSearch;
    private RelativeLayout mRlRoot;
    private LinearLayout mLlSearch;

    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {
        DaggerScanComponent //如找不到该类,请编译一下项目
                .builder()
                .appComponent(appComponent)
                .view(this)
                .build()
                .inject(this);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_scan; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRgScan = findViewById(R.id.rg_scan);
        mZxingScanner = findViewById(R.id.zxing_barcode_scanner);
        mZxingViewFinderView = findViewById(R.id.zxing_viewfinder_view);
        mRlRoot = findViewById(R.id.rl_root);
        mLlSearch = findViewById(R.id.ll_search);
        mEtSearch = findViewById(R.id.et_search);
        mTvTitle = findViewById(R.id.toolbar_title);
        mCallBacks = getIntent().getStringExtra("callBack");
        mType = getIntent().getIntExtra("type", 0);
        if (mCallBacks == null) {
            mCallBacks = JsMethodApi.APP_RES_SCAN_CODE;
        }
        if (mType == INPUT_TYPE) {
            mRgScan.setVisibility(View.VISIBLE);
        }
        mTvTitle.setText("扫一扫");
        mPresenter.initPremission();
        mCapture = new CaptureManager(this, mZxingScanner);
        mCapture.initializeFromIntent(getIntent(), savedInstanceState);
        HashMap hashMap = new HashMap<String, Object>();
        hashMap.put("type", mType);
        hashMap.put("callback", mCallBacks);
        mCapture.addExtra(hashMap);
        mCapture.decode();
    }

    @Override
    public void initListener(@Nullable Bundle savedInstanceState) {
        mRgScan.setOnCheckedChangeListener(this);
        mEtSearch.addTextChangedListener(this);
        mZxingViewFinderView.setOnLightTurnListener(turnOn -> onLightClick(turnOn));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCapture != null) {

            mCapture.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mZxingScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCapture!=null){
            mCapture.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCapture!=null){
            mCapture.onPause();
        }
    }

    @Override
    protected void onDestroy() {
       if(mCapture!=null){
           mCapture.onDestroy();
       }
        super.onDestroy();

    }

    @Override
    public void showLoading(String msg) {

    }

    @Override
    public void hideLoading() {

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
    public Activity getMzActivity() {
        return this;
    }

    @Override
    public void requestPermissionSuccess() {

    }

    private void onLightClick(boolean it) {
        if (it) {
            mZxingScanner.setTorchOn();
        } else {
            mZxingScanner.setTorchOff();
        }
    }

    @Override
    public void resInputCode(String equipmentNo, String institutionId) {
        EventBusManager.getInstance().post(new String[]{mCallBacks, equipmentNo, institutionId}, "inputCode");

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_scan) {
            mLlSearch.setVisibility(View.GONE);
            mRlRoot.setBackgroundResource(R.color.transparent);
        } else if (checkedId == R.id.rb_input) {
            mLlSearch.setVisibility(View.VISIBLE);
            mRlRoot.setBackgroundResource(R.color.darkgrey);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mPresenter.findProjectComboBoxList(s.toString().trim());
    }
}
