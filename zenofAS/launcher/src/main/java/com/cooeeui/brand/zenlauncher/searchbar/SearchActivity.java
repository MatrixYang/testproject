package com.cooeeui.brand.zenlauncher.searchbar;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.cooee.localsearch.api.LocalSearchListApi;
import com.cooee.localsearch.history.HistoryRecordDao;
import com.cooeeui.basecore.utilities.NetworkAvailableUtils;
import com.cooeeui.brand.zenlauncher.config.FlavorController;
import com.cooeeui.brand.zenlauncher.localsearch.SearchListAdapter;
import com.cooeeui.brand.zenlauncher.preferences.LauncherPreference;
import com.cooeeui.brand.zenlauncher.tips.NumberProgressBar;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.cooeeui.zenlauncher.common.ui.DialogUtil;
import com.kmob.kmobsdk.AdBaseView;
import com.kmob.kmobsdk.AdViewListener;
import com.kmob.kmobsdk.KmobManager;
import com.kmob.kmobsdk.NativeAdData;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgentJSInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends BaseActivity implements DialogUtil.DialogCancelListener {

    private static final String TAG = SearchActivity.class.getSimpleName();

    // startapp 搜索引擎
//    private String SEARCH_URL = "http://searchmobileonline.com/?pubid=204793810&q=";

    // apn(ask) 搜索引擎
    private String SEARCH_URL = "http://www.searchthis.com/web?mgct=ds&o=B10018&buid=G01&q= ";

    //添加友盟统计,更换URL
    private final String
        SEARCHPAGE_URL = "http://nanohome.cn/launcher/searchPageWithStats/index.html";

    private EditText mEditInput;
    private Button mButtonSearch;
    private ImageView mImgLogoSearch;

    private int[] mSearchDrawable = new int[SearchEnginesActivity.SEARCH_COUNT];

    private String[] mSearchUrl = new String[SearchEnginesActivity.SEARCH_COUNT];

    private int mSearchEnginesId = 0;

    private WebView mWebView;
    private LinearLayout mLinearWebView = null;
    private NumberProgressBar mProgressBar;
    public static int PROGRESSBAR_MIN = 10;
    private boolean mWebLoadFinish = false;

    private LinearLayout mLinearNetError;
    private final Handler mHandler = new Handler();
    private AdBaseView mNativeView = null;
    private String adUrl = "";
    private static int URL_GAME_LENGTH = 4;
    private static int URL_GAME_NAME_INDEX = 2;
    private static String URL_GAME_NAME = "nanohome.cn";
    private static String URL_GAME_KEYWORD = "game";

    /**
     * kuso local search
     */
    private ImageView history_record_clear;
    private ListView lv_history_record;
    private HistoryRecordDao dao;
    private List<String> searchResult;
    private ArrayList<String> viewResult = new ArrayList<String>();// 保存最近搜索的十条历史记录
    private ArrayAdapter<String> adapter; // 历史记录的适配器
    private RelativeLayout bottom;//历史记录界面
    private ListView listView1; // 本地搜索结果的listview
    private SearchListAdapter myListAdapter;//本地搜索结果的adapter
    private LocalSearchListApi localSearchListApi;
    private String result;
    private HashMap<String,String> stringMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_view);
        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("search_string") != null) {
            stringMap = (HashMap<String,String>)intent.getSerializableExtra("search_string");
            if (stringMap != null && stringMap.size() > 0) {
                StringUtil.setStringMap(stringMap);
            }
        }
        mSearchUrl[SearchEnginesActivity.SEARCH_NANO] = SEARCH_URL;
        mSearchUrl[SearchEnginesActivity.SEARCH_GOOGLE] = "http://www.google.com/search?q=";
        mSearchUrl[SearchEnginesActivity.SEARCH_YAHOO] = "http://search.yahoo.com/search?p=";
        mSearchUrl[SearchEnginesActivity.SEARCH_BING] = "http://cn.bing.com/search?q=";
        mSearchUrl[SearchEnginesActivity.SEARCH_BAIDU] = "http://m.baidu.com/s?word=";
        mSearchUrl[SearchEnginesActivity.SEARCH_DUCKDUCKGO] = "http://www.duckduckgo.com/?q=";

        mSearchEnginesId = LauncherPreference.getSearchEnginesType();
        SEARCH_URL = mSearchUrl[mSearchEnginesId];

        if (FlavorController.National) {
            SEARCH_URL = "https://www.baidu.com/s?wd=";
        }

        mEditInput = (EditText) findViewById(R.id.edit_input);
        initKusoLocalSearch();
        mButtonSearch = (Button) findViewById(R.id.button_search);
        mImgLogoSearch = (ImageView) findViewById(R.id.logo_search);

        mSearchDrawable[SearchEnginesActivity.SEARCH_NANO] = R.drawable.search_engine_nano;
        mSearchDrawable[SearchEnginesActivity.SEARCH_GOOGLE] = R.drawable.search_engine_google;
        mSearchDrawable[SearchEnginesActivity.SEARCH_YAHOO] = R.drawable.search_engine_yahoo;
        mSearchDrawable[SearchEnginesActivity.SEARCH_BING] = R.drawable.search_engine_bing;
        mSearchDrawable[SearchEnginesActivity.SEARCH_BAIDU] = R.drawable.search_engine_baidu;
        mSearchDrawable[SearchEnginesActivity.SEARCH_DUCKDUCKGO] =
            R.drawable.search_engine_duckduckgo;

        mImgLogoSearch.setBackgroundResource(mSearchDrawable[mSearchEnginesId]);

        mLinearNetError = (LinearLayout) findViewById(R.id.linearNetError);
        mLinearWebView = (LinearLayout) findViewById(R.id.ll_webView);
        mWebView = (WebView) findViewById(R.id.webView);
        mProgressBar = (NumberProgressBar) findViewById(R.id.progressBar);
        initWebViewSettings();
        initString();

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String urlArray[] = url.split("/");
                if (urlArray.length > URL_GAME_LENGTH) {
                    if (urlArray[URL_GAME_NAME_INDEX].equals(URL_GAME_NAME)
                        && urlArray[urlArray.length - 1].contains(URL_GAME_KEYWORD)) {
                        goBrowser(url);
                        return true;
                    }
                }

                goSearchWebViewActivity(url);
                return true;
            }

        });

        WebChromeClient webChromeClient = new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 90) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (mProgressBar.getVisibility() == View.GONE) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    if (newProgress < PROGRESSBAR_MIN) {
                        mProgressBar.setProgress(PROGRESSBAR_MIN);
                    } else {
                        mProgressBar.setProgress(newProgress);
                    }
                }

                if (newProgress >= 90) {
                    mWebLoadFinish = true;
                    if (adUrl != null && !adUrl.equals("")) {
                        mWebView.loadUrl("javascript:addImg('" + adUrl + "');");
                    }
                }
                super.onProgressChanged(view, newProgress);
            }
        };
        mWebView.setWebChromeClient(webChromeClient);
        new MobclickAgentJSInterface(SearchActivity.this, mWebView, webChromeClient);

        mEditInput.setHint(intent.getStringExtra("hotWords"));
        mEditInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myListAdapter.isEmpty()) {
                    listView1.setVisibility(View.GONE);
                    bottom.setVisibility(View.VISIBLE);
                } else {
                    listView1.setVisibility(View.VISIBLE);
                    bottom.setVisibility(View.GONE);
                }

                mLinearNetError.setVisibility(View.INVISIBLE);
                mLinearWebView.setVisibility(View.INVISIBLE);

                mEditInput.setFocusable(true);
                mEditInput.setFocusableInTouchMode(true);
                mEditInput.requestFocus();
                InputMethodManager
                    imm =
                    (InputMethodManager) mEditInput.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEditInput, 0);

            }
        });

        mEditInput.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                String text = view.getText().toString().trim();

                if (TextUtils.isEmpty(text)) {
                    text = view.getHint().toString().trim();
                }

                if (TextUtils.isEmpty(text) || text
                    .equals(StringUtil.getString(SearchActivity.this, R.string.search_text_hint))) {
                    Toast.makeText(SearchActivity.this,
                                   StringUtil.getString(SearchActivity.this,
                                                        R.string.search_input_tips),
                                   Toast.LENGTH_SHORT).show();
                } else {
                    doSearch(text);
                }

                return true;
            }
        });

        mButtonSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                String text = mEditInput.getText().toString().trim();

                if (TextUtils.isEmpty(text)) {
                    text = mEditInput.getHint().toString().trim();
                }
                if (TextUtils.isEmpty(text) || text
                    .equals(StringUtil.getString(SearchActivity.this, R.string.search_text_hint))) {
                    Toast.makeText(SearchActivity.this,
                                   StringUtil.getString(SearchActivity.this,
                                                        R.string.search_input_tips),
                                   Toast.LENGTH_SHORT).show();
                } else {
                    doSearch(text);
                }
                switch (mSearchEnginesId) {
                    case SearchEnginesActivity.SEARCH_NANO:
                        //使用默认搜索进行搜索次数
                        MobclickAgent.onEvent(SearchActivity.this, "Clicknanosearch");
                        break;
                    case SearchEnginesActivity.SEARCH_GOOGLE:
                        //使用谷歌搜索次数
                        MobclickAgent.onEvent(SearchActivity.this, "Clickgooglesearch");
                        break;
                    case SearchEnginesActivity.SEARCH_YAHOO:
                        //使用雅虎搜索次数
                        MobclickAgent.onEvent(SearchActivity.this, "Clickyahoosearch");
                        break;
                    case SearchEnginesActivity.SEARCH_BING:
                        //使用必应搜索次数
                        MobclickAgent.onEvent(SearchActivity.this, "Clickbingsearch");
                        break;
                    case SearchEnginesActivity.SEARCH_BAIDU:
                        //使用百度搜索次数
                        MobclickAgent.onEvent(SearchActivity.this, "Clickbaidusearch");
                        break;
                    case SearchEnginesActivity.SEARCH_DUCKDUCKGO:
                        //使用鸭子搜索次数
                        MobclickAgent.onEvent(SearchActivity.this, "Clickbucksearch");
                        break;
                }
            }
        });

        mImgLogoSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SearchActivity.this, SearchEnginesActivity.class);
                startActivityForResult(intent, 1);

            }
        });

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, mFilter);

        mLinearNetError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWebView();
            }
        });

        checkWebView();
        getAdInfo();
    }

    private void updateHistory() {
        if (result != null && !"".equals(result) && !viewResult.contains(result)) {
            //拿到搜索内容插入到历史记录中
            dao.insert(result);
            viewResult.add(0, result);
        } else if (viewResult.contains(result)) {
            viewResult.remove(result);
            viewResult.add(0, result);
            dao.delete(result);
            dao.insert(result);
        }
        adapter.notifyDataSetChanged();
    }

    private void initKusoLocalSearch() {

        dao = new HistoryRecordDao(this);
        lv_history_record = (ListView) findViewById(R.id.kuso_lv_history_record);
        bottom = (RelativeLayout) findViewById(R.id.kuso_bottom);
        getData();
        adapter =
            new ArrayAdapter<String>(this, R.layout.kuso_history_item,
                                     R.id.kuso_tv_history_item,
                                     viewResult);
        lv_history_record.setAdapter(adapter);
        listView1 = (ListView) findViewById(R.id.kuso_listview);//本地搜索结果

        if (localSearchListApi == null) {
            localSearchListApi = new LocalSearchListApi(this);
            //加载所有的app，music，Contacts
            localSearchListApi.loadContent();
            localSearchListApi.initContent();
        }
        if (myListAdapter == null) {
            myListAdapter = new SearchListAdapter(this);
        }

        listView1.setAdapter(myListAdapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(
                AdapterView<?> arg0,
                View arg1,
                int arg2,
                long arg3) {
                myListAdapter.onClick(arg2);
            }
        });
        mEditInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(
                CharSequence s,
                int start,
                int before,
                int count) {
                result = s.toString().trim();
                myListAdapter.clearSearchList();
                myListAdapter.setSearchText(s);
                myListAdapter.updateAppList(localSearchListApi.getAppList(result));
                myListAdapter.updateContactsList(localSearchListApi.getContactsList(result));
                myListAdapter.updateMusicList(localSearchListApi.getMusicList(result));
                myListAdapter.notifyDataSetInvalidated();

                if (mEditInput.isFocused()) {
                    if (myListAdapter.isEmpty()) {
                        listView1.setVisibility(View.GONE);
                        bottom.setVisibility(View.VISIBLE);
                    } else {
                        listView1.setVisibility(View.VISIBLE);
                        bottom.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void beforeTextChanged(
                CharSequence s,
                int start,
                int count,
                int after) {
            }

            @Override
            public void afterTextChanged(
                Editable s) {
            }
        });
        /**
         * 点击拿到历史记录条目--先显示本地结果，本地为空则跳转搜索
         */
        lv_history_record = (ListView) findViewById(R.id.kuso_lv_history_record);
        lv_history_record.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            private String history_item;

            @Override
            public void onItemClick(
                AdapterView<?> parent,
                View view,
                int position,
                long id) {
                history_item = adapter.getItem(position);
                viewResult.remove(history_item);
                viewResult.add(0, history_item);
                dao.delete(history_item);
                dao.insert(history_item);
                adapter.notifyDataSetChanged();

                doSearch(history_item);
            }
        });
        history_record_clear = (ImageView) findViewById(R.id.kuso_history_record_clear);
        history_record_clear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(
                View v) {
                dao.clearAllRecord();
                getData();
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void getData() {
        viewResult.clear();
        searchResult = dao.queryAllSearch();
        // viewResult = new String[searchResult.size()];
        int j = 0;
        for (int i = searchResult.size() - 1; i >= 0; i--) {
            // viewResult[i] = searchResult.get( j );
            viewResult.add(searchResult.get(j));
            j++;
        }
    }

    private void checkWebView() {
        if (NetworkAvailableUtils.isNetworkAvailable(this)) {
            mLinearNetError.setVisibility(View.INVISIBLE);
            mLinearWebView.setVisibility(View.VISIBLE);
            loadWebView();
        } else {
            mLinearNetError.setVisibility(View.VISIBLE);
            mLinearWebView.setVisibility(View.INVISIBLE);
        }
    }

    private void loadWebView() {
        mWebView.loadUrl(SEARCHPAGE_URL); // 联网,加载html
    }

    private void initString() {
        String text = StringUtil.getString(this, R.string.search_text_hint);
        mEditInput.setHint(text);
        text = StringUtil.getString(this, R.string.wallpaper_check_net);
        ((TextView) findViewById(R.id.tv_wallpaper_net_error)).setText(text);
        text = StringUtil.getString(this, R.string.kuso_history_record);
        ((TextView) findViewById(R.id.kuso_tv_history_record)).setText(text);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("deprecation")
    private void initWebViewSettings() {
        mWebView.setInitialScale(0);
        mWebView.setVerticalScrollBarEnabled(false);
        // Enable JavaScript
        final WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setTextSize(WebSettings.TextSize.NORMAL);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        // Set the nav dump for HTC 2.x devices (disabling for ICS, deprecated
        // entirely for Jellybean 4.2)
        try {
            Method gingerbread_getMethod = WebSettings.class.getMethod(
                "setNavDump", new Class[]{boolean.class});

            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB
                && android.os.Build.MANUFACTURER.contains("HTC")) {
                gingerbread_getMethod.invoke(settings, true);
            }
        } catch (NoSuchMethodException e) {
            Log.d(TAG,
                  "We are on a modern version of Android, we will deprecate HTC 2.3 devices in 2.8");
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Doing the NavDump failed with bad arguments");
        } catch (IllegalAccessException e) {
            Log.d(TAG,
                  "This should never happen: IllegalAccessException means this isn't Android anymore");
        } catch (InvocationTargetException e) {
            Log.d(TAG,
                  "This should never happen: InvocationTargetException means this isn't Android anymore.");
        }

        // We don't save any form data in the application
        settings.setSaveFormData(false);
        settings.setSavePassword(false);

        // Jellybean rightfully tried to lock this down. Too bad they didn't
        // give us a whitelist
        // while we do this
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }

        // Enable database
        // We keep this disabled because we use or shim to get around
        // DOM_EXCEPTION_ERROR_16
        String databasePath = mWebView.getContext().getApplicationContext()
            .getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(databasePath);

        // Enable DOM storage
        settings.setDomStorageEnabled(true);

        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);

        // Enable AppCache
        // Fix for CB-2282
        settings.setAppCacheMaxSize(5 * 1048576);
        settings.setAppCachePath(databasePath);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.addJavascriptInterface(new JavaScriptObject(), "nano");

    }

    @Override
    public void onBackPressed() {
        if (listView1.getVisibility() == View.VISIBLE
            || bottom.getVisibility() == View.VISIBLE) {
            listView1.setVisibility(View.INVISIBLE);
            bottom.setVisibility(View.INVISIBLE);
            if (NetworkAvailableUtils.isNetworkAvailable(this)) {
                mLinearNetError.setVisibility(View.INVISIBLE);
                mLinearWebView.setVisibility(View.VISIBLE);
            } else {
                mLinearNetError.setVisibility(View.VISIBLE);
                mLinearWebView.setVisibility(View.INVISIBLE);
            }

        } else {
            super.onBackPressed();
            // 友盟,官方规定必须在调用Process.kill或者System.exit之类的方法杀死进程前，调用如下方法
//            MobclickAgent.onKillProcess(this);
//            System.exit(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                int orderId = data.getExtras().getInt("orderId");
                mImgLogoSearch.setBackgroundResource(mSearchDrawable[orderId]);

                SEARCH_URL = mSearchUrl[orderId];

                break;
            default:
                break;
        }
    }

    private void goSearchWebViewActivity(String url) {
        Intent intent = new Intent(SearchActivity.this, SearchWebViewActivity.class);
        intent.putExtra("url", url);
        SearchActivity.this.startActivity(intent);
    }

    private void goBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(SearchActivity.this,
                           StringUtil
                               .getString(SearchActivity.this,
                                          R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }
    }

    private void doSearch(String text) {
        updateHistory();
        String url = SEARCH_URL + text;
        goSearchWebViewActivity(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNativeView != null) {
            mNativeView.onResume();
        }

        //kuso local search start
        getData();
        adapter.notifyDataSetChanged();
        //kuso local search end
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNativeView != null) {
            mNativeView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mNativeView != null) {
            mNativeView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
        }
        if (mNativeView != null) {
            mNativeView.onDestroy();
        }

        if (localSearchListApi != null) {
            localSearchListApi.onDestroy();
        }
        // 友盟,官方规定必须在调用Process.kill或者System.exit之类的方法杀死进程前，调用如下方法
        //MobclickAgent.onKillProcess(this);
        //System.exit(0);
    }

    @Override
    public void onCancelDialog() {
    }

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                ConnectivityManager
                    mConnectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isAvailable()) {

                } else {
                }
            }

        }
    };

    private String adplaceid;
    private String adid;
    private String clickurl;
    private String interactiontype;
    private String open_type;
    private String package_name;
    private String click_record_url;
    private String headline;
    private String download;
    private String summary;
    private String adlogo;

    private void getAdInfo() {
        mNativeView = KmobManager.createNative("20160119050119501", this, 1);
        mNativeView.addAdViewListener(new AdViewListener() {

            @Override
            public void onAdShow(
                String info) {

            }

            @Override
            public void onAdReady(
                String info) {
                Log.w(KmobManager.LOGTAG, "NativeAdActivity onAdReady info " + info);
                addAdView(info);
            }

            @Override
            public void onAdFailed(
                String reason) {
                Log.w(KmobManager.LOGTAG, "NativeAdActivity onAdFailed info " + reason);
            }

            @Override
            public void onAdClose(
                String info) {
                Log.w(KmobManager.LOGTAG, "NativeAdActivity onAdClose info " + info);
            }

            @Override
            public void onAdClick(
                String info) {
                Log.w(KmobManager.LOGTAG, "NativeAdActivity onAdClick info " + info);
            }

            @Override
            public void onAdCancel(
                String info) {
                Log.w(KmobManager.LOGTAG, "NativeAdActivity onAdCancel info " + info);
            }
        });
    }

    /**
     * 通过传入的info解析数据，来展示合适的广告
     */
    private void addAdView(
        String info) {
        ArrayList<NativeData> nativeAdDatas = createNativeDataByInfo(info);
        for (int i = 0; i < nativeAdDatas.size(); i++) {
            NativeData nativeData = nativeAdDatas.get(i);
            adplaceid = nativeData.getAdplaceid();
            adid = nativeData.getAdid();
            clickurl = nativeData.getClickurl();
            interactiontype = nativeData.getInteractiontype();
            open_type = nativeData.getOpen_type();
            package_name = nativeData.getPkgname();
            click_record_url = nativeData.getClick_record_url();
            headline = nativeData.getHeadline();
            download = nativeData.getDownload();
            summary = nativeData.getSummary();
            adlogo = nativeData.getAdlogo();
            String cimg = nativeData.getCtimg();
            Log.v(KmobManager.LOGTAG, "cimg = " + cimg);
            try {
                JSONArray ctimgArray = new JSONArray(cimg);
                if (ctimgArray != null && ctimgArray.length() > 0) {
                    JSONObject object = ctimgArray.getJSONObject(0);
                    String url = object.getString("url");
                    String imgwidth = object.getString("width");
                    String imgHeight = object.getString("height");
                    adUrl = url;
                    Log.v(KmobManager.LOGTAG,
                          "imgurl " + url + " imgwidth " + imgwidth + " imgHeight " + imgHeight);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            if (mWebLoadFinish && adUrl != null && !adUrl.equals("")) {
                mWebView.loadUrl("javascript:addImg('" + adUrl + "');");
            }
        }
    }

    /**
     * 通过传入的ifo生成很多个nativeAdData，如果是一个广告，则info类型为JsonObject，多个广告类型，则为JsonArray，建议解析的时候先进行检测
     */
    private ArrayList<NativeData> createNativeDataByInfo(
        String info) {
        ArrayList<NativeData> allData = new ArrayList<NativeData>();
        if (info != null) {
            try {
                JSONObject object = new JSONObject(info);//此时若不是jsonObject，则会抛出异常
                NativeData adData = createNativeData(object);
                allData.add(adData);
            } catch (Exception e) {
                try {
                    JSONArray array = new JSONArray(info);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        NativeData adData = createNativeData(object);
                        allData.add(adData);
                    }
                } catch (Exception e2) {
                    // TODO: handle exception
                }
            }
        }
        return allData;
    }

    /**
     * 通过广告传入的数据生成一个NativeAdData
     */
    private NativeData createNativeData(
        JSONObject object) {
        String summary = "";
        String headline = "";
        String adcategory = "";
        String appRating = "";
        String adlogo = "";
        String details = "";
        String adlogoWidth = "";
        String adlogoHeight = "";
        String review = "";
        String appinstalls = "";
        String download = "";
        String adplaceid = "";
        String adid = "";
        String clickurl = "";
        String interactiontype = "";
        String open_type = "";
        String hurl = "";
        String hdetailurl = "";
        String pkgname = "";
        String appsize = "";
        String version = "";
        String versionname = "";
        String ctimg = "";
        String hiimg = "";
        String click_record_url = "";
        try {
            if (object.has(NativeAdData.SUMMARY_TAG)) {
                summary = object.getString(NativeAdData.SUMMARY_TAG);
            }
            if (object.has(NativeAdData.HEADLINE_TAG)) {
                headline = object.getString(NativeAdData.HEADLINE_TAG);
            }
            if (object.has(NativeAdData.ADCATEGORY_TAG)) {
                adcategory = object.getString(NativeAdData.ADCATEGORY_TAG);
            }
            if (object.has(NativeAdData.APPRATING_TAG)) {
                appRating = object.getString(NativeAdData.APPRATING_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_TAG)) {
                adlogo = object.getString(NativeAdData.ADLOGO_TAG);
            }
            if (object.has(NativeAdData.DETAILS_TAG)) {
                details = object.getString(NativeAdData.DETAILS_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_WIDTH_TAG)) {
                adlogoWidth = object.getString(NativeAdData.ADLOGO_WIDTH_TAG);
            }
            if (object.has(NativeAdData.ADLOGO_HEIGHT_TAG)) {
                adlogoHeight = object.getString(NativeAdData.ADLOGO_HEIGHT_TAG);
            }
            if (object.has(NativeAdData.REVIEW_TAG)) {
                review = object.getString(NativeAdData.REVIEW_TAG);
            }
            if (object.has(NativeAdData.APPINSTALLS_TAG)) {
                appinstalls = object.getString(NativeAdData.APPINSTALLS_TAG);
            }
            if (object.has(NativeAdData.DOWNLOAD_TAG)) {
                download = object.getString(NativeAdData.DOWNLOAD_TAG);
            }
            if (object.has(NativeAdData.ADPLACE_ID_TAG)) {
                adplaceid = object.getString(NativeAdData.ADPLACE_ID_TAG);
            }
            if (object.has(NativeAdData.AD_ID_TAG)) {
                adid = object.getString(NativeAdData.AD_ID_TAG);
            }
            if (object.has(NativeAdData.CLICKURL_TAG)) {
                clickurl = object.getString(NativeAdData.CLICKURL_TAG);
            }
            if (object.has(NativeAdData.INTERACTION_TYPE_TAG)) {
                interactiontype = object.getString(NativeAdData.INTERACTION_TYPE_TAG);
            }
            if (object.has(NativeAdData.OPEN_TYPE_TAG)) {
                open_type = object.getString(NativeAdData.OPEN_TYPE_TAG);
            }
            if (object.has(NativeAdData.HURL_TAG)) {
                hurl = object.getString(NativeAdData.HURL_TAG);
            }
            if (object.has(NativeAdData.HDETAILURL_TAG)) {
                hdetailurl = object.getString(NativeAdData.HDETAILURL_TAG);
            }
            if (object.has(NativeAdData.PKGNAME_TAG)) {
                pkgname = object.getString(NativeAdData.PKGNAME_TAG);
            }
            if (object.has(NativeAdData.APPSIZE_TAG)) {
                appsize = object.getString(NativeAdData.APPSIZE_TAG);
            }
            if (object.has(NativeAdData.VERSION_TAG)) {
                version = object.getString(NativeAdData.VERSION_TAG);
            }
            if (object.has(NativeAdData.VERSIONNAME_TAG)) {
                versionname = object.getString(NativeAdData.VERSIONNAME_TAG);
            }
            if (object.has(NativeAdData.CTIMG_TAG)) {
                ctimg = object.getString(NativeAdData.CTIMG_TAG);
            }
            if (object.has(NativeAdData.HIIMG_TAG)) {
                hiimg = object.getString(NativeAdData.HIIMG_TAG);
            }
            if (object.has(NativeAdData.CLICK_RECORD_URL_TAG)) {
                click_record_url = object.getString(NativeAdData.CLICK_RECORD_URL_TAG);
            }
            return new NativeData(
                summary,
                headline,
                adcategory,
                appRating,
                adlogo,
                details,
                adlogoWidth,
                adlogoHeight,
                review,
                appinstalls,
                download,
                adplaceid,
                adid,
                clickurl,
                interactiontype,
                open_type,
                hurl,
                hdetailurl,
                pkgname,
                appsize,
                version,
                versionname,
                ctimg,
                hiimg,
                click_record_url);
        } catch (Exception e) {
            Log.e("KMOB", "addAdView e " + e.toString());
        }
        return null;
    }

    public class JavaScriptObject {

        @JavascriptInterface
        public void clickOnAndroid() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // This gets executed on the UI thread so it can safely modify Views
                    KmobManager.onClickDone(adplaceid, adid, clickurl, interactiontype, open_type,
                                            package_name, click_record_url, headline, download,
                                            summary, adlogo);
                }
            });
        }

        @JavascriptInterface
        public void notifyAdShow() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // This gets executed on the UI thread so it can safely modify Views
                    KmobManager.onNativeAdShow(adplaceid, adid);
                }
            });
        }
    }
}
