package com.cooeeui.brand.zenlauncher.tips;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;

/**
 * Created by Steve on 2015/4/22.
 */
public class SimpleGalleryAdapter extends BaseAdapter {

    private ArrayList<String> datas;
    private Context context;

    public SimpleGalleryAdapter(Context context, ArrayList<String> datas) {
        this.context = context;
        this.datas = datas;
    }

    private int selectItem = 0;

    public void setSelectItem(int selectItem) {
        if (this.selectItem != selectItem) {
            this.selectItem = selectItem;
        }
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View v;
        TextView tv;
        if (convertView != null) {
            v = (View) convertView;
        } else {
            v = (View) LayoutInflater.from(context).inflate(R.layout.tip_date_gallery_item, null);
        }
        tv = (TextView) v.findViewById(R.id.date);
        if (selectItem == position) {
            tv.setText(datas.get(position));
        } else {
            tv.setText(datas.get(position));
        }
        return v;
    }

}
