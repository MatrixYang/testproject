package com.cooeeui.brand.zenlauncher.changeicon.dbhelp;

import android.graphics.Bitmap;

/**
 * change app icon数据库更新类
 *
 * @author xingwang lee
 */
public class ChangeAppIconDBEntity {

    private String iconTitle;
    private int iconChangeType;
    private int iconType;
    private int iconPosition = DELAULT_ICON_POSITION;
    private String iconPackage;
    private Bitmap icon;

    // 应用默认图标
    public static final int ICON_TYPE_APP_DEFALUT = 1;
    // 桌面配的6个图标
    public static final int ICON_TYPE_ZEN_SIX_ICON = 2;
    // 我们桌面的iconPKG中的图标
    public static final int ICON_TYPE_OUR_APPLICATION = 3;
    // 第三方图标应用中的图标
    public static final int ICON_TYPE_THIRD_PARTY_APPLICATION = 4;

    // 更改所有的图标
    public static final int ICON_CHANGE_TYPE_ALL = 1;

    // 更改首页的图标
    public static final int ICON_CHANGE_TYPE_FRIST_PAGE = 2;

    // 更改常用页的图标
    public static final int ICON_CHANGE_TYPE_MOST_USED_PAGE = 3;

    // 更改最新安装页的图标
    public static final int ICON_CHANGE_TYPE_LATEST_INSTALLED_PAGE = 4;

    // 更改All apps页的图标
    public static final int ICON_CHANGE_TYPE_ALL_APPS_PAGE = 5;

    public static final int DELAULT_ICON_POSITION = 100;

    public ChangeAppIconDBEntity() {
        super();
    }

    public ChangeAppIconDBEntity(String iconTitle, int iconChangeType, int iconType,
                                 int iconPosition, String iconPackage, Bitmap icon) {
        super();
        this.iconTitle = iconTitle;
        this.iconChangeType = iconChangeType;
        this.iconType = iconType;
        this.iconPosition = iconPosition;
        this.iconPackage = iconPackage;
        this.icon = icon;
    }

    public String getIconTitle() {
        return iconTitle;
    }

    public void setIconTitle(String iconTitle) {
        this.iconTitle = iconTitle;
    }

    public int getIconChangeType() {
        return iconChangeType;
    }

    public void setIconChangeType(int iconChangeType) {
        this.iconChangeType = iconChangeType;
    }

    public int getIconType() {
        return iconType;
    }

    public void setIconType(int iconType) {
        this.iconType = iconType;
    }

    public int getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(int iconPosition) {
        this.iconPosition = iconPosition;
    }

    public String getIconPackage() {
        return iconPackage;
    }

    public void setIconPackage(String iconPackage) {
        this.iconPackage = iconPackage;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

}
