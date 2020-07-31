package com.mz.segiu.api;

public interface Api {

    //        const val BASE_URL = "https://www.u-zf.com/"//生产，更改BASE_URL,需要和BASE_SERVICE一起更改
    String BASE_URL = "http://192.168.0.210:7979";
//    String BASE_URL = "http://192.168.0.203:7979/";//203
//        const val BASE_URL = "http://192.168.1.193:7979/";//203

    //        const val BASE_UPLOAD_URL = "https://ftp.u-zf.com/";//生产
    String BASE_UPLOAD_URL = BASE_URL;//测试

    //        private const val BASE_SERVICE = "gateway/"//生产
    String BASE_SERVICE = "";//测试

    String UPLOAD_FILE = BASE_SERVICE + "zuul/ftp/api/fileOperator/uploadFile";
    String APP_UPDATE = BASE_SERVICE + "uac/api/appVersion/getNewVersion";
    /**
     * -------------平台接口start-------------
     */
    String LOGIN = BASE_SERVICE + "uac/api/user/appUserLogin";
    //获取客户已选项目
    String GET_APP_USER_RECORD = BASE_SERVICE + "uac/api/appUserRecord/getAppUserRecord";
    String FIND_PROJECT_COMBO_BOX_LIST = BASE_SERVICE + "uac/api/organizationAuthScope/findProjectComboBoxList";
    /**-------------平台接口end------------- */

    /**-------------设备接口start------------- */
    String GET_EQUIPMENT_DETAILS = BASE_SERVICE + "ewo/api/equipmentInfo/getEquipmentDetails";
    /**-------------设备接口end------------- */
    /**
     * -------------医废接口start-------------
     */
    //校验录入权限、项目是否在盘点并获取医废配置
    String VERIFY_YF_CONFIG = BASE_SERVICE + "mws/api/pc/wasteConfig/verifyAndGetConfig";
    String GET_PROCESS_CONFIG = BASE_SERVICE + "mws/api/pc/wasteConfig/getProcessConfig";
    String GET_AUTHORIZE_USER = BASE_SERVICE + "mws/api/pc/authorize/getAuthorizeUser";
    //判断是否正在盘点-APP
    String JUDGE_MAKE_INVENTORY_FLAG = BASE_SERVICE + "mws/api/pc/wasteInfo/judgeMakeInventoryFlag";
    /**-------------医废接口end------------- */

    /**
     * -------------运送接口start-------------
     */
    String TRANSFER_ORDER = BASE_SERVICE + "tms/app/order/transferOrder";//校验录入权限、项目是否在盘点并获取医废配置
    String GET_RULE_CONFIG = BASE_SERVICE + "tms/basic/getRuleConfig";//校验录入权限、项目是否在盘点并获取医废配置
    /**-------------运送接口end------------- */
}