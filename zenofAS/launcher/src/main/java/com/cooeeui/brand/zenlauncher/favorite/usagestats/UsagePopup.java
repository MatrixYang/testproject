package com.cooeeui.brand.zenlauncher.favorite.usagestats;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.zenlauncher.R;

public class UsagePopup implements OnTouchListener {

    private static final int MESSAGE_OPEN_VIEW = 0;
    private static final int MESSAGE_CLOSE_TIPS = 1;
    private static final int MESSAGE_DELAY_TIPVIEW = 500;

    private WindowManager mWindowManager;
    private View mAllView = null;
    private View mTipsView = null;
    private WindowManager.LayoutParams mParams;
    private RelativeLayout mLayoutBottom;
    private Context mContext;
    private boolean removeAnimatorDoing = false;

    public UsagePopup(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        int width = DeviceUtils.getScreenPixelsWidth(mContext);
        int height = DeviceUtils.getScreenPixelsHeight(mContext);
        if (width > height) {
            width = height;
        }

        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mParams.x = 0;
        mParams.y = 0;
        mParams.width = width;
        mParams.height = LayoutParams.MATCH_PARENT;
    }


    public void showViewTips() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_OPEN_VIEW, MESSAGE_DELAY_TIPVIEW);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_OPEN_VIEW:
                    updateTipsView();
                    break;

                case MESSAGE_CLOSE_TIPS:
                    removeTipAnimator();
                    break;
            }
        }
    };

    private void removeTipAnimator(){
        if (removeAnimatorDoing){
            return;
        }
        if (mTipsView ==null){
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.usage_set);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                removeAnimatorDoing = true;
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                removeTipsView();
                removeAnimatorDoing = false;
            }
        });
        mTipsView.startAnimation(animation);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int[] point = new int[2];
                if (mLayoutBottom != null) {
                    mLayoutBottom.getLocationOnScreen(point);
                    if (event.getRawY() < point[1]){
                        mHandler.sendEmptyMessage(MESSAGE_CLOSE_TIPS);
                    }
                }

                break;
        }
        return false;
    }


    public void updateTipsView() {

        if (mAllView == null) {
            mAllView = LayoutInflater.from(mContext).inflate(R.layout.usage_view, null);
            mAllView.setOnTouchListener(this);
            mWindowManager.addView(mAllView, mParams);
            mTipsView = mAllView.findViewById(R.id.rl_bottom);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.usage_translate);
            mTipsView.startAnimation(animation);
            mLayoutBottom = (RelativeLayout) mAllView.findViewById(R.id.rl_bottom);
        }
    }

    public void remove(){
        removeTipAnimator();
    }

    private void removeTipsView() {
        if (mAllView != null) {
            mWindowManager.removeView(mAllView);
            mAllView = null;
            mTipsView = null;
        }
    }
}
