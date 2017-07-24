package com.cooeeui.brand.zenlauncher.tips;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;

public class TopListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<TopAppInfo> topApps;

    public TopListAdapter(Context mContext, ArrayList<TopAppInfo> topApps) {
        this.mContext = mContext;
        this.topApps = topApps;
    }

    @Override
    public int getCount() {
        return topApps.size();
    }

    @Override
    public TopAppInfo getItem(int position) {
        return topApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.tip_top_listitem, parent, false);
        } else {
            v = convertView;
        }
        TopAppInfo item = getItem(position);
        ((ImageView) v.findViewById(R.id.app_icon)).setImageDrawable((item.getAppIcon()));
        ((TextView) v.findViewById(R.id.app_name)).setText(item.getAppName());
        ((NumberProgressBar) v.findViewById(R.id.numberbar2)).setProgress(item.getPercent());
        ((NumberProgressBar) v.findViewById(R.id.numberbar2)).setReachedBarColor(mContext
                                                                                     .getResources()
                                                                                     .getColor(
                                                                                         item.getProgressColor()));
        float appUsedTime = item.getAppUsedTimeWithMinute();
        if (appUsedTime > 59) {
            float f = (float) (Math.round(appUsedTime / 60 * 10)) / 10;
            String unit = StringUtil.getString(mContext, R.string.tips_appused_unit_hour);
            ((TextView) v.findViewById(R.id.app_used_time)).setText(f + unit);
        } else {
            float f = (float) (Math.round(appUsedTime * 10)) / 10;
            String unit = StringUtil.getString(mContext, R.string.tips_appused_unit_min);
            ((TextView) v.findViewById(R.id.app_used_time)).setText(f + unit);
        }
        return v;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
