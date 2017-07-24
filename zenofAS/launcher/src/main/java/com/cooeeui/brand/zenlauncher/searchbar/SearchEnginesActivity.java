package com.cooeeui.brand.zenlauncher.searchbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;

/**
 * Created by cuiqian on 2015/12/22.
 */
public class SearchEnginesActivity extends BaseActivity {

    public static final int SEARCH_NANO = 0;
    public static final int SEARCH_GOOGLE = 1;
    public static final int SEARCH_YAHOO = 2;
    public static final int SEARCH_BING = 3;
    public static final int SEARCH_BAIDU = 4;
    public static final int SEARCH_DUCKDUCKGO = 5;
    public static final int SEARCH_COUNT = 6;

    private RadioButton[] mRadioButton = new RadioButton[SEARCH_COUNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_engines);

        ((TextView) findViewById(R.id.zs_titlebarTitle)).setText(
            StringUtil.getString(this, R.string.search_set));
        ((RelativeLayout) findViewById(R.id.zen_setting_fivestar)).setVisibility(View.GONE);

        FrameLayout backArrow = (FrameLayout) findViewById(R.id.zen_setting_back);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchEnginesActivity.this.finish();
            }
        });

        RadioGroup group = (RadioGroup) findViewById(R.id.search_radioGroup);
        mRadioButton[SEARCH_NANO] =
            (RadioButton) findViewById(R.id.search_radioButton_nano);
        mRadioButton[SEARCH_NANO].setText(StringUtil.getString(this,R.string.search_engine_nano));
        mRadioButton[SEARCH_GOOGLE] =
            (RadioButton) findViewById(R.id.search_radioButton_google);
        mRadioButton[SEARCH_GOOGLE].setText(StringUtil.getString(this,R.string.search_engine_google));
        mRadioButton[SEARCH_YAHOO] =
            (RadioButton) findViewById(R.id.search_radioButton_yahoo);
        mRadioButton[SEARCH_YAHOO].setText(StringUtil.getString(this,R.string.search_engine_yahoo));
        mRadioButton[SEARCH_BING] =
            (RadioButton) findViewById(R.id.search_radioButton_bing);
        mRadioButton[SEARCH_BING].setText(StringUtil.getString(this,R.string.search_engine_bing));
        mRadioButton[SEARCH_BAIDU] =
            (RadioButton) findViewById(R.id.search_radioButton_baidu);
        mRadioButton[SEARCH_BAIDU].setText(StringUtil.getString(this,R.string.search_engine_baidu));
        mRadioButton[SEARCH_DUCKDUCKGO] =
            (RadioButton) findViewById(R.id.search_radioButton_duckduckgo);
        mRadioButton[SEARCH_DUCKDUCKGO].setText(StringUtil.getString(this,R.string.search_engine_duckduckgo));

        mRadioButton[LauncherPreference.getSearchEnginesType()].setChecked(true);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int orderId = SEARCH_NANO;
                if (checkedId == mRadioButton[SEARCH_NANO].getId()) {
                    orderId = SEARCH_NANO;
                } else if (checkedId == mRadioButton[SEARCH_GOOGLE].getId()) {
                    orderId = SEARCH_GOOGLE;
                } else if (checkedId == mRadioButton[SEARCH_YAHOO].getId()) {
                    orderId = SEARCH_YAHOO;
                } else if (checkedId == mRadioButton[SEARCH_BING].getId()) {
                    orderId = SEARCH_BING;
                } else if (checkedId == mRadioButton[SEARCH_BAIDU].getId()) {
                    orderId = SEARCH_BAIDU;
                } else if (checkedId == mRadioButton[SEARCH_DUCKDUCKGO].getId()) {
                    orderId = SEARCH_DUCKDUCKGO;
                }

                LauncherPreference.setSearchEnginesType(orderId);
                Intent intent = new Intent();

                intent.putExtra("orderId", orderId);

                setResult(RESULT_OK, intent);
                SearchEnginesActivity.this.finish();
            }
        });
    }
}
