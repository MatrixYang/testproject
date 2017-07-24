package com.cooeeui.brand.zenlauncher.settings;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;

public class SpecialThanks extends BaseActivity {

    private FrameLayout mBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting_special_thanks);

        mBackButton = (FrameLayout) findViewById(R.id.thanks_back);
        mBackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SpecialThanks.this.finish();
            }
        });

        initString();
    }

    /**
     * 初始化字符串，从而支持多国语言设置
     */
    private void initString() {
        TextView textView = (TextView) findViewById(R.id.logo_name_text);
        String text = StringUtil.getString(this, R.string.zen_launcher);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thank_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_text);
        textView.setText(text);

        textView = (TextView) findViewById(R.id.about_special_thanks_russian_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_russian);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_spanish_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_spanish);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_french_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_french);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_german_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_german);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_portuguese_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_portuguese);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_Indonesian_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_Indonesian);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_Italian_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_Italian);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_Turkish_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_Turkish);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_Czech_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_Czech);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_Dutch_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_Dutch);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_Greek_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_Greek);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_Romanian_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_Romanian);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_polish_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_polish);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_swedish_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_swedish);
        textView.setText(text);
        textView = (TextView) findViewById(R.id.about_special_thanks_ukrainian_text);
        text = StringUtil.getString(this, R.string.ds_about_special_thanks_ukrainian);
        textView.setText(text);
    }

}
