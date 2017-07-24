package com.cooeeui.basecore.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hugo.ye on 2016/1/27. 常用的通用接口，此类中收集一些开发中经常需要用到的通用的封装接口。
 */
public class CommonUtil {

    /**
     * 谷歌商店的包名及类名
     */
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME = "com.android.vending";
    private static final String GOOGLE_PLAY_STORE_CLASS_NAME =
        "com.android.vending.AssetBrowserActivity";


    /**
     * 获取当前版本号，这里获取的是versionName，将String类型转换为了int型。因为谷歌抓取的版本号就是versionName，需要进行对比
     */
    public static int getVersion(Context context) {
        int versionCode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = versionNameString2Int(pi.versionName);
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionCode;
    }

    public static String getVersionName(Context context) {
        String name = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            name = pi.versionName;
        } catch (Exception e) {
        }
        return name;
    }

    /**
     * 将String类型的versionName转换为int型
     */
    public static int versionNameString2Int(String versionName) {
        int version = 0;
        versionName = versionName.replace(".", "");
        version = Integer.valueOf(versionName);
        return version;
    }

    /**
     * 此函数是判断手机上是否有指定的APP
     *
     * @param context     上下文对象
     * @param packageName app的包名
     * @return 如果手机上已经存在指定的app则返回true 否则返回false
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isGooglePlayStoreInstalled(Context context) {
        return isAppInstalled(context, GOOGLE_PLAY_STORE_PACKAGE_NAME);
    }

    public static boolean openWithGooglePlayStore(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setClassName(GOOGLE_PLAY_STORE_PACKAGE_NAME, GOOGLE_PLAY_STORE_CLASS_NAME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将流转换为String类型
     */
    public static String inputStream2String(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toString();
    }
}
