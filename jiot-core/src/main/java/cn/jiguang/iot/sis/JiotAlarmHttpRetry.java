package cn.jiguang.iot.sis;

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
import cn.jiguang.iot.http.Call;
import cn.jiguang.iot.http.HttpClient;
import cn.jiguang.iot.http.Response;
import cn.jiguang.iot.mqtt.MqttConnection;
import cn.jiguang.iot.mqtt.MqttConstans;
import cn.jiguang.iot.mqtt.MqttContext;
import cn.jiguang.iot.util.JiotLogger;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/5/9 16:36
 * desc  : Sis重试定时器
 */
public class JiotAlarmHttpRetry {

    private Context context;
    private SisConnection sisConnection;
    private BroadcastReceiver mAlarmReceiver;
    private PendingIntent pendingIntent;
    private volatile boolean hasStarted = false;
    private final String ACTION_SENDING_HEARTBEAT = "CN.JIGUANG.IOT.HTTPRETRY";
    public static int HTTPRETRY_INTERVAL = 10 * 1000;

    public boolean isHasStarted() {
        return hasStarted;
    }

    public JiotAlarmHttpRetry(Context context){
        this.context = context;
    }

    public void init(SisConnection sisConnection){
        this.sisConnection = sisConnection;
        this.mAlarmReceiver = new JiotAlarmHttpRetry.AlarmReceiver();
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SENDING_HEARTBEAT);
        JiotLogger.d("Register JiotAlarmHttpRetryReceiver to Context ");
        if (context != null && mAlarmReceiver != null) {
            context.registerReceiver(mAlarmReceiver, intentFilter);
        }
        pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_SENDING_HEARTBEAT), PendingIntent.FLAG_UPDATE_CURRENT);
        //获取AlarmManager系统服务
        scheduleHttpRetry(HTTPRETRY_INTERVAL);
        hasStarted = true;
    }

    public void stop() {
        if(hasStarted){
            JiotLogger.d("Unregister JiotAlarmHttpRetryReceiver to Context ");
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

    public void scheduleHttpRetry(long delayInMilliseconds) {
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

        private final String wakeLockTag = "Jiot.HttpRetry.Receiver." + System.currentTimeMillis();

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
                SisRequestOptions sisRequestOptions = new SisRequestOptions();
                sisRequestOptions.setHost(SisContants.HOST);
                sisRequestOptions.setProductKey(sisConnection.getProductKey());
                sisRequestOptions.setShouldRetry(true);
                sisRequestOptions.setRequestProtocolType(sisConnection.getSisRequestProtocolType());
                sisConnection.request(sisRequestOptions);
            }finally {
                wakelock.release();
            }

        }
    }

}
