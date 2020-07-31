package com.mz.segiu.api.js;

public interface JsApi {
        String LOAD_H5_SUCCESS="appLoadH5Success";
        String APP_START_ACTIVITY="appStartActivity";
        String GET_GAODE_LOCATION = "appGetGaoDeLocation";
        String BARCODESCANNER_SCAN = "appBarcodescannerScan";
        String APP_GET_FILE_BASE64 = "appGetFileBase64";
        String CAMERA_UPLOAD = "appCameraUpload";
        String SCAN_BLUETOOTH = "appScanBluetooth";
        String APP_DISCONNECT_BLE="appDisConnectBle";
        String TH_PRINT = "appThPrint";
        String GET_TH_WEIGHT = "appGetThWeight";
        String GET_SJ_WEIGHT = "appGetSjWeight";
        String PDA_PRINT = "appPdaPrint";
        String GALLERY_UPLOAD = "appGalleryUpload";
        String FILE_UPLOAD = "appFileUpload";
        String CLEAR_CACHE = "appClearCache";
        String GET_CACHE_SIZE = "appGetCacheSize";
        String DOWNLOAD_FILE = "appDownloadFile";
        String PHONE_DEVICE = "appPhoneDevice";
        String MEDIA_START_RECORD = "appMediaStartRecord";
        String MEDIA_STOP_RECORD = "appMediaStopRecord";
        String PDA_SCAN = "appPdaScan";
        String APP_BLE_CONNECTED = "appBleConnected";
        String APP_USB_CONNECTED = "appUsbConnected";
        String APP_CONNECT_USB = "appConnectUsb";
        String APP_BACK_PAGE = "appBackPage";
        String APP_LOGOUT="appLogout";
        String APP_TOKEN_TIMEOUT="appTokenTimeOut";
        String APP_TO_BACKLOG="appToBacklog";
        String APP_SET_ORG_DATA="appSetOrgData";
}