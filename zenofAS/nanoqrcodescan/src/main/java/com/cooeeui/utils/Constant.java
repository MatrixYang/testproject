package com.cooeeui.utils;

import com.mobvista.msdk.out.MvNativeHandler;

/**
 * Created by user on 2016/6/1.
 */
final public class Constant {

    // 表示IP是否为国内
    public static boolean isDomestic;

    public static Object scanResultAd;
    public static Object scanHistoryAd;
    public static MvNativeHandler mvNativeHandleForScanResult;
    public static MvNativeHandler mvNativeHandleForScanHistory;
    public static String kmobAdJsonStrForScanResult;
    public static String kmobAdJsonStrForScanHistory;

    // SharedPreferences key string
    public static final String SP_FILE_NAME = "ad_sp_file";
    public static final String SP_KEY_DATE = "ad_date";
    public static final String SP_KEY_IS_DOMESTIC = "ad_is_domestic";
}
