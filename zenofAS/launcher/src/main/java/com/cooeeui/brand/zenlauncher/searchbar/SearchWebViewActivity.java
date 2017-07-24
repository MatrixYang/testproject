package com.cooeeui.brand.zenlauncher.searchbar;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.cooeeui.brand.zenlauncher.tips.NumberProgressBar;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.cooeeui.zenlauncher.common.StringUtil;
import com.umeng.analytics.MobclickAgentJSInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by cuiqian on 2016/1/20.
 */
public class SearchWebViewActivity extends BaseActivity {

    private WebView mWebView = null;
    private NumberProgressBar mProgressBar;
    private static String TAG = "SearchWebActivity";

    private boolean mFirstLoad;
    private int mCount;
    private boolean mFromHomePage;
    private HashMap<String,String> stringMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_web);

        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("search_string") != null) {
            stringMap = (HashMap<String,String>) intent.getSerializableExtra("search_string");
            if (stringMap != null && stringMap.size() > 0) {
                StringUtil.setStringMap(stringMap);
            }
        }

        mProgressBar = (NumberProgressBar) findViewById(R.id.progressBar);

        mFromHomePage = getIntent().getBooleanExtra("fromHomePage", false);
        mFirstLoad = true;
        mCount = 0;
        mWebView = (WebView) findViewById(R.id.search_webview);
        initWebViewSettings();
        mWebView.loadUrl(getIntent().getStringExtra("url"));
        WebChromeClient webChromeClient = new WebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        mWebView.setWebViewClient(new WebViewClient());
        new MobclickAgentJSInterface(SearchWebViewActivity.this, mWebView, webChromeClient);
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

        // Enable DOM storage
        settings.setDomStorageEnabled(true);

        // Enable built-in geolocation
        settings.setGeolocationEnabled(true);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFromHomePage) {
            startSearchClick();
        }
        // 友盟,官方规定必须在调用Process.kill或者System.exit之类的方法杀死进程前，调用如下方法
//        MobclickAgent.onKillProcess(this);
//        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 友盟,官方规定必须在调用Process.kill或者System.exit之类的方法杀死进程前，调用如下方法
//        MobclickAgent.onKillProcess(this);
//        System.exit(0);
    }


    private void startSearchClick() {
        Intent intent = new Intent(SearchWebViewActivity.this, SearchActivity.class);
        intent.putExtra("hotWords", getIntent().getStringExtra("hotWords"));
        startActivity(intent);
    }

    private void goBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(SearchWebViewActivity.this,
                           StringUtil
                               .getString(SearchWebViewActivity.this,
                                          R.string.activity_not_found),
                           Toast.LENGTH_SHORT).show();
        }
    }

    private class WebViewClient extends android.webkit.WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mCount++;
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 这里的计数器，是为了解决重定向链接问题。产品希望首次链接都需要采用webview显示，然而重定向问题会导致首次链接进入了浏览器
            if (mCount >= 2 && mFirstLoad) {
                view.loadUrl(url);
                mFirstLoad = false;
            } else {
                goBrowser(url);
            }
            return true;
        }
    }

    private class WebChromeClient extends android.webkit.WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress >= 90) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                if (mProgressBar.getVisibility() == View.GONE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                if (newProgress < SearchActivity.PROGRESSBAR_MIN) {
                    mProgressBar.setProgress(SearchActivity.PROGRESSBAR_MIN);
                } else {
                    mProgressBar.setProgress(newProgress);
                }
            }
            super.onProgressChanged(view, newProgress);
        }
    }
}
