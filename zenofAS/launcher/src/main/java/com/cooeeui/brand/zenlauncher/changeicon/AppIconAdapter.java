package com.cooeeui.brand.zenlauncher.changeicon;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;

public class AppIconAdapter extends BaseAdapter {

    private ArrayList<IconBase> iconBases = new ArrayList<IconBase>();
    private Context context;
    private Context otherContext;

    public AppIconAdapter(Context context, ArrayList<IconBase> iconBases) {
        this.context = context;
        this.iconBases.clear();
        this.iconBases = iconBases;
        try {
            otherContext = this.context.createPackageContext(
                ChangeAppIcon.iconPackageName,
                Context.CONTEXT_IGNORE_SECURITY);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return iconBases.size();
    }

    @Override
    public Object getItem(int position) {
        return iconBases.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                R.layout.app_icon_item, null);
            holder.icon = (ImageView) convertView
                .findViewById(R.id.iv_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        /**
         * 根据不同的图标类型用不同的方法取出app icon
         */
        switch (iconBases.get(position).getTypeIcon()) {
            case IconBase.ICON_TYPE_APP_DEFALUT:
                holder.icon.setImageBitmap(iconBases.get(position)
                                               .getIconBitmap());
                break;
            case IconBase.ICON_TYPE_ZEN_SIX_ICON:
                holder.icon.setImageDrawable(context.getResources()
                                                 .getDrawable(
                                                     iconBases.get(position).getImageId()));
                break;
            case IconBase.ICON_TYPE_OUR_APPLICATION:
                holder.icon.setImageDrawable(otherContext.getResources()
                                                 .getDrawable(
                                                     iconBases.get(position).getImageId()));
                break;
            case IconBase.ICON_TYPE_THIRD_PARTY_APPLICATION:
                holder.icon.setImageDrawable(otherContext.getResources()
                                                 .getDrawable(
                                                     iconBases.get(position).getImageId()));
                break;
        }

        return convertView;
    }

    class ViewHolder {

        ImageView icon;
    }
}
