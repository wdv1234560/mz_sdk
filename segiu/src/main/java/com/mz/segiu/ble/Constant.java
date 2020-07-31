package com.mz.segiu.ble;

/**
 * Created by Administrator
 *
 * @author 猿史森林
 *         Date: 2017/10/14
 *         Class description:
 */
public interface Constant {
   String SERIALPORTPATH = "SerialPortPath";
   String SERIALPORTBAUDRATE = "SerialPortBaudrate";
   String WIFI_CONFIG_IP = "wifi config ip";
   String WIFI_CONFIG_PORT = "wifi config port";
   String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
   int BLUETOOTH_REQUEST_CODE = 0x001;
   int USB_REQUEST_CODE = 0x002;
   int WIFI_REQUEST_CODE = 0x003;
   int SERIALPORT_REQUEST_CODE = 0x006;
   int CONN_STATE_DISCONN = 0x007;
   int MESSAGE_UPDATE_PARAMETER = 0x009;
   int CONNECT_TYPE_SJ = 1;
   int CONNECT_TYPE_TH = 2;
   int tip=0x010;
   int abnormal_Disconnection=0x011;//异常断开

    /**
     * wifi 默认ip
     */
    String WIFI_DEFAULT_IP = "192.168.123.100";

    /**
     * wifi 默认端口号
     */
    int WIFI_DEFAULT_PORT = 9100;
}