package com.mz.segiu.api.js;

import org.jetbrains.annotations.NotNull;

public enum JsCode {
    SUCCESS(200, "成功"),
    PRINT_SUCCESS(201, "打印成功"),
    USB_SUCCESS(202, "usb连接成功"),
    SEND_BLE_PRINT_DATA_SUCCESS(203, "发送蓝牙打印数据成功"),
    USB_CONNECTING(301, "usb连接中"),
    USB_DISCONNECT(302, "usb断开连接"),
    FAILED(400, "失败"),
    NO_USB(401, "没有usb设备或端口被占用"),
    USB_FAILED(402, "usb连接失败"),
    PRINT_FAILED(403, "打印失败"),
    SEND_BLE_PRINT_DATA_FAILED(404, "发送蓝牙打印数据失败"),
    PRINT_PAPER_ERR(501, "打印机缺纸"),
    PRINT_COVER_OPEN(502, "打印机开盖"),
    PRINT_ERR_OCCURS(503, "打印机出错"),
    PRINT_DATA_NULL(504, "打印信息为空");

    public final int code;
    public final String msg;

    public final int getCode() {
        return this.code;
    }

    public final String getMsg() {
        return this.msg;
    }

    private JsCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @NotNull
    public static String getMsg(int code) {
        if (code == JsCode.SUCCESS.getCode()) {
            return JsCode.SUCCESS.getMsg();
        } else if (code == JsCode.PRINT_SUCCESS.getCode()) {
            return JsCode.PRINT_SUCCESS.getMsg();
        } else if (code == JsCode.USB_SUCCESS.getCode()) {
            return JsCode.USB_SUCCESS.getMsg();
        } else if (code == JsCode.SEND_BLE_PRINT_DATA_SUCCESS.getCode()) {
            return JsCode.SEND_BLE_PRINT_DATA_SUCCESS.getMsg();
        } else if (code == JsCode.FAILED.getCode()) {
            return JsCode.FAILED.getMsg();
        } else if (code == JsCode.NO_USB.getCode()) {
            return JsCode.NO_USB.getMsg();
        } else if (code == JsCode.USB_FAILED.getCode()) {
            return JsCode.USB_FAILED.getMsg();
        } else if (code == JsCode.PRINT_FAILED.getCode()) {
            return JsCode.PRINT_FAILED.getMsg();
        } else if (code == JsCode.SEND_BLE_PRINT_DATA_FAILED.getCode()) {
            return JsCode.SEND_BLE_PRINT_DATA_FAILED.getMsg();
        } else if (code == JsCode.PRINT_PAPER_ERR.getCode()) {
            return JsCode.PRINT_PAPER_ERR.getMsg();
        } else if (code == JsCode.PRINT_COVER_OPEN.getCode()) {
            return JsCode.PRINT_COVER_OPEN.getMsg();
        } else {
            return code == JsCode.PRINT_ERR_OCCURS.getCode() ? JsCode.PRINT_ERR_OCCURS.getMsg() : "";
        }
    }

}