package com.cooeeui.brand.zenlauncher.changeicon;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * 图标个体实体类用于存储自己的icon或者第三方的icon信息
 *
 * @author xingwang lee
 */
public class IconBase {

    private String name;
    private int imageId;
    private Context context;
    private Bitmap iconBitmap;
    private int typeIcon;

    // 应用默认图标
    public static final int ICON_TYPE_APP_DEFALUT = 1;
    // 桌面配的6个图标
    public static final int ICON_TYPE_ZEN_SIX_ICON = 2;
    // 我们桌面的iconPKG中的图标
    public static final int ICON_TYPE_OUR_APPLICATION = 3;
    // 第三方图标应用中的图标
    public static final int ICON_TYPE_THIRD_PARTY_APPLICATION = 4;

    public IconBase() {
        super();
    }

    public IconBase(String name, int imageId, Context context, int typeIcon, Bitmap iconBitmap) {
        super();
        this.name = name;
        this.imageId = imageId;
        this.context = context;
        this.typeIcon = typeIcon;
        this.iconBitmap = iconBitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getTypeIcon() {
        return typeIcon;
    }

    public void setTypeIcon(int typeIcon) {
        this.typeIcon = typeIcon;
    }

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }
}
