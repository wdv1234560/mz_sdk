package com.mz.segiu.api.js;

public interface JsUrl {

//        String BASE_URL = "https://app.u-zf.com/#/";//生产
//        String BASE_URL = "https://ftp.u-zf.com/#/";//生产-灰度
        String BASE_URL = "http://192.168.0.215:8080/#/";//210
//        String BASE_URL="http://192.168.0.203:80/#/";
//        String BASE_URL="http://192.168.1.94:8081/#/";//刘波
        String POPUP = "popup";
        String MAIN_PAGE = "mainPage";
        String TRANSFER_PAGE = BASE_URL + "transferPage";
        String CLIENT_REGISTER = BASE_URL + "clientRegister";
        String FORGET_PASSWORD = BASE_URL + "forgetPassword";
        String BKLOG = BASE_URL + "staffPersonalCenter";
}