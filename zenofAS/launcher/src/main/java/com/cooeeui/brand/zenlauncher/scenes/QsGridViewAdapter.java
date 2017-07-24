package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.Utilities;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class QsGridViewAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Object> mDatas;
    private Workspace mWorkspace;

    public QsGridViewAdapter(Context context) {
        super();
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        mWorkspace = ((Launcher) context).workspace;
    }

    public void setDatas(ArrayList<Object> mList) {
        this.mDatas = mList;
    }

    @Override
    public int getCount() {
        if (mDatas == null) {
            return 0;
        } else {
            return this.mDatas.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mDatas == null) {
            return null;
        } else {
            return this.mDatas.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.qs_gridview_item, parent, false);
            holder.application = (RelativeLayout) convertView
                .findViewById(R.id.gridview_application);
            holder.application_textview = (TextView) convertView
                .findViewById(R.id.gridview_application_textview);
            holder.application_icon = (ImageView) convertView
                .findViewById(R.id.gridview_application_icon);
            holder.imageView = (ImageView) convertView.findViewById(R.id.hideStatus);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.mDatas != null) {
            final AppInfo appinfo = (AppInfo) this.mDatas.get(position);
            if (holder.application != null) {
                holder.application_textview.setText(appinfo.title.toString());
                holder.application_icon.setImageDrawable(Utilities
                                                             .createIconDrawable(
                                                                 appinfo.iconBitmap));
                if (mWorkspace.isEditAllApp()) {
                    holder.imageView.setImageResource(appinfo.hideTemp ? R.drawable.hide
                                                                       : R.drawable.unhide);
                    UIEffectTools.onSelectedHiden(holder.application, appinfo.hideTemp);
                } else {
                    holder.imageView.setImageDrawable(null);
                }
                holder.application.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mWorkspace.isEditAllApp()) {
                            appinfo.hideTemp = !appinfo.hideTemp;
                            UIEffectTools.onSelectedHiden(v, appinfo.hideTemp);
                        } else {
                            UIEffectTools.onClickEffect(v);
                            Launcher.getInstance().startActivitySafely(appinfo.intent);
                            Launcher.getInstance().needHideAllapp = true;

                            // all app中应用点击次数
                            MobclickAgent.onEvent(mContext, "AllAppClickApp");
                        }
                    }
                });
                holder.application.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        // TODO Auto-generated method stub
                        Launcher.getInstance().Allapp.OnLongClickForGridItem(v, appinfo);
                        return true;
                    }
                });
            }
        }
        return convertView;
    }

    private class ViewHolder {

        RelativeLayout application;
        TextView application_textview;
        ImageView application_icon;
        ImageView imageView;

    }

}
