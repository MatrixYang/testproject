package com.cooeeui.brand.zenlauncher.scenes.utils;

import com.cooeeui.zenlauncher.R;

public class IconNameOrId {

    public static final String ICON_NAME_PACKAGE = "package";
    public static final String ICON_NAME_CAMERA = "camera";
    public static final String ICON_NAME_CONTACTS = "contacts";
    public static final String ICON_NAME_DEFAULTAPP = "defaultApp";
    public static final String ICON_NAME_DIAL = "dial";
    public static final String ICON_NAME_SMS = "sms";
    public static final String ICON_NAME_BROWSER = "browser";
    public static final String ICON_NAME_WEIXIN = "weixin";

    public static int getIconId(String name) {
        if (ICON_NAME_PACKAGE.equalsIgnoreCase(name)) {
            return -1;
        }
        if (ICON_NAME_CAMERA.equalsIgnoreCase(name)) {
            return R.raw.camera;
        }
        if (ICON_NAME_CONTACTS.equalsIgnoreCase(name)) {
            return R.raw.contacts;
        }
        if (ICON_NAME_DEFAULTAPP.equalsIgnoreCase(name)) {
            return R.raw.setting;
        }
        if (ICON_NAME_DIAL.equalsIgnoreCase(name)) {
            return R.raw.dial;
        }
        if (ICON_NAME_SMS.equalsIgnoreCase(name)) {
            return R.raw.sms;
        }
        if (ICON_NAME_BROWSER.equalsIgnoreCase(name)) {
            return R.raw.browser;
        }

        return -1;
    }

    public static String getIconName(int iconId) {
        String name = ICON_NAME_PACKAGE;
        switch (iconId) {
            case R.raw.camera:
                name = ICON_NAME_CAMERA;
                break;
            case R.raw.contacts:
                name = ICON_NAME_CONTACTS;
                break;
            case R.raw.setting:
                name = ICON_NAME_DEFAULTAPP;
                break;
            case R.raw.dial:
                name = ICON_NAME_DIAL;
                break;
            case R.raw.sms:
                name = ICON_NAME_SMS;
                break;
            case R.raw.browser:
                name = ICON_NAME_BROWSER;
                break;
            default:
                name = ICON_NAME_PACKAGE;
                break;
        }
        return name;
    }
}
