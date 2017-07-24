package com.cooeeui.brand.zenlauncher.scenes;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.cooeeui.brand.zenlauncher.tips.NumberProgressBar;
import com.cooeeui.zenlauncher.R;
import com.cooeeui.zenlauncher.common.BaseActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Steve on 2015/7/30.
 */
public class ZenSettingLife extends BaseActivity implements View.OnClickListener {

    private static final String LIFEURL = "http://nanohome.cn/launcher/zenlife/cartoon001/cartoon.html";
    AnimatorSet setBigger = new AnimatorSet();
    AnimatorSet setSmaller = new AnimatorSet();
    private WebView zenLifeWebView;
    private FrameLayout frameLayoutBack;
    private ImageButton imageButton;
    private NumberProgressBar progressBar;
    private String shareTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zen_setting_life);
        shareTitle = getIntent().getStringExtra("share_title");
        zenLifeWebView = (WebView) findViewById(R.id.zenLifeWebView);
        frameLayoutBack = (FrameLayout) findViewById(R.id.zen_LifeBack);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        progressBar = (NumberProgressBar) findViewById(R.id.progressBarStatus);
        imageButton.setOnClickListener(this);
        frameLayoutBack.setOnClickListener(this);
        WebSettings webSettings = zenLifeWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        //保持比例
        zenLifeWebView.setInitialScale(1);
        zenLifeWebView.loadUrl(LIFEURL);
        zenLifeWebView.setWebChromeClient(new WebChromeClient());
        ObjectAnimator
            scaleSmallerX =
            ObjectAnimator.ofFloat(imageButton, View.SCALE_X, 1.0f, 0.8f);
        ObjectAnimator
            scaleSmallerY =
            ObjectAnimator.ofFloat(imageButton, View.SCALE_Y, 1.0f, 0.8f);
        ObjectAnimator scaleBiggerX = ObjectAnimator.ofFloat(imageButton, View.SCALE_X, 0.8f, 1.0f);
        ObjectAnimator scaleBiggerY = ObjectAnimator.ofFloat(imageButton, View.SCALE_Y, 0.8f, 1.0f);
        setBigger.play(scaleBiggerX).with(scaleBiggerY);
        setSmaller.play(scaleSmallerX).with(scaleSmallerY);
        setBigger.setDuration(1500);
        setSmaller.setDuration(1500);
        setBigger.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setSmaller.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        setSmaller.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setBigger.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        setSmaller.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.zen_LifeBack:
                finish();
                // 友盟
                MobclickAgent.onKillProcess(this);
                System.exit(0);
                break;
            case R.id.imageButton:
                shareLifeURL();
                break;
        }
    }

    private void shareLifeURL() {
        String title = shareTitle;
        String message = LIFEURL;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(share, title));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 友盟
        MobclickAgent.onKillProcess(this);
        System.exit(0);
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            } else {
                if (progressBar.getVisibility() == View.GONE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }
}
