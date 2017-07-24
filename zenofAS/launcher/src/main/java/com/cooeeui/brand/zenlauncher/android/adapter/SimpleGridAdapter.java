package com.cooeeui.brand.zenlauncher.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cooeeui.brand.zenlauncher.addingapp.ApplicationSimpleInfo;
import com.cooeeui.brand.zenlauncher.android.view.HandyTextView;
import com.cooeeui.zenlauncher.R;

import java.util.List;

public class SimpleGridAdapter extends BaseObjectListAdapter {

    public SimpleGridAdapter(Context context) {
        super(context);
    }

    SimpleGridAdapter(Context context, List<? extends ObjectEntity> datas) {
        super(context, datas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_addingapp_grid
                , null);
            holder.iv = (ImageView) convertView.findViewById(R.id.spinnedgrid_item_iv);
            holder.tv = (HandyTextView) convertView.findViewById(R.id.spinnedgrid_item_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ApplicationSimpleInfo info = (ApplicationSimpleInfo) getItem(position);
        holder.iv.setImageBitmap(info.getmIcon());
        holder.iv.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.tv.setText(info.getmTitle());

        return convertView;
    }

    class ViewHolder {

        ImageView iv;
        HandyTextView tv;

    }

}
