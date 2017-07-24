package com.cooeeui.zenlauncher.common;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();


        // 友盟
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();


        // 友盟
        MobclickAgent.onPause(this);
    }
}
