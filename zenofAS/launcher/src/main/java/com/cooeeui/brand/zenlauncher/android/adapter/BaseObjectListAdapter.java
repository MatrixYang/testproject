package com.cooeeui.brand.zenlauncher.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseObjectListAdapter extends BaseAdapter {

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<? extends ObjectEntity> mDatas = new ArrayList<ObjectEntity>();

    public BaseObjectListAdapter(Context context,
                                 List<? extends ObjectEntity> datas) {
        this(context);
        if (datas != null) {
            mDatas = datas;
        }
    }

    public BaseObjectListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public void refresh(List<? extends ObjectEntity> datas) {

        this.mDatas = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public List<? extends ObjectEntity> getDatas() {
        return mDatas;
    }

    public void addDatas(List<ObjectEntity> newDatas) {

        // if (newDatas != null){
        // for(int i=0;i<newDatas.size();i++){
        // this.mDatas.add(newDatas.get(i));
        // }
        // }
        // this.mDatas.addAll(newDatas);
        notifyDataSetChanged();

    }

    public void clear() {
        this.mDatas.clear();
        notifyDataSetChanged();
    }

}
