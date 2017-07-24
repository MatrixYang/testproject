package com.cooeeui.brand.zenlauncher.scenes;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DensityUtil;
import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.favorite.SpeedyContainer;
import com.cooeeui.brand.zenlauncher.favorite.usagestats.UsageUtil;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragLayer;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

public class GuidePage {

    private static final int GUIDE_DURATION = 500;
    private static final int DELAY_DURATION = 200;
    private Launcher mLauncher;
    private DragLayer mDragLayer;
    private SpeedDial mSpeedDial;
    private GridView mFavoriteContainer;
    private SpeedyContainer mSpeedyContainer;
    private PopupWindow mGuideWindow = null;
    private int mWidth;
    private int mHeight;
    private int mState;
    private LayoutInflater mInflater;
    private Interpolator mAccelerate;
    private ValueAnimator mAnimator1;
    private ValueAnimator mAnimator2;
    private ValueAnimator mAnimator3;
    private ValueAnimator mAnimator4;
    private ValueAnimator mAnimatorNextButton;

    private View mGuide1;
    private View mGuide2;
    private View mGuide3;
    private ImageView mImage1;
    private ImageView mImage2;
    private Button mButton;
    private Button mButtonNext;

    public GuidePage(Launcher launcher) {
        mLauncher = launcher;
        mDragLayer = launcher.getDragLayer();
        mSpeedDial = launcher.getSpeedDial();
        mFavoriteContainer = launcher.getFavoriteContainer();
        mSpeedyContainer = launcher.getSpeedyContainer();

        mWidth = DeviceUtils.getScreenPixelsWidth(mLauncher);
        mHeight = DeviceUtils.getScreenPixelsHeight(mLauncher);

        mInflater = LayoutInflater.from(mLauncher);

        mAccelerate = AnimationUtils.loadInterpolator(mLauncher,
                                                      android.R.anim.accelerate_interpolator);
        mAnimator1 = ValueAnimator.ofFloat(0, 1f);
        mAnimator1.setDuration(GUIDE_DURATION);
        mAnimator1.setStartDelay(500);
        mAnimator1.setInterpolator(mAccelerate);
        mAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                if (mState != Launcher.STATE_HOMESCREEN) {
                    mImage1.setAlpha(value * 255);
                }
                mGuide1.setAlpha(value);
            }
        });

        mAnimator2 = ValueAnimator.ofFloat(0, 1f);
        mAnimator2.setDuration(GUIDE_DURATION);
        mAnimator2.setStartDelay(DELAY_DURATION);
        mAnimator2.setInterpolator(mAccelerate);
        mAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                if (mState != Launcher.STATE_FAVORITE) {
                    mImage2.setAlpha(value * 255);
                }
                mGuide2.setAlpha(value);
            }
        });

        mAnimatorNextButton = ValueAnimator.ofFloat(0, 1f);
        mAnimatorNextButton.setDuration(GUIDE_DURATION);
        mAnimatorNextButton.setInterpolator(mAccelerate);
        mAnimatorNextButton.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mButtonNext.setAlpha(value);
            }
        });

        mAnimator3 = ValueAnimator.ofFloat(0, 1f);
        mAnimator3.setDuration(GUIDE_DURATION);
        mAnimator3.setInterpolator(mAccelerate);
        mAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mGuide3.setAlpha(value);
            }
        });

        mAnimator4 = ValueAnimator.ofFloat(0, 1f);
        mAnimator4.setDuration(GUIDE_DURATION);
        mAnimator4.setInterpolator(mAccelerate);
        mAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mButton.setAlpha(value);
            }
        });
    }

    private BitmapDrawable getGuideDrawable(View v, int padding, boolean isCircle) {
        Bitmap b = getBitmapFromView(v);
        int w = v.getWidth() + padding;
        int h = v.getHeight() + padding;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(bitmap);
        can.drawBitmap(b, padding / 2, padding / 2, null);

        if (isCircle) {
            Rect rect = new Rect(0, 0, w, h);
            Drawable d = mLauncher.getResources().getDrawable(R.drawable.select_circle);
            d.setBounds(rect);
            d.draw(can);
        }

        BitmapDrawable drawable = new BitmapDrawable(mLauncher.getResources(), bitmap);

        return drawable;
    }

    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private BitmapDrawable getGuideDrawable(int id, int w, int h, int padding) {
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Rect r = new Rect(0, 0, w, h);
        Drawable d = mLauncher.getResources().getDrawable(id);
        d.setBounds(r);
        d.draw(c);

        w += padding;
        h += padding;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(b, padding / 2, padding / 2, null);
        Rect rect = new Rect(0, 0, w, h);
        d = mLauncher.getResources().getDrawable(R.drawable.select_circle);
        d.setBounds(rect);
        d.draw(canvas);

        BitmapDrawable drawable = new BitmapDrawable(mLauncher.getResources(), bitmap);

        return drawable;
    }

    private void setButton(View view) {
        int x = mWidth - DensityUtil.dip2px(mLauncher, 100);
        int y = mHeight - DensityUtil.dip2px(mLauncher, 65);
        mButton = (Button) view.findViewById(R.id.guide_ok);
        mButton.setTranslationX(x);
        mButton.setTranslationY(y);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissGuide();

            }
        });
        mButton.setAlpha(0f);
    }

    private void setButtonNext(View view) {
        mButtonNext = (Button) view.findViewById(R.id.guide_next);
        if (UsageUtil.isNoOption(mLauncher)) {
            int x = mWidth - DensityUtil.dip2px(mLauncher, 100);
            int y = mHeight - DensityUtil.dip2px(mLauncher, 65 + 200);
            mButtonNext.setTranslationX(x);
            mButtonNext.setTranslationY(y);
            mButtonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UsageUtil.startUsageSettingActivity(mLauncher,
                                                        Launcher.REQUEST_USAGE_SETTING_GUIDE_PAGE);
                    MobclickAgent.onEvent(mLauncher, "clickNext");
                    Intent intent = new Intent(LauncherConstants.ACTION_USAGE_SETTING_TIP_SHOW);
                    mLauncher.sendBroadcast(intent);


                }
            });
            mButtonNext.setAlpha(0f);
        } else {
            mButtonNext.setVisibility(View.GONE);
        }

    }

    public void showGuide(int state) {
        mState = state;

        switch (state) {
            case Launcher.STATE_HOMESCREEN:
                showGuideHomeScreen();
                break;

            case Launcher.STATE_FAVORITE:
                showGuideFavorite();
                break;

            case Launcher.STATE_ALLAPP:
                showGuideAllApp();
                break;
        }
    }

    private void showGuideHomeScreen() {
        View view = mInflater.inflate(R.layout.guide_homescreen, null);
        mGuideWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        int x = DensityUtil.dip2px(mLauncher, 30);
        int y = mHeight * 4 / 10;
        mGuide1 = view.findViewById(R.id.guide1);
        TextView textView = (TextView) view.findViewById(R.id.guide_text1);
        String text = StringUtil.getString(mLauncher, R.string.guide_home_text1);
        textView.setText(text);
        mGuide1.setTranslationX(x);
        mGuide1.setTranslationY(y);
        mGuide1.setAlpha(0f);

        mImage2 = (ImageView) view.findViewById(R.id.guide_select);
        ArrayList<BubbleView> bubbleViews = mSpeedDial.getBubbleViews();
        if (bubbleViews.size() > 0 && bubbleViews.get(0).getWidth() > 0) {
            Rect r = new Rect();
            bubbleViews.get(0).getGlobalVisibleRect(r);
            BitmapDrawable drawable = getGuideDrawable(bubbleViews.get(0), 0, true);
            mImage2.setImageDrawable(drawable);
            mImage2.setTranslationX(r.left);
            mImage2.setTranslationY(r.top);

            int padding = drawable.getIntrinsicWidth() * 2 / 5;
            x = r.right - padding;
            y = r.bottom - padding;
        } else {
            x = DensityUtil.dip2px(mLauncher, 60);
            y = mHeight - DensityUtil.dip2px(mLauncher, 180);
            int s = DensityUtil.dip2px(mLauncher, 40);
            BitmapDrawable drawable = getGuideDrawable(R.drawable.guide_icon, s, s, s / 2);
            mImage2.setImageDrawable(drawable);
            mImage2.setTranslationX(x);
            mImage2.setTranslationY(y);
            x += s;
            y += s;
        }
        mImage2.setAlpha(0f);

        mGuide2 = view.findViewById(R.id.guide2);
        textView = (TextView) view.findViewById(R.id.guide_text2);
        text = StringUtil.getString(mLauncher, R.string.guide_home_text2);
        textView.setText(text);
        mGuide2.setTranslationX(x);
        mGuide2.setTranslationY(y);
        mGuide2.setAlpha(0f);

        setButton(view);

        mGuideWindow.setAnimationStyle(R.style.GuideAnimStyle);
        mGuideWindow.showAtLocation(mDragLayer, Gravity.TOP, 0, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        mAnimator4.setStartDelay(0);
        animatorSet.play(mAnimator1).with(mAnimator2);
        animatorSet.play(mAnimator4).after(mAnimator1);
        animatorSet.start();


    }

    private void showGuideFavorite() {
        View view = mInflater.inflate(R.layout.guide_favorite, null);
        mGuideWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        int x, y;
        Rect r = new Rect();
        mImage1 = (ImageView) view.findViewById(R.id.guide_select);
        if (mFavoriteContainer != null && mFavoriteContainer.getChildCount() > 0
            && mFavoriteContainer.getChildAt(0).getWidth() > 0) {
            View v = mFavoriteContainer.getChildAt(0);
            v.getGlobalVisibleRect(r);
            int p = v.getWidth() / 2;
            BitmapDrawable drawable = getGuideDrawable(v, p, true);
            mImage1.setImageDrawable(drawable);
            mImage1.setTranslationX(r.left - p / 2);
            mImage1.setTranslationY(r.top - p / 2);

            int padding = drawable.getIntrinsicWidth() / 4;
            x = r.right - padding;
            y = r.bottom - padding;
        } else {
            x = DensityUtil.dip2px(mLauncher, 20);
            y = DensityUtil.dip2px(mLauncher, 80);
            int s = DensityUtil.dip2px(mLauncher, 40);
            BitmapDrawable drawable = getGuideDrawable(R.drawable.guide_icon, s, s, s / 2);
            mImage1.setImageDrawable(drawable);
            mImage1.setTranslationX(x);
            mImage1.setTranslationY(y);
            x += s;
            y += s;
        }
        mImage1.setAlpha(0f);

        mGuide1 = view.findViewById(R.id.guide1);
        TextView textView = (TextView) view.findViewById(R.id.guide_text1);
        String text = StringUtil.getString(mLauncher, R.string.guide_favorite_text1);
        textView.setText(text);
        mGuide1.setTranslationX(x);
        mGuide1.setTranslationY(y);
        mGuide1.setAlpha(0f);

        mGuide2 = view.findViewById(R.id.guide2);
        textView = (TextView) view.findViewById(R.id.guide_text2);
        if (UsageUtil.isNoOption(mLauncher)) {
            text = StringUtil.getString(mLauncher, R.string.guide_favorite_text2_usage);
        } else {
            text = StringUtil.getString(mLauncher, R.string.guide_favorite_text2);
        }

        textView.setText(text);
        x = DensityUtil.dip2px(mLauncher, 30);
        y = mHeight * 3 / 10;
        mGuide2.setTranslationX(x);
        mGuide2.setTranslationY(y);
        mGuide2.setAlpha(0f);

        mGuide3 = view.findViewById(R.id.guide3);
        textView = (TextView) view.findViewById(R.id.guide_text3);
        text = StringUtil.getString(mLauncher, R.string.guide_favorite_text3);
        textView.setText(text);
        mSpeedyContainer.getGlobalVisibleRect(r);
        x = mWidth - DensityUtil.dip2px(mLauncher, 220);
        y = r.top - DensityUtil.dip2px(mLauncher, 130);
        mGuide3.setTranslationX(x);
        mGuide3.setTranslationY(y);
        mGuide3.setAlpha(0f);

        View v = mDragLayer.findViewById(R.id.speedy_container);
        v.getGlobalVisibleRect(r);
        BitmapDrawable drawable = getGuideDrawable(v, 0, false);
        ImageView speedy = (ImageView) view.findViewById(R.id.guide_speedy);
        speedy.setImageDrawable(drawable);
        speedy.setTranslationX(r.left);
        speedy.setTranslationY(r.top);

        setButton(view);
        setButtonNext(view);

        mGuideWindow.setAnimationStyle(R.style.GuideAnimStyle);
        mGuideWindow.showAtLocation(mDragLayer, Gravity.TOP, 0, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        if (UsageUtil.isNoOption(mLauncher)) {
            mAnimatorNextButton.setStartDelay(0);
            animatorSet.play(mAnimator1).with(mAnimator2);
            animatorSet.play(mAnimatorNextButton).after(mAnimator1);
            animatorSet.start();
        } else {
            mAnimator4.setStartDelay(DELAY_DURATION);
            animatorSet.play(mAnimator1).with(mAnimator2);
            animatorSet.play(mAnimator3).after(mAnimator1);
            animatorSet.play(mAnimator3).with(mAnimator4);
        }

        animatorSet.start();
    }

    private void showGuideAllApp() {
        View view = mInflater.inflate(R.layout.guide_allapp, null);
        mGuideWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        int x, y;
        Rect r = new Rect();
        View hide = mLauncher.Allapp.findViewById(R.id.textViewHideIcon);
        mImage1 = (ImageView) view.findViewById(R.id.guide_select);
        int w = DensityUtil.dip2px(mLauncher, 20);
        BitmapDrawable drawable = getGuideDrawable(R.drawable.menu_moreoverflow, w, w, w);
        mImage1.setImageDrawable(drawable);
        hide.getGlobalVisibleRect(r);
        mImage1.setTranslationX(r.right - w * 3 / 2);
        mImage1.setTranslationY(r.top - w / 2);
        mImage1.setAlpha(0f);

        x = r.right - DensityUtil.dip2px(mLauncher, 215) - w / 4;
        y = r.bottom - drawable.getIntrinsicWidth() / 4;

        mGuide1 = view.findViewById(R.id.guide1);
        TextView textView = (TextView) view.findViewById(R.id.guide_text1);
        String text = StringUtil.getString(mLauncher, R.string.guide_allapp_text1);
        textView.setText(text);
        mGuide1.setTranslationX(x);
        mGuide1.setTranslationY(y);
        mGuide1.setAlpha(0f);

        mImage2 = (ImageView) view.findViewById(R.id.guide_select2);
        View v = mLauncher.Allapp.getMiddleView();
        if (v != null && v.getWidth() > 0) {
            int p = v.getWidth() / 2;
            drawable = getGuideDrawable(v, p, true);
            v.getGlobalVisibleRect(r);
            mImage2.setImageDrawable(drawable);
            mImage2.setTranslationX(r.left - p / 2);
            mImage2.setTranslationY(r.top - p / 2);

            int padding = drawable.getIntrinsicWidth() / 4;
            x = r.right - padding;
            y = r.bottom - padding;
        } else {
            x = DensityUtil.dip2px(mLauncher, 90);
            y = mHeight / 2;
            int s = DensityUtil.dip2px(mLauncher, 40);
            drawable = getGuideDrawable(R.drawable.guide_icon, s, s, s / 2);
            mImage2.setImageDrawable(drawable);
            mImage2.setTranslationX(x);
            mImage2.setTranslationY(y);
            x += s;
            y += s;
        }
        mImage2.setAlpha(0f);

        mGuide2 = view.findViewById(R.id.guide2);
        textView = (TextView) view.findViewById(R.id.guide_text2);
        text = StringUtil.getString(mLauncher, R.string.guide_allapp_text2);
        textView.setText(text);
        mGuide2.setTranslationX(x);
        mGuide2.setTranslationY(y);
        mGuide2.setAlpha(0f);

        setButton(view);

        mGuideWindow.setAnimationStyle(R.style.GuideAnimStyle);
        mGuideWindow.showAtLocation(mDragLayer, Gravity.TOP, 0, 0);

        AnimatorSet animatorSet = new AnimatorSet();
        mAnimator4.setStartDelay(0);
        animatorSet.play(mAnimator1).with(mAnimator2);
        animatorSet.play(mAnimator4).after(mAnimator1);
        animatorSet.start();
    }

    public void dismissGuide() {
        if (mGuideWindow != null) {
            if (mGuideWindow.isShowing()) {
                mGuideWindow.dismiss();
            }
            mGuideWindow = null;
            mLauncher.mGuiding = false;
        }
    }

    /**
     * 隐藏next按钮，显示常用页下面的提示，弹出提醒框
     */
    public void checkUsageStatus(Context context) {
        mButtonNext.setVisibility(View.GONE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mAnimator3).with(mAnimator4);
        animatorSet.start();
        if (!UsageUtil.isUsageAllowed(context)) {
            MobclickAgent.onEvent(mLauncher, "clickNextfailed");
            UsageUtil.showUsageAlert(mLauncher);
        }
    }
}
