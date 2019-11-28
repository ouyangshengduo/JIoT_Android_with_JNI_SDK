package cn.jiguang.iot.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import cn.jiguang.iot.http.Call;
import cn.jiguang.iot.http.Response;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:01
 * desc  :
 */

public interface PublishCallback {

    void onFailure(String topic, MqttMessage message,MqttContext mqttContext,IMqttDeliveryToken token ,Throwable throwable);
}
