package com.cooeeui.brand.zenlauncher.widgets;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.zenlauncher.R;

public class WidgetDrag {

    private Launcher mLauncher;

    private PopupWindow mPopupWindow;

    private View mView;

    public WidgetDrag(Context context) {
        mLauncher = (Launcher) context;
        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.widget_drag_text, null);

        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(mLauncher)) {
            View v = mView.findViewById(R.id.end_drag_view);
            v.setVisibility(View.VISIBLE);
        }

        int width = DeviceUtils.getScreenPixelsWidth(context);
        mPopupWindow = new PopupWindow(mView, width, LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.menu_anim_style);
    }

    public void updateString() {
        TextView textView = (TextView) mView.findViewById(R.id.widget_drag_text);
        String text = StringUtil.getString(mLauncher, R.string.widget_drag_text);
        textView.setText(text);
    }

    public void show() {
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(mLauncher.getDragLayer(), Gravity.BOTTOM, 0, 0);
        }
    }

    public void hide() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }
}
