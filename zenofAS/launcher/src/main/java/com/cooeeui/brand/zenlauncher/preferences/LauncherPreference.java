package com.cooeeui.brand.zenlauncher.preferences;

import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;

public class LauncherPreference {

    public static boolean getFirstStart() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_FIRST_START,
            true);
    }

    public static void setFirstStart(boolean value) {
        SharedPreferencesUtil.get().putBoolean(
            LauncherConstants.SP_KEY_FIRST_START,
            value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 设置菜单壁纸库提醒图标小红点的状态
     */
    public static void setMenuWallpaperAlert(boolean value) {
        SharedPreferencesUtil.get()
            .putBoolean(LauncherConstants.SP_KEY_MENU_WALLPAPER_ALERT, value);
        SharedPreferencesUtil.get().putBoolean(
            LauncherConstants.SP_KEY_MENU_WALLPAPER_ALERT_SELECTOR_FUNCTION, value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取菜单壁纸库提醒图标小红点的状态，默认首次为需要显示
     */
    public static boolean getMenuWallpaperAlert() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_MENU_WALLPAPER_ALERT,
                                                      true)
               | SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_MENU_WALLPAPER_ALERT_SELECTOR_FUNCTION, true);
    }

    /**
     * 设置菜单壁纸库提醒图标小红点的状态
     */
    public static void setAllAppStoreAlert(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_ALL_APP_STORE_ALERT, value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取壁纸单屏还是可滚动，默认首次为单屏
     */
    public static boolean getWallpaperFixed() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_WALLPAPER_FIXED, true);
    }

    /**
     * 设置壁纸单屏还是可滚动
     */
    public static void setWallpaperrFixed(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_WALLPAPER_FIXED, value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取菜单壁纸库提醒图标小红点的状态，默认首次为需要显示
     */
    public static boolean getAllAppStoreAlert() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_ALL_APP_STORE_ALERT,
                                                      true);
    }

    /**
     * 设置菜单智能提醒提醒图标小红点的状态
     */
    public static void setMenuTipsAlert(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_MENU_TIPS_ALERT, value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取菜单智能提醒提醒图标小红点的状态，默认首次为需要显示
     */
    public static boolean getMenuTipsAlert() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_MENU_TIPS_ALERT,
                                                      true);
    }

    /**
     * 设置菜单zen设置提醒图标小红点的状态
     */
    public static void setMenuZenSettingAlert(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_MENU_ZEN_SETTING_ALERT,
                                               value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取菜单zen设置提醒图标小红点的状态，默认首次为需要显示
     */
    public static boolean getMenuZenSettingAlert() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_MENU_ZEN_SETTING_ALERT, true);
    }

    /**
     * 设置菜单facebook提醒图标小红点的状态
     */
    public static void setMenuFacebookAlert(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_MENU_FACEBOOK_ALERT,
                                               value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取菜单facebook提醒图标小红点的状态，默认首次为需要显示
     */
    public static boolean getMenuFacebookAlert() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_MENU_FACEBOOK_ALERT, true);
    }

    /**
     * 设置菜单zen life 设置提醒图标小红点的状态
     */
    public static void setMenuZenLifeAlert(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_MENU_ZEN_LIFE_ALERT,
                                               value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取菜单zen life 设置提醒图标小红点的状态，默认首次为需要显示
     */
    public static boolean getMenuZenLifeAlert() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_MENU_ZEN_LIFE_ALERT,
            true);
    }

    /*设置搜索引擎类型*/
    public static void setSearchEnginesType(int value) {
        SharedPreferencesUtil.get().putInteger(
            LauncherConstants.SP_KEY_SEARCH_ENGINES_TYPE,
            value);
        SharedPreferencesUtil.get().flush();
    }

    /*设置搜索引擎类型*/
    public static int getSearchEnginesType() {
        return SharedPreferencesUtil.get().getInteger(
            LauncherConstants.SP_KEY_SEARCH_ENGINES_TYPE, 0);
    }

    /**
     * 常用页启动次数，每天第五次回到常用页面，弹框提醒打开应用使用情况的权限
     *
     * @param value 常用页启动次数
     */
    public static void setFavoritePageDisplayTimes(int value) {
        SharedPreferencesUtil.get().putInteger(
            LauncherConstants.SP_KEY_FAVORITE_PAGE_TIMES,
            value);
        SharedPreferencesUtil.get().flush();
    }

    public static int getFavoritePageDisplayTimes() {
        return SharedPreferencesUtil.get().getInteger(
            LauncherConstants.SP_KEY_FAVORITE_PAGE_TIMES, 0);
    }

    /**
     * 获取版本更新弹框中，用户是否选择忽略此版本
     */
    public static boolean getVersionUpdateForgetStatus() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_VERSION_UPDATE_FORGET, false);
    }

    /**
     * 版本更新弹框中，用户是否忽略此版本的设置
     */
    public static void setVersionUpdateForgetStatus(boolean value) {
        SharedPreferencesUtil.get().putBoolean(
            LauncherConstants.SP_KEY_VERSION_UPDATE_FORGET, value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 获取版本更新弹框中，用户忽略的版本号
     */
    public static int getVersionUpdateForgotVersion() {
        return SharedPreferencesUtil.get()
            .getInteger(LauncherConstants.SP_KEY_VERSION_UPDATE_FORGET_VERSION, Integer.MAX_VALUE);
    }

    /**
     * 版本更新弹框中，设置用户忽略的版本号
     */
    public static void setVersionUpdateForgotVersion(int value) {
        SharedPreferencesUtil.get()
            .putInteger(LauncherConstants.SP_KEY_VERSION_UPDATE_FORGET_VERSION, value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 最新的版本的 升级信息
     */
    public static String getVersionInfo() {
        return SharedPreferencesUtil.get().getString(LauncherConstants.SP_KEY_VERSION_INFO,
                                                     null);
    }

    /**
     * 最新的版本的 升级信息
     */
    public static void setVersionInfo(String value) {
        SharedPreferencesUtil.get().putString(LauncherConstants.SP_KEY_VERSION_INFO, value);
        SharedPreferencesUtil.get().flush();
    }

    /**
     * 最新的版本号
     */
    public static String getRemoteVersionName() {
        return SharedPreferencesUtil.get().getString(LauncherConstants.SP_KEY_REMOTE_VERSION_NAME,
                                                     null);
    }

    /**
     * 最新的版本号
     */
    public static void setRemoteVersionName(String value) {
        SharedPreferencesUtil.get().putString(LauncherConstants.SP_KEY_REMOTE_VERSION_NAME,value);
        SharedPreferencesUtil.get().flush();
    }
}
