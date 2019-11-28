package cn.jiguang.iot.util;

import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jiguang.iot.JiotClient;

/**
 * @author: ouyangshengduo
 * e-mail: ouysd@jiguang.cn
 * date  : 2019/4/19 9:34
 * desc  :
 */
public class JiotLogger {

    private static final String TAG = "JIGUANG_IOT";

    /**
     * Log switch open, development, released when closed(LogCat)
     */
    public static boolean DEBUG = true;

    public static void d(String msg) {
        trace(Log.DEBUG ,msg);
    }

    public static void d(String msg, Throwable tr) {
        trace(Log.DEBUG, msg, tr);
    }

    public static void i(String msg) {
        trace(Log.INFO , msg);
    }

    public static void i(String msg, Throwable tr) {
        trace(Log.INFO, msg, tr);
    }


    public static void e(String msg) {
        trace(Log.ERROR,msg);
    }

    public static void e(String msg, Throwable tr) {
        trace(Log.ERROR, msg, tr);
    }


    /**
     * Custom Log output style
     *
     * @param type Log type
     * @param msg  Log message
     */
    private static void trace(final int type, final String msg) {
        if(DEBUG) {
            switch (type) {
                case Log.DEBUG:
                    if (JiotClient.logLevel >= 3) {
                        Log.d(TAG, msg);
                    }
                    break;
                case Log.INFO:
                    if (JiotClient.logLevel >= 2) {
                        Log.i(TAG, msg);
                    }
                    break;
                case Log.ERROR:
                    if (JiotClient.logLevel >= 1) {
                        Log.e(TAG, msg);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Custom Log output style
     *
     * @param type
     * @param msg
     * @param tr
     */
    private static void trace(final int type, final String msg, final Throwable tr) {
        // LogCat
        if (DEBUG) {
            switch (type) {
                case Log.DEBUG:
                    if(JiotClient.logLevel >= 3) {
                        Log.d(TAG, msg);
                    }
                    break;
                case Log.INFO:
                    if(JiotClient.logLevel >= 2) {
                        Log.i(TAG, msg);
                    }
                    break;
                case Log.ERROR:
                    if(JiotClient.logLevel >= 1) {
                        Log.e(TAG, msg + tr.getMessage());
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
