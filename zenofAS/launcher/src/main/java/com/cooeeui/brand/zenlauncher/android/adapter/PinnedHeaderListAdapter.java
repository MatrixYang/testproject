package com.cooeeui.brand.zenlauncher.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SectionIndexer;

import com.cooeeui.brand.zenlauncher.addingapp.ApplicationSection;
import com.cooeeui.brand.zenlauncher.addingapp.ApplicationSimpleInfo;
import com.cooeeui.brand.zenlauncher.android.view.HandyTextView;
import com.cooeeui.brand.zenlauncher.android.view.SimpleGridView;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;
import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PinnedHeaderListAdapter extends BaseObjectListAdapter implements SectionIndexer,
                                                                              PinnedHeaderAdapter {

    private OnGridViewSelecte mOnGridViewSelecte;
    private List<ApplicationSection> mApplicationSections = new ArrayList<ApplicationSection>();
    ;
    private int[] mPositions;
    private int mCount = 0;
    private Context mContext;

    public PinnedHeaderListAdapter(Context context, List<? extends ObjectEntity> datas) {
        super(context, datas);
        mContext = context;

        for (int i = 0; i < datas.size(); i++) {
            mApplicationSections.add((ApplicationSection) datas.get(i));
        }

        mPositions = new int[datas.size()];

        int postion = 0;

        for (int i = 0; i < mApplicationSections.size(); i++) {
            mPositions[i] = postion;
            postion += 1;
        }

        mCount = postion;
    }

    public static interface OnGridViewSelecte {

        public void OnItemSelected(Bitmap bitmap, String title);

    }

    public void setOnGridViewSelecte(OnGridViewSelecte onGridViewSelecte) {
        this.mOnGridViewSelecte = onGridViewSelecte;
    }

    public PinnedHeaderListAdapter(Context context) {
        super(context);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_addingapp_list
                , null);
            holder.tv = (HandyTextView) convertView.findViewById(R.id.spinnedlist_txt);
            holder.gv = (SimpleGridView) convertView.findViewById(R.id.spinnedlist_gridview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String l = mApplicationSections.get(position).getmLetter();
        holder.tv.setText(l);

        if (holder.gv != null) {

            final List<ApplicationSimpleInfo> items = mApplicationSections.get(position)
                .getmApplications();
            SimpleGridAdapter ga = new SimpleGridAdapter(mContext, items);
            holder.gv.setAdapter(ga);
            holder.gv.setOnItemClickListener(new GridView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int
                    position, long id) {

                    UIEffectTools.onClickEffect(view);
                    mOnGridViewSelecte.OnItemSelected(items.get(position).getmIcon(),
                                                      items.get(position).getmTitle());
                }
            });
            // holder.tv.setText(mApplicationSections.get(position).getmTitle());

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mPositions.length;
    }

    class ViewHolder {

        HandyTextView tv;
        SimpleGridView gv;
    }

    @Override
    public int getPositionForSection(int position) {
        if (position < 0 || position >= mApplicationSections.size()) {
            return -1;
        }
        return mPositions[position];
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mCount) {
            return -1;
        }
        int index = Arrays.binarySearch(mPositions, position);
        return index >= 0 ? index : -index - 2;
    }

    @Override
    public Object[] getSections() {
        return mApplicationSections.toArray();
    }

    @Override
    public int getPinnedHeaderState(int position) {
        int realPosition = position;// position - 1;
        if (realPosition < 0) {
            return PINNED_HEADER_GONE;
        }
        int section = getSectionForPosition(realPosition);
        int nextSectionPosition = getPositionForSection(section + 1);
        if (nextSectionPosition != -1
            && realPosition == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {
        int realPosition = position - 1;
        int section = getSectionForPosition(realPosition);
        if (mApplicationSections.size() <= position) {
            return;
        }
        ApplicationSection as = mApplicationSections.get(position);
        HandyTextView tv = (HandyTextView)
            header.findViewById(R.id.spinnedlist_txt);
        tv.setText(as.getmLetter());
    }

}
