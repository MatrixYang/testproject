package com.cooeeui.brand.zenlauncher.android.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class HandyTextView extends TextView {

    public HandyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HandyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        setLines(1);
        // setFocusable(true);
        // setFocusableInTouchMode(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);

    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
            text = "";
        }
        super.setText(text, type);
    }

}
