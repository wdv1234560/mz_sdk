package com.mz.segiu.api.js;

import android.text.TextUtils;

import com.jess.arms.utils.Convert;

import java.util.HashMap;

import com.jess.arms.utils.RxUtils;

import com.mz.segiu.widget.webview.MzWebView;

public class JsMethodApi {
    public final static String APP_RES_SCAN_CODE = "appResScanCode";//响应二维码扫码
    public final static String APP_RES_XMG_CODE = "appResXmgCode";//响应小马哥扫码
    public final static String APP_RES_PDA_PRINT = "appResPdaPrint";//响应pda打印
    public final static String APP_RES_TH_PRINT = "appResThPrint";//响应台秤打印
   public final static String APP_RES_BLE_CONNECTED = "appResBleConnected";//响应蓝牙状态
   public final static String APP_RES_USB_CONNECTED = "appResUsbConnected";//响应USB状态
   public final static String APP_RES_CONNECT_USB = "appResConnectUsb";//响应USB状态
   public final static String APP_RES_FILE_BASE64 = "appResFileBase64";//响应filebase64

    /*=========================巡检离线缓存start===========================*/
    public final static String RES_SAVE_USER_INFO="resSaveUserInfo";
    public final static String RES_OFF_LOGIN="resOffLogin";
    public final static String RES_SAVE_SERVICE_DATA="resSaveServiceData";
    public final static String RES_LOAD_SERVICE_DATA="resLoadServiceData";
    public final static String RES_LOAD_FILE_DATA="resLoadFileData";
    public final static String RES_DELETE_CACHE_BY_ID="resDeleteCacheById";
    public final static String RES_DELETE_ALL_CACHE="resDeleteAllCache";
    public final static String RES_LOAD_USER_INFO="resLoadUserInfo";
    public final static String RES_CLEAR_USER="resClearUser";
    /*=========================巡检离线缓存end===========================*/
    public final static String BASE_URL = "javascript:";


    /**
     * @param value
     * @return
     */
    public static String doJsByJson(String method, String value) {
        if (TextUtils.isEmpty(method)) {
            //ArmsUtils.makeText("js方法名为空");
        }
        return "javascript:" + method + "(" + Convert.toJson(value) + ")";
    }

    /**
     * @param value
     * @return
     */
    public static String doJsByStr(String method, String value) {
        if (TextUtils.isEmpty(method)) {
            //ArmsUtils.makeText("js方法名为空");
        }
        return "javascript:" + method + "('" + Convert.toJson(value) + "')";
    }

    /**
     * @param value
     * @return 高德地位信息
     */
    public static String doJsByMap(String method, HashMap value) {
        if (TextUtils.isEmpty(method)) {
            //ArmsUtils.makeText("js方法名为空");
        }
        return "javascript:" + method + "(" + Convert.toJson(value) + ")";
    }

    /**
     * @param value
     * @return
     */
    public static void webLoadUrl(MzWebView webView, String method, HashMap value, String requestCode) {
        if (TextUtils.isEmpty(method)) {
            //ArmsUtils.makeText("js方法名为空");
        }
        value.put("requestCode", requestCode);
        String url = "javascript:" + method + "(" + Convert.toJson(value) + ")";
        RxUtils.runOnMainThread(o -> {
            webView.loadUrl(url);
        });
    }
    public static void webLoadUrl(MzWebView webView, String method, int  code, String message,String requestCode) {
        if (TextUtils.isEmpty(method)) {
            //ArmsUtils.makeText("js方法名为空");
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", message);
        map.put("requestCode", message);
        String url = "javascript:" + method + "(" + Convert.toJson(map) + ")";
        RxUtils.runOnMainThread(o -> {
            webView.loadUrl(url);
        });
    }
//
//    /**
//     * @param value
//     * @return 高德地位信息
//     */
//    public static String appResLocation(LocationEntity value) {
//        return "javascript:appResLocation('" + Convert.toJson(value) + "')";
//    }

    /*---------------硬件相关start---------------*/

    /**
     * @param value
     * @return 便携式蓝牙体重秤重量
     */
    public static String appResWeightBxs(float value) {
        return "javascript:appResWeightBxs('" + value + "')";
    }

    /**
     * @param value
     * @return 台衡台秤
     */
    public static String appResThWeight(String value) {
        return "javascript:appResThWeight('" + value + "')";
    }
    /*---------------硬件相关end---------------*/

}
