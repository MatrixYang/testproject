package com.cooeeui.brand.zenlauncher.favorite;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.PorterDuff.Mode;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.basecore.utilities.TelephonyInfo;
import com.cooeeui.zenlauncher.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SpeedySetting {

    public final static int BRIGHTNESS_LOW = 0;
    public final static int BRIGHTNESS_MIDDLE = 1;
    public final static int BRIGHTNESS_HIGH = 2;
    public final static int BRIGHTNESS_AUTO = 3;

    public static boolean isBrightControlShow = false;
    public static View controlView = null;
    public static View controlViewGroup = null;
    public static View mSelectedView = null;
    public static ImageButton adjustImageButton = null;
    public static FrameLayout mFrameLayoutAdjustBright = null;
    public static int selectedHeight = 0;
    public static int selectedY = 0;
    public static int previousScreenSize = 5;

    private Context mContext;
    private SpeedySettingCallBack mCallback;

    private WifiManager mWifiManager = null;
    private ConnectivityManager mConnectivityManager = null;
    private volatile Camera mCamera = null;
    private AudioManager mAudioManager = null;
    private ContentObserver mContentObserver;
    private ContentObserver mBrightnessObserver;
    private Window window;
    private int birghtMAX = 255;
    private int brightMIN = 20;

    public SpeedySetting(Context context, SpeedySettingCallBack callback) {
        mContext = context;
        mCallback = callback;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        window = ((Activity) mContext).getWindow();
        brightMIN = getMinimumScreenBrightnessSetting(mContext);
        birghtMAX = getMaximumScreenBrightnessSetting(mContext);
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mContext.registerReceiver(mSettingSoundReceiver, filter);
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter1.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter1.addAction("android.intent.action.ANY_DATA_STATE");
        filter1.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter1.setPriority(1000);
        mContext.registerReceiver(mSettingNetReceiver, filter1);
        mContentObserver = new SettingsObserver();
        if (VERSION.SDK_INT >= 17) {
            mContext.getApplicationContext().getContentResolver().registerContentObserver(
                Settings.Global.CONTENT_URI, true, mContentObserver);
        } else {
            mContext.getApplicationContext().getContentResolver().registerContentObserver(
                Settings.Secure.CONTENT_URI, true, mContentObserver);
        }
        mBrightnessObserver = new SettingsBrightnessObserver(new Handler());
        mContext.getApplicationContext().getContentResolver().registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
            mBrightnessObserver);
        mContext.getApplicationContext().getContentResolver().registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
            mBrightnessObserver);
    }

    public void unRegisterReceiver() {
        mContext.unregisterReceiver(mSettingSoundReceiver);
        mContext.unregisterReceiver(mSettingNetReceiver);
        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
        mContext.getContentResolver().unregisterContentObserver(mBrightnessObserver);
    }

    private BroadcastReceiver mSettingSoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                if (mCallback != null) {
                    mCallback.onSoundStateChanged(getSoundMode());
                }
            }
        }
    };

    private BroadcastReceiver mSettingNetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (mCallback != null) {
                    mCallback.onWifiStateChanged(isWifiEnabled());
                }
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                if (mCallback != null) {
                    try {
                        Method getMethod = mConnectivityManager.getClass().getMethod(
                            "getMobileDataEnabled");
                        getMethod.setAccessible(true);
                        boolean isEnabled = (Boolean) getMethod.invoke(mConnectivityManager);
                        mCallback.onMobileNetChanged(isEnabled);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        // 对于没有SIM卡的手机做相应的处理
                        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(mContext);
                        if(telephonyInfo.isDualSIM()){
                            boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
                            boolean isSIM2Ready = telephonyInfo.isSIM2Ready();
                            if (isSIM1Ready||isSIM2Ready){
                                return;
                            }else{
                                mCallback.onMobileNetChanged(true);
                            }
                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    };

    public class SettingsObserver extends ContentObserver {

        public SettingsObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mCallback != null) {
                try {
                    Method getMethod = mConnectivityManager.getClass().getMethod(
                        "getMobileDataEnabled");
                    getMethod.setAccessible(true);
                    boolean isEnabled = (Boolean) getMethod.invoke(mConnectivityManager);
                    mCallback.onMobileNetChanged(isEnabled);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class SettingsBrightnessObserver extends ContentObserver {

        public SettingsBrightnessObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                int brightness = getScreenBrightness();
                if (getScreenMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    mCallback.onBrightnessChanged(BRIGHTNESS_AUTO);
                    return;
                } else if (brightness <= 63) {
                    setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    mCallback.onBrightnessChanged(BRIGHTNESS_LOW);
                } else if (brightness <= 191) {
                    setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    mCallback.onBrightnessChanged(BRIGHTNESS_MIDDLE);
                } else {
                    setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    mCallback.onBrightnessChanged(BRIGHTNESS_HIGH);
                }
                setScreenBrightness(brightness);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isWifiEnabled() {
        if (mWifiManager == null) {
            return false;
        }
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED
            || mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING
            || mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
            return true;
        }

        return false;
    }

    public void clickWifi() {
        if (mWifiManager == null) {
            return;
        }
        if (isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        } else {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public int getSoundMode() {
        if (mAudioManager == null) {
            return AudioManager.RINGER_MODE_NORMAL;
        }
        return mAudioManager.getRingerMode();
    }

    public void clickSound() {
        if (mAudioManager == null) {
            return;
        }

        switch (getSoundMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;

            case AudioManager.RINGER_MODE_SILENT:
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
        }
    }

    public boolean isFlashlightOn() {
        return mCamera != null;
    }

    public void clickFlashlight() {
        if (mCallback == null) {
            return;
        }

        //判断是否有手电筒服务
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            return;
        }

        boolean isOn = isFlashlightOn();
        mCallback.onFlashlightChanged(!isOn);

        if (isOn) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        } else { // 检查相机是否被占用
            try {
                mCamera = Camera.open();
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                mCamera.setPreviewTexture(new SurfaceTexture(0));
                mCamera.startPreview();
            } catch (Exception e) {
                // 表示不可用，或者被占用
                Toast.makeText(mContext, StringUtil.getString(mContext, R.string.camera_occupied),
                               Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private int getScreenMode() {
        int screenMode = 0;
        try {
            screenMode = Settings.System.getInt(mContext.getContentResolver(),
                                                Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenMode;
    }

    private void setScreenMode(int mode) {
        // 还原window的亮度参数
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = -1;
            window.setAttributes(layoutParams);
        }
        try {
            Settings.System.putInt(mContext.getContentResolver(),
                                   Settings.System.SCREEN_BRIGHTNESS_MODE,
                                   mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getScreenBrightness() {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(mContext.getContentResolver(),
                                                      Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenBrightness;
    }

    private void setScreenBrightness(int brightness) {
        LayoutParams layoutParams = window.getAttributes();
        if (brightness < brightMIN) {
            brightness = brightMIN;
        }
        if (brightness > birghtMAX) {
            brightness = birghtMAX;
        }
        layoutParams.screenBrightness = brightness / (float) 255;
        window.setAttributes(layoutParams);
        // 手动的话需要写入系统
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    public int getBrightnessMode() {
        int brightness = getScreenBrightness();
        if (getScreenMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            return BRIGHTNESS_AUTO;
        } else if (brightness <= 63) {
            return BRIGHTNESS_LOW;
        } else if (brightness <= 191) {
            return BRIGHTNESS_MIDDLE;
        } else {
            return BRIGHTNESS_HIGH;
        }
    }

    // click to pop brightness set
    public void ClickSetBright(View adapterView, View selectedView) {
        selectedHeight = selectedView.getHeight();
        isBrightControlShow = true;
        mSelectedView = selectedView;
        ViewParent parent = adapterView.getParent().getParent();
        View group = ((View) parent).findViewById(R.id.frameLayoutBrightGroup);
        controlViewGroup = group;
        group.setVisibility(View.VISIBLE);
        View linearLayoutBrightControl = ((View) parent).findViewById(R.id.LinearLayout_bright);
        controlView = linearLayoutBrightControl;
        adjustImageButton = (ImageButton) group.findViewById(R.id.buttonAdjustBright);
        mFrameLayoutAdjustBright = (FrameLayout) group.findViewById(R.id.fragmelayoutAdjust);
        mFrameLayoutAdjustBright.setVisibility(View.VISIBLE);
        final ImageView centerImageView = (ImageView) group
            .findViewById(R.id.buttonAdjustBrightCenter);
        final VerticalSeekBar mVerticalSeekBar = (VerticalSeekBar) linearLayoutBrightControl
            .findViewById(R.id.myVerticalSeekBar);
        mVerticalSeekBar.setEnabled(true);
        mVerticalSeekBar.setKeyProgressIncrement(10);
        mVerticalSeekBar.setProgress(0);
        mVerticalSeekBar.updateThumb();
        int color = mContext.getResources().getColor(R.color.seekbarcolor);
        Drawable progressDrawable = mVerticalSeekBar.getProgressDrawable();
        progressDrawable.setColorFilter(color, Mode.SRC_IN);
        mVerticalSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    setScreenBrightness(progress);
                    setCenterImageView(centerImageView,
                                       Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL, progress);
                    previousScreenSize = progress;
                }
            }
        });

        adjustImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getScreenMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    setCenterImageView(centerImageView,
                                       Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                                       previousScreenSize);
                    setScreenBrightness(previousScreenSize);
                    mVerticalSeekBar.setProgress(previousScreenSize);
                } else {
                    setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    setCenterImageView(centerImageView,
                                       Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC, 50);
                    mVerticalSeekBar.setProgress(50);
                }
                mVerticalSeekBar.updateThumb();
            }
        });

        group.setX(selectedView.getX());
        View selectedViewP = (View) selectedView.getParent();
        // Y轴平移
        float fromY = adapterView.getY() + selectedViewP.getY();
        selectedY = (int) fromY;
        float toY = fromY - linearLayoutBrightControl.getHeight() + selectedView.getHeight();

        ObjectAnimator linearLayoutControlYAnimator = ObjectAnimator.ofFloat(
            linearLayoutBrightControl, "Y", fromY,
            toY);
        // 小太阳旋转动画
        ObjectAnimator rotationImageButton = ObjectAnimator.ofFloat(adjustImageButton, "rotation",
                                                                    0f, 90f);
        ObjectAnimator fragmeAdjustAnimator = ObjectAnimator.ofFloat(mFrameLayoutAdjustBright, "Y",
                                                                     fromY, toY);
        // 本身的淡入
        ObjectAnimator linearControlAnimator = ObjectAnimator.ofFloat(linearLayoutBrightControl,
                                                                      "alpha",
                                                                      0f, 1.0f);

        // 获取当前的亮度值
        int nowBrightness = getScreenBrightness();
        int screenMode = getScreenMode();
        setCenterImageView(centerImageView, screenMode, nowBrightness);

        // 自动增长
        if (screenMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            nowBrightness = brightMIN + 1;
        }
        // 让进度条显示正常。
        if (nowBrightness <= brightMIN) {
            nowBrightness = 0;
        }
        if (nowBrightness > birghtMAX) {
            nowBrightness = birghtMAX;
        }
        ValueAnimator seekbarIncreaseAnimator = ValueAnimator.ofInt(0, nowBrightness);
        seekbarIncreaseAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                mVerticalSeekBar.setProgress((int) value);
                mVerticalSeekBar.updateThumb();
            }
        });

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.play(linearLayoutControlYAnimator).with(fragmeAdjustAnimator)
            .with(rotationImageButton)
            .with(linearControlAnimator).with(seekbarIncreaseAnimator);
        animatorSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mSelectedView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

        animatorSet.start();
    }

    private void setCenterImageView(ImageView centerImageView, int screenMode, int nowBrightness) {
        if (Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC == screenMode) {
            centerImageView.setBackgroundResource(R.drawable.brightcontrol_auto);
            return;
        }
        if (nowBrightness < 64) {
            centerImageView.setBackgroundResource(R.drawable.brightcontrol_white);
        } else if (nowBrightness < 192) {
            centerImageView.setBackgroundResource(R.drawable.brightcontrol_half);
        } else {
            centerImageView.setBackgroundResource(R.drawable.brightcontrol_whole);
        }
    }

    public static void onPostBrightness() {
        if (controlView == null) {
            return;
        }
        ((VerticalSeekBar) controlView.findViewById(R.id.myVerticalSeekBar)).setEnabled(false);
        ObjectAnimator buttonRotation = ObjectAnimator
            .ofFloat(adjustImageButton, "rotation", 90f, 0f);
        ObjectAnimator controlAlphaAnimator = ObjectAnimator.ofFloat(controlView, "alpha", 1f, 0f);

        final float fromY = controlView.getY();
        final float toY = selectedY;
        ObjectAnimator controlYAnimator = ObjectAnimator.ofFloat(controlView, "Y", fromY, toY);
        ObjectAnimator autoImageButtonAnimator = ObjectAnimator
            .ofFloat(mFrameLayoutAdjustBright, "Y", fromY, toY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.play(controlYAnimator).with(buttonRotation).with(controlAlphaAnimator)
            .with(autoImageButtonAnimator);// 两个动画同时开始
        animatorSet.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFrameLayoutAdjustBright.setVisibility(View.INVISIBLE);
                mSelectedView.setVisibility(View.VISIBLE);
                isBrightControlShow = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        animatorSet.start();
    }

    public void clickBrightness() {
        if (mCallback == null) {
            return;
        }
        switch (getBrightnessMode()) {
            case BRIGHTNESS_AUTO:
                setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                setScreenBrightness(63);
                mCallback.onBrightnessChanged(BRIGHTNESS_LOW);
                break;
            case BRIGHTNESS_LOW:
                setScreenBrightness(191);
                mCallback.onBrightnessChanged(BRIGHTNESS_MIDDLE);
                break;
            case BRIGHTNESS_MIDDLE:
                setScreenBrightness(255);
                mCallback.onBrightnessChanged(BRIGHTNESS_HIGH);
                break;
            case BRIGHTNESS_HIGH:
                setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                mCallback.onBrightnessChanged(BRIGHTNESS_AUTO);
                break;
        }
    }

    public boolean isMobileNetOn() {
        boolean isEnabled = false;
        try {
            // 对于没有SIM卡的手机做相应的处理
            TelephonyManager tm = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null || TelephonyManager.SIM_STATE_UNKNOWN == tm.getSimState()
                || tm.getNetworkOperatorName().equals("") || tm.getNetworkType() == 0) {
                mCallback.onMobileNetChanged(false);
                return isEnabled;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        try {
            Method getMethod = mConnectivityManager.getClass().getMethod(
                "getMobileDataEnabled");
            getMethod.setAccessible(true);
            isEnabled = (Boolean) getMethod.invoke(mConnectivityManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isEnabled;
    }

    public void clickMobileNet() {
        if (mConnectivityManager == null || mCallback == null) {
            return;
        }
        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(mContext);
        boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
        boolean isSIM2Ready = telephonyInfo.isSIM2Ready();
        if (!(isSIM1Ready||isSIM2Ready)){
            return ;
        }
        // 对于5.0以上的点击mobile 直接进入移动数据统计界面
        if (VERSION.SDK_INT >20) {
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.settings",
                                                      "com.android.settings.Settings$DataUsageSummaryActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
                // boolean mobileDataAllowed =
                // Settings.Secure.getInt(mContext.getContentResolver(),
                // "mobile_data", 1) == 1;
                // Log.i("AAAAA", "mobile_data:  " + mobileDataAllowed);
                // Settings.Secure.putInt(mContext.getContentResolver(),
                // "mobile_data",
                // (!mobileDataAllowed) == true ? 1 : 0);
                // mCallback.onMobileNetChanged(!mobileDataAllowed);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        // 对于5.0以下的点击mobile 直接设置移动数据
        try {
            Method getMethod = mConnectivityManager.getClass().getMethod("getMobileDataEnabled");
            getMethod.setAccessible(true);
            boolean isEnabled = (Boolean) getMethod.invoke(mConnectivityManager);
            setMobileDataEnabled(!isEnabled);
            mCallback.onMobileNetChanged(!isEnabled);
        } catch (Exception e) {
            mCallback.onMobileNetChanged(false);
            e.printStackTrace();
        }
    }

    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    private void setMobileDataEnabled(boolean state) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass()
                                                                      .getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod(
                "setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 长按进入WiFi设置界面
     */
    public void longClickWifi() {
        try {
            mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 长按进入音量控制设置界面
     */
    public void longClickSound() {
        try {
            mContext.startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 长按进入亮度控制设置界面
     */
    public void longClickBright() {
        try {
            mContext.startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 长按进入移动流量设置界面
     */
    public void longClickMobile() {
        // 对于5.0以上的点击mobile 直接进入移动数据设置界面
        if (VERSION.SDK_INT >= 21) {
            mContext.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
            return;
        }
        // 对于5.0以下的点击mobile 直接进入移动数据统计界面
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.android.settings",
                                                  "com.android.settings.Settings$DataUsageSummaryActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static interface SpeedySettingCallBack {

        public void onWifiStateChanged(
            boolean isOn);

        public void onSoundStateChanged(
            int mode);

        public void onFlashlightChanged(
            boolean isOn);

        public void onBrightnessChanged(
            int mode);

        public void onMobileNetChanged(
            boolean isOn);
    }

    public int getMaximumScreenBrightnessSetting(Context context) {
        final Resources res = Resources.getSystem();
        final int id = res.getIdentifier("config_screenBrightnessSettingMaximum", "integer",
                                         "android"); // API17+
        if (id != 0) {
            try {
                return res.getInteger(id);
            } catch (Resources.NotFoundException e) {
                // ignore
            }
        }
        return 255;
    }

    public int getMinimumScreenBrightnessSetting(Context context) {
        final Resources res = Resources.getSystem();
        int
            id =
            res.getIdentifier("config_screenBrightnessSettingMinimum", "integer",
                              "android"); // API17+
        if (id == 0) {
            id = res.getIdentifier("config_screenBrightnessDim", "integer", "android"); // lower
        }
        // API
        // levels
        if (id != 0) {
            try {
                return res.getInteger(id);
            } catch (Resources.NotFoundException e) {
                // ignore
            }
        }
        return 0;
    }
}
