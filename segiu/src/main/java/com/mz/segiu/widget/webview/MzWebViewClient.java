package com.mz.segiu.widget.webview;

//import android.webkit.WebView;
//import android.webkit.WebViewClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.RxUtils;
import com.jess.arms.widget.ProgresDialog;
import com.mz.segiu.R;
import com.mz.segiu.utils.SpUtils;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import org.jetbrains.annotations.NotNull;

import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst;
import timber.log.Timber;

public final class MzWebViewClient extends WebViewClient {
    private WebView mWebView;
    private Activity mActivity;
    private OnJsCallListener mOnJsCallListener;
    private ProgresDialog mLoadingDialog;
    private boolean mIsLoadWebError;
    private boolean mShowLoading = true;
    private View mErrorView;

    public MzWebViewClient(@Nullable WebView webView, @Nullable Activity activity, @Nullable OnJsCallListener onJsCallListener) {
        this.mWebView = webView;
        this.mActivity = activity;
        this.mOnJsCallListener = onJsCallListener;
        this.mLoadingDialog = new ProgresDialog(mActivity);
        this.initErrorView();
    }

    private void initErrorView() {
        this.mErrorView = View.inflate(this.mActivity, R.layout.layout_net_error, (ViewGroup) null);
        if (mErrorView != null) {
            TextView var3 = mErrorView.findViewById(R.id.tv_reload);
            if (var3 != null) {
                var3.setOnClickListener(v-> {
                        MzWebViewClient.this.reload();
                });
            }
        }

        WebView var4 = this.mWebView;
        ViewParent var5 = mWebView != null ? mWebView.getParent() : null;
        if (var5 != null) {
            ViewGroup parent = (ViewGroup) var5;
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-1, -1);
            parent.addView(this.mErrorView, 1, layoutParams);
        }
    }

    public final void reload() {
        this.mIsLoadWebError = false;
        this.showErrorView();
        if (mWebView != null) {
            mWebView.reload();
        }

    }

    @Nullable
    public WebResourceResponse shouldInterceptRequest(@NotNull WebView webView, @Nullable String url) {
        Timber.d("shouldInterceptRequest url========%s", url);
        return WebResourceResponseAdapter.adapter(WebViewCacheInterceptorInst.getInstance().interceptRequest(url));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    public WebResourceResponse shouldInterceptRequest(@Nullable WebView webView, @Nullable WebResourceRequest webResourceRequest) {
        return WebResourceResponseAdapter.adapter(WebViewCacheInterceptorInst.getInstance().interceptRequest(WebResourceRequestAdapter.adapter(webResourceRequest)));
    }

    public boolean shouldOverrideUrlLoading(@Nullable WebView p0, @Nullable WebResourceRequest p1) {
        return super.shouldOverrideUrlLoading(p0, p1);
    }

    public boolean shouldOverrideUrlLoading(@Nullable WebView view, @Nullable String url) {

        if (!TextUtils.isEmpty(url) && url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ArmsUtils.startActivity(intent);
            return true;
        } else {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    public void onPageStarted(@Nullable WebView view, @Nullable String url, @Nullable Bitmap favicon) {
        Timber.d("onPageStarted url========%s", url);
        super.onPageStarted(view, url, favicon);
        if (this.mLoadingDialog != null && this.mShowLoading) {
            ProgresDialog var10000 = this.mLoadingDialog;
            if (var10000 != null) {
                var10000.show();
            }
        }

        if (this.mOnJsCallListener != null) {
            OnJsCallListener var4 = this.mOnJsCallListener;
            if (var4 != null) {
                var4.pageStarted(url);
            }
        }

    }

    public void onPageFinished(@Nullable WebView view, @Nullable final String url) {
        super.onPageFinished(view, url);
        this.writeData();
        RxUtils.runOnMainThread(it->{
                if (!MzWebViewClient.this.mIsLoadWebError) {
                    MzWebViewClient.this.showErrorView();
                }

                MzWebViewClient.this.closeLoading(url);
        });
    }

    public void onReceivedError(@Nullable WebView view, int errorCode, @Nullable String description, @Nullable String failingUrl) {
        Timber.i("onReceivedError: ------->errorCode" + errorCode + ':' + description);
        this.mIsLoadWebError = true;
        this.showErrorView();
        this.closeLoading(failingUrl);
    }

    private void showErrorView() {
        if (this.mIsLoadWebError) {
            if (mErrorView != null) {
                mErrorView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mErrorView != null) {
                mErrorView.setVisibility(View.GONE);
            }
        }

    }

    public final void closeLoading(@Nullable String url) {
        if (this.mLoadingDialog != null && this.mShowLoading) {
            ProgresDialog var10000 = this.mLoadingDialog;
            if (var10000 != null) {
                var10000.dismiss();
            }
        }

        if (this.mOnJsCallListener != null) {
            OnJsCallListener var2 = this.mOnJsCallListener;
            if (var2 != null) {
                var2.pageFinished(url);
            }
        }

    }

    public final void setShowLoading(boolean b) {
        this.mShowLoading = b;
    }

    public final void writeData() {
        String key = "TOKEN_APP";
        String token = SpUtils.getInstance(mActivity).getToken();
        WebView var3;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mWebView != null) {
                mWebView.evaluateJavascript("window.localStorage.setItem('" + key + "','" + token + "');", null);
            }
        } else {
            if (mWebView != null) {
                mWebView.loadUrl("javascript:localStorage.setItem('" + key + "','" + token + "');");
            }
        }

    }

}
