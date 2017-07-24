package com.cooeeui.brand.zenlauncher.scenes;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;

public class ZenSettingPhone extends BaseActivity implements OnClickListener,
                                                             OnCheckedChangeListener {

    private FrameLayout mBackArrow;
    private RelativeLayout mRlRecommendSetting;
    private RelativeLayout mRlBlurSwitch;

    private ToggleButton mRecommendSwitch;
    private ToggleButton mBlurSwitch;

    private TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting_phone);
        initView();
        intiEvent();
        initString();
    }

    private void intiEvent() {
        mBackArrow.setOnClickListener(this);
        mRlRecommendSetting.setOnClickListener(this);
        mRlBlurSwitch.setOnClickListener(this);

        mRecommendSwitch.setOnCheckedChangeListener(this);
        mBlurSwitch.setOnCheckedChangeListener(this);
    }

    private void initView() {
        findViewById(R.id.zen_setting_fivestar).setVisibility(View.GONE);
        mBackArrow = (FrameLayout) findViewById(R.id.zen_setting_back);

        mRlRecommendSetting = (RelativeLayout) findViewById(R.id.rl_Recommend_setting);

        mRlBlurSwitch = (RelativeLayout) findViewById(R.id.rl_blur_switch);
        mRlBlurSwitch.setVisibility(View.GONE);//del blur menu

        mRecommendSwitch = (ToggleButton) findViewById(R.id.tb_recommend_switch);
        //mRecommendSwitch.setChecked(SettingPreference.getRecommendValue());
        mRecommendSwitch.setChecked(SettingPreference.getAutoScrollValue());//该选项改成是否能自动滚动.
        mBlurSwitch = (ToggleButton) findViewById(R.id.tb_blur_switch);
        mBlurSwitch.setChecked(SettingPreference.getBlurFlag());

        mTvTitle = (TextView) findViewById(R.id.zs_titlebarTitle);
        mTvTitle.setText(StringUtil.getString(this, R.string.zs_Phone));
        // 隐藏国内广告开关
        if (FlavorController.National) {
            mRlRecommendSetting.setVisibility(View.GONE);
        }
    }

    private void initString() {
        TextView textView = (TextView) findViewById(R.id.recommend_setting_text);
        String text = StringUtil.getString(this, R.string.recommend_setting);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.zs_blurMainTitle);
        text = StringUtil.getString(this, R.string.zs_BlurMainTitle);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.blur_switch_text);
        text = StringUtil.getString(this, R.string.ds_blur_switch);
        textView.setText(text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zen_setting_back:
                finish();
                break;
            case R.id.rl_Recommend_setting:
                mRecommendSwitch.setChecked(!mRecommendSwitch.isChecked());
                break;
            case R.id.rl_blur_switch:
                mBlurSwitch.setChecked(!mBlurSwitch.isChecked());
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.tb_recommend_switch:
                //SettingPreference.setRecommendValue(isChecked);
                //if (!isChecked) {
                //    // 发广播到DefinedScrollView让其立即跳转到最新安装界面
                //    Intent intent = new Intent(DefinedScrollView.NO_RECOMMEND_ACTION);
                //    sendBroadcast(intent);
                //}
                SettingPreference.setAutoScrollValue(isChecked);
                break;
            case R.id.tb_blur_switch:
                SettingPreference.setBlurFlag(isChecked);
                if (Launcher.getInstance() != null) {
                    Launcher.getInstance().needHideAllapp = true;
                    if (isChecked) {
                        Launcher.getInstance().favoriteScene.blurFavoriteScene();
                    }
                }
                break;
        }
    }
}
