package com.cooeeui.wallpaper.http;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.cooeeui.wallpaper.util.Assets;
import com.cooeeui.wallpaper.util.Installation;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;


/**
 * Created by Steve on 2015/7/21.
 *
 * 负责生成post时候的参数
 */
public class HttpParamsUtil {

    public static final String ACTION_USE_LOG = "0033";
    public static final String ACTION_DOWNLOAD_LOG = "0030";
    public static final String ACTION_INSTALL_LOG = "0031";
    public static final String ACTION_UNINSTALL_LOG = "0032";
    private static final String VERSION_CODE = "2";
    private static final String APP_VERSION_CODE = "19627";
    private static final String ACTION_TAB = "1302";
    private static final String
        DEFAULT_KEY =
        "f24657aafcb842b185c98a9d3d7c6f4725f6cc4597c3a4d531c70631f7c7210fd7afd2f8287814f3dfa662ad82d1b02268104e8ab3b2baee13fab062b3d27bff";
    protected static char hexDigits[] = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String getParams(Context context, String logText, boolean isAddMd5) {
        String action = null;
        String resid = null;
        String packageName = null;
        String[] itemsTemp = logText.split("#");
        int len = itemsTemp.length;
        if (len > 0) {
            action = itemsTemp[0];
            if (len > 1) {
                resid = itemsTemp[1];
            }
            if (len > 2) {
                packageName = itemsTemp[2];
            }
        }
        String appid = null;
        String sn = null;
        PackageManager pm;
        JSONObject res;
        int networktype = -1;
        int networksubtype = -1;
        ConnectivityManager connMgr =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo != null) {
            networktype = netInfo.getType();
            networksubtype = netInfo.getSubtype();
        }
        appid = Assets.getAppId(context);
        sn = Assets.getSerialNo(context);
        if (appid == null || sn == null) {
            return null;
        }
        pm = context.getPackageManager();
        res = new JSONObject();
        try {
            res.put("Action", action);
            if (isAddMd5) {
                res.put("packname", context.getPackageName());
                res.put("versioncode", pm.getPackageInfo(context.getPackageName(), 0).versionCode);
                res.put("versionname", pm.getPackageInfo(context.getPackageName(), 0).versionName);
                res.put("sn", sn);
                res.put("appid", appid);
                res.put("shellid", getShellID());
                res.put("timestamp", 0);
                res.put("uuid", Installation.id(context));
                TelephonyManager mTelephonyMgr =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                res.put("imsi",
                        mTelephonyMgr.getSubscriberId() == null ? ""
                                                                : mTelephonyMgr.getSubscriberId());
                res.put("iccid",
                        mTelephonyMgr.getSimSerialNumber() == null ? "" : mTelephonyMgr
                            .getSimSerialNumber());
                res.put("imei",
                        mTelephonyMgr.getDeviceId() == null ? "" : mTelephonyMgr.getDeviceId());
                res.put("phone",
                        mTelephonyMgr.getLine1Number() == null ? ""
                                                               : mTelephonyMgr.getLine1Number());
                java.text.DateFormat format = new java.text.SimpleDateFormat("yyyyMMddhhmmss");
                res.put("localtime", format.format(new Date()));
                res.put("model", Build.MODEL);
                res.put("display", Build.DISPLAY);
                res.put("product", Build.PRODUCT);
                res.put("device", Build.DEVICE);
                res.put("board", Build.BOARD);
                res.put("manufacturer", Build.MANUFACTURER);
                res.put("brand", Build.BRAND);
                res.put("hardware", Build.HARDWARE);
                res.put("buildversion", Build.VERSION.RELEASE);
                res.put("sdkint", Build.VERSION.SDK_INT);
                res.put("androidid",
                        android.provider.Settings.Secure.getString(context.getContentResolver(),
                                                                   android.provider.Settings.Secure.ANDROID_ID));
                res.put("buildtime", Build.TIME);
                res.put("heightpixels", context.getResources().getDisplayMetrics().heightPixels);
                res.put("widthpixels", context.getResources().getDisplayMetrics().widthPixels);
                res.put("networktype", networktype);
                res.put("networksubtype", networksubtype);
                res.put("producttype", 4);
                res.put("productname", "uipersonalcenter");
                res.put("count", 0);
                res.put("opversion", getVersion());
            }
            if (action.equals(ACTION_USE_LOG)) {
                res.put("param1", "");
                res.put("param2", "");
                res.put("count", getUseCount());
            } else if (action.equals(ACTION_UNINSTALL_LOG)) {
                res.put("param1", resid);
                res.put("param2", packageName);
                res.put("count", 0);
            } else if (action.equals(ACTION_DOWNLOAD_LOG) || action.equals(ACTION_INSTALL_LOG)) {
                res.put("param1", resid);
                res.put("param2", packageName);
                res.put("count", 0);
            }
            String content = res.toString();
            String params = content;
            if (isAddMd5) {
                String md5_res = getMD5EncruptKey(content + DEFAULT_KEY);
                String newContent = content.substring(0, content.lastIndexOf('}'));
                params = newContent + ",\"md5\":\"" + md5_res + "\"}";
            }
            return params;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMyUUID(Context context) {
        final String androidId;
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(),
                                                                    android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), androidId.hashCode());
        String uniqueId = deviceUuid.toString();
        return uniqueId;
    }

    private static String getShellID() {
        return "";
    }

    private static int getUseCount() {
        int count = 0;
        return count;
    }

    private static String getVersion() {
        String clientVersionCode = VERSION_CODE;
        String interfaceVersionCode = "0.0";
        String appVersionCode = APP_VERSION_CODE;
        return clientVersionCode + "." + interfaceVersionCode + "." + appVersionCode;
    }

    private static String getMD5EncruptKey(String logInfo) {
        String res = null;
        MessageDigest messagedigest;
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        messagedigest.update(logInfo.getBytes());
        res = bufferToHex(messagedigest.digest());
        return res;
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

}
