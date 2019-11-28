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

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.jiguang.iot.JiotClient;
import cn.jiguang.iot.util.JiotLogger;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/9 16:36
 * desc  :
 */
public class JiotAlarmHeartBeat{

    private Context context;
    private MqttConnection mqttConnection;
    private BroadcastReceiver mAlarmReceiver;
    private PendingIntent pendingIntent;
    private volatile boolean hasStarted = false;
    private final String ACTION_SENDING_HEARTBEAT = "CN.JIGUANG.IOT.HEARTBEAT";
    public static final int HEARTBEAT_INTERVAL = 270 * 1000;

    public JiotAlarmHeartBeat(Context context){
        this.context = context;
    }

    public void init(MqttConnection mqttConnection){
        this.mqttConnection = mqttConnection;
        this.mAlarmReceiver = new JiotAlarmHeartBeat.AlarmReceiver();
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        JiotLogger.d("Register JiotAlarmHeartBeatReceiver to Context " + mqttConnection.getClientId());
        if (context != null && mAlarmReceiver != null) {
            context.registerReceiver(mAlarmReceiver, intentFilter);
        }
        pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_SENDING_HEARTBEAT), PendingIntent.FLAG_UPDATE_CURRENT);
        //获取AlarmManager系统服务
        scheduleHeartbeat(HEARTBEAT_INTERVAL);
        hasStarted = true;
    }

    public void stop() {
        if(hasStarted){
            JiotLogger.d("Unregister JiotAlarmHeartBeatReceiver to Context " + mqttConnection.getClientId());
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

    public void scheduleHeartbeat(long delayInMilliseconds) {
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

        private final String wakeLockTag = "Jiot.HeartBeat.Receiver." + mqttConnection.getClientId();

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
                Map<String, Long> data = new HashMap<String, Long>();
                long currentMills = System.currentTimeMillis();
                data.put("seq_no", currentMills);
                // MQTT消息
                MqttMessage message = new MqttMessage();

                JSONObject jsonObject = new JSONObject();
                try {
                    for (Map.Entry<String, Long> entrys : data.entrySet()) {
                        jsonObject.put(entrys.getKey(), entrys.getValue());
                    }
                } catch (JSONException e) {
                    JiotLogger.e("pack json data failed!",e);
                }
                message.setQos(MqttConstans.QOS1);
                message.setPayload(jsonObject.toString().getBytes());
                // 用户上下文（请求实例）
                MqttContext mqttContext = new MqttContext("publishTopic", currentMills);
                mqttConnection.publish(mqttContext,String.format(MqttConstans.JMQTT_TOPIC_IOT_PING_REQ,mqttConnection.getDeviceInfo().getProductKey(),mqttConnection.getDeviceInfo().getDeviceName()),message);
            }finally {
                wakelock.release();
            }

        }
    }

}
