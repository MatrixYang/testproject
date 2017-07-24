package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;

public class QsListViewAdapter extends BaseAdapter {

    private ArrayList<String> mOrderedSection;
    private ArrayList<ArrayList<Object>> mDatas;
    private ArrayList<Object> mGridDatas;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public QsListViewAdapter(Context mContext, ArrayList<String> orderedSections,
                             ArrayList<ArrayList<Object>> mList) {
        super();
        this.mContext = mContext;
        this.mOrderedSection = orderedSections;
        this.mDatas = mList;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    public ArrayList<String> getOrderedSection() {
        return this.mOrderedSection;
    }

    public ArrayList<ArrayList<Object>> getDatas() {
        return this.mDatas;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.qs_listview_item, null, false);
            holder.scetion = (TextView) convertView.findViewById(R.id.listview_item_scetion);
            holder.gridView = (GridView) convertView.findViewById(R.id.listview_item_gridview);
            holder.gridViewAdapter = new QsGridViewAdapter(mContext);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (this.mDatas != null) {
            if (holder.scetion != null) {
                holder.scetion.setText(this.mOrderedSection.get(position));
            }
            if (holder.gridView != null) {
                mGridDatas = this.mDatas.get(position);
                holder.gridViewAdapter.setDatas(mGridDatas);
                holder.gridView.setAdapter(holder.gridViewAdapter);
            }
        }
        return convertView;
    }

    private class ViewHolder {

        TextView scetion;
        GridView gridView;
        QsGridViewAdapter gridViewAdapter;
    }

    public void configureHeaderView(LinearLayout headerView, int position) {
        TextView textView = (TextView) headerView.getChildAt(0);
        textView.setText(mOrderedSection.get(position));
    }
}
