package com.cooeeui.brand.zenlauncher.scenes;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.favorite.usagestats.UsageUtil;
import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.brand.zenlauncher.settings.AboutActivity;
import com.cooeeui.brand.zenlauncher.settings.DefaultLauncherGuide;
import com.cooeeui.brand.zenlauncher.settings.RateDialog;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

public class ZenSetting extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

    private FrameLayout mBackArrow;
    private RelativeLayout mRateIcon;
    private ImageView mRateAlert;
    private RelativeLayout mRlAbout;
    private RelativeLayout mRlDefaultLauncherSwitch;
    private DefaultLauncherGuide mDefaultLauncherGuide;
    private LinearLayout mLLDefaultLauncherSwitch;
    private RelativeLayout mRlPhone;
    private RelativeLayout mRlEngine;
    private RelativeLayout mRlFeedBack;
    private RelativeLayout mRlLanguage;
    private RelativeLayout mRlAdvanced;
    private RelativeLayout mRlUsage;
    private RelativeLayout mRlZenLife;
    private ImageView mLanAlert;
    private ImageView mAdvAlert;
    private ImageView mUsageAlert;
    private ImageView mLifeAlert;
    private ToggleButton mUsageSwitch;

    //wang
    private Handler mHandler = new Handler();


    /**
     * 此函数是判断手机上是否有指定的APP
     *
     * @param context     上下文对象
     * @param packageName app的包名
     * @return 如果手机上已经存在指定的app则返回true 否则返回false
     */
    public static boolean isAPKInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 此函数是判断手机的某个service是否在运行
     *
     * @param context     上下文对象
     * @param packageName app的包名
     * @return 此函数是判断手机的某个service是否在运行，如果在运行则返回true 否则返回false
     */
    public static boolean isAPKRunning(Context context, String packageName) {
        boolean isRunning = false;
        ActivityManager activityManager =
            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo>
            serviceList =
            activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(packageName)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting);

        initView();
        initEvent();
        if (SettingPreference.getFirstPop()) {
            SettingPreference.setFirstPop(false);
            mHandler.postDelayed(mRunnable, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initString();
        if (mDefaultLauncherGuide != null) {
            if (mDefaultLauncherGuide.isDefaultLauncher()) {
                mLLDefaultLauncherSwitch.setVisibility(View.GONE);
            } else {
                mLLDefaultLauncherSwitch.setVisibility(View.VISIBLE);
            }

            mDefaultLauncherGuide.removeGuide();
        }
        if (!LauncherPreference.getMenuZenLifeAlert() && mLifeAlert != null) {
            mLifeAlert.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDefaultLauncherGuide != null) {
            mDefaultLauncherGuide.removeGuide();
        }
    }

    public void initView() {
        mBackArrow = (FrameLayout) findViewById(R.id.zen_setting_back);
        mRateIcon = (RelativeLayout) findViewById(R.id.zen_setting_fivestar);
        mRateAlert = (ImageView) findViewById(R.id.zen_setting_fivestar_alert);
        if (!SettingPreference.getRateAlertStatus()) {
            mRateAlert.setVisibility(View.GONE);
        }
        if (FlavorController.National) {
            mRateIcon.setVisibility(View.GONE);
        }

        mRlAbout = (RelativeLayout) findViewById(R.id.rl_about);
        mDefaultLauncherGuide = new DefaultLauncherGuide(getApplicationContext());
        mRlDefaultLauncherSwitch = (RelativeLayout) findViewById(R.id.rl_default_launcher_switch);
        mLLDefaultLauncherSwitch = (LinearLayout) findViewById(R.id.ll_default_launcher_switch);

        mRlPhone = (RelativeLayout) findViewById(R.id.rl_phone);
        mRlEngine = (RelativeLayout) findViewById(R.id.rl_engine);
        mRlFeedBack = (RelativeLayout) findViewById(R.id.rl_Feedback);
        mRlLanguage = (RelativeLayout) findViewById(R.id.rl_language);
        mRlAdvanced = (RelativeLayout) findViewById(R.id.rl_advanced);

        mRlUsage = (RelativeLayout) findViewById(R.id.rl_usage);
        mUsageSwitch = (ToggleButton) findViewById(R.id.tb_usage_switch);
        mUsageAlert = (ImageView)findViewById(R.id.img_zen_usage_alert);

        if (UsageUtil.isNoOption(this)){
            mUsageSwitch.setChecked(UsageUtil.isUsageAllowed(this));

            if (SettingPreference.getFirstUsage()) {
                mUsageAlert.setVisibility(View.VISIBLE);
            } else {
                mUsageAlert.setVisibility(View.INVISIBLE);
            }
        }else{
            mRlUsage.setVisibility(View.GONE);
        }

        mRlZenLife = (RelativeLayout) findViewById(R.id.rl_ZenLife);
        mLanAlert = (ImageView) findViewById(R.id.img_zen_language_alert);
        mAdvAlert = (ImageView) findViewById(R.id.img_zen_advanced_alert);
        if (SettingPreference.getFirstLanguage()) {
            mLanAlert.setVisibility(View.VISIBLE);
        } else {
            mLanAlert.setVisibility(View.INVISIBLE);
        }
        if (SettingPreference.getFirstAdvanced()) {
            mAdvAlert.setVisibility(View.VISIBLE);
        } else {
            mAdvAlert.setVisibility(View.INVISIBLE);
        }
        mLifeAlert = (ImageView) findViewById(R.id.img_zen_lifeAlert);
        if (LauncherPreference.getMenuZenLifeAlert()) {
            mLifeAlert.setVisibility(View.VISIBLE);
        } else {
            mLifeAlert.setVisibility(View.INVISIBLE);
        }

    }

    private void initString() {
        TextView textView = (TextView) findViewById(R.id.zs_titlebarTitle);
        textView.setText(StringUtil.getString(this, R.string.zen_settings));
        textView = (TextView) findViewById(R.id.rl_default_switch_text);
        String text = StringUtil.getString(this, R.string.ds_default_launcher_setting);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.zs_setting_text);
        text = StringUtil.getString(this, R.string.zs_setting);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.zs_phone_text);
        text = StringUtil.getString(this, R.string.zs_Phone);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.zs_engine_text);
        text = StringUtil.getString(this, R.string.zs_Engine);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.zs_language_text);
        text = StringUtil.getString(this, R.string.zs_language);
        textView.setText(text);

        textView = (TextView) findViewById(R.id.zs_advanced_text);
        text = StringUtil.getString(this, R.string.zs_advanced);
        textView.setText(text);

        textView = (TextView) findViewById(R.id.zs_usage_text);
        text = StringUtil.getString(this, R.string.usage_zs);
        textView.setText(text);
        textView = (TextView)findViewById(R.id.zs_usage_sub_title);
        text = StringUtil.getString(this, R.string.usage_zs_summary);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.zs_more_text);
        text = StringUtil.getString(this, R.string.zs_more);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.zs_feedback_text);
        text = StringUtil.getString(this, R.string.zs_Feedback);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_zen_text);
        text = StringUtil.getString(this, R.string.about_zen);
        textView.setText(text);
    }

    public void initEvent() {
        mRateIcon.setOnClickListener(this);
        mRlAbout.setOnClickListener(this);
        mBackArrow.setOnClickListener(this);
        mRlDefaultLauncherSwitch.setOnClickListener(this);
        mRlPhone.setOnClickListener(this);
        mRlEngine.setOnClickListener(this);
        mRlFeedBack.setOnClickListener(this);
        mRlLanguage.setOnClickListener(this);
        mRlAdvanced.setOnClickListener(this);
        mRlUsage.setOnClickListener(this);
        mRlZenLife.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zen_setting_fivestar:
                RateDialog rate = new RateDialog(this);
                rate.showAlertDialog();
                if (SettingPreference.getRateAlertStatus()) {
                    mRateAlert.setVisibility(View.GONE);
                    SettingPreference.setRateAlertStatus(false);
                }

                // 五星好评用户主动点击

                break;
            case R.id.zen_setting_back:
                finish();
                break;
            case R.id.rl_about:
                Intent intent = new Intent(ZenSetting.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_default_launcher_switch:
                if (mDefaultLauncherGuide != null) {
                    mDefaultLauncherGuide.showGuide();
                    // zen设置中点击设置默认桌面次数
                    MobclickAgent.onEvent(ZenSetting.this, "ZenSettingClickDefaultLauncher");
                }
                break;
            case R.id.rl_phone:
                Intent intent2Phone = new Intent(ZenSetting.this, ZenSettingPhone.class);
                startActivity(intent2Phone);
                break;

            case R.id.rl_engine:
                Intent intent2Engine = new Intent(ZenSetting.this, ZenSettingEngine.class);
                startActivity(intent2Engine);
                break;

            case R.id.rl_Feedback:
                // Intent intent2FeedBack = new Intent(ZenSetting.this,
                // ZenSettingFeedBack.class);
                // startActivity(intent2FeedBack);
                Resources resources = this.getResources();
                String[] receiver = new String[]{
                    StringUtil.getString(ZenSetting.this,R.string.zs_Email_Feedback_Receiver)
                };
                String subject = StringUtil.getString(ZenSetting.this,R.string.zs_Email_Feedback_Subject);
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("message/rfc822");
                // 设置邮件发收人
                email.putExtra(Intent.EXTRA_EMAIL, receiver);
                // 设置邮件标题
                email.putExtra(Intent.EXTRA_SUBJECT, subject);
                // 设置邮件内容
                email.putExtra(Intent.EXTRA_TEXT, "");
                // 调用系统的邮件系统
                startActivity(
                    Intent.createChooser(email, StringUtil.getString(ZenSetting.this,
                                                                     R.string.zs_Email_Feedback_Choose_Client)));
                break;

            case R.id.rl_language:
                if (SettingPreference.getFirstLanguage()) {
                    SettingPreference.setFirstLanguage(false);
                    mLanAlert.setVisibility(View.INVISIBLE);
                }
                Intent intent2Language = new Intent(ZenSetting.this, ZenSettingLanguage.class);
                startActivity(intent2Language);
                break;
            case R.id.rl_advanced:
                if (SettingPreference.getFirstAdvanced()) {
                    SettingPreference.setFirstAdvanced(false);
                    mAdvAlert.setVisibility(View.INVISIBLE);
                }
                Intent intentadvanced = new Intent(ZenSetting.this, ZenAdvancedSetting.class);
                startActivity(intentadvanced);
                break;

            case R.id.rl_usage:
                if (SettingPreference.getFirstUsage()) {
                    SettingPreference.setFirstUsage(false);
                    mUsageAlert.setVisibility(View.INVISIBLE);
                }
                UsageUtil.startUsageSettingActivity(this,
                                                    Launcher.REQUEST_USAGE_SETTING_NANO_SETTING);
                Intent intentShowTip = new Intent(LauncherConstants.ACTION_USAGE_SETTING_TIP_SHOW);
                this.sendBroadcast(intentShowTip);
                break;

            case R.id.rl_ZenLife:
                LauncherPreference.setMenuZenLifeAlert(false);
                Intent intent2ZenLife = new Intent(ZenSetting.this, ZenSettingLife.class);
                intent2ZenLife.putExtra("share_title",StringUtil.getString(ZenSetting.this,R.string.zs_ZenLifeShareMessageTitle));
                startActivity(intent2ZenLife);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Launcher.REQUEST_USAGE_SETTING_NANO_SETTING) {
            mUsageSwitch.setChecked(UsageUtil.isUsageAllowed(this));
            Intent intent1 = new Intent(LauncherConstants.ACTION_USAGE_SETTING_TIP_REMOVE);
            this.sendBroadcast(intent1);
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    /**
     * 显示popupWindow
     */
    private void showPopwindow() {
        // 利用layoutInflater获得View
        LayoutInflater
            inflater =
            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.alert_setting_popwindow, null);

        // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

        final PopupWindow window = new PopupWindow(view,
                                                   WindowManager.LayoutParams.MATCH_PARENT,
                                                   WindowManager.LayoutParams.WRAP_CONTENT);
        window.setFocusable(true);

        // 设置popWindow的显示和消失动画
        window.setAnimationStyle(R.style.slide_up_in_down_out);
        // 在底部显示
        window.showAtLocation(ZenSetting.this.findViewById(R.id.ll_setting),
                              Gravity.BOTTOM, 0, 0);

        // 这里检验popWindow里的button是否可以点击
        TextView tv_pop_later = (TextView) view.findViewById(R.id.tv_pop_later);
        String text = StringUtil.getString(this, R.string.pop_later);
        tv_pop_later.setText(text);
        TextView tv_pop_try = (TextView) view.findViewById(R.id.tv_pop_try);
        text = StringUtil.getString(this, R.string.pop_try);
        tv_pop_try.setText(text);
        TextView tv_pop_title = (TextView) view.findViewById(R.id.tv_pop_title);
        text = StringUtil.getString(this, R.string.new_feature);
        tv_pop_title.setText(text);
        TextView tv_pop_content = (TextView) view.findViewById(R.id.tv_pop_content);
        text = StringUtil.getString(this, R.string.pop_content);
        tv_pop_content.setText(text);


        tv_pop_later.setOnClickListener(new OnClickListener() {
//            textView = (TextView) findViewById(R.id.zs_more_text);
//            text = StringUtil.getString(this, R.string.zs_more);
//            textView.setText(text);

            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });
        tv_pop_try.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SettingPreference.getFirstAdvanced()) {
                    SettingPreference.setFirstAdvanced(false);
                    mAdvAlert.setVisibility(View.INVISIBLE);
                }
                Intent intentadvanced = new Intent(ZenSetting.this, ZenAdvancedSetting.class);
                startActivity(intentadvanced);
                window.dismiss();
            }
        });

        //popWindow消失监听方法
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                System.out.println("popWindow消失");
            }
        });

    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            // 弹出PopupWindow的具体代码
            showPopwindow();
        }
    };
}
