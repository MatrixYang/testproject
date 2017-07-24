package com.cooeeui.nanobooster.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.cooeeui.nanobooster.R;

public class DragViewForBooster extends FrameLayout {

    private FrameLayout mFl;
    private RippleView mFl_Bt_booster;

    private int mList_height;
    private int mButton_height;
    private int mButton_leftAndRight_margin;
    private int mButton_topAndBottom_margin;
    private int mButton_cover_top_margin;
    private int mHiddenListHeight;
    private ScrollView mSc_after_clean;
    private FrameLayout mFl_button_false_finish;

    public DragViewForBooster(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DragViewForBooster(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragViewForBooster(Context context) {
        super(context);
        init();
    }

    private void init() {

        mList_height =
            (int) (getContext().getResources().getDimension(R.dimen.mList_height) + 0.5f);
        mButton_height =
            (int) (getContext().getResources().getDimension(R.dimen.mButton_height) + 0.5f);
        mButton_leftAndRight_margin =
            (int) (getResources().getDimension(R.dimen.mButton_leftAndRight_margin) + 0.5f);
        mButton_topAndBottom_margin =
            (int) (getContext().getResources().getDimension(R.dimen.mButton_topAndBottom_margin)
                   + 0.5f);
        mButton_cover_top_margin =
            (int) (getContext().getResources().getDimension(R.dimen.mButton_cover_top_margin)
                   + 0.5f);
        mHiddenListHeight =
            (int) (getContext().getResources().getDimension(R.dimen.mHiddenListHeight) + 0.5f);

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mFl = (FrameLayout) findViewById(R.id.fl_child_include_list);
        mFl_Bt_booster = (RippleView) findViewById(R.id.fl_button_true);
        mSc_after_clean= (ScrollView)findViewById(R.id.sc_after_clean);
        mFl_button_false_finish= (FrameLayout)findViewById(R.id.fl_button_false_finish);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);


        /*
        * 1.确保listView的宽度全部可以拉出
        * 2.确保移动的同步性
        * */

        int
            bt_true_move =
            (int) (getContext().getResources().getDimension(R.dimen.bt_true_move) + 0.5f);

        int
            bt_finish =
            (int) (getContext().getResources().getDimension(R.dimen.bt_finish) + 0.5f);

        mFl.layout(left, bottom, right,
                   bottom + mList_height + mButton_height + mButton_topAndBottom_margin
        );//贴边隐藏

        mFl_Bt_booster.layout(left, bottom - bt_true_move
            , right, bottom);

        //摆放scrollView
        mSc_after_clean.layout(left,bottom-350,right,bottom+mSc_after_clean.getMeasuredHeight()-350);

        //摆放finish
        mFl_button_false_finish.layout(left, bottom - bt_true_move
            , right, bottom);

    }

}
