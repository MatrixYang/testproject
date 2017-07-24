package com.cooeeui.brand.zenlauncher.searchbar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.basecore.utilities.CommonUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.preferences.SettingPreference;
import com.cooeeui.brand.zenlauncher.utils.LauncherConstants;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.AlertDialogUtil;
import com.umeng.analytics.MobclickAgent;

public class SearchBarGroup extends RelativeLayout {

    public static final String SCAN_PACKAGENAME = "com.cooeeui.nanoqrcodescan";
    private static final int SEARCH_BAIDU = 0;
    private static final int SEARCH_ZEN = 1;
    private Launcher mLauncher;
    private ImageView searchBg = null;
    private ImageView searchButton = null;
    private ImageView searchScan = null;
    private TextView search_hotWords = null;
    // apn(ask) 搜索引擎
    private String SEARCH_URL = "http://www.searchthis.com/web?mgct=ds&o=B10018&buid=G01&q= ";
    //scan搜索数组
    private String[] mSearchUrl =
        {SEARCH_URL, "http://www.google.com/search?q=", "http://search.yahoo.com/search?p=",
         "http://cn.bing.com/search?q=", "http://m.baidu.com/s?word=",
         "http://www.duckduckgo.com/?q="};

    private SearchOnClickListener searchOnClickListener = null;

    public SearchBarGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setup(Launcher launcher) {
        initHotWords();

        if (FlavorController.National) {
            initDefaultSearch(launcher, SEARCH_BAIDU);
            LinearLayout clockAndSpeedDial = Launcher.getInstance().getClockAndSpeedDial();
            LayoutParams lp = (LayoutParams) clockAndSpeedDial
                .getLayoutParams();
            lp.bottomMargin = (int) (getContext().getResources().getDimension(
                R.dimen.bottom_height));
            clockAndSpeedDial.setLayoutParams(lp);
        } else {
            initDefaultSearch(launcher, SEARCH_ZEN);

            boolean isOn = SettingPreference.getSearch();
            setVisibility(isOn ? View.VISIBLE : View.GONE);
            LinearLayout clockAndSpeedDial = Launcher.getInstance().getClockAndSpeedDial();
            LayoutParams lp = (LayoutParams) clockAndSpeedDial
                .getLayoutParams();
            lp.bottomMargin = (int) (isOn ? getContext().getResources().getDimension(
                R.dimen.bottom_height) : 0);
            clockAndSpeedDial.setLayoutParams(lp);
        }
        checkQRCodeInstallStatus();
    }

    private void initDefaultSearch(Launcher launcher, int searchEngine) {

        //   search_engine.setImageResource(R.drawable.search_zen);
        mLauncher = launcher;
        searchBg = (ImageView) this.findViewById(R.id.search_bg);
        searchScan = (ImageView) this.findViewById(R.id.search_scan); //二维码
        searchButton = (ImageView) this.findViewById(R.id.search_button); //搜索图片
        searchButton.setImageResource(R.drawable.search_button);
        searchOnClickListener = new SearchOnClickListener(searchEngine);
        searchBg.setOnClickListener(searchOnClickListener);
        searchButton.setOnClickListener(searchOnClickListener);
        searchScan.setOnClickListener(searchOnClickListener);
    }

    private void initHotWords() {
        search_hotWords = (TextView) findViewById(R.id.tv_search_engine);
    }

    private class SearchOnClickListener implements OnClickListener {

        String eventName = "ClickIntoYahooSerach"; //雅虎

        public SearchOnClickListener(int searchEngine) {
            switch (searchEngine) {
                case SEARCH_BAIDU: // 百度
                    eventName = "ClickIntoBaiduSerach";
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.search_scan) {
                //判断二维码是否安装
                if (CommonUtil.isAppInstalled(getContext(), SCAN_PACKAGENAME)) {
                    //友盟统计二维码点击次数
                    MobclickAgent.onEvent(mLauncher, "QRCodeClick");
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    ComponentName cn = new ComponentName(SCAN_PACKAGENAME,
                                                         "com.cooeeui.activity.CaptureActivity");
                    intent.setComponent(cn);
                    getContext().startActivity(intent);
                } else {
                    AlertDialogUtil alertDialogUtil = new AlertDialogUtil(mLauncher);
                    alertDialogUtil.showAlertDialog(true, false,
                                                    AlertDialogUtil.AlertDialogType.TYPE_FAVORITE_SCAN,
                                                    R.layout.alert_scan);
                }

            } else if (v.getId() == R.id.search_button) {
                String text = search_hotWords.getText().toString().trim();

                if (TextUtils.isEmpty(text)) {
                    text = search_hotWords.getHint().toString().trim();
                }
                if (TextUtils.isEmpty(text) || text
                    .equals(StringUtil.getString(getContext(), R.string.search_text_hint))) {
                    Toast.makeText(getContext(),
                                   StringUtil.getString(getContext(),
                                                        R.string.search_input_tips),
                                   Toast.LENGTH_SHORT).show();
                } else {
                    //友盟统计搜索框内搜索按钮点击次数
                    MobclickAgent.onEvent(mLauncher, "SearchClickinSearchBar");
                    doSearch(text);
                }
            } else {
                startSearchClick();
                // nano搜索点击次数
                MobclickAgent.onEvent(mLauncher, "nanosearchclick");
//            SoloSearch.launchNewsFeed(mLauncher.getApplicationContext());
                // 进入zen搜索次数
                //MobclickAgent.onEvent(mLauncher,"IntoSearch");
            }
        }
    }

    private void startSearchClick() {
        Intent intent = new Intent(mLauncher, SearchActivity.class);
        intent.putExtra("hotWords", search_hotWords.getText());
        intent.putExtra("search_string", StringUtil.mStringMap);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mLauncher.startActivitySafely(intent);
    }


    public void resetEngineDisplay(Launcher launcher) {
        this.mLauncher = launcher;

        if (FlavorController.National) {
            searchOnClickListener = new SearchOnClickListener(SEARCH_BAIDU);
            searchButton.setImageResource(R.drawable.search_button);
            searchButton.setOnClickListener(searchOnClickListener);
        } else {
            if (searchOnClickListener == null) {
                searchOnClickListener = new SearchOnClickListener(SEARCH_ZEN);
            }
            searchBg.setOnClickListener(searchOnClickListener);
            searchButton.setImageResource(R.drawable.search_button);
            searchButton.setOnClickListener(searchOnClickListener);
        }

        String str = SearchHotWords.getHotWords();

        if (str == null || TextUtils.isEmpty(str)) {
            search_hotWords
                .setText(StringUtil.getString(Launcher.getInstance(), R.string.search_text_hint));
        } else {
            search_hotWords.setText(str);
        }
    }


    private void doSearch(String text) {
        //多进程之间sp需要实时获取，否则默认sp不会变
        SharedPreferences sharedPreferences = mLauncher.getSharedPreferences(
            LauncherConstants.SHARED_PREFERENCES_NAME, Context.MODE_MULTI_PROCESS);
        int n = sharedPreferences.getInt(LauncherConstants.SP_KEY_SEARCH_ENGINES_TYPE, 0);
        SEARCH_URL = mSearchUrl[n];
        String url = SEARCH_URL;
        goSearchWebViewActivity(url, text);
    }

    private void goSearchWebViewActivity(String url, String text) {
        Intent intent = new Intent(getContext(), SearchWebViewActivity.class);
        intent.putExtra("url", url + text);
        intent.putExtra("fromHomePage", true);
        intent.putExtra("hotWords", text);
        intent.putExtra("search_string", StringUtil.mStringMap);
        getContext().startActivity(intent);
    }

    /**
     * 判断二维码是否安装
     */
    public void checkQRCodeInstallStatus() {
        if (searchScan == null) {
            return;
        }

        if (CommonUtil.isAppInstalled(getContext(), SCAN_PACKAGENAME)) {
            searchScan.setImageResource(R.drawable.alert_scan_load_selector);
        } else {
            searchScan.setImageResource(R.drawable.alert_scan_pro);
        }
    }

}
