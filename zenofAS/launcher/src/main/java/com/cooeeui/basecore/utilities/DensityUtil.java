package com.cooeeui.basecore.utilities;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class DensityUtil {

    public static boolean isTablet(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // get size of screen, the unit is inch.
        double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2)
                                          + Math.pow(dm.heightPixels, 2));
        double physicalSize = diagonalPixels / (160 * dm.density);
        // we regard it as tablet if it is bigger than 7inch
        boolean isTablet = physicalSize > 7;
        return isTablet;
    }

    public static float getTabletScale(Context context) {
        return 2.5f / getDensity(context);
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
