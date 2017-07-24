package com.cooeeui.brand.zenlauncher.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;

public class AboutActivity extends BaseActivity implements OnClickListener {

    private FrameLayout mBackButton;
    private RelativeLayout mRlCheckUpdate;
    private RelativeLayout mRlFacebook;
    private RelativeLayout mRlEmail;
    private RelativeLayout mRlHelpUs;
    private RelativeLayout mThanks;
    private TextView mVersionName;
    private VersionUpdateDetector mVersionDetector;


    public static Intent getOpenFacebookIntent(Context context) {
        PackageInfo localPackageInfo;
        Intent localIntent;
        try {
            localPackageInfo = context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            if ((localPackageInfo == null) || (localPackageInfo.versionName.equals("11.0.0.8.23"))
                || (localPackageInfo.versionName.equals("11.0.0.3.23"))
                || (localPackageInfo.versionName.equals("11.0.0.1.23"))) {
                localIntent = new Intent(Intent.ACTION_VIEW,
                                         Uri.parse("https://m.facebook.com/zenlauncher"));
                localIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            } else {
                localIntent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1375708226061901"));
                localIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            }
        } catch (Exception e) {
            localIntent = new Intent(Intent.ACTION_VIEW,
                                     Uri.parse("https://m.facebook.com/zenlauncher"));
            localIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }

        return localIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting_about);
        setupViews();
        mVersionDetector = new VersionUpdateDetector(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initString();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setupViews() {
        mBackButton = (FrameLayout) findViewById(R.id.zen_about_back);
        mRlCheckUpdate = (RelativeLayout) findViewById(R.id.rl_check_update);
        mRlFacebook = (RelativeLayout) findViewById(R.id.rl_facebook);
        mRlEmail = (RelativeLayout) findViewById(R.id.rl_email);
        mRlHelpUs = (RelativeLayout) findViewById(R.id.rl_helpus_translate);
        mThanks = (RelativeLayout) findViewById(R.id.special_thanks);
        mVersionName = (TextView) findViewById(R.id.tv_version_name);

        mBackButton.setOnClickListener(this);
        mRlCheckUpdate.setOnClickListener(this);
        mRlFacebook.setOnClickListener(this);
        mRlEmail.setOnClickListener(this);
        mRlHelpUs.setOnClickListener(this);
        mThanks.setOnClickListener(this);

        if (FlavorController.National) {
            mRlFacebook.setVisibility(View.GONE);
            mRlHelpUs.setVisibility(View.GONE);
            mThanks.setVisibility(View.GONE);
        }

        mVersionName.setText(CommonUtil.getVersionName(this));
    }

    private void initString() {
        TextView textView = (TextView) findViewById(R.id.check_update_text);
        String text = StringUtil.getString(this, R.string.ds_check_update_text);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_email_name_text);
        text = StringUtil.getString(this, R.string.ds_about_email_name_text);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_helpus_tranlate_text);
        text = StringUtil.getString(this, R.string.ds_about_helpus_tranlate_text);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.logo_title_text);
        text = StringUtil.getString(this, R.string.zen_launcher);
        textView.setText(text);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zen_about_back:
                finish();
                break;

            case R.id.rl_check_update:
                mVersionDetector.checkUpdate(true, false);
                break;

            case R.id.rl_facebook:
                startActivitySafely(getOpenFacebookIntent(this));
                break;

            case R.id.rl_email:
                contact();
                break;

            case R.id.rl_helpus_translate:
                String url_help = StringUtil.getString(this, R.string.ds_about_helpus_tranlate_url);
                openWithURL(url_help);
                break;

            case R.id.special_thanks:
                Intent intent = new Intent(this, SpecialThanks.class);
                startActivitySafely(intent);
                break;
        }
    }


    private void openWithURL(String url) {
        final Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivitySafely(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, StringUtil.getString(this, R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }
    }

    private void contact() {
        String[] receiver = new String[]{
            StringUtil.getString(AboutActivity.this,R.string.zs_Email_Feedback_Receiver)
        };
        String subject = StringUtil.getString(AboutActivity.this,R.string.zs_Email_Feedback_Subject);
        String content = "";
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("message/rfc822");
        // 设置邮件发收人
        email.putExtra(Intent.EXTRA_EMAIL, receiver);
        // 设置邮件标题
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        // 设置邮件内容
        email.putExtra(Intent.EXTRA_TEXT, content);
        // 调用系统的邮件系统
        startActivity(Intent.createChooser(email,
                                           StringUtil.getString(AboutActivity.this,
                                                                R.string.zs_Email_Feedback_Choose_Client)));
    }


    public boolean startActivitySafely(Intent intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, StringUtil.getString(this, R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
