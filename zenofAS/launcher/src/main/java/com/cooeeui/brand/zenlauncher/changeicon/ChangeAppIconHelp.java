package com.cooeeui.brand.zenlauncher.changeicon;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

/**
 * ChangeAppIcon 帮助类
 *
 * @author xingwang lee
 */
public class ChangeAppIconHelp {

    public static String
        CHANGE_APP_PKG_URL =
        "https://play.google.com/store/apps/details?id=com.cooeeui.iconui";

    public static String
        CHANGE_APP_PKG_MORE_URL =
        "https://play.google.com/store/search?q=icon%20pack&c=apps";

    /**
     * 去Google paly下载相关的应用
     *
     * @param context    上下文对象
     * @param uriForeign 应用的url
     */
    public static void gotoDownloadAPK(Context context, String uriForeign) {
        if (CommonUtil.isGooglePlayStoreInstalled(context)) {
            CommonUtil.openWithGooglePlayStore(context, uriForeign);
        } else {
            Toast.makeText(context, StringUtil.getString(context, R.string.google_play_not_install),
                           Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 此函数功能为获取Apk 的version code
     *
     * @param context     上下文对象
     * @param packageName Application 的version code
     * @return 版本号
     */
    public static int getAPKVersionCode(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName,
                                                                          PackageManager.GET_ACTIVITIES);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
