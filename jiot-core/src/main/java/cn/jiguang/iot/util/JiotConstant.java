package cn.jiguang.iot.util;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/4/19 9:16
 * desc  :
 */
public interface JiotConstant {

    int SIS_PROTOCOL_TYPE_TCP = 0;
    int SIS_PROTOCOL_TYPE_SSL = 1;

    String SDK_VERSION = "1.0.2";
    int SDK_BUILDID = 37;
    String INIT_LOG = "jiotInit()|SDK_VERSION[%s] SDK_BUILDID[%d]";
    String CONN_LOG = "jiotConn()| enter method .";
    String DISCONN_LOG = "jiotDisConn()| enter method .";
    String RELEASE_LOG = "jiotRelease()| enter method .";
    String GET_CONN_STATUS_LOG = "jiotGetConnStatus()| enter method .";
    String REPORT_PROPERTY_LOG = "jiotPropertyReportReq()| enter method .";
    String REPORT_EVENT_LOG = "jiotEventReportReq()| enter method .";
    String REPORT_VERSION_LOG = "jiotVersionReportReq()| enter method .";
    String SET_LOG_LEVEL_LOG = "jiotSetLogLevel()| enter method . log level[%d]";

    /**
     * #define PRODUCT_KEY_MAX_LEN     24
     * #define DEVICE_NAME_MAX_LEN     24
     * #define DEVICE_SECRET_MAX_LEN   24
     */
    int PRODUCT_KEY_MAX_LEN = 24;
    int DEVICE_NAME_MAX_LEN = 24;
    int DEVICE_SECRET_MAX_LEN = 24;

    enum CLIENT_STATUS{
        /**
         * 对象未实例化
         */
        NONE,
        /**
         * 已经初始化完成
         */
        CLIENT_INITIALIZED,
        /**
         * 连接中
         */
        CLIENT_CONNECTING,
        /**
         * 已连接
         */
        CLIENT_CONNECTED,
        /**
         * 断开中
         */
        CLIENT_DISCONNECTING,
        /**
         * 已断开
         */
        CLIENT_DISCONNECTED,
        /**
         * 重连中
         */
        CLIENT_RECONNECTING
    }

}
