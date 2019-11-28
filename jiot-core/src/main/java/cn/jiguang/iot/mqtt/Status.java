package cn.jiguang.iot.mqtt;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/8 9:44
 * desc  :
 */

public enum Status {
    /**
     * Indicates that the operation succeeded
     */
    OK,

    /**
     * Indicates that the operation failed
     */
    ERROR,

    /**
     * Indicates that the operation's result may be returned asynchronously
     */
    NO_RESULT,

    /**
     * Indicates that the operation's result may be timeout
     */
    TIME_OUT,
    /**
     * Indicates that the parameter is invalid
     */
    PARAMETER_INVALID,

    /**
     * Indicates that the MQTT connecting is in progress
     */
    MQTT_CONNECT_IN_PROGRESS,


    /**
     * Indicates that the mqtt connection is not established
     */
    MQTT_NO_CONN,

    /**
     * Indicates that the mqtt topic is not subscribed
     */
    ERROR_TOPIC_UNSUBSCRIBED,

    /**
     * 表示超过JSON文档中的最大TOKEN数
     */
    ERR_MAX_JSON_TOKEN,

    /**
     * 表示文档请求数超并发
     */
    ERR_MAX_APPENDING_REQUEST,

    /**
     *  子设备初始状态
     */
    SUBDEV_STAT_INIT,

    /**
     *  子设备在线状态
     */
    SUBDEV_STAT_ONLINE,

    /**
     *  子设备掉线状态
     */
    SUBDEV_STAT_OFFLINE,

    /**
     *  子设备不存在
     */
    SUBDEV_STAT_NOT_EXIST
}
