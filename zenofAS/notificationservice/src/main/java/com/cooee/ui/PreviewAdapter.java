package com.cooee.ui;


import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooeeui.notificationservice.R;


public class PreviewAdapter extends PagerAdapter {

    private Context mContext;
    private View[] layoutArray;

    public PreviewAdapter(
        Context context) {
        mContext = context;
        int[] imagIds = initImageIDS(context);
        layoutArray = new LinearLayout[imagIds.length];
        for (int i = 0; i < imagIds.length; i++) {
            layoutArray[i] = View.inflate(mContext, R.layout.viewpager_item_preview, null);
            ImageView imgView = (ImageView) layoutArray[i].findViewById(R.id.imageView);
            imgView.setImageResource(imagIds[i]);
        }
    }

    private int[] initImageIDS(Context context) {

        int[] is = new int[]{
            R.drawable.preview_img_1,
            R.drawable.preview_img_2,
            R.drawable.preview_img_3
        };
        return is;

    }


    @Override
    public int getCount() {
        return layoutArray.length;
    }

    /**
     * 从指定的position创建page
     *
     * @param container ViewPager容器
     * @param position  The page position to be instantiated.
     * @return 返回指定position的page，这里不需要是一个view，也可以是其他的视图容器.
     */
    @Override
    public Object instantiateItem(
        View collection,
        int position) {
        ((ViewPager) collection).addView(layoutArray[position]);
        return layoutArray[position];
    }

    @Override
    public void destroyItem(
        View collection,
        int position,
        Object view) {
        ((ViewPager) collection).removeView(layoutArray[position]);
    }

    @Override
    public boolean isViewFromObject(
        View view,
        Object object) {
        return view == (object);
    }

    @Override
    public void finishUpdate(
        View arg0) {
    }

    @Override
    public void restoreState(
        Parcelable arg0,
        ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void startUpdate(
        View arg0) {
    }
}
