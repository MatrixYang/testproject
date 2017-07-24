package com.cooeeui.brand.zenlauncher.scenes;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;

/**
 * Created by Administrator on 2016/3/31.
 */
public class ZenAdvancedSetting extends BaseActivity implements View.OnClickListener {

    private FrameLayout mBackArrow;
    private ToggleButton mNanoscreen;
    private RelativeLayout rl_nano_screen;
    private ComponentName mDeviceAdminSample;
    private DevicePolicyManager dpm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting_advanced);
        findViewById(R.id.zen_setting_fivestar).setVisibility(View.GONE);
        initView();
    }

    private void initView() {
        rl_nano_screen = (RelativeLayout) findViewById(R.id.rl_nano_screen);
        mNanoscreen = (ToggleButton) findViewById(R.id.tb_nano_screen);
        mBackArrow = (FrameLayout) findViewById(R.id.zen_setting_back);

        rl_nano_screen.setOnClickListener(this);
        mBackArrow.setOnClickListener(this);

        mDeviceAdminSample = new ComponentName(this, MyDeviceManager.class);
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        Boolean isActive = dpm.isAdminActive(mDeviceAdminSample);
        initString();
        if (!isActive) {
            SettingPreference.setAdvanced(false);
        }
        mNanoscreen.setChecked(SettingPreference.getAdvanced());
    }

    private void initString() {

        TextView textView = (TextView) findViewById(R.id.tv_advanced_text);
        String text = StringUtil.getString(this, R.string.tv_advanced_text);
        textView.setText(text);

        textView = (TextView) findViewById(R.id.tv_advanced_content);
        text = StringUtil.getString(this, R.string.tv_advanced_content);
        textView.setText(text);

        textView = (TextView) findViewById(R.id.zs_titlebarTitle);
        text = StringUtil.getString(this, R.string.tv_advanced_title);
        textView.setText(text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zen_setting_back:
                finish();
                break;
            case R.id.rl_nano_screen:
                //判断管理员是否激活
                boolean isActive = dpm.isAdminActive(mDeviceAdminSample);
                if (isActive) {
                    if (SettingPreference.getAdvanced()) {
                        mNanoscreen.setChecked(false);
                        SettingPreference.setAdvanced(false);
                        Toast.makeText(this, StringUtil.getString(this, R.string.lock_disable),
                                       Toast.LENGTH_SHORT).show();
                    } else {
                        mNanoscreen.setChecked(true);
                        SettingPreference.setAdvanced(true);
                        Toast.makeText(this, StringUtil.getString(this, R.string.lock_enable),
                                       Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(
                        DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                    mDeviceAdminSample);
                    try {
                        startActivityForResult(intent, 0);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this,
                                       StringUtil.getString(this, R.string.activity_not_found),
                                       Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            SettingPreference.setAdvanced(true);
            mNanoscreen.setChecked(true);
            Toast.makeText(this, StringUtil.getString(this, R.string.lock_success),
                           Toast.LENGTH_SHORT).show();

        } else {
            SettingPreference.setAdvanced(false);
            mNanoscreen.setChecked(false);
            Toast.makeText(this, StringUtil.getString(this, R.string.lock_failed),
                           Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}


