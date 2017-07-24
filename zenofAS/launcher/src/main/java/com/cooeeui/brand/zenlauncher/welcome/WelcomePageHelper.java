package com.cooeeui.brand.zenlauncher.welcome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooeeui.basecore.utilities.DensityUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.zenlauncher.R;

/**
 * Created by xingwang lee on 2015/8/6.
 */
public class WelcomePageHelper {

    private Activity welcomeContext;
    public ViewGroup welcomeRootLayout;
    private ImageView welcomeImgBigPic;
    private ImageView welcomeImgLoadingLogo;
    private LinearLayout welcomeLL_content_text;
    private Button welcomeButStartLauncher;
    private ViewGroup welcomeRootView;
    public boolean isShowing = false;

    public WelcomePageHelper(Context context) {
        this.welcomeContext = (Activity) context;
    }

    public void welcomePageShow() {
        isShowing = true;
        createGuideLayout();
        initGuideView();
    }

    private void createGuideLayout() {
        welcomeRootView = (ViewGroup) welcomeContext.getWindow().getDecorView();
        LayoutInflater lf = welcomeContext.getLayoutInflater();
        welcomeRootLayout = (ViewGroup) lf.inflate(R.layout.welcome_page_launcher, null);
        welcomeRootView.addView(welcomeRootLayout);


    }

    public void initGuideView() {
        welcomeImgBigPic = (ImageView) welcomeRootLayout.findViewById(R.id.im_loading_big_pic);
        welcomeImgLoadingLogo = (ImageView) welcomeRootLayout.findViewById(R.id.im_loading_logo);
        welcomeLL_content_text =
            (LinearLayout) welcomeRootLayout.findViewById(R.id.ll_content_text);
        welcomeButStartLauncher = (Button) welcomeRootLayout.findViewById(R.id.but_start_launcher);
        welcomeRootLayout.setClickable(true);
        welcomeRootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        welcomeButStartLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet animSet = new AnimatorSet();
                ObjectAnimator anim = ObjectAnimator.ofFloat(welcomeRootLayout, "alpha",
                                                             1.0f, 0.0f);
                animSet = new AnimatorSet();
                animSet.setDuration(100);
                animSet.setInterpolator(new LinearInterpolator());
                animSet.playTogether(anim);
                animSet.start();
                animSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        RemoveViewShowGuide();
                    }
                });
            }
        });
        //startAimation();
    }

    public void RemoveViewShowGuide() {
        welcomeRootLayout.setVisibility(View.GONE);
        welcomeRootView.removeView(welcomeRootLayout);
        Launcher.getInstance().startLauncher();
        isShowing = false;
    }

    private void startAimation() {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(welcomeImgBigPic, "scaleX",
                                                      1.5f, 1f);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(welcomeImgBigPic, "scaleY",
                                                      1.5f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(2000);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.playTogether(anim1, anim2);
        animSet.start();
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(welcomeImgLoadingLogo, "scaleX",
                                                      0.8f, 1f);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(welcomeImgLoadingLogo, "scaleY",
                                                      0.8f, 1f);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(welcomeImgLoadingLogo, "alpha",
                                                      0.0f, 1f);
        animSet = new AnimatorSet();
        animSet.setDuration(2000);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.playTogether(anim3, anim4, anim5);
        animSet.start();
        ObjectAnimator anim6 = ObjectAnimator.ofFloat(welcomeLL_content_text, "scaleX",
                                                      0.8f, 1f);
        ObjectAnimator anim7 = ObjectAnimator.ofFloat(welcomeLL_content_text, "scaleY",
                                                      0.8f, 1f);
        ObjectAnimator anim8 = ObjectAnimator.ofFloat(welcomeLL_content_text, "alpha",
                                                      0.0f, 1f);
        animSet = new AnimatorSet();
        animSet.setDuration(2000);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.playTogether(anim6, anim7, anim8);
        animSet.start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startLauncherButton();
            }
        });
    }

    private void startLauncherButton() {
        welcomeButStartLauncher.setVisibility(View.VISIBLE);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(welcomeButStartLauncher, "alpha",
                                                      0.0f, 1f);
        ObjectAnimator anim6 =
            ObjectAnimator.ofFloat(welcomeButStartLauncher, "y", welcomeButStartLauncher.getY(),
                                   welcomeButStartLauncher.getY() -
                                   DensityUtil.dip2px(welcomeContext, 80)
            );
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(1000);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.playTogether(anim5, anim6);
        animSet.start();

    }
}
