package com.cooeeui.brand.zenlauncher.tips;

import android.view.ViewConfiguration;

class ViewConfigurationCompatFroyo {

    public static int getScaledPagingTouchSlop(ViewConfiguration config) {
        return config.getScaledPagingTouchSlop();
    }
}
