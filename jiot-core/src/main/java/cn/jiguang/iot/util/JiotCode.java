package cn.jiguang.iot.util;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/17 9:55
 * desc  :
 */
public class JiotCode {

    public static final int JIOT_SUCCESS = 0;

    public static final int JIOT_ERR_MQTT_CONNECT_ERROR = 11028;

    public static final int JIOT_ERR_SIS_HTTP_FAIL = 14001;

    public static final int JIOT_ERR_SIS_CONTENT_ERROR = 14002;

    public static final int JIOT_ERR_SIS_JSON_PARSE_FAIL = 14003;
    //JSON解析异常
    public static final int  JIOT_ERR_JCLI_JSON_PARSE_ERR = 12002;
    //product_key超过长度
    public static final int JIOT_ERR_PRODUCTKEY_OVERLONG = 10002;
    //deviceName超过长度
    public static final int JIOT_ERR_DEVICENAME_OVERLONG = 10003;
    //productKey超过长度
    public static final int JIOT_ERR_DEVICESECRET_OVERLONG = 10004;
    //序号错误
    public static final int JIOT_ERR_SEQNO_ERROR = 10006;
    //参数异常
    public static final int JIOT_ERR_ARGU_FORMAT_ERROR = 10009;

    public static final int JIOT_ERR_VERSION_FORMAT_ERROR = 10010;

    public static final int JIOT_ERR_PROPERTY_FORMAT_ERROR = 10011;

    public static final int JIOT_ERR_PROPERTY_NAME_FORMAT_ERROR = 10012;

    public static final int JIOT_ERR_PROPERTY_VALUE_FORMAT_ERROR = 10013;

    public static final int JIOT_ERR_EVENT_FORMAT_ERROR = 10014;

    /**
     * mqtt状态异常
     */
    public static final int JIOT_ERR_MQTT_STATE_ERROR = 11013;

    //MQTT断开连接
    public static final int JIOT_ERR_DISCONNECT_ERROR = 11033;

}
