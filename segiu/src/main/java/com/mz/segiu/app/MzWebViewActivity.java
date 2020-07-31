package com.mz.segiu.app;

import android.os.Bundle;

import com.jess.arms.base.BaseActivity;
import com.jess.arms.mvp.IPresenter;
import com.jess.arms.utils.ArmsUtils;
import com.mz.segiu.R;

import com.mz.segiu.api.js.JsUrl;
import com.mz.segiu.widget.webview.MzWebView;

public abstract class MzWebViewActivity<P extends IPresenter> extends BaseActivity<P> {

    protected MzWebView mWebView;
    private long exitTime;

    @Override
    protected void findWebView(Bundle savedInstanceState) {
        try {
            mWebView = findViewById(R.id.web_view);
            mWebView.initConfig(this);
            mWebView.setOnLongClickListener(v -> true);
        } catch (Exception e) {
            throw new IllegalArgumentException("布局文件没有MzWebView");
        }

    }

    public void setWebView(MzWebView webView) {
        mWebView = webView;
    }

    @Override
    public void onBackPressed() {
//        if (mWebView != null) {
//            String url = mWebView.getUrl();
//            String[] split = url.split("\\?");
//            boolean isPopup;
//            try {
//                isPopup = split[1].contains(JsUrl.POPUP);
//            } catch (Exception e) {
//                isPopup = false;
//            }
//            if (mWebView.canGoBack()) {
//                if (url != null && url.contains(JsUrl.MAIN_PAGE)) {
//                    if (isPopup) {
//                        mWebView.goBack();
//                    } else {
//                        exitApp();
//                    }
//
//                } else {
//                    mWebView.goBack();
//                }
//            } else {
//                super.onBackPressed();
//            }
//        } else {
//
//            super.onBackPressed();
//        }
            super.onBackPressed();
    }

    /**
     * 双击退出App
     */
    private void exitApp() {

        if (System.currentTimeMillis() - exitTime > 2000) {
            ArmsUtils.makeText("再按一次退出");
            exitTime = System.currentTimeMillis();
        } else {
            ArmsUtils.exitApp();
        }
    }
}
