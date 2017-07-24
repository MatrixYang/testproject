package com.cooeeui.brand.zenlauncher.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.wallpaper.util.PreferencesUtils;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class WidgetAdapter extends BaseAdapter implements WidgetsListView.SwappableList {

    private long num;

    private ArrayList<LauncherAppWidgetInfo> mWidgets = new ArrayList<LauncherAppWidgetInfo>();

    private HashMap<LauncherAppWidgetInfo, Long>
        mIdMap =
        new HashMap<LauncherAppWidgetInfo, Long>();

    private Context mContext;

    private View mEndView;

    private View mEndHeight;

    private Launcher mLauncher;

    private int mCount;

    private TextView mTextView;
    private ImageView imageViewTip;

    public WidgetAdapter(Context context) {
        super();
        num = 0;
        mContext = context;
        mLauncher = (Launcher) context;
        mEndView = LayoutInflater.from(mContext).inflate(R.layout.widget_view_end, null);
        mEndView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLauncher.showAddWidget(false);
            }
        });
        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(mLauncher)) {
            View v = mEndView.findViewById(R.id.end_view);
            v.setVisibility(View.VISIBLE);
        }
        mEndHeight = mEndView.findViewById(R.id.end_height);
        mTextView = (TextView) mEndView.findViewById(R.id.widget_end_text);
        imageViewTip = (ImageView) mEndView.findViewById(R.id.widget_new_tip);
        imageViewTip.setVisibility(View.VISIBLE);
    }

    public void updateString() {
        mTextView.setText(StringUtil.getString(mLauncher, R.string.widget_add));
    }

    public void setEndViewVisibility(int visibility) {
        mEndView.setVisibility(visibility);
    }

    public int getEndViewVisibility() {
        return mEndView.getVisibility();
    }

    public int getEndHeight() {
        return mEndHeight.getHeight();
    }

    public void setEndHeight(int h) {
        mEndHeight.setVisibility(View.VISIBLE);
        mEndHeight.getLayoutParams().height = h;
        mEndHeight.requestLayout();
    }

    public void reSetEndHeight() {
        mEndHeight.setVisibility(View.GONE);
    }

    public void updateBottomView(int count) {
        if (count < 1) {
            setEndViewVisibility(View.INVISIBLE);
            mLauncher.hideBottomView();
        } else if (count < 2) {
            setEndViewVisibility(View.INVISIBLE);
            mLauncher.showBottomView();
        }
    }

    @Override
    public int getCount() {
        mCount = mWidgets.size();
        if (mCount == 0) {
            return 0;
        }
        return mCount + 1;
    }

    @Override
    public Object getItem(int position) {
        mCount = mWidgets.size();
        if (position < 0 || position >= mCount) {
            return null;
        }
        return mWidgets.get(position);
    }

    public int getPosition(long id) {
        mCount = mWidgets.size();
        for (int i = 0; i < mCount; i++) {
            LauncherAppWidgetInfo info = mWidgets.get(i);
            if (mIdMap.get(info) == id) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public long getItemId(int position) {
        mCount = mWidgets.size();
        if (position < 0 || position >= mCount) {
            return -1;
        }
        LauncherAppWidgetInfo item = mWidgets.get(position);

        return mIdMap.get(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout = null;

        if (convertView == null) {
            layout = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.widget_view_item, null);
        } else {
            layout = (LinearLayout) convertView;
            layout.removeAllViews();
        }

        View view = null;

        mCount = mWidgets.size();
        if (position == mCount) {
            view = mEndView;
            if (!PreferencesUtils.getBoolean(mContext, "weather_widget", false)){
                imageViewTip.setVisibility(View.VISIBLE);
            }else{
                imageViewTip.setVisibility(View.INVISIBLE);
            }
        } else {
            if ("nano".equals(mWidgets.get(position).type)){
                view = mWidgets.get(position).hostView;
                PreferencesUtils.putBoolean(mContext, "weather_widget", true);
            }else{
                view = mWidgets.get(position).hostView;
            }

        }
        ViewGroup p = (ViewGroup) view.getParent();
        if (p != null) {
            p.removeAllViews();
        }

        layout.addView(view);
        return layout;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void startBinding() {
        num = 0;
        mCount = 0;
        mWidgets.clear();
        mIdMap.clear();
    }

    public void bindAppWidget(LauncherAppWidgetInfo info) {
        mIdMap.put(info, num);
        mWidgets.add(info);
        num++;
        mCount = mWidgets.size();
        if (info.position != mCount - 1) {
            info.position = mCount - 1;
            LauncherModel.updateItemInDatabase(mContext, info);
        }
    }

    public void finishBindWidget() {
        mCount = mWidgets.size();
        updateBottomView(mCount);
        notifyDataSetChanged();
    }

    public void bindRemoved(ArrayList<String> packageNames, ArrayList<AppInfo> appInfos) {
        HashSet<String> cns = new HashSet<String>();

        for (AppInfo info : appInfos) {
            cns.add(info.componentName.getPackageName());
        }
        for (String pkgName : packageNames) {
            cns.add(pkgName);
        }

        mCount = mWidgets.size();
        for (int i = mCount - 1; i >= 0; i--) {
            String name = mWidgets.get(i).providerName.getPackageName();
            if (cns.contains(name)) {
                mWidgets.remove(i);
            }
        }

        mCount = mWidgets.size();
        updateBottomView(mCount);
        notifyDataSetChanged();
    }

    public void addWidget(LauncherAppWidgetInfo info) {
        if (info != null) {
            mIdMap.put(info, num);
            mWidgets.add(info);
            num++;
            mCount = mWidgets.size();
            updateBottomView(mCount);
            LauncherModel.addItemToDatabase(mContext, info);
            notifyDataSetChanged();
        }
    }

    public void removeWidget(LauncherAppWidgetInfo info) {
        if (info != null) {
            mIdMap.remove(info);
            mWidgets.remove(info);
            mCount = mWidgets.size();
            updateBottomView(mCount);
            LauncherModel.deleteItemFromDatabase(mContext, info);
            notifyDataSetChanged();

            Intent intent = new Intent(NanoWidgetUtils.ACTION_WIDGET_DELETE);
            intent.putExtra("widget_id",info.appWidgetId);
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void swap(int pos1, int pos2) {
        mCount = mWidgets.size();
        if (pos1 < 0 || pos1 >= mCount || pos2 < 0 || pos2 >= mCount) {
            return;
        }

        LauncherAppWidgetInfo info1 = mWidgets.get(pos1);
        LauncherAppWidgetInfo info2 = mWidgets.get(pos2);
        int position1 = info1.position;
        int position2 = info2.position;
        Collections.swap(mWidgets, pos1, pos2);
        info1.position = position2;
        info2.position = position1;
        LauncherModel.updateItemInDatabase(mContext, info1);
        LauncherModel.updateItemInDatabase(mContext, info2);
        notifyDataSetChanged();
    }

}
