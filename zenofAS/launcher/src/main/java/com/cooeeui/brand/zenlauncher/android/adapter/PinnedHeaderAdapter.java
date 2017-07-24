package com.cooeeui.brand.zenlauncher.android.adapter;

import android.view.View;

public interface PinnedHeaderAdapter {

    public static final int PINNED_HEADER_GONE = 0;
    public static final int PINNED_HEADER_VISIBLE = 1;
    public static final int PINNED_HEADER_PUSHED_UP = 2;

    int getPinnedHeaderState(int position);

    void configurePinnedHeader(View header, int position, int alpha);
}
