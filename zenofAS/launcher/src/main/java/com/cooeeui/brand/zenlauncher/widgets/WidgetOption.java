package com.cooeeui.brand.zenlauncher.widgets;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.zenlauncher.R;
import com.umeng.analytics.MobclickAgent;

public class WidgetOption {

    private Launcher mLauncher;

    private PopupWindow mPopupWindow;

    private View mView;

    public WidgetOption(Context context) {
        mLauncher = (Launcher) context;
        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.widget_option, null);
        View delete = mView.findViewById(R.id.widget_delete);
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLauncher.removeAppWidget();

                // 插件页删除插件次数
                MobclickAgent.onEvent(Launcher.getInstance(), "Deletewidgetsuccess");
            }
        });
        View add = mView.findViewById(R.id.widget_add);
        add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLauncher.getWidgetsView().resetSelect();
                mLauncher.hideWidgetOption();
                mLauncher.showAddWidget(false);
            }
        });
        View ok = mView.findViewById(R.id.widget_ok);
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLauncher.getWidgetsView().resetSelect();
                mLauncher.hideWidgetOption();
            }
        });

        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(mLauncher)) {
            View v = mView.findViewById(R.id.end_option_view);
            v.setVisibility(View.VISIBLE);
        }

        int width = DeviceUtils.getScreenPixelsWidth(context);
        mPopupWindow = new PopupWindow(mView, width, LayoutParams.WRAP_CONTENT);
        mPopupWindow.setAnimationStyle(R.style.menu_anim_style);
    }

    public void updateString() {
        TextView textView = (TextView) mView.findViewById(R.id.widget_add_text);
        String text = StringUtil.getString(mLauncher, R.string.widget_add);
        textView.setText(text);
    }

    public void show() {
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(mLauncher.getDragLayer(), Gravity.BOTTOM, 0, 0);
        }
    }

    public boolean hide() {
        boolean result = false;

        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            result = true;
        }

        return result;
    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }
}
