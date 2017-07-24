package com.cooeeui.brand.zenlauncher.appIntentUtils;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppIntentUtil {

    private Context context = null;
    private HashMap<String, ResolveInfo> allAppsMap = new HashMap<String, ResolveInfo>();
    private final String cameraName = "camera";
    private final String browserUri = "*BROWSER*";
    private final String defaultAppName = "defaultApp";
    public BrowserIntentUtil browserUtil = null;
    /**
     * 配置默认浏览器的包名和类名，包名在前，类名在后，用;号隔开
     */
    // private final String[] browserApps = new String[] {
    // "com.tencent.mtt;com.tencent.mtt.SplashActivity",
    // "com.baidu.browser.apps;com.baidu.browser.framework.BdBrowserActivity",
    // "com.UCMobile;com.UCMobile.main.UCMobile",
    // "com.htc.sense.browser;com.htc.sense.browser.BrowserActivity"
    // };
    private final String[] browserApps = new String[]{
        "com.htc.sense.browser;com.htc.sense.browser.BrowserActivity"
    };
    private final String[] defaultApps = new String[]{
        "com.example.pagedemo;com.example.pagedemo.MainActivity",
        "com.example.workspacedemo;com.example.workspacedemo.MainActivity", ""
    };

    public AppIntentUtil(Context context) {
        this.context = context;
        getAllAppList();
    }

    /**
     * 通过该intent查找该应用是否存在于该手机中
     */
    private boolean isIntentAvailable(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                                                                      PackageManager.GET_ACTIVITIES);
        return list.size() > 0;
    }

    /**
     * 获得手机中所有的应用，并将类名和ResolveInfo存在allAppsMap中
     */
    private void getAllAppList() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        Iterator<ResolveInfo> ite = apps.iterator();
        ResolveInfo info = null;
        while (ite.hasNext()) {
            info = ite.next();
            if (info != null && info.loadLabel(packageManager) != null) {
                String clsName = info.activityInfo.name;
                allAppsMap.put(clsName, info);
            }

        }
    }

    /**
     * 通过一个uri获得一个应用的Intent： 获得规则：
     * 1.通过Uri直接获得intent，若此intent存在于该手机中则直接返回该intent（例如短信，联系人，通讯录可以直接获取）
     * 2.若不存在，则通过名称来匹配默认的包名和类名来查找该应用是否存在于手机中，若存在则返回该intent（主要针对照相机） 3.若还不存在，则通过名称来查找手机应用的类名是否包含该名称，若查找到则返回
     * 4.对于配置项，先是匹配配置应用的包名和类名，若都查不到，则使用手机中设置的包名和类名 
     * 5.若是浏览器，则先查找所有配置的包名和类名，若匹配不成功，则查找出所有的浏览器应用，让用户选择后开始记录该应用的包名和类名并保存，下次再点击的时候则直接进入该浏览器
     */
    public Intent getIntentByUri(String uri, String name) {
        Intent intent = null;
        if (uri != null) {
            if (uri.equals(browserUri)) {
                intent = getBrowserIntent();
            } else if (defaultAppName.equals(name)) {
                intent = getDefaultIntent(uri);
            } else {
                try {
                    intent = Intent.parseUri(uri, 0);
                    if (!isIntentAvailable(context, intent)) {
                        if (cameraName.equals(name)) {
                            intent = getAppIntent(cameraName);
                        }
                    }
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        Log.d("ymctest","uri : "+uri+"name : "+name);
        Log.d("ymctest","retutn  intent : " + intent);
        return intent;
    }

    /**
     * 获得默认配置的应用
     */
    private Intent getDefaultIntent(String uri) {
        // TODO Auto-generated method stub
        Intent intent = null;
        for (int i = 0; i < defaultApps.length; i++) {
            String pkgCls = defaultApps[i];
            intent = getIntentByPkgAndCls(pkgCls);
            if (isIntentAvailable(context, intent)) {
                return intent;
            }
        }
        Intent setting = new Intent(android.provider.Settings.ACTION_SETTINGS);
        setting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                         | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return setting;
    }

    /**
     * 通过包名和类名生成一个intent
     */
    private Intent getIntentByPkgAndCls(String pkgCls) {
        // TODO Auto-generated method stub
        Intent intent = null;
        if (pkgCls != null) {
            String[] cp = pkgCls.split(";");
            if (cp.length > 1) {
                String pkgName = cp[0];
                String clsName = cp[1];
                ComponentName componentName = new ComponentName(pkgName, clsName);
                intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            }
        }

        return intent;
    }

    /**
     * 获得浏览器的intent
     */
    private Intent getBrowserIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (int i = 0; i < browserApps.length; i++) {
            String pkgCls = browserApps[i];
            String[] pcs = pkgCls.split(";");
            if (pcs.length > 1) {
                ComponentName cp = new ComponentName(pcs[0], pcs[1]);
                intent.setComponent(cp);
                if (isIntentAvailable(context, intent)) {
                    return intent;
                }
            }
        }
        // 若匹配不到配置的浏览器应用，则查找出手机中所有的浏览器先，然后显示出来
        return null;
    }

    /**
     * 获得手机中关于appName（例如camera）的intent
     */
    private Intent getAppIntent(String appName) {
        // TODO Auto-generated method stub
        Intent intent = null;
        Iterator iter = allAppsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            if (key instanceof String && val instanceof ResolveInfo) {
                String clsKey = (String) key;
                ResolveInfo resolveInfo = (ResolveInfo) val;
                if (clsKey.contains(appName)) {
                    String pkgName = resolveInfo.activityInfo.packageName;
                    ComponentName cp = new ComponentName(pkgName, clsKey);
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(cp);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    return intent;
                }
            }
        }
        return intent;
    }

    @SuppressLint("CommitPrefEdits")
    public void startBrowserIntent() {
        if (browserUtil == null) {
            browserUtil = new BrowserIntentUtil(context);
        }
        if (browserUtil.findAllbrowserApp().size() == 1) {
            ResolveInfo info = browserUtil.findAllbrowserApp().get(0);
            String pkgName = info.activityInfo.packageName;
            String clsName = info.activityInfo.name;
            startBrowserActivity(pkgName, clsName, context);
            return;
        }
        SharedPreferences preference = context.getSharedPreferences(
            LauncherConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (!"defValue".equals(preference.getString(BrowserIntentUtil.PACKAGE_NAME, "defValue"))
            && isBrowserInstalled(preference
                                      .getString(BrowserIntentUtil.PACKAGE_NAME, "defValue"))) {
            String pkgName = preference.getString(BrowserIntentUtil.PACKAGE_NAME, "defValue");
            String clsName = preference.getString(BrowserIntentUtil.CLASS_NAME, "defValue");
            startBrowserActivity(pkgName, clsName, context);
            return;
        } else {
            Editor editor = preference.edit();
            editor.putString(BrowserIntentUtil.CLASS_NAME, "defValue");
            editor.putString(BrowserIntentUtil.PACKAGE_NAME, "defValue");
            editor.commit();
            browserUtil.createDialog();

        }

    }

    /**
     * 启动相应的浏览器
     *
     * @param pkgName 浏览器的包名；
     * @param clsName 浏览器的类名；
     * @param context 上下文对象；
     */
    private void startBrowserActivity(String pkgName, String clsName, Context context) {
        ComponentName cp = new ComponentName(pkgName, clsName);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(cp);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context,
                           StringUtil.getString(context, R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 判断手机中是否安装了与包名对应的浏览器
     *
     * @param playPkgName 浏览器包名
     * @return 安装了返回true ；否则返回false；
     */
    private boolean isBrowserInstalled(String playPkgName) {
        try {
            PackageInfo pckInfo = Launcher.getInstance().getPackageManager()
                .getPackageInfo(playPkgName,
                                PackageManager.GET_ACTIVITIES);
            Log.d("packageName", pckInfo.packageName + " installed");
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            Log.d("packageName", playPkgName + "not installed");
            return false;
        }
    }
}
