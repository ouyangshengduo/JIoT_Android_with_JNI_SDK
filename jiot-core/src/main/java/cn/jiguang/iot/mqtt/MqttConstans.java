package cn.jiguang.iot.mqtt;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/10 14:01
 * desc  :
 */
public class MqttConstans {



    /**
     * topic字符串的最大长度
     */
    public static final int MAX_SIZE_OF_STRING_TOPIC = 128;

    /**
     * MQTT Command Type
     */
    public static final int PUBLISH = 0;

    public static final int SUBSCRIBE = 1;

    public static final int UNSUBSCRIBE = 2;

    /**
     * QOS等级
     */
    public static final int QOS0 = 0;

    public static final int QOS1 = 1;

    public static final int QOS2 = 2;

    /**
     * 消息回调信息成功信息
     */
    public static final String PUBLISH_SUCCESS = "publish success";

    public static final String SUBSCRIBE_SUCCESS = "subscribe success";

    public static final String SUBSCRIBE_FAIL = "subscribe fail";

    public static final String UNSUBSCRIBE_SUCCESS = "unsubscribe success";

    public static final String MQTT_SDK_VER = "1.2.1";


    /**
     * topic
     */
    public static final String JMQTT_TOPIC_PROPERTY_SET_RSP = "pub/sys/%s/%s/property/set_resp";
    public static final String JMQTT_TOPIC_MSG_DELIVER_RSP = "pub/sys/%s/%s/msg/deliver_resp";
    public static final String JMQTT_TOPIC_PROPERTY_REPORT_REQ = "pub/sys/%s/%s/property/report";
    public static final String JMQTT_TOPIC_EVENT_REPORT_REQ = "pub/sys/%s/%s/event/report";
    public static final String JMQTT_TOPIC_VERSION_REPORT_REQ = "pub/sys/%s/%s/version/report";
    public static final String JMQTT_TOPIC_IOT_PING_REQ = "pub/sys/%s/%s/iotping/req";
    public static final String JMQTT_TOPIC_CLIENT_REQ_RECEIVE = "sub/sys/%s/%s/+/+";
    public static final String JMQTT_TOPIC_CLIENT_RECEIVE_MSG = "sub/sys/%s/*/msg/deliver";
    public static final String JMQTT_TOPIC_MSG_DELIVER_REQ = "sub/sys/%s/%s/msg/deliver";
    public static final String JMQTT_TOPIC_PROPERTY_SET_REQ = "sub/sys/%s/%s/property/set";
    public static final String JMQTT_TOPIC_PROPERTY_REPORT_RSP = "sub/sys/%s/%s/property/report_resp";
    public static final String JMQTT_TOPIC_EVENT_REPORT_RSP = "sub/sys/%s/%s/event/report_resp";
    public static final String JMQTT_TOPIC_VERSION_REPORT_RSP = "sub/sys/%s/%s/version/report_resp";
    public static final String JMQTT_TOPIC_IOT_PING_RESP = "sub/sys/%s/%s/iotping/resp";



    public enum MQTT_CONNECT_STATUS{
        /**
         * mqtt客户端空闲，未连接
         */
        CONNECT_IDLE,
        /**
         * 连接中
         */
        CONNECTING,
        /**
         * 已连接
         */
        CONNECTED,
        /**
         * 连接失败
         */
        CONNTECT_FALIED,
        /**
         * 已断开连接
         */
        DISCONNECTED,

    }

}
