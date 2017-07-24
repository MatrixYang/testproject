package com.cooeeui.brand.zenlauncher.favorite;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.favorite.SpeedySetting.SpeedySettingCallBack;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.ui.uieffect.UIEffectTools;
import com.umeng.analytics.MobclickAgent;

public class SpeedyContainer extends LinearLayout implements OnClickListener,
                                                             OnLongClickListener {

    private SpeedySetting mSpeedySetting = null;
    private FrameLayout mBrightness;
    private ImageButton mBrightIb;
    private ImageView mBrightnessCenter;
    private ImageButton mSoundIb;
    private ImageButton mWifiIb;
    private ImageButton mMobileIb;
    private ImageButton mFlashIb;

    public SpeedyContainer(Context context) {
        super(context);
    }

    public SpeedyContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeedyContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void configureSpeedy(Context context) {
        mSpeedySetting = new SpeedySetting(context, mCallBack);

        mBrightness = (FrameLayout) findViewById(R.id.fl_brightness);
        mBrightIb = (ImageButton) findViewById(R.id.ib_brightness_bg);
        mBrightIb.setOnClickListener(this);
        mBrightIb.setOnLongClickListener(this);
        mBrightnessCenter = (ImageView) findViewById(R.id.iv_brightness_center);
        setBrightImage(mSpeedySetting.getBrightnessMode());

        mSoundIb = (ImageButton) findViewById(R.id.ib_sound);
        mSoundIb.setOnClickListener(this);
        mSoundIb.setOnLongClickListener(this);
        setSoundImage(mSpeedySetting.getSoundMode());

        mWifiIb = (ImageButton) findViewById(R.id.ib_wifi);
        mWifiIb.setOnClickListener(this);
        mWifiIb.setOnLongClickListener(this);
        if (mSpeedySetting.isWifiEnabled()) {
            mWifiIb.setImageResource(R.drawable.quick_wifi_on);
        } else {
            mWifiIb.setImageResource(R.drawable.quick_wifi_off);
        }

        mMobileIb = (ImageButton) findViewById(R.id.ib_mobile);
        mMobileIb.setOnClickListener(this);
        mMobileIb.setOnLongClickListener(this);
        if (mSpeedySetting.isMobileNetOn()) {
            mMobileIb.setImageResource(R.drawable.quick_mobile_on);
        } else {
            mMobileIb.setImageResource(R.drawable.quick_mobile_off);
        }

        mFlashIb = (ImageButton) findViewById(R.id.ib_flash);
        mFlashIb.setOnClickListener(this);
        mFlashIb.setOnLongClickListener(this);
        if (mSpeedySetting.isFlashlightOn()) {
            mFlashIb.setImageResource(R.drawable.quick_flashlight_on);
        } else {
            mFlashIb.setImageResource(R.drawable.quick_flashlight_off);
        }
    }

    public void registerReceiver() {
        mSpeedySetting.registerReceiver();
    }

    public void unRegisterReceiver() {
        mSpeedySetting.unRegisterReceiver();
    }


    SpeedySettingCallBack mCallBack = new SpeedySettingCallBack() {

        @Override
        public void onWifiStateChanged(boolean isOn) {
            if (isOn) {
                mWifiIb.setImageResource(R.drawable.quick_wifi_on);
            } else {
                mWifiIb.setImageResource(R.drawable.quick_wifi_off);
            }
        }

        @Override
        public void onSoundStateChanged(int mode) {
            setSoundImage(mode);
        }

        @Override
        public void onFlashlightChanged(boolean isOn) {
            if (isOn) {
                mFlashIb.setImageResource(R.drawable.quick_flashlight_on);
            } else {
                mFlashIb.setImageResource(R.drawable.quick_flashlight_off);
            }
        }

        @Override
        public void onBrightnessChanged(int mode) {
            setBrightImage(mode);
        }

        @Override
        public void onMobileNetChanged(boolean isOn) {
            if (isOn) {
                mMobileIb.setImageResource(R.drawable.quick_mobile_on);
            } else {
                mMobileIb.setImageResource(R.drawable.quick_mobile_off);
            }
        }
    };

    private void setSoundImage(int mode) {
        switch (mode) {
            case AudioManager.RINGER_MODE_NORMAL:
                mSoundIb.setImageResource(R.drawable.quick_sound_normal);
                break;

            case AudioManager.RINGER_MODE_VIBRATE:
                mSoundIb.setImageResource(R.drawable.quick_sound_vibrate);
                break;

            case AudioManager.RINGER_MODE_SILENT:
                mSoundIb.setImageResource(R.drawable.quick_sound_silent);
                break;
        }
    }

    private void setBrightImage(int mode) {
        switch (mode) {
            case SpeedySetting.BRIGHTNESS_AUTO:
                mBrightnessCenter.setImageResource(R.drawable.brightcontrol_auto);
                break;

            case SpeedySetting.BRIGHTNESS_LOW:
                mBrightnessCenter.setImageResource(R.drawable.brightcontrol_white);
                break;

            case SpeedySetting.BRIGHTNESS_MIDDLE:
                mBrightnessCenter.setImageResource(R.drawable.brightcontrol_half);
                break;

            case SpeedySetting.BRIGHTNESS_HIGH:
                mBrightnessCenter.setImageResource(R.drawable.brightcontrol_whole);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.fl_brightness) {
            UIEffectTools.onClickEffect(v);
        }

        if (mSpeedySetting == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.ib_brightness_bg:
                mSpeedySetting.ClickSetBright(this, mBrightness);
                // 快捷开关中点击亮度次数
                MobclickAgent.onEvent(Launcher.getInstance(), "QuickSwitchClickBrightnessButton");
                break;
            case R.id.ib_sound:
                mSpeedySetting.clickSound();
                // 快捷开关中点击声音次数
                MobclickAgent.onEvent(Launcher.getInstance(), "QuickSwitchClickSoundButton");
                break;
            case R.id.ib_wifi:
                mSpeedySetting.clickWifi();
                // 快捷开关中点击wifi次数
                MobclickAgent.onEvent(Launcher.getInstance(), "QuickSwitchClickWifiButton");
                break;
            case R.id.ib_mobile:
                mSpeedySetting.clickMobileNet();
                // 快捷开关中点击流量次数
                MobclickAgent.onEvent(Launcher.getInstance(), "QuickSwitchClickStreamButton");
                break;
            case R.id.ib_flash:
                mSpeedySetting.clickFlashlight();
                // 快捷开关点击手电筒次数
                MobclickAgent.onEvent(Launcher.getInstance(), "QuickSwitchClickFlashlightButton");
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        UIEffectTools.onClickEffect(v);
        if (mSpeedySetting == null) {
            return true;
        }
        switch (v.getId()) {
            case R.id.ib_brightness_bg:
                mSpeedySetting.longClickBright();
                break;
            case R.id.ib_sound:
                mSpeedySetting.longClickSound();
                break;
            case R.id.ib_wifi:
                mSpeedySetting.longClickWifi();
                break;
            case R.id.ib_mobile:
                mSpeedySetting.longClickMobile();
                break;
            case R.id.ib_flash:

                break;
        }
        return true;
    }

}
