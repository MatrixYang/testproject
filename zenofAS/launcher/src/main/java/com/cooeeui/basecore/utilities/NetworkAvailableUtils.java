package com.cooeeui.basecore.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * 网络状态工具类 主要作用是判断各种类型的网络状态以及可用性
 *
 * @author leexingwang
 * @version 1.0
 * @date 2015.03.26
 */
public class NetworkAvailableUtils {

    /**
     * 判断网络是否可用
     *
     * @param context 上下文对象
     * @return 可用返回true，反之返回false
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断WiFi是否可用
     *
     * @param context 上下文对象
     * @return 可用返回true，反之返回false
     */
    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager mgrConn = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) context
            .getSystemService(Context.TELEPHONY_SERVICE);
        return ((mgrConn.getActiveNetworkInfo() != null && mgrConn.getActiveNetworkInfo()
                                                               .getState()
                                                           == NetworkInfo.State.CONNECTED) || mgrTel
                                                                                                  .getNetworkType()
                                                                                              == TelephonyManager.NETWORK_TYPE_UMTS);
    }

    /**
     * 判断3G是否可用
     *
     * @param context 上下文对象
     * @return 可用返回true，反之返回false
     */
    public static boolean is3GEnabled(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
            && networkINfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    
    /**
     * 判断是wifi还是3g网络
     *
     * @param context 上下文对象
     * @return 可用返回true，反之返回false
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
            && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }
}
