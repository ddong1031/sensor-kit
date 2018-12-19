package com.sensoro.libbleserver.ble.utils;

import android.util.Log;

public class LogUtils {
    //    private static boolean isShowLog = BuildConfig.DEBUG;
    private static boolean isShowLog = true;
    private static String TAG = "sensoro_ble";

    public static void loge(String msg) {
        if (isShowLog) {
            e(TAG, msg);
        }
    }

    public static void logd(String msg) {
        if (isShowLog) {
            d(TAG, msg);
        }
    }

    public static void loge(Object o, String msg) {
        if (isShowLog) {
            if (o instanceof String) {
                e(TAG + "-->" + o, msg);
            } else {
                e(TAG + "-->" + o.getClass().getSimpleName(), msg);
            }
        }
    }

    public static void logd(Object o, String msg) {
        if (isShowLog) {
            if (o instanceof String) {
                d(TAG + "-->" + o, msg);
            } else {
                d(TAG + "-->" + o.getClass().getSimpleName(), msg);
            }

        }
    }

    private static void d(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.d(tag, msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        Log.d(tag, msg);
    }

    private static void e(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.e(tag, msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        Log.e(tag, msg);
    }

}
