package com.cooeeui.brand.zenlauncher.scenes;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.umeng.analytics.MobclickAgent;

public class ZenSettingEngine extends BaseActivity implements OnClickListener,
                                                              OnCheckedChangeListener {

    private FrameLayout mBackArrow;
    private RelativeLayout mRlSearchSwitch;
    private ToggleButton mSearchSwitch;
    private TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting_engine);
        findViewById(R.id.zen_setting_fivestar).setVisibility(View.GONE);
        mSearchSwitch = (ToggleButton) findViewById(R.id.tb_search_switch);
        mRlSearchSwitch = (RelativeLayout) findViewById(R.id.rl_search_switch);
        mBackArrow = (FrameLayout) findViewById(R.id.zen_setting_back);
        mTvTitle = (TextView) findViewById(R.id.zs_titlebarTitle);
        mTvTitle.setText(StringUtil.getString(this, R.string.zs_Engine));

        mSearchSwitch.setChecked(SettingPreference.getSearch());
        mSearchSwitch.setOnCheckedChangeListener(this);
        mRlSearchSwitch.setOnClickListener(this);
        mBackArrow.setOnClickListener(this);
        initString();
    }

    private void initString() {
        TextView textView = (TextView) findViewById(R.id.desktop_search_text);
        String text = StringUtil.getString(this, R.string.desktop_search);
        textView.setText(text);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.tb_search_switch:
                SettingPreference.setSearch(isChecked);
                Launcher.getInstance().getSearchBarGroup()
                    .setVisibility(isChecked ? View.VISIBLE : View.GONE);
                LinearLayout clockAndSpeedDial = Launcher.getInstance()
                    .getClockAndSpeedDial();
                LayoutParams lp = (LayoutParams) clockAndSpeedDial
                    .getLayoutParams();
                lp.bottomMargin = (int) (isChecked ? getResources().getDimension(
                    R.dimen.bottom_height) : 0);
                clockAndSpeedDial.setLayoutParams(lp);
                if (!isChecked) {
                    // zen设置中点击隐藏搜索
                    MobclickAgent.onEvent(this, "ZenSettingsCloseDesktopSearch");
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zen_setting_back:
                finish();
                break;
            case R.id.rl_search_switch:
                mSearchSwitch.setChecked(!mSearchSwitch.isChecked());
                break;
        }
    }
}
