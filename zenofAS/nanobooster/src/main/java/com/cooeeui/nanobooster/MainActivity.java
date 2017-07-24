package com.cooeeui.nanobooster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cooeeui.nanobooster.common.util.AccessibilityServiceUtil;
import com.cooeeui.nanobooster.common.util.MemoryUtil;
import com.cooeeui.nanobooster.common.util.ThreadUtil;
import com.cooeeui.nanobooster.model.NanoboosterAdpter;
import com.cooeeui.nanobooster.model.domain.AppInfo;
import com.cooeeui.nanobooster.services.BoosterAccessibilityService;
import com.cooeeui.nanobooster.services.FloatWindowService;
import com.cooeeui.nanobooster.views.DragViewForBooster;
import com.cooeeui.nanobooster.views.RippleView;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    //codes of qixiangxiang (start)
    private ListView mLv_1;
    public boolean ignore = false;
    public boolean running = false;
    public boolean isTitleChecked = true;
    public List<AppInfo> items_ignore = new ArrayList<AppInfo>();
    public List<AppInfo> items_running = new ArrayList<AppInfo>();
    public List<AppInfo> items_total = new ArrayList<AppInfo>();
    public List<AppInfo> items_delFrom_running = new ArrayList<AppInfo>();
    public List<AppInfo> items_current = new ArrayList<AppInfo>();
    private NanoboosterAdpter mNanoboosterAdpter;
    public int items_total_size;
    public int items_ignore_size;
    public int items_running_size;
    private SharedPreferences mSp;
    private int mTotal_memory_size = 0;

    private RelativeLayout mRl_ignore_label;
    public TextView tv_ignore_size_label;

    private RelativeLayout mLl_running_label_first;
    public CheckBox ck_running_first;
    private TextView tv_running_manager_first;
    private TextView tv_running_total_first;

    private RelativeLayout ll_runnuing_label_second;
    public CheckBox ck_running_second;
    private TextView tv_running_manager_second;
    private TextView tv_running_total_second;

    private TextView tv_line;


    //大圆部分
    private ImageView mIv_inner;
    private ImageView MIv_outter;
    private RotateAnimation mRaInner;
    private RotateAnimation mRaOutter;
    private AnimationSet mAsInner;
    private AlphaAnimation mAaInner;
    private AnimationSet mAsOutter;
    private AlphaAnimation mAaOutter;
    private ImageView mIv_inner_total;
    private AlphaAnimation mAaInner_total;
    private TranslateAnimation mTa_root;
    private ScaleAnimation mSa_root;
    private AnimationSet mAs_root;
    private RelativeLayout mFl_root;
    private int mAs_root_duration = 2000;
    private int mAaInner_total_duration = 5000;
    private int mRaInner_duration = 3000;
    private int mAaInner_duration = 3000;
    private int mAaOutter_duration = 3000;
    private int mTranstionEndX;
    private int mTranstionEndY;

    //三段文字
    private TextView mTv_size;
    private TextView mTv_name;
    private TextView mTv_desc;
    private ObjectAnimator mAnim_tv_size;
    private ObjectAnimator mAnim_tv_desc;
    private ObjectAnimator mAnim_tv_name;
    private AnimatorSet mAnimatorSet_threeParagraph;
    private AlphaAnimation mAa_mAnim_tv_size;
    private int mAa_mAnim_tv_size_duration = 2000;
    private int mAnim_tv_size_duration = 2000;
    private int mAnim_tv_name_delay = 200;
    private int mAnim_tv_name_duration = 2000;
    private int mAnim_tv_desc_delay = 400;
    private int mAnim_tv_desc_duration = 2000;
    private int mRaOutter_duration = 3000;

    //提示小箭头
    private RelativeLayout mRl_guide_arrow;
    private ImageView mGuide_arrow_1;
    private ImageView mGuide_arrow_2;
    private ImageView mGuide_arrow_3;
    private AlphaAnimation mAa_guide_arrow_1_in;
    private AlphaAnimation mAa_guide_arrow_1_out;
    private AlphaAnimation mAa_guide_arrow_2_in;
    private AlphaAnimation mAa_guide_arrow_2_out;
    private AlphaAnimation mAa_guide_arrow_3_in;
    private AlphaAnimation mAa_guide_arrow_3_out;
    private int mAa_guide_arrow_1_in_duration = 100;
    private int mAa_guide_arrow_1_out_duration = 100;
    private int mAa_guide_arrow_2_in_duration = 100;
    private int mAa_guide_arrow_2_out_duration = 100;
    private int mAa_guide_arrow_3_in_duration = 100;
    private int mAa_guide_arrow_3_out_duration = 100;

    //小圆部分描述信息
    private LinearLayout mLl_root_small_desc;
    private AlphaAnimation mAa_small_desc;
    private int mAa_small_desc_duration = 2000;
    private TextView mTv_total_small;
    private TextView mTv_small_desc;

    //扫描进度信息
    private TextView mTv_progress;
    private int mLoopNo;
    private int mLoopNo_total;
    private ValueAnimator mValueAnimator_progress;
    private int mValueAnimator_progress_duration = 2000;

    //露出listview
    private FrameLayout mFl_child_include_list;
    private ObjectAnimator mOa_fl_child_include_list_part;
    private ObjectAnimator mOa_fl_child_include_list_total;
    private FrameLayout mFl_button_false;
    private Button mBt_booster_false;
    private int mOa_fl_child_include_list_part_duration = 2000;
    private int mOa_fl_child_include_list_total_duration = 2000;
    private int mTansition_total_start;
    private int mTansition_total_end;
    private int mTransition_part_end;


    //private FrameLayout mFl_button_true;
    private RelativeLayout mFl_button_true;
    private Button mBt_booster_true;
    private ObjectAnimator oa_mBt_booster_false;


    private Message mMessage_list;
    // 扫描开始
    protected static final int BEGING = 1;
    // 扫描结束
    protected static final int FINISH = 3;


    //返回动画
    private AlphaAnimation mAaOutter_reverse;
    private RotateAnimation mRaOutter_reverse;
    private AnimationSet mAsOutter_reverse;
    private RotateAnimation mRaInner_reverse;
    private AlphaAnimation mAaInner_reverse;
    private AnimationSet mAsInner_reverse;
    private AlphaAnimation mAaInner_total_reverse;

    //进度减小
    private ValueAnimator mValueAnimator_progress_reverse;

    //清理多少
    private LinearLayout ll_detail_clean;
    private RelativeLayout mRl_clean_no_desc;
    private TextView mTv_clean_desc;
    private AlphaAnimation mAaCleanAnimation_rl_clean_no_desc;
    private AlphaAnimation mAaCleanAnimation_tv_clean_desc;

    //清理完成之后的界面
    private LinearLayout ll_firstChild_for_DragViewForBooster;
    private ScrollView mSc_after_clean;
    private LinearLayout ll_total_false;

    private ObjectAnimator mOa_ll_detail_clean;
    private FrameLayout mFl_av;
    private ObjectAnimator mOa_mFl_av;

    private DragViewForBooster db_dragBooster;
    private FrameLayout mFl_button_false_finish;

    private AlphaAnimation mAa_aninamtio_finish;

    private boolean returnAnimationIsDo = false;
    private FrameLayout mFl_total_upMove;

    public static Handler mMainHandler = new Handler();
    // ad begin
    private final int mAdCountMax = 3;
    private RelativeLayout mAdUnit;
    private RelativeLayout mAdUnit2;
    private RelativeLayout mAdUnit3;
    // Mobvista ad begin
    private MvNativeHandler nativeHandle;
    private ArrayList<Campaign> mNativeCampaign = new ArrayList<>();
    // Mobvista ad end

    //返回动画的初始化
    private AlphaAnimation mAa_small_desc_reverse;
    private ObjectAnimator mOa_fl_child_include_list_total_reverse;
    private ObjectAnimator mOfl_button_true_reverse;
    private TranslateAnimation mTa_root_reverse;
    private ScaleAnimation mSa_root_reverse;
    private AnimationSet mAs_root_reverse;
    private AlphaAnimation mAa_mAnim_tv_size_reverse;
    private AlphaAnimation mAa_mAnim_tv_name_reverse;

    //清理动画的初始化

    private static FrameLayout mRate_alert_dialog;
    private static ImageView mRateAlertDialog_bg;

    private TextView mTv_Later;
    private TextView mTv_ok;
    //codes of qixiangxiang (end)

    private RelativeLayout mTitlebar_icon;
    RelativeLayout mTitlebar_icon_false;

    private LinearLayout mLinearLayout;

    private ImageView mIv_background;
    Boolean flag = true;
    //动画变化
    float anim1 = 0f, anim2 = 0.5f;
    //动画持续时间
    long duration = 500;

    // 返回键退出时间控制
    private long mExitTime;

    private FrameLayout mAccessibilityDialog;
    private RelativeLayout mAccessibilityAlertDialog;
    private ImageView mAccessibilityAlertDialogBg;
    private int mAccessibilityAnimDuration = 2000;
    private RippleView mAccessibilityEnableNowBtn;

    //wave animation
    private ImageView mWaveSecondBg;
    private ImageView wave_second_one;
    private ImageView wave_second_two;
    private ImageView mWaveFirstBg;
    private ImageView wave_first_one;
    private ImageView wave_first_two;
    private float repeatValue = 0;
    private ValueAnimator second_one_anim;
    private ValueAnimator second_two_anim;

    public static int runningAppSize;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case BEGING:
                    break;
                case FINISH:
                    initListView();
                    if (mNanoboosterAdpter != null) {
                        changeIngoreAndRuningSet(null);

                        if (mNanoboosterAdpter.ck_running != null) {
                            isTitleChecked = runningItermIsChecked();
                            mNanoboosterAdpter.ck_running.setChecked(isTitleChecked);
                        }
                        // mNanoboosterAdpter.notifyDataSetChanged();
                    }
                    mTv_total_small.setText((int) (mTotal_memory_size / 1024f / 1024f + 0.5f) + "");
                    break;
            }

        }
    };
    int j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.drawer_view);

        mTitlebar_icon = (RelativeLayout) findViewById(R.id.titlebar_icon);
        mTitlebar_icon.setOnClickListener(this);

        //假的actionbar
        mTitlebar_icon_false = (RelativeLayout) findViewById(R.id.titlebar_icon_false);
        mTitlebar_icon_false.setOnClickListener(this);

        mLinearLayout = (LinearLayout) findViewById(R.id.ll_layout);
        // mIv_background = (ImageView)findViewById(R.id.iv_background);

        //codes of qixiangxiang (start)
        // ad begin
        mAdUnit = (RelativeLayout) findViewById(R.id.ad_unit);
        mAdUnit2 = (RelativeLayout) findViewById(R.id.ad_unit2);
        mAdUnit3 = (RelativeLayout) findViewById(R.id.ad_unit3);
        // ad end
        initView();
        init();
        // initView();
        initData();
        initAnimation();
        startAnimation();
        initListener();
        initWaveAnimation();
        initReturnAnimation();
        initCleanAnimation();
        initMobvista();
        //codes of qixiangxiang (end)
        addShortcutToDesktop(MainActivity.this);
    }

    //初始化广告
    public void initMobvista() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, String> map =
            sdk.getMVConfigurationMap("22466", "686dfddcac68d078f4de704b947cff0c");
        sdk.init(map, this);
    }

    public void loadMobvistaNative() {
        if (nativeHandle == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("544");
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "826581090784415_870544966388027");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, mAdCountMax);//请求广告条数，不设默认为1
            nativeHandle = new MvNativeHandler(properties, MainActivity.this);
            nativeHandle.setAdListener(new MvNativeHandler.NativeAdListener() {

                @Override
                public void onAdLoaded(List<Campaign> campaigns, int template) {

                    mNativeCampaign.clear();
                    for (int i = 0; i < campaigns.size(); i++) {
                        if (campaigns.get(i) != null) {
                            mNativeCampaign.add(campaigns.get(i));
                        }
                    }

                    final ImageView nativeAdCover =
                        (ImageView) mAdUnit.findViewById(R.id.native_ad_cover);
                    final ImageView nativeAdIcon =
                        (ImageView) mAdUnit.findViewById(R.id.native_ad_icon);
                    final TextView nativeAdTitle =
                        (TextView) mAdUnit.findViewById(R.id.native_ad_title);
                    final TextView nativeAdBody =
                        (TextView) mAdUnit.findViewById(R.id.native_ad_body);

                    nativeAdBody.setText(mNativeCampaign.get(0).getAppDesc());
                    nativeAdTitle.setText(mNativeCampaign.get(0).getAppName());
                    //mobvista绑定点击事件
                    nativeHandle.registerView(nativeAdCover, mNativeCampaign.get(0));
                    final String urlIcon = mNativeCampaign.get(0).getIconUrl();
                    Picasso.with(MainActivity.this)
                        .load(urlIcon)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon);
                    // Downloading and setting the ad icon.
                    final String urlImage = mNativeCampaign.get(0).getImageUrl();
                    Picasso.with(MainActivity.this)
                        .load(urlImage)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdCover);

                    //第二条广告

                    final ImageView nativeAdCover2 =
                        (ImageView) mAdUnit2.findViewById(R.id.native_ad_cover);
                    final ImageView nativeAdIcon2 =
                        (ImageView) mAdUnit.findViewById(R.id.native_ad_icon);
                    final TextView nativeAdTitle2 =
                        (TextView) mAdUnit.findViewById(R.id.native_ad_title);
                    final TextView nativeAdBody2 =
                        (TextView) mAdUnit.findViewById(R.id.native_ad_body);

                    nativeAdBody2.setText(mNativeCampaign.get(1).getAppDesc());
                    nativeAdTitle2.setText(mNativeCampaign.get(1).getAppName());
                    //mobvista绑定点击事件
                    nativeHandle.registerView(nativeAdCover2, mNativeCampaign.get(1));
                    final String urlIcon2 = mNativeCampaign.get(1).getIconUrl();
                    Picasso.with(MainActivity.this)
                        .load(urlIcon2)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon2);
                    // Downloading and setting the ad icon.
                    final String urlImage2 = mNativeCampaign.get(1).getImageUrl();
                    Picasso.with(MainActivity.this)
                        .load(urlImage2)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdCover2);

                    //第三条广告
                    final ImageView nativeAdCover3 =
                        (ImageView) mAdUnit3.findViewById(R.id.native_ad_cover);
                    final ImageView nativeAdIcon3 =
                        (ImageView) mAdUnit3.findViewById(R.id.native_ad_icon);
                    final TextView nativeAdTitle3 =
                        (TextView) mAdUnit3.findViewById(R.id.native_ad_title);
                    final TextView nativeAdBody3 =
                        (TextView) mAdUnit3.findViewById(R.id.native_ad_body);

                    nativeAdBody3.setText(mNativeCampaign.get(2).getAppDesc());
                    nativeAdTitle3.setText(mNativeCampaign.get(2).getAppName());
                    //mobvista绑定点击事件
                    nativeHandle.registerView(nativeAdCover3, mNativeCampaign.get(2));
                    final String urlIcon3 = mNativeCampaign.get(2).getIconUrl();
                    Picasso.with(MainActivity.this)
                        .load(urlIcon3)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon3);
                    // Downloading and setting the ad icon.
                    final String urlImage3 = mNativeCampaign.get(2).getImageUrl();
                    Picasso.with(MainActivity.this)
                        .load(urlImage3)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdCover3);
                }

                @Override
                public void onAdLoadError(String message) {
                    Log.i("", "onAdLoadError : " + message);
                }

                @Override
                public void onAdClick(Campaign campaign) {

                }
            });

            //STEP3: Load native ad
            nativeHandle.load();
        } else {

            //STEP3: Load native ad
            nativeHandle.load();
        }
    }

    private void initWaveAnimation() {
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        mWaveSecondBg = (ImageView) findViewById(R.id.iv_wave_second_bg);
        wave_second_one = (ImageView) findViewById(R.id.wave_second_one);
        wave_second_two = (ImageView) findViewById(R.id.wave_second_two);
        mWaveFirstBg = (ImageView) findViewById(R.id.iv_wave_first_bg);
        wave_first_one = (ImageView) findViewById(R.id.wave_first_one);
        wave_first_two = (ImageView) findViewById(R.id.wave_first_two);

        second_one_anim = ValueAnimator.ofFloat(0, -screenWidth);
        second_one_anim.setInterpolator(new LinearInterpolator());
        second_one_anim.setRepeatCount(Animation.INFINITE);
        second_one_anim.setDuration(1000);

        second_two_anim = ValueAnimator.ofFloat(screenWidth, -screenWidth);
        second_two_anim.setInterpolator(new LinearInterpolator());
        second_two_anim.setRepeatCount(Animation.INFINITE);
        second_two_anim.setDuration(2000);

        second_one_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                if (repeatValue == 0) {
                    repeatValue = screenWidth;
                } else {
                    repeatValue = 0;
                }
            }
        });
        second_one_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                wave_second_one.setTranslationX(repeatValue + value);
                wave_first_one.setTranslationX(-repeatValue - value);
            }
        });
        second_one_anim.start();

        second_two_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }
        });
        second_two_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                wave_second_two.setTranslationX(value);
                wave_first_two.setTranslationX(-value);
            }
        });
        second_two_anim.start();
    }

    //初始化动画
    private void initAnimation() {

        initBigPicAnimation();
        initThreeDescparagraphAnimation();
        initRemindArrowAniamtion();
        initSmallPicAnimation();
        initExposeListViewAnimation();
        initNoProgressAnimation();


    }

    private void initExposeListViewAnimation() {

        //listView部分露出
        mTransition_part_end =
            (int) (getResources().getDimension(R.dimen.transition_part_end) + 0.5f);

        mOa_fl_child_include_list_part =
            ObjectAnimator.ofFloat(mFl_child_include_list, "translationY", 0,
                                   mTransition_part_end);
        mOa_fl_child_include_list_part.setDuration(mOa_fl_child_include_list_part_duration);

        mOa_fl_child_include_list_part.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                mFl_button_false.setVisibility(View.INVISIBLE);
                mFl_button_true.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        //listView全部露出

        mTansition_total_start =
            (int) (getResources().getDimension(R.dimen.tansition_total_start) + 0.5f);
        mTansition_total_end =
            (int) (getResources().getDimension(R.dimen.tansition_total_end) + 0.5f);

        mOa_fl_child_include_list_total =
            ObjectAnimator.ofFloat(mFl_child_include_list, "translationY",
                                   mTansition_total_start, mTansition_total_end);

        mOa_fl_child_include_list_total.setDuration(mOa_fl_child_include_list_total_duration);


    }

    private void initNoProgressAnimation() {

        mValueAnimator_progress = ValueAnimator.ofFloat(0, mLoopNo_total);
        mValueAnimator_progress.setDuration(mValueAnimator_progress_duration);

        mValueAnimator_progress.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float current_prigress = (Float) valueAnimator.getAnimatedValue();

                mTv_progress.setText((int) (current_prigress + 0.5f) + "");

            }
        });


    }

    private void initSmallPicAnimation() {
        mAa_small_desc = new AlphaAnimation(0, 1);
        mAa_small_desc.setDuration(mAa_small_desc_duration);
        mAa_small_desc.setFillAfter(true);
    }

    private void initRemindArrowAniamtion() {
        //箭头1渐入
        mAa_guide_arrow_1_in = new AlphaAnimation(0, 1);
        mAa_guide_arrow_1_in.setDuration(mAa_guide_arrow_1_in_duration);
        mAa_guide_arrow_1_in.setFillAfter(true);

        mAa_guide_arrow_1_in.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mGuide_arrow_2.startAnimation(mAa_guide_arrow_2_in);
            }
        });

        mAa_guide_arrow_1_out = new AlphaAnimation(1, 0);
        mAa_guide_arrow_1_out.setDuration(mAa_guide_arrow_1_out_duration);
        mAa_guide_arrow_1_out.setFillAfter(true);
        mAa_guide_arrow_1_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //guide_arrow_1.startAnimation(aa_guide_arrow_1_in);
                mGuide_arrow_2.startAnimation(mAa_guide_arrow_2_out);
            }
        });

		/*####################箭头二的系类操作###########################*/

        mAa_guide_arrow_2_in = new AlphaAnimation(0, 1);
        mAa_guide_arrow_2_in.setDuration(mAa_guide_arrow_2_in_duration);
        mAa_guide_arrow_2_in.setFillAfter(true);
        mAa_guide_arrow_2_in.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mGuide_arrow_3.startAnimation(mAa_guide_arrow_3_in);
            }
        });

        mAa_guide_arrow_2_out = new AlphaAnimation(1, 0);
        mAa_guide_arrow_2_out.setDuration(mAa_guide_arrow_2_out_duration);
        mAa_guide_arrow_2_out.setFillAfter(true);
        mAa_guide_arrow_2_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //guide_arrow_1.startAnimation(aa_guide_arrow_1_out);
                mGuide_arrow_3.startAnimation(mAa_guide_arrow_3_out);
            }
        });

		/*####################箭头三的系类操作###########################*/

        mAa_guide_arrow_3_in = new AlphaAnimation(0, 1);
        mAa_guide_arrow_3_in.setDuration(mAa_guide_arrow_3_in_duration);
        mAa_guide_arrow_3_in.setFillAfter(true);
        mAa_guide_arrow_3_in.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //guide_arrow_3.startAnimation(aa_guide_arrow_3_out);
                mGuide_arrow_1.startAnimation(mAa_guide_arrow_1_out);
            }
        });

        mAa_guide_arrow_3_out = new AlphaAnimation(1, 0);
        mAa_guide_arrow_3_out.setDuration(mAa_guide_arrow_3_out_duration);
        mAa_guide_arrow_3_out.setFillAfter(true);
        mAa_guide_arrow_3_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //guide_arrow_2.startAnimation(aa_guide_arrow_2_out);
                mGuide_arrow_1.startAnimation(mAa_guide_arrow_1_in);
            }
        });


    }

    //初始化描述的三段文字的动画
    public void initThreeDescparagraphAnimation() {

        int
            tansition_threeDesc_tv_size =
            (int) (getResources().getDimension(R.dimen.tansition_threeDesc_tv_size) + 0.5f);
        int
            tansition_threeDesc_tv_name =
            (int) (getResources().getDimension(R.dimen.tansition_threeDesc_tv_name) + 0.5f);
        int
            tansition_threeDesc_tv_desc =
            (int) (getResources().getDimension(R.dimen.tansition_threeDesc_tv_desc) + 0.5f);

        mAa_mAnim_tv_size = new AlphaAnimation(1, 0);
        mAa_mAnim_tv_size.setDuration(mAa_mAnim_tv_size_duration);
        mAa_mAnim_tv_size.setFillAfter(true);

        mAnim_tv_size =
            ObjectAnimator.ofFloat(mTv_size, "translationY", tansition_threeDesc_tv_size);
        mAnim_tv_size.setInterpolator(new BounceInterpolator());
        mAnim_tv_size.setDuration(mAnim_tv_size_duration);

        mAnim_tv_name =
            ObjectAnimator.ofFloat(mTv_name, "translationY", tansition_threeDesc_tv_name);
        mAnim_tv_name.setInterpolator(new BounceInterpolator());
        mAnim_tv_name.setStartDelay(mAnim_tv_name_delay);
        mAnim_tv_name.setDuration(mAnim_tv_name_duration);

        mAnim_tv_desc =
            ObjectAnimator.ofFloat(mTv_desc, "translationY", tansition_threeDesc_tv_desc);
        mAnim_tv_desc.setInterpolator(new OvershootInterpolator(4));
        mAnim_tv_desc.setStartDelay(mAnim_tv_desc_delay);
        mAnim_tv_desc.setDuration(mAnim_tv_desc_duration);

        mAnimatorSet_threeParagraph = new AnimatorSet();

        mAnimatorSet_threeParagraph.playTogether(mAnim_tv_size, mAnim_tv_name, mAnim_tv_desc);

        mAnimatorSet_threeParagraph.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mWaveSecondBg.setVisibility(View.INVISIBLE);
                wave_second_one.setVisibility(View.INVISIBLE);
                wave_second_two.setVisibility(View.INVISIBLE);
                mWaveFirstBg.setVisibility(View.INVISIBLE);
                wave_first_one.setVisibility(View.INVISIBLE);
                wave_first_two.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mRl_guide_arrow.setVisibility(View.VISIBLE);
                mGuide_arrow_1.startAnimation(mAa_guide_arrow_1_in);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    //初始化大图的动画
    public void initBigPicAnimation() {
        mTranstionEndX = (int) (getResources().getDimension(R.dimen.transtionEndX) + 0.5f);
        mTranstionEndY = (int) (getResources().getDimension(R.dimen.transtionEndY) + 0.5f);
        //位移+缩放

        mTa_root = new TranslateAnimation(0, mTranstionEndX, 0, mTranstionEndY);
        mSa_root = new ScaleAnimation(1.0f, 0.65f, 1.0f, 0.65f);

        mAs_root = new AnimationSet(false);
        mAs_root.addAnimation(mTa_root);
        mAs_root.addAnimation(mSa_root);
        mAs_root.setDuration(mAs_root_duration);
        mAs_root.setFillAfter(true);

        mAaInner_total = new AlphaAnimation(0, 1);
        // aaInner_total.setDuration(7000);
        mAaInner_total.setDuration(mAaInner_total_duration);

        //里面圈的动画效果
        mRaInner =
            new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);

        mRaInner.setDuration(mRaInner_duration);
        mRaInner.setFillAfter(true);

        mAaInner = new AlphaAnimation(1, 0);
        mAaInner.setDuration(mAaInner_duration);

        mAsInner = new AnimationSet(false);
        mAsInner.addAnimation(mRaInner);
        mAsInner.addAnimation(mAaInner);
        mAsInner.setFillAfter(true);
        mAsInner.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //iv_inner.setBackgroundResource(R.drawable.solid);
                        /*###################改变了这里###################*/
                mIv_inner.setVisibility(View.GONE);
            }
        });

        //外面圈的动画效果
        mRaOutter =
            new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
        mRaOutter.setDuration(mRaOutter_duration);
        //raOutter.setRepeatCount(Animation.INFINITE);

        mAaOutter = new AlphaAnimation(1, 0);
        mAaOutter.setDuration(mAaOutter_duration);
        mAaOutter.setFillAfter(true);

        mAsOutter = new AnimationSet(false);
        mAsOutter.addAnimation(mRaOutter);
        mAsOutter.addAnimation(mAaOutter);
        mAsOutter.setFillAfter(true);

        mAsOutter.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                /*###################改变了这里###################*/
                MIv_outter.setVisibility(View.INVISIBLE);

                mAnimatorSet_threeParagraph.start();

                //lisview出现
                mOa_fl_child_include_list_part.start();
            }
        });

    }

    //初始化监听事件
    private void initListener() {

        mRl_guide_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                doAnimation();
                returnAnimationIsDo = true;
            }
        });
    }

    //code  of qixiangxiang (end)
    private void init() {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mValueAnimator_progress.start();
                    }
                });

                Message mMessage_list = Message.obtain();

                items_total = MemoryUtil.getRunningAppInfos(MainActivity.this);//真实数据
                //过滤数据 分别将白名单和运行名单的数据 分门别类
                String packageNames = getIgnorePacknameFromSP();
                for (AppInfo appInfo : items_total) {
                    if (packageNames.contains(appInfo.getPackName())) {//是白名单的条目
                        appInfo.setIgonreApp(true);
                        appInfo.setIsCouldUse(true);
                        items_ignore.add(appInfo);
                    } else {//是运行的条目
                        appInfo.setIsCouldUse(true);
                        appInfo.setIgonreApp(false);
                        appInfo.setIsChecked(true);
                        items_running.add(appInfo);
                    }
                }

                items_current = items_total;
                items_total_size = items_total.size();
                items_ignore_size = items_ignore.size();
                items_running_size = items_running.size();

                //获取运行名单中APP所占的内存

                for (int i = 0; i < items_running.size(); i++) {
//                     j=i;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
////                            Toast.makeText(getApplicationContext(),
////                                           "大小：" + items_running.get(j).getMemorySize(),
////                                           Toast.LENGTH_SHORT).show();
//
////                            Toast.makeText(getApplicationContext(), "大小dada：" + mTotal_memory_size,
////                                           Toast.LENGTH_SHORT).show();
//                        }
//                    });

                    mTotal_memory_size += items_running.get(i).getMemorySize();
                }

                mMessage_list.what = FINISH;
                mHandler.sendMessage(mMessage_list);

            }
        });

    }


    private void initView() {

        mTv_Later = (TextView) findViewById(R.id.tv_Later);
        mTv_ok = (TextView) findViewById(R.id.tv_ok);
        mTv_Later.setOnClickListener(this);
        mTv_ok.setOnClickListener(this);
        mFl_total_upMove = (FrameLayout) findViewById(R.id.fl_total_upMove);
        mFl_button_false_finish = (FrameLayout) findViewById(R.id.fl_button_false_finish);
        db_dragBooster = (DragViewForBooster) findViewById(R.id.db_dragBooster);

        mFl_av = (FrameLayout) findViewById(R.id.fl_av);

        //清理完成之后的动画移动
        ll_firstChild_for_DragViewForBooster =
            (LinearLayout) findViewById(R.id.ll_firstChild_for_DragViewForBooster);
        mSc_after_clean = (ScrollView) findViewById(R.id.sc_after_clean);

        //清理了多少相关内容
        ll_detail_clean = (LinearLayout) findViewById(R.id.ll_detail_clean);
        mRl_clean_no_desc = (RelativeLayout) findViewById(R.id.rl_clean_no_desc);
        mTv_clean_desc = (TextView) findViewById(R.id.tv_clean_desc);
        ll_total_false = (LinearLayout) findViewById(R.id.ll_total_false);

        //大圆部分信息
        mTv_progress = (TextView) findViewById(R.id.tv_progress);

        //真假button的获取
        mFl_button_false = (FrameLayout) findViewById(R.id.fl_button_false);
        mBt_booster_false = (Button) findViewById(R.id.bt_booster_false);

        // mFl_button_true = (FrameLayout) findViewById(R.id.fl_button_true);
        mFl_button_true = (RelativeLayout) findViewById(R.id.fl_button_true);
        mBt_booster_true = (Button) findViewById(R.id.bt_booster_true);
        mBt_booster_true.setOnClickListener(this);
        //listview露出
        mFl_child_include_list = (FrameLayout) findViewById(R.id.fl_child_include_list);

        //小圆部分描述信息
        mLl_root_small_desc = (LinearLayout) findViewById(R.id.ll_root_small_desc);
        mTv_total_small = (TextView) findViewById(R.id.tv_total);
        mTv_small_desc = (TextView) findViewById(R.id.tv_small_desc);

        //提示小箭头
        mRl_guide_arrow = (RelativeLayout) findViewById(R.id.guide_arrow);
        mGuide_arrow_1 = (ImageView) findViewById(R.id.guide_arrow_1);
        mGuide_arrow_2 = (ImageView) findViewById(R.id.guide_arrow_2);
        mGuide_arrow_3 = (ImageView) findViewById(R.id.guide_arrow_3);

        // 三段文字
        mTv_size = (TextView) findViewById(R.id.tv_size);
        mTv_name = (TextView) findViewById(R.id.tv_name);
        mTv_desc = (TextView) findViewById(R.id.tv_desc);

        //大圆部分
        mIv_inner = (ImageView) findViewById(R.id.iv_inner);
        MIv_outter = (ImageView) findViewById(R.id.iv_outter);
        mIv_inner_total = (ImageView) findViewById(R.id.iv_inner_total);
        mFl_root = (RelativeLayout) findViewById(R.id.fl_root);

        //点击动画开始小箭头
        mRl_guide_arrow = (RelativeLayout) findViewById(R.id.guide_arrow);


        /*#########第二次提交修改 start ###########*/
        mRl_ignore_label = (RelativeLayout) findViewById(R.id.rl_ignore_label);
        tv_ignore_size_label = (TextView) findViewById(R.id.tv_ignore_size_label);

        mLl_running_label_first = (RelativeLayout) findViewById(R.id.ll_running_label_first);
        ck_running_first = (CheckBox) findViewById(R.id.ck_running_first);
        tv_running_manager_first = (TextView) findViewById(R.id.tv_running_manager_first);
        tv_running_total_first = (TextView) findViewById(R.id.tv_running_total_first);

        ll_runnuing_label_second = (RelativeLayout) findViewById(R.id.ll_runnuing_label_second);
        ck_running_second = (CheckBox) findViewById(R.id.ck_running_second);
        tv_running_manager_second = (TextView) findViewById(R.id.tv_running_manager_second);
        tv_running_total_second = (TextView) findViewById(R.id.tv_running_total_second);

        tv_line = (TextView) findViewById(R.id.tv_line);
//        /*#########第二次提交修改 end ###########*/

        mLv_1 = (ListView) findViewById(R.id.lv_1);
        mNanoboosterAdpter = new NanoboosterAdpter(MainActivity.this);
        // 辅助功能弹框提示
        mAccessibilityDialog = (FrameLayout) findViewById(R.id.fl_accessibility_dialog);
        mAccessibilityAlertDialog =
            (RelativeLayout) findViewById(R.id.rl_accessibility_alert_dialog);
        mAccessibilityAlertDialogBg =
            (ImageView) findViewById(R.id.iv_accessibility_alert_dialog_bg);
        mAccessibilityEnableNowBtn = (RippleView) findViewById(R.id.btn_accessibility_enable_now);
        mAccessibilityEnableNowBtn.setOnClickListener(this);

        //弹框评价
        mRate_alert_dialog = (FrameLayout) findViewById(R.id.fl_rate_alert_dialog);

        //mRate_alert_dialog.setVisibility(View.VISIBLE);

    }

    private void initData() {

        //设置内存使用情况
        float totalMemory = MemoryUtil.getTotalMemory(this) / 1024f / 1024f;
        totalMemory = (float) (Math.round(totalMemory * 100)) / 100;

        float used = MemoryUtil.getUsedMemory(this) / 1024f / 1024f;
        used = (float) (Math.round(used * 100)) / 100;
        mTv_size.setText(used + "GB" + "/" + totalMemory + "GB");
        String
            tv_progress_value =
            MemoryUtil.getUsedPercentValue(this)
                .substring(0, MemoryUtil.getUsedPercentValue(this).length() - 1);
        mTv_progress.setText(tv_progress_value);
        mLoopNo = Integer.parseInt(tv_progress_value.substring(0, 1));
        mLoopNo_total = Integer.parseInt(tv_progress_value);

        mTv_small_desc.setText(used + "GB " + "/ " + totalMemory + "GB");
        //initListView();
    }


    public void initListView() {
        mNanoboosterAdpter = new NanoboosterAdpter(MainActivity.this);
                  /*###########第二次提交修改 start##############*/
        tv_ignore_size_label.setText(items_ignore_size + "");

        tv_running_manager_first.setText(items_running_size + "");
        tv_running_total_first.setText("(" + items_total_size + ")");

        tv_running_manager_second.setText(items_running_size + "");
        tv_running_total_second.setText("(" + items_total_size + ")");

         /*###########第二次提交修改 end  ##############*/

        mLv_1.setAdapter(mNanoboosterAdpter);

        mLv_1.setOnScrollListener(new AbsListView.OnScrollListener() {//实现动态布局的改变
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if (!ignore && !running) {
                    if (mLv_1.getFirstVisiblePosition() >= items_ignore.size()) {
                        tv_line.setVisibility(View.VISIBLE);
                        mLl_running_label_first.setVisibility(View.VISIBLE);
                        tv_running_manager_first.setText(items_running_size + "");
                        tv_running_total_first.setText("(" + items_total_size + ")");


                    }
                    if (mLv_1.getFirstVisiblePosition() < items_ignore.size()) {
                        tv_line.setVisibility(View.GONE);
                        mLl_running_label_first.setVisibility(View.GONE);

                    }
                    if (mLv_1.getLastVisiblePosition() >= items_ignore.size() + 1) {
                        ll_runnuing_label_second.setVisibility(View.GONE);

                    }
                    if (mLv_1.getLastVisiblePosition() <= items_ignore.size() + 1) {
                        ll_runnuing_label_second.setVisibility(View.VISIBLE);
                        tv_running_manager_second.setText(items_running_size + "");
                        tv_running_total_second.setText("(" + items_total_size + ")");

                    }
                    if (items_running_size == 0) {
                        ll_runnuing_label_second.setVisibility(View.GONE);
                    }
                }
            }
        });

        mLv_1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if ((ignore == false) && (running == false)) {//全部显示

                    if (position == 0) {//白名单头标题
                        //ignore = !ignore;
                    }
                    if (position == items_ignore.size() + 1) {//运行名单头标题
                        // running = !running;
                    }
                    if (position > items_ignore.size() + 1
                        && position < items_current.size() + 2) {//运行条目
                        //点击的就是内容条目
                        mNanoboosterAdpter.updateItemView(view, position);//实现局部的刷新

                    }
                    if (position < items_ignore.size() + 1 && position > 0) {
                        // TO DO
                    }

                }

            }
        });

    }

    public void changeIngoreAndRuningSet(AppInfo app) {

        //对两个集合进行操作了
        // mIterms_Running, mIterms_Ignore, mIterms_total,mIterms_delFrom_running
        if (app != null) {
            AppInfo af = new AppInfo();
            if (items_ignore.contains(app)) {

                af = app;

                items_ignore.remove(app);
                af.setIgonreApp(false);
                af.setIsChecked(true);
                af.setIsCouldUse(true);

                items_running.add(0, af);
                items_total.clear();
                items_total.addAll(items_ignore);
                items_total.addAll(items_running);

            }
        } else {

            if (items_delFrom_running != null && items_delFrom_running.size() > 0) {
                for (AppInfo appInfo : items_delFrom_running) {
                    if (items_running.contains(appInfo)) {

                        items_running.remove(appInfo);
                    }
                    if (!items_ignore.contains(appInfo)) {
                        appInfo.setIsCouldUse(true);
                        appInfo.setIgonreApp(true);
                        appInfo.setIsChecked(false);
                        items_ignore.add(0, appInfo);

                    }
                }
            }
            items_total.clear();
            items_total.addAll(items_ignore);
            items_total.addAll(items_running);
            items_delFrom_running = null;
            items_delFrom_running = new ArrayList<AppInfo>();
            /*###########改变############*/
//            mNanoboosterAdpter.notifyDataSetChanged();
            if (mNanoboosterAdpter != null) {
                mNanoboosterAdpter.notifyDataSetChanged();
            }
        }
        /*###########改变############*/
        // myadpter.notifyDataSetChanged();

    }


    public void selectAll(View view) {
            /*###########改变##############*/
        //running = !running;
        CheckBox cb = (CheckBox) view;
        isTitleChecked = !isTitleChecked;
        cb.setChecked(isTitleChecked);

        if (cb.isChecked()) {
            getRunningItemSize();//首先将当前选中条目的个数计算出来
            for (AppInfo app : items_running) {
                if (!app.isIgonreApp()) {
                    app.setIsChecked(true);
                }
            }
        }

        /*###########改变##############*/
        //mMyAdpter.notifyDataSetChanged();
    }

    /*###########第二次提交修改 start##############*/
    public void selectAll_first(View view) {
        selectAll(mNanoboosterAdpter.ck_running);
        SystemClock.sleep(200);
        mNanoboosterAdpter.notifyDataSetChanged();
    }

    public void selectAll_second(View view) {
        selectAll(mNanoboosterAdpter.ck_running);
    }

    /*###########第二次提交修改 end  ##############*/


    public void getRunningItemSize() {
        int temp = 0;
        for (AppInfo appInfo : items_running) {
            if (!appInfo.isChecked() && appInfo.isCouldUse()) {
                temp++;
            }
        }

        items_running_size += temp;
    }


    public void putIgnorePackname2SP(String appPackname) {
        if (mSp == null) {
            mSp = getSharedPreferences("config_packageName", MODE_PRIVATE);
        }

        String packageNames = mSp.getString("packageNames", "");

        if (!packageNames.contains(appPackname)) {
            appPackname = packageNames + appPackname + ",";
            mSp.edit().putString("packageNames", appPackname).commit();
        }

    }


    public void updateIgore(String packageNames) {
        if (mSp == null) {
            mSp = getSharedPreferences("config_packageName", MODE_PRIVATE);
        }

        mSp.edit().putString("packageNames", packageNames).commit();
    }

    public String getIgnorePacknameFromSP() {
        if (mSp == null) {
            mSp = getSharedPreferences("config_packageName", MODE_PRIVATE);
        }

        return mSp.getString("packageNames", "");
    }


    public void jumpToSecondActivity(View view) {
        Intent intent = new Intent(this, SecondTestActivity.class);
        startActivity(intent);
    }

    public void deleteFromSP(AppInfo appInfo) {

        String currentPackage = appInfo.getPackName() + ",";
        String packageNames = getIgnorePacknameFromSP();
        if (packageNames.contains(currentPackage)) {

            //int totalLen = packageNames.length();

            String firstPart = "";
            String secondPart = "";

            int firstEnd = packageNames.indexOf(currentPackage);
            int secondStart = firstEnd + currentPackage.length();

            firstPart = packageNames.substring(0, firstEnd);//不包括最后一个
            secondPart = packageNames.substring(secondStart);

            packageNames = firstPart + secondPart;

            updateIgore(packageNames);
        }
    }


    public boolean runningItermIsChecked() {

        for (AppInfo appInfo : items_running) {
            if (!(appInfo.isChecked() && appInfo.isCouldUse())) {
                return false;
            }
        }

        return true;
    }

    //code  of qixiangxiang (end)
    @Override
    protected void onResume() {
        super.onResume();

        //code of xiangxiang (start)
        if (mNanoboosterAdpter != null) {
            changeIngoreAndRuningSet(null);

            if (mNanoboosterAdpter.ck_running != null) {
                isTitleChecked = runningItermIsChecked();
                mNanoboosterAdpter.ck_running.setChecked(isTitleChecked);
            }
        }
        //code of xiangxiang (end)

        // 如果辅助功能已经打开，并且弹框显示，则隐藏弹框
        if (AccessibilityServiceUtil.isAccessibleEnabled(getApplicationContext())) {
            if (mAccessibilityDialog.getVisibility() == View.VISIBLE) {
                mAccessibilityDialog.setVisibility(View.GONE);
            }
        }


    }

    public static void showRate_alert_dialog() {

        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRate_alert_dialog.setVisibility(View.VISIBLE);
                ObjectAnimator translateYAnim =
                    ObjectAnimator.ofFloat(mRate_alert_dialog, "translationY",
                                           mRate_alert_dialog.getMeasuredHeight(),
                                           0);//Y轴平移旋转
                ObjectAnimator alphaAnim =
                    ObjectAnimator.ofFloat(mRateAlertDialog_bg, "alpha", 0, 1);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(translateYAnim, alphaAnim);
                set.setDuration(2000);
                set.setInterpolator(new DecelerateInterpolator());
                set.start();
            }
        }, 1000);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.titlebar_icon:
                if (flag) {
                    flag = false;
                    ObjectAnimator
                        oa_1 =
                        ObjectAnimator
                            .ofFloat(mLinearLayout, "translationX", 0, mLinearLayout.getWidth());
                    oa_1.setDuration(duration);
                    oa_1.start();

                    Animation alphaAnimation = new AlphaAnimation(anim1, anim2);
                    alphaAnimation.setDuration(duration);
                    alphaAnimation.setFillAfter(true);
                    //mIv_background.startAnimation(alphaAnimation);

                } else {
                    flag = true;
                    ObjectAnimator
                        oa_2 =
                        ObjectAnimator
                            .ofFloat(mLinearLayout, "translationX", mLinearLayout.getWidth(), 0);
                    oa_2.setDuration(duration);
                    oa_2.start();

                    Animation alphaAnimation = new AlphaAnimation(anim2, anim1);
                    alphaAnimation.setDuration(duration);
                    alphaAnimation.setFillAfter(true);
                    //mIv_background.startAnimation(alphaAnimation);

                }

                break;
            case R.id.titlebar_icon_false:
                //Toast.makeText(getApplicationContext(), "进来", Toast.LENGTH_SHORT).show();

                if (flag) {
                    flag = false;
                    ObjectAnimator
                        oa_1 =
                        ObjectAnimator
                            .ofFloat(mLinearLayout, "translationX", 0, mLinearLayout.getWidth());
                    oa_1.setDuration(duration);
                    oa_1.start();

                    Animation alphaAnimation = new AlphaAnimation(anim1, anim2);
                    alphaAnimation.setDuration(duration);
                    alphaAnimation.setFillAfter(true);
                    //mIv_background.startAnimation(alphaAnimation);

                } else {
                    flag = true;
                    //  rl_view.getBackground().setAlpha(60);
                    ObjectAnimator
                        oa_2 =
                        ObjectAnimator
                            .ofFloat(mLinearLayout, "translationX", mLinearLayout.getWidth(), 0);
                    oa_2.setDuration(duration);
                    oa_2.start();

                    Animation alphaAnimation = new AlphaAnimation(anim2, anim1);
                    alphaAnimation.setDuration(duration);
                    alphaAnimation.setFillAfter(true);
                    //mIv_background.startAnimation(alphaAnimation);

                }

                break;
            case R.id.ll_setting:
                break;
            case R.id.ll_update:
                break;
            case R.id.ll_feedback:
                break;
            case R.id.ll_about:
                break;
            case R.id.ll_good:
                break;
            case R.id.guide_arrow:
                //Toast.makeText(getApplicationContext(),"进来了",Toast.LENGTH_LONG).show();

                break;

            case R.id.bt_booster_true:
                if (AccessibilityServiceUtil.isAccessibleEnabled(getApplicationContext())) {

                    if (returnAnimationIsDo) {//做过动画
                        reversweAnimation();

                    } else {//没有做动画
                        if (items_running.size() > 0) {
                            showDeepCleanWindow();
                        }
                    }

                } else {
                    mAccessibilityDialog.setVisibility(View.VISIBLE);

                    ObjectAnimator translateYAnim =
                        ObjectAnimator.ofFloat(mAccessibilityAlertDialog, "translationY",
                                               mAccessibilityAlertDialog.getMeasuredHeight(),
                                               0);//Y轴平移旋转
                    ObjectAnimator alphaAnim =
                        ObjectAnimator.ofFloat(mAccessibilityAlertDialogBg, "alpha", 0, 1);

                    AnimatorSet set = new AnimatorSet();
                    set.playTogether(translateYAnim, alphaAnim);
                    set.setDuration(mAccessibilityAnimDuration);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.start();
                }

                break;

            case R.id.btn_accessibility_enable_now:
                AccessibilityServiceUtil.openAccessibilitySetting(MainActivity.this);
                break;
            case R.id.tv_Later:
                mRate_alert_dialog.setVisibility(View.GONE);
                break;

            case R.id.tv_ok:
                mRate_alert_dialog.setVisibility(View.GONE);
                break;
        }
    }


    private void showDeepCleanWindow() {
        BoosterAccessibilityService.isDeepCleaning = true;
        Intent intent =
            new Intent(getApplicationContext(), FloatWindowService.class);
        intent.putExtra("showDeepCleanWindow", true);
        startService(intent);
        runningAppSize = items_running.size();
        MemoryUtil.deepCleanMemory(MainActivity.this, items_running);
    }

    private void removeDeepCleanWindow() {
        if (BoosterAccessibilityService.isDeepCleaning) {
            BoosterAccessibilityService.isDeepCleaning = false;
            Intent intent = new Intent(getApplicationContext(), FloatWindowService.class);
            intent.putExtra("removeDeepCleanWindow", true);
            startService(intent);
        }
    }

    //开始初始动画
    private void startAnimation() {

        mIv_inner.startAnimation(mAsInner);
        mIv_inner_total.startAnimation(mAaInner_total);
        MIv_outter.startAnimation(mAsOutter);

    }

    @Override
    public void onBackPressed() {
        removeDeepCleanWindow();

        if (mAccessibilityDialog.getVisibility() == View.VISIBLE) {
            mAccessibilityDialog.setVisibility(View.GONE);
            return;
        }

        if (System.currentTimeMillis() - mExitTime > 2000) {
            Toast.makeText(this, R.string.exit_warn, Toast.LENGTH_SHORT)
                .show();
            mExitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    public void doAnimation() {

        //大圆动画完成之后需要执行以下动画

        //大图位移与缩放
        mIv_inner.clearAnimation();
        mIv_inner_total.clearAnimation();
        MIv_outter.clearAnimation();
        mFl_root.startAnimation(mAs_root);

        //小圆部分的描述信息的渐变-->显示
        mLl_root_small_desc.startAnimation(mAa_small_desc);

        //三段文字描述信息的 渐变-->隐藏
        mTv_size.startAnimation(mAa_mAnim_tv_size);

        //提示小箭头的隐藏

        //listView全部显示出来
        mOa_fl_child_include_list_total.start();


    }

    /**
     * 判断是否有快捷方式
     */
    private static boolean hasShortcut(Context context) {
        String AUTHORITY =
            getAuthorityFromPermission(context, "com.android.launcher.permission.READ_SETTINGS");
        System.out.println(" AUTHORITY ..." + AUTHORITY);
        if (AUTHORITY == null) {
            return false;
        }
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        String title = "";
        final PackageManager packageManager = context.getPackageManager();
        try {
            title = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(context.getPackageName(),
                                                  PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Cursor c = context.getContentResolver().
                query(CONTENT_URI, new String[]{"title"}, "title=?", new String[]{title}, null);
            if (c != null) {
                c.close();
                if (c.getCount() > 0) {
                    return true;
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return false;
    }

    private static String getAuthorityFromPermission(Context context, String permission) {
        if (TextUtils.isEmpty(permission)) {
            return null;
        }
        List<PackageInfo> packageInfoList =
            context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        if (packageInfoList == null) {
            return null;
        }
        for (PackageInfo packageInfo : packageInfoList) {
            ProviderInfo[] providerInfos = packageInfo.providers;
            if (providerInfos != null) {
                for (ProviderInfo providerInfo : providerInfos) {
                    if (permission.equals(providerInfo.readPermission) ||
                        permission.equals(providerInfo.writePermission)) {
                        return providerInfo.authority;
                    }
                }
            }
        }
        return null;
    }

    public static void addShortcutToDesktop(Context context) {
        if (!hasShortcut(context)) {
            Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            shortcutIntent.putExtra("duplicate", false);

            shortcutIntent
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                    Intent.ShortcutIconResource.fromContext(context,
                                                                            R.mipmap.ic_launcher));

//            shortcutIntent.putExtra("duplicate", false);
//            Intent intent = new Intent(context, MainActivity.class);
//            intent.setAction("android.intent.action.MAIN");
//            intent.addCategory("android.intent.category.LAUNCHER");
//            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
//            context.sendBroadcast(shortcutIntent);
            // 点击快捷图片，运行的程序主入口
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                                    new Intent(context.getApplicationContext(),
                                               context.getClass()));
            // 发送广播
            context.sendBroadcast(shortcutIntent);
        }
    }

    private void reversweAnimation() {

        mTv_size.setTranslationY(5f);
        mTv_name.setTranslationY(5f);
        mTv_desc.setVisibility(View.GONE);
//        ll_detail_clean.setVisibility(View.VISIBLE);
        ll_detail_clean.setTranslationY(-60.f);
        //部分需要隐藏的信息
        mTv_desc.setVisibility(View.INVISIBLE);
        mRl_guide_arrow.setVisibility(View.INVISIBLE);

        //小圆部分的描述信息 大圆圈的reverse

        mLl_root_small_desc.startAnimation(mAa_small_desc_reverse);

        //ListView 部分的reverse

        mOa_fl_child_include_list_total_reverse.start();

        //booster按钮回移不可见

        mOfl_button_true_reverse =
            ObjectAnimator.ofFloat(mFl_button_true, "translationY",
                                   mFl_button_true.getMeasuredHeight());
        mOfl_button_true_reverse.start();

        //大圆圈的reverse

        mFl_root.startAnimation(mAs_root_reverse);

        //波浪出来

        mTv_size.startAnimation(mAa_mAnim_tv_size_reverse);

        mTv_name.startAnimation(mAa_mAnim_tv_name_reverse);

//        //大圆继续做旋转动画

    }

    //初始化返回动画
    public void initReturnAnimation() {
        //小圆部分的描述信息 大圆圈的reverse
        mAa_small_desc_reverse = new AlphaAnimation(1, 0);
        mAa_small_desc_reverse.setFillAfter(true);
        mAa_small_desc.setDuration(mAa_small_desc_duration);

        //ListView 部分的reverse
        mOa_fl_child_include_list_total_reverse =
            ObjectAnimator.ofFloat(mFl_child_include_list, "translationY",
                                   mTansition_total_end,
                                   mTansition_total_start - mTransition_part_end);

        //大圆圈的reverse
        mTa_root_reverse =
            new TranslateAnimation(mTranstionEndX, 0, mTranstionEndY, 0);
        mSa_root_reverse = new ScaleAnimation(0.65f, 1.0f, 0.65f, 1.0f);

        mAs_root_reverse = new AnimationSet(false);
        mAs_root_reverse.addAnimation(mTa_root_reverse);
        mAs_root_reverse.addAnimation(mSa_root_reverse);
        mAs_root_reverse.setDuration(mAs_root_duration);
        mAs_root_reverse.setFillAfter(true);
        mAs_root_reverse.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Toast.makeText(getApplicationContext(), "大圈位移动画结束", Toast.LENGTH_SHORT).show();

                if (items_running.size() > 0) {
                    showDeepCleanWindow();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //大圆下描述信息出来
        mAa_mAnim_tv_size_reverse = new AlphaAnimation(0, 1);
        mAa_mAnim_tv_size_reverse.setDuration(mAa_mAnim_tv_size_duration);
        mAa_mAnim_tv_size_reverse.setFillAfter(true);

        mAa_mAnim_tv_name_reverse = new AlphaAnimation(0, 1);
        mAa_mAnim_tv_name_reverse.setDuration(mAa_mAnim_tv_size_duration);
        mAa_mAnim_tv_name_reverse.setFillAfter(true);


    }

    public void startCleanAnimation() {

        if (!returnAnimationIsDo) {//做过动画
            //隐藏15app描述信息
            //隐藏小箭头
            //隐藏listview
            //隐藏booster
            mTv_desc.setVisibility(View.INVISIBLE);
            mRl_guide_arrow.setVisibility(View.INVISIBLE);
            mFl_child_include_list.setVisibility(View.INVISIBLE);
            mFl_button_true.setVisibility(View.INVISIBLE);

        }
        mIv_inner.startAnimation(mAsInner_reverse);
        mIv_inner_total.startAnimation(mAaInner_total_reverse);
        MIv_outter.startAnimation(mAsOutter_reverse);
    }

    //初始化清理动画
    public void initCleanAnimation() {

        //大圆继续做旋转动画

        mAaInner_total_reverse = new AlphaAnimation(0, 1);
        // aaInner_total.setDuration(7000);
        mAaInner_total_reverse.setDuration(mAaInner_total_duration);

        //里面圈的动画效果
        mRaInner_reverse =
            new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);

        mRaInner_reverse.setDuration(mRaInner_duration);
        mRaInner_reverse.setFillAfter(true);

        mAaInner_reverse = new AlphaAnimation(1, 0);
        mAaInner_reverse.setDuration(mAaInner_duration);

        mAsInner_reverse = new AnimationSet(false);
        mAsInner_reverse.addAnimation(mRaInner_reverse);
        mAsInner_reverse.addAnimation(mAaInner_reverse);
        mAsInner_reverse.setFillAfter(true);
        mAsInner_reverse.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //iv_inner.setBackgroundResource(R.drawable.solid);
                        /*###################改变了这里###################*/
                mIv_inner.setVisibility(View.GONE);
            }
        });

        //外面圈的动画效果
        mRaOutter_reverse =
            new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
        mRaOutter_reverse.setDuration(mRaOutter_duration);
        //raOutter.setRepeatCount(Animation.INFINITE);

        mAaOutter_reverse = new AlphaAnimation(1, 0);
        mAaOutter_reverse.setDuration(mAaOutter_duration);
        mAaOutter_reverse.setFillAfter(true);

        mAsOutter_reverse = new AnimationSet(false);
        mAsOutter_reverse.addAnimation(mRaOutter_reverse);
        mAsOutter_reverse.addAnimation(mAaOutter_reverse);
        mAsOutter_reverse.setFillAfter(true);

        mAsOutter_reverse.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mValueAnimator_progress_reverse.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                /*###################改变了这里###################*/
//                MIv_outter.setVisibility(View.INVISIBLE);
//
//                mAnimatorSet_threeParagraph.start();
//
//                //lisview出现
//                mOa_fl_child_include_list_part.start();

                ll_detail_clean.setVisibility(View.VISIBLE);
                mTv_clean_desc.setVisibility(View.INVISIBLE);
                mRl_clean_no_desc.startAnimation(mAaCleanAnimation_rl_clean_no_desc);

            }
        });

        Random rd = new Random();
        int rd_no = 10 + rd.nextInt(10);
        mValueAnimator_progress_reverse = ValueAnimator.ofFloat(mLoopNo_total, rd_no);
        mValueAnimator_progress_reverse.setDuration(mValueAnimator_progress_duration);

        mValueAnimator_progress_reverse
            .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Float current_prigress = (Float) valueAnimator.getAnimatedValue();

                    mTv_progress.setText((int) (current_prigress + 0.5f) + "");

                }
            });

        //清理了多少相关内容的渐变
        mAaCleanAnimation_rl_clean_no_desc = new AlphaAnimation(0, 1);
        mAaCleanAnimation_rl_clean_no_desc.setDuration(500);
        mAaCleanAnimation_rl_clean_no_desc.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTv_clean_desc.setVisibility(View.VISIBLE);
                mTv_clean_desc.startAnimation(mAaCleanAnimation_tv_clean_desc);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mAaCleanAnimation_tv_clean_desc = new AlphaAnimation(0, 1);
        mAaCleanAnimation_tv_clean_desc.setDuration(500);
        mAaCleanAnimation_tv_clean_desc.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                SystemClock.sleep(500);
                // Toast.makeText(getApplicationContext(), "在这里做位移动画", Toast.LENGTH_SHORT).show();
                animationAfterMemoryClean();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void animationAfterMemoryClean() {
        mAa_aninamtio_finish = new AlphaAnimation(0, 1);
        mAa_aninamtio_finish.setDuration(1000);
        mAa_aninamtio_finish.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        mTv_size.setTranslationY(-40f);
//        mTv_name.setTranslationY(-40f);

        mTv_size.setTranslationY(
            (int) (getResources().getDimension(R.dimen.tv_size_afterCleanAnimation) + 0.5f));
        mTv_name.setTranslationY(
            (int) (getResources().getDimension(R.dimen.tv_name_afterCleanAnimation) + 0.5f));

//        mOa_mFl_av = ObjectAnimator.ofFloat(mFl_av,"translationY",0,-40);//-64
        mOa_mFl_av =
            ObjectAnimator.ofFloat(mFl_av, "translationY", 0, (int) (
                getResources().getDimension(R.dimen.fl_av_afterCleanAnimation) + 0.5f));//-64

        mOa_mFl_av.setDuration(1000);

//        mOa_ll_detail_clean = ObjectAnimator.ofFloat(ll_detail_clean, "translationY", 0, 5f);
        mOa_ll_detail_clean =
            ObjectAnimator.ofFloat(ll_detail_clean, "translationY", 0, (int) (
                getResources().getDimension(R.dimen.ll_detail_clean_afterCleanAnimation) + 0.5f));

        mOa_ll_detail_clean.setDuration(200);
        mOa_ll_detail_clean.setInterpolator(new CycleInterpolator(1));
        mOa_ll_detail_clean.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                //ll_detail_clean.setVisibility(View.INVISIBLE);

                ll_total_false.setVisibility(View.VISIBLE);
                mFl_total_upMove.setVisibility(View.VISIBLE);
                ll_detail_clean.setVisibility(View.INVISIBLE);

                mFl_button_false_finish.setVisibility(View.VISIBLE);

                mFl_button_false_finish.startAnimation(mAa_aninamtio_finish);

                //加载广告
                loadMobvistaNative();
                //mSc_after_clean.scrollTo(0,40);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        // ll_firstChild_for_DragViewForBooster mSc_after_clean
        // ObjectAnimator oa_animation_ll_firstChild_for_DragViewForBooster= ObjectAnimator.ofFloat(ll_firstChild_for_DragViewForBooster,"translationY",0,-ll_firstChild_for_DragViewForBooster.getMeasuredHeight()+(ll_firstChild_for_DragViewForBooster.getMeasuredHeight()-ll_detail_clean.getTop()));

        //Toast.makeText(getApplicationContext(),"top:"+ll_detail_clean.getTop(),Toast.LENGTH_LONG).show();
//        Toast.makeText(getApplicationContext(),
//                       "height:" + ll_firstChild_for_DragViewForBooster.getHeight(),
//                       Toast.LENGTH_LONG).show();
        //ll_detail_clean

//        int move = ll_firstChild_for_DragViewForBooster.getHeight()-370;
        int
            move =
            ll_firstChild_for_DragViewForBooster.getHeight() - (int) (
                getResources().getDimension(R.dimen.move_first_child) + 0.5f);
        //ObjectAnimator oa_animation_ll_firstChild_for_DragViewForBooster= ObjectAnimator.ofFloat(ll_firstChild_for_DragViewForBooster,"translationY",0,-move);
        ObjectAnimator
            oa_animation_ll_firstChild_for_DragViewForBooster =
            ObjectAnimator.ofFloat(ll_firstChild_for_DragViewForBooster, "translationY", 0, -move);
        oa_animation_ll_firstChild_for_DragViewForBooster.setDuration(1000);
        oa_animation_ll_firstChild_for_DragViewForBooster.addListener(
            new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    // ll_detail_clean.setTranslationY(5f);
                    mOa_ll_detail_clean.start();

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        // oa_animation_ll_firstChild_for_DragViewForBooster.start();

        // ObjectAnimator oa_animation_mSc_after_clean= ObjectAnimator.ofFloat(mSc_after_clean,"translationY",0,-ll_firstChild_for_DragViewForBooster.getMeasuredHeight()+84-6);

        //int move_second_child = (int)(getResources().getDimension(R.dimen.move_second_child)+0.5f);
        int
            move_second_child =
            -move - (int) (getResources().getDimension(R.dimen.move_second_child) + 0.5f);
//        ObjectAnimator oa_animation_mSc_after_clean= ObjectAnimator.ofFloat(mSc_after_clean,"translationY",0,-move+58-40);
        ObjectAnimator
            oa_animation_mSc_after_clean =
            ObjectAnimator.ofFloat(mSc_after_clean, "translationY", 0, move_second_child);
        oa_animation_mSc_after_clean.setDuration(1000);
        oa_animation_mSc_after_clean.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // ll_total_false.setVisibility(View.VISIBLE);
                // ll_detail_clean.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        //oa_animation_mSc_after_clean.start();

        AnimatorSet as_after_clean = new AnimatorSet();
        as_after_clean.playTogether(oa_animation_ll_firstChild_for_DragViewForBooster,
                                    oa_animation_mSc_after_clean, mOa_mFl_av);
        as_after_clean.start();
    }

}
