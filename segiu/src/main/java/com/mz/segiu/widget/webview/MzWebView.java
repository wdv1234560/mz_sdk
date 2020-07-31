package com.mz.segiu.widget.webview;

//import android.webkit.*
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import com.google.gson.internal.LinkedTreeMap;
import com.jess.arms.integration.EventBusManager;
import com.jess.arms.utils.Convert;
import com.jess.arms.utils.DeviceUtils;
import com.jess.arms.utils.RxUtils;
import com.mz.segiu.api.js.JsMethodApi;
import com.mz.segiu.app.MzWebViewActivity;
import com.mz.segiu.mvp.model.entity.BaseFormJsEntity;
import com.mz.segiu.mvp.model.entity.DeviceEntity;
import com.mz.segiu.utils.SpUtils;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import io.reactivex.functions.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simple.eventbus.Subscriber;

import timber.log.Timber;
import java.util.*;

public final class MzWebView extends WebView {
    private Context mContext;
    private MzWebViewActivity mActivity;
    private OnJsCallListener mOnJsCallListener;
    private MzWebViewClient mMzWebViewClient;
    private String mRequestCode = "";

    private final void init(Context context) {
        EventBusManager.getInstance().register(this);
        this.mContext = context;
        WebSettings settings = this.getSettings();
        settings.setCacheMode(-1);
        settings.setAppCacheEnabled(true);
        settings.setSupportZoom(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
        this.addJavascriptInterface(this, "android");
        this.setWebChromeClient((WebChromeClient)(new WebChromeClient() {
            public void onProgressChanged(@Nullable WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        }));
        this.setOnLongClickListener(v -> true);
    }

    @Subscriber(
            tag = "clearCache"
    )
    public final void onClearCacheEvent(boolean b) {
        this.clearCache(true);
        this.clearHistory();
    }

    public void loadUrl(@Nullable String url) {
        super.loadUrl(url);
    }

    @JavascriptInterface
    public final void callMz(@Nullable final String data) {
        RxUtils.runOnMainThread(it -> {
            MzWebView.this.doJsCall(data);
            if (mOnJsCallListener != null) {
                if (mOnJsCallListener != null) {
                    mOnJsCallListener.onJsCall(data);
                }
            }

        });
    }

    private final void doJsCall(String data) {
        BaseFormJsEntity json = (BaseFormJsEntity)Convert.fromJson(data, BaseFormJsEntity.class);
        if (json.parameters != null) {
            LinkedTreeMap params = (LinkedTreeMap)json.parameters;
            String var10001 = json.requestCode;
            this.mRequestCode = var10001;
            String var12 = json.methodName;
            if (var12 != null) {
                String var4 = var12;
                switch(var4.hashCode()) {
                    case -1655906871:
                        if (var4.equals("appTokenTimeOut")) {
                            this.toLogin(params);
                        }
                        break;
                    case -1426244265:
                        if (var4.equals("appBackPage") && this.mActivity != null) {
                            MzWebViewActivity var14 = this.mActivity;
                            if (var14 != null) {
                                var14.onBackPressed();
                            }
                        }
                        break;
                    case -1046189331:
                        if (var4.equals("appSetOrgData")) {
                            try {
                                SpUtils var13 = SpUtils.getInstance(getContext());
                                Map var5 = (Map)params;
                                String var6 = "orgData";
                                String var9 = "orgRecord";
                                SpUtils var8 = var13;
                                boolean var7 = false;
                                Object var10 = var5.get(var6);
                                if (var10 == null) {
                                }

                                var8.putString(var9, (String)var10);
                            } catch (Exception var11) {
                                Timber.e((Throwable)var11);
                            }
                        }
                        break;
                    case -300092191:
                        if (var4.equals("appToBacklog")) {
                            EventBusManager.getInstance().post(true, "appToBacklog");
                            EventBusManager.getInstance().post(true, "showBottom");
                        }
                        break;
                    case 1294507787:
                        if (var4.equals("appLogout")) {
                            this.toLoginGuide();
                        }
                        break;
                    case 1450523779:
                        if (var4.equals("appPhoneDevice")) {
                            this.getDeviceInfo(json.callBack);
                        }
                        break;
                    case 1841990896:
                        if (var4.equals("appStartActivity")) {
                            EventBusManager.getInstance().post(false, "showBottom");
                        }
                        break;
                }
            }

        }
    }

    public void reloadUrl() {
        if (mMzWebViewClient != null) {
            mMzWebViewClient.reload();
        }

    }

    public void webLoadUrl(@NotNull String method, int code, @NotNull String message, @Nullable String requestCode) {
        if (!TextUtils.isEmpty((CharSequence)method)) {
            HashMap map = new HashMap();
            ((Map)map).put("code", code);
            ((Map)map).put("message", message);
            Map var10000 = (Map)map;
            String var10002 = requestCode;
            if (requestCode == null) {
                var10002 = "";
            }

            var10000.put("requestCode", var10002);
            final String url = "javascript:" + method + "(" + Convert.toJson(map) + ")";
            RxUtils.runOnMainThread((Consumer)(new Consumer() {
                public final void accept(@Nullable Object o) {
                    MzWebView.this.loadUrl(url);
                }
            }));
        }
    }

    public final void getDeviceInfo(@Nullable final String callBack) {
        final String deviceId = DeviceUtils.getDeviceId(this.getContext());
        final int versionCode = DeviceUtils.getVersionCode(this.getContext());
        final String versionName = DeviceUtils.getVersionName(this.getContext());
        RxUtils.runOnMainThread((Consumer)(new Consumer() {
            public final void accept(Object it) {
                MzWebView.this.loadUrl(JsMethodApi.doJsByJson(callBack, Convert.toJson(new DeviceEntity(deviceId, versionCode, versionName))));
            }
        }));
    }

    private final void toLoginGuide() {
    }

    private final void toLogin(LinkedTreeMap params) {
    }

    public final void initConfig(@NotNull MzWebViewActivity activity) {
        this.mActivity = activity;
        this.mMzWebViewClient = new MzWebViewClient((WebView)this, (Activity)this.mActivity, this.mOnJsCallListener);
        this.setWebViewClient((WebViewClient)this.mMzWebViewClient);
    }

    public void initConfig(@NotNull MzWebViewActivity activity, boolean showLoading) {
        this.mActivity = activity;
        this.mMzWebViewClient = new MzWebViewClient(this, mActivity, this.mOnJsCallListener);
        if (mMzWebViewClient != null) {
            mMzWebViewClient.setShowLoading(showLoading);
        }

        this.setWebViewClient((WebViewClient)this.mMzWebViewClient);
    }

    public void setOnJsCallListener(@NotNull OnJsCallListener onJsCallListener) {
        this.mOnJsCallListener = onJsCallListener;
    }

    protected void onDetachedFromWindow() {
        if (mMzWebViewClient != null) {
                mMzWebViewClient.closeLoading(this.getUrl());
        }

        super.onDetachedFromWindow();
    }

    public MzWebView(@Nullable Context context) {
        super(context);
        this.init(context);
    }

    public MzWebView(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public MzWebView(@Nullable Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

}