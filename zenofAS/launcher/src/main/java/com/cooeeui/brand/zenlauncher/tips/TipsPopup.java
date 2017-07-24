package com.cooeeui.brand.zenlauncher.tips;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.cooeeui.basecore.utilities.DateUtil;
import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.CommonTimeInfo;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.favorite.FavoritesData;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class TipsPopup implements OnTouchListener {

    private static final int MESSAGE_OPEN_VIEW = 0;
    private static final int MESSAGE_CLOSE_TIPS = 1;

    private static final int MESSAGE_DELAY_TIME = 15000;
    private static final int MESSAGE_DELAY_TIPVIEW = 2000;

    private static int VALUE_UNLOCK_LOW = 50;
    private static int VALUE_UNLOCK_MID = 125;
    private static int VALUE_UNLOCK_HIGH = 200;
    private static int UNLOCK_INTERVAL = 20;

    private static int VALUE_TIME_LOW = 120;
    private static int VALUE_TIME_MID = 240;
    private static int VALUE_TIME_HIGH = 360;
    private static int TIME_INTERVAL = 60;

    private static int VALUE_OTHER_LOW = 120;
    private static int VALUE_OTHER_MID = 190;
    private static int VALUE_OTHER_HIGH = 270;
    private static int OTHER_INTERVAL = 20;

    private static int VALUE_APP_LOW = 30;
    private static int VALUE_APP_MID = 60;
    private static int VALUE_APP_HIGH = 90;

    public static final int NONE = -1;
    public static int mDataId = NONE;
    public static float mAngle = 0;
    private static int mStringId = NONE;
    private static int mPreId = NONE;

    private WindowManager mWindowManager;
    private View mAllView = null;
    private View mAllApp = null;
    private View mTipsView = null;
    private WindowManager.LayoutParams mParams;
    private CommonTimeInfo mCommonTimeInfo;

    public boolean isShowen;

    public static long unlockCount;
    public static long userTime;
    private boolean isOn;

    private long curTime;

    private Context mContext;

    public TipsPopup(Context context) {
        if (FlavorController.testVersion) {
            VALUE_UNLOCK_LOW = 5;
            VALUE_UNLOCK_MID = 10;
            VALUE_UNLOCK_HIGH = 15;
            UNLOCK_INTERVAL = 5;

            VALUE_TIME_LOW = 30;
            VALUE_TIME_MID = 60;
            VALUE_TIME_HIGH = 90;
            TIME_INTERVAL = 5;

            VALUE_OTHER_LOW = 20;
            VALUE_OTHER_MID = 40;
            VALUE_OTHER_HIGH = 60;
            OTHER_INTERVAL = 10;

            VALUE_APP_LOW = 5;
            VALUE_APP_MID = 10;
            VALUE_APP_HIGH = 15;
        }
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mCommonTimeInfo = new CommonTimeInfo();
        isOn = true;
        curTime = SystemClock.uptimeMillis();
        initBackAnimator();

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
        mParams.height = LayoutParams.WRAP_CONTENT;
    }

    public void userPresent() {
        unlockCount++;
        mCommonTimeInfo.input_time = DateUtil.getNowTime();
        mCommonTimeInfo.phone_time = 0;
        mCommonTimeInfo.lock_type = CommonTimeInfo.UNLOCK;
        LauncherModel.addItemToDatabase(mContext, mCommonTimeInfo);
        showViewTips();
    }

    public void screenOn() {
        isOn = true;
        curTime = SystemClock.uptimeMillis();
        mCommonTimeInfo.input_time = DateUtil.getNowTime();
        mCommonTimeInfo.phone_time = 0;
        mCommonTimeInfo.lock_type = CommonTimeInfo.SCREEN_ON;
        LauncherModel.addItemToDatabase(mContext, mCommonTimeInfo);
        if (unlockCount == 0) {
            showViewTips();
        }
    }

    public void screenOff() {
        if (isOn) {
            long t = (SystemClock.uptimeMillis() - curTime) / 1000;
            isOn = false;
            mCommonTimeInfo.input_time = DateUtil.getNowTime();
            mCommonTimeInfo.phone_time = t;
            mCommonTimeInfo.lock_type = CommonTimeInfo.SCREEN_OFF;
            LauncherModel.addItemToDatabase(mContext, mCommonTimeInfo);
        }
        mHandler.removeMessages(MESSAGE_OPEN_VIEW);
        removeTipsView();
    }

    public void showViewTips() {
        // 判断是否在桌面
//        if (RunningAppHelper.isAtZenLauncherHomeScreen(mContext)) {
        mHandler.sendEmptyMessageDelayed(MESSAGE_OPEN_VIEW, MESSAGE_DELAY_TIPVIEW);
//        }
    }

    public void dataChanged() {
        TextCircleViewInfo tipCircleInfo = TipsSettingDataUtil.geteTipCircleInfoByTime(mContext, 0);
        unlockCount = tipCircleInfo.getUnlock_times();
        userTime = (long) tipCircleInfo.getPhone_time();

        if (isOn) {
            long t = (SystemClock.uptimeMillis() - curTime) / 1000;
            mCommonTimeInfo.input_time = DateUtil.getEndTime();
            mCommonTimeInfo.phone_time = t;
            mCommonTimeInfo.lock_type = CommonTimeInfo.SCREEN_OFF;
            LauncherModel.addItemToDatabase(mContext, mCommonTimeInfo);

            curTime = SystemClock.uptimeMillis();
        }
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
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.tips_set);
                    animation.setAnimationListener(new AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation arg0) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation arg0) {

                        }

                        @Override
                        public void onAnimationEnd(Animation arg0) {
                            removeTipsView();
                            removeTipsApp();

                            isShowen = false;
                        }
                    });
                    mTipsView.startAnimation(animation);
                    break;
            }
        }
    };

    private float mDownX;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float x = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mHandler.removeMessages(MESSAGE_CLOSE_TIPS);
                break;

            case MotionEvent.ACTION_MOVE:
                mTipsView.setTranslationX(event.getX() - mDownX);
                x = mTipsView.getTranslationX();
                x = (x < 0) ? -x : x;
                if (x <= mTipsView.getWidth() / 4) {
                    mTipsView.setAlpha(1f - 4 * x / mTipsView.getWidth());
                }
                break;

            case MotionEvent.ACTION_UP:
                x = mTipsView.getTranslationX();
                x = (x < 0) ? -x : x;
                if (x <= mTipsView.getWidth() / 4) {
                    mBackWidth = mTipsView.getTranslationX();
                    mBackAnimator.start();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_CLOSE_TIPS, MESSAGE_DELAY_TIME);
                } else {
                    removeTipsView();
                    removeTipsApp();
                    isShowen = false;
                }
                break;
        }
        return false;
    }

    private ValueAnimator mBackAnimator;
    private float mBackWidth;

    private void initBackAnimator() {
        Interpolator interpolator = AnimationUtils.loadInterpolator(
            mContext,
            android.R.anim.decelerate_interpolator);
        mBackAnimator = ValueAnimator.ofFloat(1f, 0);
        mBackAnimator.setInterpolator(interpolator);
        mBackAnimator.setDuration(500);
        mBackAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                float x = mBackWidth * value;
                mTipsView.setTranslationX(x);
                x = (x < 0) ? -x : x;
                mTipsView.setAlpha(1f - 4 * x / mTipsView.getWidth());
            }
        });
    }

    private void getUnlockResId() {
        long time = userTime / 60;
        long unlock = unlockCount;

        mDataId = NONE;
        mAngle = 0;
        mStringId = NONE;

        if (unlock > time * 10) {
            if (unlock >= VALUE_UNLOCK_HIGH) {
                if ((unlock - VALUE_UNLOCK_HIGH) % UNLOCK_INTERVAL == 0) {
                    mDataId = R.drawable.tips_red;
                    mAngle = 360f;
                    mStringId = R.string.tips_unlock_high;
                }
            } else if (unlock >= VALUE_UNLOCK_MID) {
                if (mPreId != R.string.tips_unlock_mid) {
                    mDataId = R.drawable.tips_orange;
                    mAngle = (float) unlock * 360f / VALUE_UNLOCK_HIGH;
                    mStringId = R.string.tips_unlock_mid;
                }
            } else if (unlock >= VALUE_UNLOCK_LOW) {
                if (mPreId != R.string.tips_unlock_low) {
                    mDataId = R.drawable.tips_yellow;
                    mAngle = (float) unlock * 360f / VALUE_UNLOCK_HIGH;
                    mStringId = R.string.tips_unlock_low;
                }
            }
            return;
        }

        if (time > unlock * 10) {
            if (time >= VALUE_TIME_HIGH) {
                if ((time - VALUE_TIME_HIGH) % TIME_INTERVAL == 0) {
                    mDataId = R.drawable.tips_red;
                    mAngle = 360f;
                    mStringId = R.string.tips_time_high;
                }
            } else if (time >= VALUE_TIME_MID) {
                if (mPreId != R.string.tips_time_mid) {
                    mDataId = R.drawable.tips_orange;
                    mAngle = (float) time * 360f / VALUE_TIME_HIGH;
                    mStringId = R.string.tips_time_mid;
                }
            } else if (time >= VALUE_TIME_LOW) {
                if (mPreId != R.string.tips_time_low) {
                    mDataId = R.drawable.tips_yellow;
                    mAngle = (float) time * 360f / VALUE_TIME_HIGH;
                    mStringId = R.string.tips_time_low;
                }
            }
            return;
        }

        long total = unlock + time / 5;

        if (total >= VALUE_OTHER_HIGH) {
            if ((total - VALUE_OTHER_HIGH) % OTHER_INTERVAL == 0) {
                mDataId = R.drawable.tips_red;
                mAngle = 360f;
                mStringId = R.string.tips_other_high;
            }
        } else if (total >= VALUE_OTHER_MID) {
            if (mPreId != R.string.tips_other_mid) {
                mDataId = R.drawable.tips_orange;
                mAngle = (float) total * 360f / VALUE_OTHER_HIGH;
                mStringId = R.string.tips_other_mid;
            }
        } else if (total >= VALUE_OTHER_LOW) {
            if (mPreId != R.string.tips_other_low) {
                mDataId = R.drawable.tips_yellow;
                mAngle = (float) total * 360f / VALUE_OTHER_HIGH;
                mStringId = R.string.tips_other_low;
            }
        }
    }

    private void getAppResId(long time) {
        mDataId = NONE;
        mAngle = 0;

        time /= 60;
        if (time >= VALUE_APP_HIGH) {
            mDataId = R.drawable.tips_red;
            mAngle = 360f;
            return;
        }
        if (time >= VALUE_APP_MID) {
            mDataId = R.drawable.tips_orange;
            mAngle = (float) time * 360f / VALUE_APP_HIGH;
            return;
        }
        if (time >= VALUE_APP_LOW) {
            mDataId = R.drawable.tips_yellow;
            mAngle = (float) time * 360f / VALUE_APP_HIGH;
            return;
        }
    }

    public void updateTipsView() {
        if (!SettingPreference.getTips()) {
            return;
        }

        if (LauncherModel.isScreenOff) {
            return;
        }

        getUnlockResId();

        if (mAngle <= 0) {
            return;
        }

        mPreId = mStringId;

        removeTipsApp();

        if (mAllView == null) {
            mAllView = LayoutInflater.from(mContext).inflate(R.layout.tips_view, null);
            mAllView.setOnTouchListener(this);
            mWindowManager.addView(mAllView, mParams);
            mTipsView = mAllView.findViewById(R.id.tips);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.tips_translate);
            mTipsView.startAnimation(animation);
            mHandler.sendEmptyMessageDelayed(MESSAGE_CLOSE_TIPS, MESSAGE_DELAY_TIME);
            TextView textView = (TextView) mAllView.findViewById(R.id.tips_detail_text);
            String text = StringUtil.getString(mContext, R.string.tips_detail);
            textView.setText(text);
            textView = (TextView) mAllView.findViewById(R.id.text_time);
            text = StringUtil.getString(mContext, R.string.tips_time_text);
            textView.setText(text);
            textView = (TextView) mAllView.findViewById(R.id.text_unlock);
            text = StringUtil.getString(mContext, R.string.tips_unlock_text);
            textView.setText(text);
            textView = (TextView) mAllView.findViewById(R.id.tips_title);
            text = StringUtil.getString(mContext, R.string.tips_title);
            textView.setText(text);
        }

        TextView text = (TextView) mAllView.findViewById(R.id.tips_text);
        String tips = StringUtil.getString(mContext, mStringId);
        text.setText(tips);

        text = (TextView) mAllView.findViewById(R.id.text_time_num);
        text.setText(getTime(userTime));
        text = (TextView) mAllView.findViewById(R.id.text_unlock_num);
        String unit = StringUtil.getString(mContext, R.string.tip_view_unlockcount_unit);
        text.setText(unlockCount + unit);

        mAllView.invalidate();
        mWindowManager.updateViewLayout(mAllView, mParams);
        // 应用外弹pop
        MobclickAgent.onEvent(mContext, "SmartReminderPopupWindowOutAPP");
        isShowen = true;
    }

    private void removeTipsView() {
        if (mAllView != null) {
            mWindowManager.removeView(mAllView);
            mAllView = null;
        }
    }

    private String getTime(long time) {
        String s = null;

        if (time < 3600) {
            float f = (float) (Math.round((float) time / 60 * 10)) / 10;
            s = f + StringUtil.getString(mContext, R.string.tips_time_unit_min);
        } else {
            float f = (float) (Math.round((float) time / 60 / 60 * 10)) / 10;
            s = f + StringUtil.getString(mContext, R.string.tips_time_unit_hour);
        }

        return s;
    }

    public void updateTipsApp(String name, long time) {
        if (!SettingPreference.getTips()) {
            return;
        }

        if (LauncherModel.isScreenOff) {
            return;
        }

        AppInfo app = FavoritesData.getAppInfo(name);
        if (app == null) {
            return;
        }

        getAppResId(time);

        if (mAngle <= 0) {
            return;
        }

        removeTipsView();

        if (mAllApp == null) {
            mAllApp = LayoutInflater.from(mContext).inflate(R.layout.tips_app, null);
            mAllApp.setOnTouchListener(this);
            mWindowManager.addView(mAllApp, mParams);
            mTipsView = mAllApp.findViewById(R.id.tips);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.tips_translate);
            mTipsView.startAnimation(animation);
            mHandler.sendEmptyMessageDelayed(MESSAGE_CLOSE_TIPS, MESSAGE_DELAY_TIME);
            TextView textView = (TextView) mAllApp.findViewById(R.id.tips_detail_text);
            String text = StringUtil.getString(mContext, R.string.tips_detail);
            textView.setText(text);
            textView = (TextView) mAllApp.findViewById(R.id.text_time);
            text = StringUtil.getString(mContext, R.string.tips_time_app);
            textView.setText(text);
            textView = (TextView) mAllApp.findViewById(R.id.tips_title);
            text = StringUtil.getString(mContext, R.string.tips_title);
            textView.setText(text);
        }

        String t = getTime(time);
        TextView text = (TextView) mAllApp.findViewById(R.id.tips_text);
        String format = StringUtil.getString(mContext, R.string.tips_app);
        String tips = String.format(format, t, app.title);
        text.setText(tips);

        text = (TextView) mAllApp.findViewById(R.id.text_time_num);
        text.setText(t);

        ImageView image = (ImageView) mAllApp.findViewById(R.id.img_time);
        image.setImageBitmap(app.iconBitmap);

        mAllApp.invalidate();
        mWindowManager.updateViewLayout(mAllApp, mParams);
        // 应用内弹pop
        MobclickAgent.onEvent(mContext, "SmartReminderPopupWindowInAPP");
        isShowen = true;
    }

    private void removeTipsApp() {
        if (mAllApp != null) {
            mWindowManager.removeView(mAllApp);
            mAllApp = null;
        }
    }
}
