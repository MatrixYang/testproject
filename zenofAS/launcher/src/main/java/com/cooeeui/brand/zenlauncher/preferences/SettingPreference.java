package com.cooeeui.brand.zenlauncher.preferences;

import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;

public class SettingPreference {

    // TODO: 以后将设置的默认值做统一管理。
    final static boolean SearchDefaultValue = true;
    final static boolean AdvancedDefaultValue = false;
    //    final static boolean StatusBarDefaultValueu = true;
    final static boolean NoticeDefaultValue = true;
    final static boolean AutoScrollDefaultValue = true;
    final static boolean RecommendDefaultValue = true;

    public static boolean getSearch() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_SETTING_SEARCH,
            SearchDefaultValue);
    }

    public static void setSearch(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_SETTING_SEARCH,
                                               value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getAdvanced() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_SETTING_ADVANCED,
            AdvancedDefaultValue);
    }

    public static void setAdvanced(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_SETTING_ADVANCED,
                                               value);
        SharedPreferencesUtil.get().flush();
    }

//    public static boolean getStatusBar() {
//        return SharedPreferencesUtil.get().getBoolean(
//            LauncherConstants.SP_KEY_SETTING_STATUS_BAR, StatusBarDefaultValueu);
//    }
//
//    public static void setStatusBar(boolean value) {
//        SharedPreferencesUtil.get().putBoolean(
//            LauncherConstants.SP_KEY_SETTING_STATUS_BAR,
//            value);
//        SharedPreferencesUtil.get().flush();
//    }
//
//    public static boolean getDefaultLauncherStatus() {
//        return SharedPreferencesUtil.get().getBoolean(
//            LauncherConstants.SP_KEY_SETTING_DEFAULT_LAUNCHER, false);
//    }
//
//    public static void setDefaultLauncherStatus(boolean value) {
//        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_SETTING_DEFAULT_LAUNCHER,
//                                               value);
//        SharedPreferencesUtil.get().flush();
//    }


    /**
     * 获取设置中的AutoScroll的值默认值为true即为显示recommend
     */
    public static boolean getAutoScrollValue() {
        return SharedPreferencesUtil.get().getBoolean(
            LauncherConstants.SP_KEY_SETTING_AUTO_SCROLL_CHECKBOX, AutoScrollDefaultValue);
    }

    /**
     * 设置设置中的recommend的值
     */
    public static void setAutoScrollValue(boolean value) {
        SharedPreferencesUtil.get()
            .putBoolean(LauncherConstants.SP_KEY_SETTING_AUTO_SCROLL_CHECKBOX,
                        value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getTips() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_SETTING_TIPS, true);
    }

    public static void setTips(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_SETTING_TIPS, value);
        SharedPreferencesUtil.get().flush();
    }


    public static boolean getBlurFlag() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_SETTING_BLUR, false);
    }

    public static void setBlurFlag(boolean enable) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_SETTING_BLUR, enable);
        SharedPreferencesUtil.get().flush();
    }

    public static int getWidgetId() {
        return SharedPreferencesUtil.get().getInteger(LauncherConstants.SP_KEY_WIDGET_ID, -1);
    }

    public static void setWidgetId(int value) {
        SharedPreferencesUtil.get().putInteger(LauncherConstants.SP_KEY_WIDGET_ID, value);
        SharedPreferencesUtil.get().flush();
    }

    public static int getAreaState() {
        return SharedPreferencesUtil.get().getInteger(LauncherConstants.SP_KEY_AREA_STATE, 0);
    }

    public static void setAreaState(int value) {
        SharedPreferencesUtil.get().putInteger(LauncherConstants.SP_KEY_AREA_STATE, value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getRateAlertStatus() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_RATE_ALERT_STATUS,
                                                      true);
    }

    public static void setRateAlertStatus(boolean flag) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_RATE_ALERT_STATUS, flag);
        SharedPreferencesUtil.get().flush();
    }

    public static int getRateAutoRemindCount() {
        return SharedPreferencesUtil.get().getInteger(
            LauncherConstants.SP_KEY_RATE_AUTO_REMIND_COUNT, 0);
    }

    public static void setRateAutoRemindCount(int value) {
        SharedPreferencesUtil.get().putInteger(LauncherConstants.SP_KEY_RATE_AUTO_REMIND_COUNT,
                                               value);
        SharedPreferencesUtil.get().flush();
    }

    public static long getRateAutoRemindSpecifiedDate() {
        return SharedPreferencesUtil.get().getLong(
            LauncherConstants.SP_KEY_RATE_AUTO_REMIND_SPECIFIED_DATE, 0);
    }

    public static void setRateAutoRemindSpecifiedDate(long date) {
        SharedPreferencesUtil.get().putLong(
            LauncherConstants.SP_KEY_RATE_AUTO_REMIND_SPECIFIED_DATE, date);
        SharedPreferencesUtil.get().flush();
    }

    public static long getRateAutoRemindFirstTime() {
        return SharedPreferencesUtil.get().getLong(
            LauncherConstants.SP_KEY_RATE_AUTO_REMIND_FIRST_TIME, 0);
    }

    public static void setRateAutoRemindFirstTime(long date) {
        SharedPreferencesUtil.get().putLong(
            LauncherConstants.SP_KEY_RATE_AUTO_REMIND_FIRST_TIME, date);
        SharedPreferencesUtil.get().flush();
    }

    public static int getZenLanguage() {
        return SharedPreferencesUtil.get().getInteger(LauncherConstants.SP_KEY_ZEN_LANGUAGE, 0);
    }

    public static void setZenLanguage(int value) {
        SharedPreferencesUtil.get().putInteger(LauncherConstants.SP_KEY_ZEN_LANGUAGE, value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getFirstLanguage() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_FIRST_LANGUAGE,
                                                      true);
    }

    public static boolean getFirstAdvanced() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_FIRST_ADVANCED,
                                                      true);
    }

    public static boolean getFirstPop() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_FIRST_POP,
                                                      true);
    }

    public static void setFirstUsage(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_FIRST_USAGE, value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getFirstUsage() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_FIRST_USAGE,
                                                      true);
    }

    public static void setFirstLanguage(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_FIRST_LANGUAGE, value);
        SharedPreferencesUtil.get().flush();
    }

    public static void setFirstAdvanced(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_FIRST_ADVANCED, value);
        SharedPreferencesUtil.get().flush();
    }

    public static void setFirstPop(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_FIRST_POP, value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getGuideHome() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_GUIDE_HOME,
                                                      true);
    }

    public static void setGuideHome(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_GUIDE_HOME, value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getGuideFavorite() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_GUIDE_FAVORITE,
                                                      true);
    }

    public static void setGuideFavorite(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_GUIDE_FAVORITE, value);
        SharedPreferencesUtil.get().flush();
    }

    public static boolean getGuideAllApp() {
        return SharedPreferencesUtil.get().getBoolean(LauncherConstants.SP_KEY_GUIDE_ALLAPP,
                                                      true);
    }

    public static void setGuideAllApp(boolean value) {
        SharedPreferencesUtil.get().putBoolean(LauncherConstants.SP_KEY_GUIDE_ALLAPP, value);
        SharedPreferencesUtil.get().flush();
    }

}
