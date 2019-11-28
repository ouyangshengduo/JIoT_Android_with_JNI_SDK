package cn.jiguang.iot.mqtt;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.jiguang.iot.util.JiotLogger;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/9 16:36
 * desc  :
 */
public class JiotAlarmMqttReconnect {

    private Context context;
    private MqttAsyncClient mqttClient;
    private MqttConnectOptions options;
    private IMqttActionListener listener;
    private BroadcastReceiver mAlarmReceiver;
    private PendingIntent pendingIntent;
    private volatile boolean hasStarted = false;
    //默认定时器下一个操作执行时间 5秒
    private static final int DEFALUT_INTERVAL_TIME = 5000;
    private final String ACTION_SENDING_HEARTBEAT = "CN.JIGUANG.IOT.RECONNECT";

    public JiotAlarmMqttReconnect(Context context){
        this.context = context;
    }

    public void init(MqttAsyncClient mqttClient, MqttConnectOptions options,IMqttActionListener listener){
        this.listener = listener;
        this.mqttClient = mqttClient;
        this.options = options;
        this.mAlarmReceiver = new JiotAlarmMqttReconnect.AlarmReceiver();
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        JiotLogger.d("Register JiotAlarmMqttReconnectReceiver to Context " + mqttClient.getClientId());
        if (context != null && mAlarmReceiver != null) {
            context.registerReceiver(mAlarmReceiver, intentFilter);
        }
        pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_SENDING_HEARTBEAT), PendingIntent.FLAG_UPDATE_CURRENT);
        //获取AlarmManager系统服务
        scheduleMqttReconnect(DEFALUT_INTERVAL_TIME);
        hasStarted = true;
    }

    public void stop() {

        if(hasStarted){
            JiotLogger.d("Unregister JiotAlarmMqttReconnectReceiver to Context " + mqttClient.getClientId());
            if(pendingIntent != null){
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
            hasStarted = false;
            try{
                context.unregisterReceiver(mAlarmReceiver);
            }catch(IllegalArgumentException e){
                //Ignore unregister errors.
            }
        }
    }

    public void scheduleMqttReconnect(long delayInMilliseconds) {
        long nextAlarmInMilliseconds = System.currentTimeMillis() + delayInMilliseconds;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(Build.VERSION.SDK_INT >= 23){
            // In SDK 23 and above, dosing will prevent setExact, setExactAndAllowWhileIdle will force
            // the device to run this task whilst dosing.
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, pendingIntent);
        }
    }

    /**
     * PingReq发送类
     */
    class AlarmReceiver extends BroadcastReceiver {

        private PowerManager.WakeLock wakelock;

        private final String wakeLockTag = "Jiot.HeartBeat.Receiver." + mqttClient.getClientId();

        @Override
        @SuppressLint("Wakelock")
        public void onReceive(Context context, Intent intent) {
            // According to the docs, "Alarm Manager holds a CPU wake lock as
            // long as the alarm receiver's onReceive() method is executing.
            // This guarantees that the phone will not sleep until you have
            // finished handling the broadcast.", but this class still get
            // a wake lock to wait for ping finished.
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
            wakelock.acquire();

            try{
                // 要发布的数据
               if(null != mqttClient && mqttClient.isConnected()){
                   try {
                       mqttClient.disconnect();
                   } catch (MqttException e) {
                       e.printStackTrace();
                   }
               }
                try {
                    mqttClient.connect(options,null,listener);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }finally {
                wakelock.release();
            }

        }
    }

}
