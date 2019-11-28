package cn.jiguang.iot.mqtt;

import android.content.Context;
import android.support.annotation.NonNull;

import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSuback;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.jiguang.iot.bean.DeviceInfo;
import cn.jiguang.iot.util.JiotConstant;
import cn.jiguang.iot.util.JiotLogger;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/7 17:21
 * desc  :
 */
public class MqttConnection implements MqttCallback {

    private DeviceInfo deviceInfo;
    private MqttHandleCallback mqttHandleCallback;
    protected MqttAsyncClient mqttClient = null;
    private JiotAlarmPingSender alarmPingSender;
    private String serverURI;
    private String clientId;
    private Context context;
    private static int INVALID_MESSAGE_ID = -1;
    protected int mLastReceivedMessageId = INVALID_MESSAGE_ID;
    private HashMap<String, Integer> subscribedTopicCache = new HashMap<>();
    private int mqttReconnectTimes = 0;
    private JiotAlarmMqttReconnect alarmMqttReconnect;
    private MqttConnectOptions options;
    private volatile MqttConstans.MQTT_CONNECT_STATUS clientConnectStatus = MqttConstans.MQTT_CONNECT_STATUS.CONNECT_IDLE;
    //声明一个线程池
    private ExecutorService executorService;
    /**
     * 断连状态下buffer缓冲区，当连接重新建立成功后自动将buffer中数据写出
     */
    protected DisconnectedBufferOptions bufferOpts = null;


    public String getClientId() {
        return clientId;
    }

    public DeviceInfo getDeviceInfo(){
        return deviceInfo;
    }

    public MqttConnection(Context context, MqttHandleCallback mqttHandleCallback){
        this.context = context;
        this.mqttHandleCallback = mqttHandleCallback;
    }

    /**
     * 释放资源
     */
    public void releaseAlarm(){
        if(null != alarmPingSender){
            alarmPingSender.stop();
        }
        if(null != alarmMqttReconnect){
            alarmMqttReconnect.stop();
        }
    }

    /**
     * 关闭当前连接
     */
    public void close(){

        if(null != mqttClient){
            try {
                mqttClient.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized Status connect(@NonNull final MqttContext mqttContext, @NonNull final String sisServer, @NonNull DeviceInfo deviceInfo,int protocolType){
        if(protocolType == JiotConstant.SIS_PROTOCOL_TYPE_SSL) {
            this.serverURI = "ssl://" + sisServer;
        }else{
            this.serverURI = "tcp://" + sisServer;
        }
        this.deviceInfo = deviceInfo;
        this.clientId = deviceInfo.getProductKey() + "." + deviceInfo.getDeviceName();


        if(null != mqttClient && mqttClient.isConnected()){
            return Status.ERROR;
        }
        IMqttActionListener mActionListener = new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken token) {
                setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNECTED);
                mqttHandleCallback.onConnectCompleted(Status.OK, false, token.getUserContext(), "connected to " + serverURI);
            }

            @Override
            public void onFailure(IMqttToken token, Throwable exception) {
                JiotLogger.e( "onFailure! ",exception);
                setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNTECT_FALIED);
                if(null != mqttClient){
                    alarmPingSender.stop();
                    try {
                        mqttClient.close();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                mqttHandleCallback.onConnectCompleted(Status.ERROR, false, token.getUserContext(), exception.toString());
            }
        };

        bufferOpts = new DisconnectedBufferOptions();
        bufferOpts.setBufferEnabled(true);
        bufferOpts.setBufferSize(1024);
        //bufferOpts.setDeleteOldestMessages(true);
        try {
            alarmPingSender = new JiotAlarmPingSender(context);
            mqttClient = new MqttAsyncClient(serverURI,clientId,null,alarmPingSender );
            mqttClient.setCallback(this);
            mqttClient.setBufferOpts(bufferOpts);
            mqttClient.setManualAcks(false);
        } catch (MqttException e) {
            e.printStackTrace();
            setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNTECT_FALIED);
            return Status.ERROR;
        }
        JiotLogger.d("mqtt connect server uri : " + serverURI + " and client id : " + clientId);
        options = new MqttConnectOptions();
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(300);
        //options.setAutomaticReconnect(true);
        options.setUserName(deviceInfo.getProductKey());
        options.setPassword(deviceInfo.getDeviceSecret().toCharArray());
        if(protocolType == JiotConstant.SIS_PROTOCOL_TYPE_SSL) {
            options.setSocketFactory(MqttSslUtils.getSocketFactory());
        }
        try {
            JiotLogger.d("Start connecting to " + serverURI);
            setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNECTING);
            mqttClient.connect(options,mqttContext,mActionListener);
        } catch (MqttException e) {
            JiotLogger.e("MqttClient connect failed", e);
            setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNTECT_FALIED);
            return Status.ERROR;
        }
        return Status.OK;
    }


    /**
     * 重新连接, 结果通过回调函数通知。
     *
     * @return 发送请求成功时返回Status.OK; 其它返回值表示发送请求失败；
     */
    public synchronized Status reconnect() {
        if (mqttClient == null) {
            JiotLogger.e("Reconnect mqttClient = null. Will not do reconnect");
            return Status.MQTT_NO_CONN;
        }

        if (getConnectStatus().equals(MqttConstans.MQTT_CONNECT_STATUS.CONNECTING)) {
            JiotLogger.d("The client is connecting. Reconnect return directly.");
            return Status.MQTT_CONNECT_IN_PROGRESS;
        }

        if(null != mqttHandleCallback){
            mqttHandleCallback.onStartReConnecting();
        }
        if (options.isAutomaticReconnect() && !getConnectStatus().equals(MqttConstans.MQTT_CONNECT_STATUS.CONNECTING)) {
            JiotLogger.d("Requesting Automatic reconnect using New Java AC");
            try {
                mqttClient.reconnect();
            } catch (Exception ex) {
                JiotLogger.e("Exception occurred attempting to reconnect: ", ex);
                setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNTECT_FALIED);
                return Status.ERROR;
            }
        } else {
            final IMqttActionListener listenerReconnect = new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken token) {
                    JiotLogger.i( "onSuccess!");
                    setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNECTED);

                    mqttReconnectTimes = 0;
                    if(null != alarmMqttReconnect) {
                        alarmMqttReconnect.stop();
                    }
                    mqttHandleCallback.onConnectCompleted(Status.OK, false, token.getUserContext(), "connected to " + serverURI);
                }

                @Override
                public void onFailure(IMqttToken token, Throwable exception) {
                    JiotLogger.e( "onFailure! ",exception);
                    setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNTECT_FALIED);

                    if(mqttReconnectTimes == 0){
                        if(null != alarmMqttReconnect) {

                            alarmMqttReconnect.stop();
                            alarmMqttReconnect.start();
                            mqttReconnectTimes++;
                        }
                    }else if(mqttReconnectTimes < 3){

                        int delayTime = (int) Math.pow(2,mqttReconnectTimes) * 5;
                        mqttReconnectTimes ++;
                        alarmMqttReconnect.scheduleMqttReconnect(delayTime * 1000);
                    }else{
                        mqttReconnectTimes = 0;
                        alarmMqttReconnect.stop();
                        mqttHandleCallback.onConnectCompleted(Status.ERROR, true, token.getUserContext(), exception.toString());
                    }
                }
            };

            IMqttActionListener listenerDisconnect = new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    setConnectState(MqttConstans.MQTT_CONNECT_STATUS.DISCONNECTED);
                    try {
                        alarmMqttReconnect = new JiotAlarmMqttReconnect(context);
                        alarmMqttReconnect.init(mqttClient,options,listenerReconnect);
                        if(null != mqttHandleCallback){
                            mqttHandleCallback.onDisconnectCompleted(Status.OK, asyncActionToken.getUserContext(), "disconnected to " + serverURI);
                        }
                        mqttClient.connect(options, null, listenerReconnect);
                        setConnectState(MqttConstans.MQTT_CONNECT_STATUS.DISCONNECTED);
                    } catch (Exception e) {
                        JiotLogger.e("Exception occurred attempting to reconnect: ", e);
                        setConnectState(MqttConstans.MQTT_CONNECT_STATUS.CONNTECT_FALIED);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable cause) {
                    mqttHandleCallback.onDisconnectCompleted(Status.ERROR, asyncActionToken.getUserContext(), cause.toString());
                }
            };

            if(getConnectStatus().equals(MqttConstans.MQTT_CONNECT_STATUS.CONNECTED)){
                //
                try {
                    mqttClient.disconnect(null,listenerDisconnect);
                } catch (MqttException e) {
                    //ignore exception
                }
            }else{
                alarmMqttReconnect = new JiotAlarmMqttReconnect(context);
                alarmMqttReconnect.init(mqttClient,options,listenerReconnect);
                try {
                    mqttClient.connect(options, null, listenerReconnect);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
        return Status.OK;
    }


    public Status subscribe(@NonNull MqttContext mqttContext, @NonNull final String []topics) {

        for(int i = 0 ; i < topics.length ; i ++) {
            if (topics[i] == null || topics[i].trim().length() == 0) {
                JiotLogger.e("Topic is empty!!!");
                return Status.PARAMETER_INVALID;
            }
            if (topics[i].length() > MqttConstans.MAX_SIZE_OF_STRING_TOPIC) {
                JiotLogger.e("Topic length is too long!!!");
                return Status.PARAMETER_INVALID;
            }
        }

        int []qoss = new int[topics.length];
        for(int i = 0 ; i < topics.length ; i ++){
            qoss[i] = MqttConstans.QOS1;
        }

        if ((mqttClient != null) && (mqttClient.isConnected())) {
            try {
                mqttClient.subscribe(topics,qoss,mqttContext, new SelfMqttActionListener((MqttConstans.SUBSCRIBE)));
            } catch (Exception e) {
                JiotLogger.e("subscribe topics",e);
                return Status.ERROR;
            }
        } else {
            JiotLogger.e("subscribe topics failed, because mMqttClient not connected.");
            return Status.MQTT_NO_CONN;
        }

        for(int i = 0; i < topics.length ; i ++) {
            subscribedTopicCache.put(topics[i], qoss[i]);
        }

        return Status.OK;
    }


    public Status publish(@NonNull MqttContext mqttContext,@NonNull String topic, @NonNull MqttMessage message) {
        if (topic == null || topic.trim().length() == 0) {
            JiotLogger.e("Topic is empty!!!");
            return Status.PARAMETER_INVALID;
        }
        if (topic.length() > MqttConstans.MAX_SIZE_OF_STRING_TOPIC) {
            JiotLogger.e( "Topic length is too long!!!");
            return Status.PARAMETER_INVALID;
        }

        JiotLogger.i("Starting publish topic: " + topic + " Message: " + message.toString());

        if ((mqttClient != null) && (mqttClient.isConnected())) {
            initExecutorService().execute(new AsyncPublish(topic, message, mqttContext, new PublishCallback() {
                @Override
                public void onFailure(String topic, MqttMessage message, MqttContext mqttContext, IMqttDeliveryToken token, Throwable throwable) {
                    if(null != mqttHandleCallback){
                        mqttHandleCallback.onPublishCompleted(Status.TIME_OUT,token,mqttContext,throwable.getMessage());
                    }
                }
            }));
        } else if ((mqttClient != null) && (this.bufferOpts != null) && (this.bufferOpts.isBufferEnabled())) { //放入缓存
            initExecutorService().execute(new AsyncPublish(topic,message,mqttContext,new PublishCallback() {
                @Override
                public void onFailure(String topic, MqttMessage message, MqttContext mqttContext, IMqttDeliveryToken token, Throwable throwable) {
                    if(null != mqttHandleCallback){
                        mqttHandleCallback.onPublishCompleted(Status.TIME_OUT,token,mqttContext,throwable.getMessage());
                    }
                }
            }));
        } else {
            JiotLogger.e("publish topic: " + topic + " failed, mMqttClient not connected and disconnect buffer not enough.");
            return Status.ERROR;
        }

        return Status.OK;
    }

    /**
     * 线程池的初始化
     * @return
     */
    public synchronized ExecutorService initExecutorService(){

        if(null == executorService){
            //这里只是给这个线程起一个名字
            ThreadFactory threadFactory = new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable runnable) {
                    Thread thread = new Thread(runnable,"Http Client Thread");
                    return thread;
                }
            };
            //这里按照OkHttp的线程池样式来创建，单个线程在闲置的时候保留60秒
            executorService = new ThreadPoolExecutor(0,Integer.MAX_VALUE,60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),threadFactory);
        }
        return executorService;
    }

    final class AsyncPublish implements Runnable{

        private IMqttDeliveryToken sendToken = null;
        private PublishCallback callback;
        private String topic;
        private MqttMessage message;
        private MqttContext mqttContext;
        public AsyncPublish(String topic,MqttMessage message,MqttContext mqttContext,PublishCallback callback){
            this.topic = topic;
            this.message = message;
            this.mqttContext = mqttContext;
            this.callback = callback;
        }

        @Override
        public void run() {

            try {
                sendToken = mqttClient.publish(topic, message, mqttContext, new SelfMqttActionListener(MqttConstans.PUBLISH));
                sendToken.waitForCompletion(30000);
            } catch (Exception e) {
                if(null != callback){
                    callback.onFailure(topic,message,mqttContext,sendToken,e);
                }
            }
        }
    }

    /**
     * MQTT断连，结果通过回调函数通知。
     *
     * @param mqttContext 用户上下文（这个参数在回调函数时透传给用户）
     * @return 发送请求成功时返回Status.OK; 其它返回值表示发送请求失败；
     */
    public Status disConnect(MqttContext mqttContext) {
        return disConnect(0, mqttContext);
    }

    /**
     * MQTT断连, 结果通过回调函数通知。
     *
     * @param timeout     等待时间（必须大于0）。单位：毫秒
     * @param mqttContext 用户上下文（这个参数在回调函数时透传给用户）
     * @return 发送请求成功时返回Status.OK; 其它返回值表示发送请求失败；
     */
    public Status disConnect(long timeout, MqttContext mqttContext) {
        mLastReceivedMessageId = INVALID_MESSAGE_ID;

        IMqttActionListener mActionListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                setConnectState(MqttConstans.MQTT_CONNECT_STATUS.DISCONNECTED);
                mqttHandleCallback.onDisconnectCompleted(Status.OK, asyncActionToken.getUserContext(), "disconnected to " + serverURI);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable cause) {
                mqttHandleCallback.onDisconnectCompleted(Status.ERROR, asyncActionToken.getUserContext(), cause.toString());
            }
        };
        if(null != mqttClient) {
            try {
                if (timeout <= 0) {
                    mqttClient.disconnect(mqttContext, mActionListener);
                } else {
                    mqttClient.disconnect(timeout, mqttContext, mActionListener);
                }
            } catch (MqttException e) {
                JiotLogger.e("manual disconnect failed.", e);
                mqttHandleCallback.onDisconnectCompleted(Status.OK, mqttContext, "disconnected to " + serverURI);
                return Status.ERROR;
            }
        }else{
            mqttHandleCallback.onDisconnectCompleted(Status.OK, mqttContext, "disconnected to " + serverURI);
        }
        return Status.OK;
    }


    /**
     * 设置当前连接状态
     * @param connectStatus 当前连接状态
     */
    protected synchronized void setConnectState(MqttConstans.MQTT_CONNECT_STATUS connectStatus) {
        this.clientConnectStatus = connectStatus;
    }

    /**
     * @return 当前连接状态
     */
    public MqttConstans.MQTT_CONNECT_STATUS getConnectStatus() {
        return this.clientConnectStatus;
    }


    @Override
    public void connectionLost(Throwable cause) {
        JiotLogger.e("connection lost because of: "+ cause.toString());
        setConnectState(MqttConstans.MQTT_CONNECT_STATUS.DISCONNECTED);
        if(null != mqttHandleCallback){
            mqttHandleCallback.onConnectionLost(cause);
        }
        mLastReceivedMessageId = INVALID_MESSAGE_ID;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {


        if (message.getQos() > 0 && message.getId() == mLastReceivedMessageId) {
            JiotLogger.e("Received topic: " + topic + ", seq_no: " + message.getId() + ", message: " + message + ", discard repeated message!!!");
            return;
        }
        mLastReceivedMessageId = message.getId();

        boolean consumed = false;

        if (mqttHandleCallback != null) {
            if (!consumed) {
                mqttHandleCallback.onMessageReceived(topic, message);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }


    /**
     * 事件回调
     */
    private class SelfMqttActionListener implements IMqttActionListener {
        private int command;

        public SelfMqttActionListener(int command) {
            this.command = command;
        }

        @Override
        public void onSuccess(IMqttToken token) {

            MqttWireMessage mqttWireMessage = token.getResponse();

            switch (command) {
                case MqttConstans.PUBLISH:
                    mqttHandleCallback.onPublishCompleted(Status.OK, token, token.getUserContext(), MqttConstans.PUBLISH_SUCCESS);
                    break;

                case MqttConstans.SUBSCRIBE:
                    int[] qos = ((MqttSuback) mqttWireMessage).getGrantedQos();
                    if (null != qos && qos.length >= 1 && qos[0] == 128) {
                        mqttHandleCallback.onSubscribeCompleted(Status.ERROR, token, token.getUserContext(), MqttConstans.SUBSCRIBE_FAIL);
                    } else {
                        mqttHandleCallback.onSubscribeCompleted(Status.OK, token, token.getUserContext(), MqttConstans.SUBSCRIBE_SUCCESS);
                    }
                    break;

                case MqttConstans.UNSUBSCRIBE:
                    mqttHandleCallback.onUnSubscribeCompleted(Status.OK, token, token.getUserContext(), MqttConstans.UNSUBSCRIBE_SUCCESS);
                    break;

                default:
                    JiotLogger.e("Unknown message on Success:" + token);
                    break;
            }
        }

        @Override
        public void onFailure(IMqttToken token, Throwable exception) {
            switch (command) {
                case MqttConstans.PUBLISH:
                    mqttHandleCallback.onPublishCompleted(Status.ERROR, token, token.getUserContext(), exception.toString());
                    break;
                case MqttConstans.SUBSCRIBE:
                    mqttHandleCallback.onSubscribeCompleted(Status.ERROR, token, token.getUserContext(), exception.toString());
                    break;
                case MqttConstans.UNSUBSCRIBE:
                    mqttHandleCallback.onUnSubscribeCompleted(Status.ERROR, token, token.getUserContext(), exception.toString());
                    break;
                default:
                    JiotLogger.e("Unknown message on onFailure:" + token);
                    break;
            }
        }
    }
}
