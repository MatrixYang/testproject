package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.NetworkAvailableUtils;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.brand.zenlauncher.preferences.SharedPreferencesUtil;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;

public class ZenSettingFeedBack extends BaseActivity implements OnClickListener {

    private EditText mEditTextFeedBackContent;
    private EditText mEditTextFeedBackContact;
    private TextView mTextViewSend;
    private RelativeLayout mFiveStar;
    private ImageView mRateAlert;
    private FrameLayout mBackArrow;
    private TextView mTvTitle;
    private String content = null;
    private String contact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting_feedback);
        initView();
        initEvent();

    }

    private void initEvent() {
        mFiveStar.setOnClickListener(this);
        mBackArrow.setOnClickListener(this);
    }

    private void initView() {
        mEditTextFeedBackContent = (EditText) findViewById(
            R.id.zenSetting_edittext_FeedBackContent);
        mEditTextFeedBackContent.setHint(
            StringUtil.getString(this, R.string.zs_FeedbackContentHint));
        mEditTextFeedBackContact = (EditText) findViewById(
            R.id.zenSetting_edittext_FeedBackContact);
        mEditTextFeedBackContact.setHint(
            StringUtil.getString(this, R.string.zs_FeedbackContackHint));
        mTextViewSend = (TextView) findViewById(R.id.zen_setting_fivestarImg);
        mFiveStar = (RelativeLayout) findViewById(R.id.zen_setting_fivestar);
        mRateAlert = (ImageView) findViewById(R.id.zen_setting_fivestar_alert);
        mRateAlert.setVisibility(View.GONE);
        mBackArrow = (FrameLayout) findViewById(R.id.zen_setting_back);
        mTextViewSend.setBackground(null);
        String send = StringUtil.getString(this, R.string.zs_FeedbackCSend);
        mTextViewSend.setText(send);
        mTvTitle = (TextView) findViewById(R.id.zs_titlebarTitle);
        mTvTitle.setText(StringUtil.getString(this, R.string.zs_Feedback));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSharedPerence();
        mEditTextFeedBackContent.setText(content);
        mEditTextFeedBackContact.setText(contact);
        setEditTextSelection();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zen_setting_back:
                hideKeyBoard();
                saveToSharedParerence();
                finish();
                break;
            case R.id.zen_setting_fivestar:
                if (!NetworkAvailableUtils.isNetworkAvailable(this)) {
                    String tip = StringUtil.getString(this, R.string.networkerror);
                    Toast.makeText(this, tip, Toast.LENGTH_LONG).show();
                    return;
                }
                getEditString();
                if (content != null && content.trim().length() != 0) {
                    String[] receiver = new String[]{
                        "nanolauncher@gmail.com"
                    };
                    String subject = "Feedback on Nano Launcher";
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.setType("message/rfc822");
                    // 设置邮件发收人
                    email.putExtra(Intent.EXTRA_EMAIL, receiver);
                    // 设置邮件标题
                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
                    // 设置邮件内容
                    email.putExtra(Intent.EXTRA_TEXT, content + "\n" + contact);
                    // 调用系统的邮件系统
                    startActivity(Intent.createChooser(email, StringUtil.getString(this,
                                                                                   R.string.zs_Email_Feedback_Choose_Client)));
                    mEditTextFeedBackContent.setText(null);
                    mEditTextFeedBackContact.setText(null);
                    saveToSharedParerence();
                    this.finish();
                } else {
                    Toast.makeText(this, StringUtil.getString(this,
                                                              R.string.zs_Email_Feedback_input_isempty),
                                   Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        saveToSharedParerence();
        super.onPause();
    }

    @Override
    protected void onStop() {
        saveToSharedParerence();
        super.onStop();
    }

    private void saveToSharedParerence() {
        getEditString();
        SharedPreferencesUtil.get().putString(LauncherConstants.SP_KEY_FEEDBACK_CONTENT,
                                              content);
        SharedPreferencesUtil.get().putString(LauncherConstants.SP_KEY_FEEDBACK_CONTACT,
                                              contact);
        SharedPreferencesUtil.get().flush();
    }

    private void getEditString() {
        content = mEditTextFeedBackContent.getText().toString();
        contact = mEditTextFeedBackContact.getText().toString();
    }

    private void getSharedPerence() {
        content = SharedPreferencesUtil.get().getString(LauncherConstants.SP_KEY_FEEDBACK_CONTENT);
        contact = SharedPreferencesUtil.get().getString(LauncherConstants.SP_KEY_FEEDBACK_CONTACT);
    }

    // 收回键盘，不然会有阴影
    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
            Context.INPUT_METHOD_SERVICE);
        if (mEditTextFeedBackContact.isFocused()) {
            imm.hideSoftInputFromWindow(mEditTextFeedBackContact.getWindowToken(), 0);
        }
        if (mEditTextFeedBackContent.isFocused()) {
            imm.hideSoftInputFromWindow(mEditTextFeedBackContact.getWindowToken(), 0);
        }
    }

    // 设置eidttext的光标位置
    private void setEditTextSelection() {
        mEditTextFeedBackContact.setSelection(TextUtils.isEmpty(contact) ? 0 : contact.length());
        mEditTextFeedBackContent.setSelection(TextUtils.isEmpty(content) ? 0 : content.length());
    }

}
