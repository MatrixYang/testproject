package com.cooeeui.brand.zenlauncher.tips;

import android.os.Build;
import android.view.ViewConfiguration;

public class ViewConfigurationCompat {

    static final ViewConfigurationVersionImpl IMPL;

    public static int getScaledPagingTouchSlop(ViewConfiguration config) {
        return IMPL.getScaledPagingTouchSlop(config);
    }

    static {
        if (Build.VERSION.SDK_INT >= 11) {
            IMPL = new FroyoViewConfigurationVersionImpl();
        } else {
            IMPL = new BaseViewConfigurationVersionImpl();
        }
    }

    static class FroyoViewConfigurationVersionImpl implements
                                                   ViewConfigurationCompat.ViewConfigurationVersionImpl {

        public int getScaledPagingTouchSlop(ViewConfiguration config) {
            return ViewConfigurationCompatFroyo
                .getScaledPagingTouchSlop(config);
        }
    }

    static class BaseViewConfigurationVersionImpl implements
                                                  ViewConfigurationCompat.ViewConfigurationVersionImpl {

        public int getScaledPagingTouchSlop(ViewConfiguration config) {
            return config.getScaledTouchSlop();
        }
    }

    static abstract interface ViewConfigurationVersionImpl {

        public abstract int getScaledPagingTouchSlop(
            ViewConfiguration paramViewConfiguration);
    }

}
