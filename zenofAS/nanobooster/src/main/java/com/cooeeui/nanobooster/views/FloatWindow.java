package com.cooeeui.nanobooster.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cooeeui.nanobooster.R;
import com.cooeeui.nanobooster.broadcast.FloatWindowFinishReceiver;
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

/**
 * Created by hugo.Ye on 2016/4/15. 单实例对象实现
 */
final public class FloatWindow {

    private static final String TAG = FloatWindow.class.getSimpleName();

    private static FloatWindow sInstance;

    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;

    private LayoutInflater mLayoutInflater;
    private DragViewForBooster mFloatLayout;

    // code of xiangxiang start
    private ListView mLv_1;
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


    // ad begin
    private final int mAdCountMax = 3;
    private RelativeLayout mAdUnit;
    private RelativeLayout mAdUnit2;
    private RelativeLayout mAdUnit3;
    // Mobvista ad begin
    private MvNativeHandler nativeHandle;
    private ArrayList<Campaign> mNativeCampaign = new ArrayList<>();
    // Mobvista ad end
    private Button mBt_booster_finish;
    //清理动画的初始化

    // code of xiangxiang end

    private FloatWindow() {
    }

    public static FloatWindow getInstance() {
        if (sInstance == null) {
            sInstance = new FloatWindow();
        }

        return sInstance;
    }

    public void initial(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 23) {  // android 6.0
            mWindowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;

//        initViews();
//        initAnimation();

    }

    private void initViews() {
        mFloatLayout =
            (DragViewForBooster) mLayoutInflater.inflate(R.layout.drag_for_booster_view, null);
        initMobvista();

        //波浪
//        mLl_first_wave= (LinearLayout)mFloatLayout.findViewById(R.id.ll_first_wave);
//        mLl_second_wave= (LinearLayout)mFloatLayout.findViewById(R.id.ll_second_wave);
//        mLl_second_wave.setVisibility(View.VISIBLE);
//        mLl_first_wave.setVisibility(View.VISIBLE);

        /*
        *
        * 初始化view
        * */

        //广告
        mAdUnit = (RelativeLayout) mFloatLayout.findViewById(R.id.ad_unit);
        mAdUnit2 = (RelativeLayout) mFloatLayout.findViewById(R.id.ad_unit2);
        mAdUnit3 = (RelativeLayout) mFloatLayout.findViewById(R.id.ad_unit3);

        mFl_total_upMove = (FrameLayout) mFloatLayout.findViewById(R.id.fl_total_upMove);
        mFl_button_false_finish = (FrameLayout) mFloatLayout.findViewById(
            R.id.fl_button_false_finish);
        db_dragBooster = (DragViewForBooster) mFloatLayout.findViewById(R.id.db_dragBooster);

        mFl_av = (FrameLayout) mFloatLayout.findViewById(R.id.fl_av);

        //清理完成之后的动画移动
        ll_firstChild_for_DragViewForBooster =
            (LinearLayout) mFloatLayout.findViewById(R.id.ll_firstChild_for_DragViewForBooster);
        mSc_after_clean = (ScrollView) mFloatLayout.findViewById(R.id.sc_after_clean);

        //清理了多少相关内容
        ll_detail_clean = (LinearLayout) mFloatLayout.findViewById(R.id.ll_detail_clean);
        mRl_clean_no_desc = (RelativeLayout) mFloatLayout.findViewById(R.id.rl_clean_no_desc);
        mTv_clean_desc = (TextView) mFloatLayout.findViewById(R.id.tv_clean_desc);
        ll_total_false = (LinearLayout) mFloatLayout.findViewById(R.id.ll_total_false);

        //大圆部分信息
        mTv_progress = (TextView) mFloatLayout.findViewById(R.id.tv_progress);

        //真假button的获取
        mFl_button_false = (FrameLayout) mFloatLayout.findViewById(R.id.fl_button_false);
        mBt_booster_false = (Button) mFloatLayout.findViewById(R.id.bt_booster_false);

        // mFl_button_true = (FrameLayout) findViewById(R.id.fl_button_true);
        mFl_button_true = (RelativeLayout) mFloatLayout.findViewById(R.id.fl_button_true);
        mBt_booster_true = (Button) mFloatLayout.findViewById(R.id.bt_booster_true);

        //listview露出
        mFl_child_include_list =
            (FrameLayout) mFloatLayout.findViewById(R.id.fl_child_include_list);

        //小圆部分描述信息
        mLl_root_small_desc = (LinearLayout) mFloatLayout.findViewById(R.id.ll_root_small_desc);
        mTv_total_small = (TextView) mFloatLayout.findViewById(R.id.tv_total);
        mTv_small_desc = (TextView) mFloatLayout.findViewById(R.id.tv_small_desc);

        //提示小箭头
        mRl_guide_arrow = (RelativeLayout) mFloatLayout.findViewById(R.id.guide_arrow);
        mGuide_arrow_1 = (ImageView) mFloatLayout.findViewById(R.id.guide_arrow_1);
        mGuide_arrow_2 = (ImageView) mFloatLayout.findViewById(R.id.guide_arrow_2);
        mGuide_arrow_3 = (ImageView) mFloatLayout.findViewById(R.id.guide_arrow_3);

        // 三段文字
        mTv_size = (TextView) mFloatLayout.findViewById(R.id.tv_size);
        mTv_name = (TextView) mFloatLayout.findViewById(R.id.tv_name);
        mTv_desc = (TextView) mFloatLayout.findViewById(R.id.tv_desc);

        //大圆部分
        mIv_inner = (ImageView) mFloatLayout.findViewById(R.id.iv_inner);
        MIv_outter = (ImageView) mFloatLayout.findViewById(R.id.iv_outter);
        mIv_inner_total = (ImageView) mFloatLayout.findViewById(R.id.iv_inner_total);
        mFl_root = (RelativeLayout) mFloatLayout.findViewById(R.id.fl_root);

        //点击动画开始小箭头
        mRl_guide_arrow = (RelativeLayout) mFloatLayout.findViewById(R.id.guide_arrow);

        mLv_1 = (ListView) mFloatLayout.findViewById(R.id.lv_1);

        mBt_booster_finish = (Button)mFloatLayout.findViewById(R.id.bt_booster_finish);

        mBt_booster_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intnet = new Intent(FloatWindowFinishReceiver.INTENT_ACTION_FLOAT_WINDOW_FINISH);
                mContext.sendBroadcast(intnet);

               // Toast.makeText(mContext,"点击finish",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void showDeepCleanWindow() {
        initViews();
        initAnimation();
        if (mWindowManager != null && mFloatLayout != null) {
            mWindowManager.addView(mFloatLayout, mWindowParams);
            startCleanAnimation();
        }
    }

    public void removeDeepCleanWindow() {
        if (mWindowManager != null && mFloatLayout != null) {
            mWindowManager.removeViewImmediate(mFloatLayout);
        }
    }


    public void initAnimation() {
        initCleanAnimation();
        //initWaveAnimation();
    }


    //初始化清理动画
    public void initCleanAnimation() {

        //大圆继续做旋转动画

        mAaInner_total_reverse = new AlphaAnimation(0, 1);
        // aaInner_total.setDuration(7000);
       // mAaInner_total_reverse.setDuration(mAaInner_total_duration);
        mAaInner_total_reverse.setDuration(10000);

        //里面圈的动画效果
        mRaInner_reverse =
            new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);

       // mRaInner_reverse.setDuration(mRaInner_duration);
        mRaInner_reverse.setDuration(7000);
        mRaInner_reverse.setFillAfter(true);

        mAaInner_reverse = new AlphaAnimation(1, 0);
        mAaInner_reverse.setDuration(7000);
        //mAaInner_reverse.setDuration(mAaInner_duration);

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

                mIv_inner.setVisibility(View.GONE);
            }
        });

        //外面圈的动画效果
        mRaOutter_reverse =
            new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);

        mRaOutter_reverse.setDuration(7000);
//        mRaOutter_reverse.setDuration(mRaOutter_duration);
        //raOutter.setRepeatCount(Animation.INFINITE);

        mAaOutter_reverse = new AlphaAnimation(1, 0);


         mAaOutter_reverse.setDuration(7000);
//        mAaOutter_reverse.setDuration(mAaOutter_duration);
        mAaOutter_reverse.setFillAfter(true);

        mAsOutter_reverse = new AnimationSet(false);
        mAsOutter_reverse.addAnimation(mRaOutter_reverse);
        mAsOutter_reverse.addAnimation(mAaOutter_reverse);
        mAsOutter_reverse.setFillAfter(true);




        mAsOutter_reverse.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mValueAnimator_progress_reverse.start();

                //波浪开始
//                mLl_second_wave.setVisibility(View.VISIBLE);
//                mLl_first_wave.setVisibility(View.VISIBLE);
//                second_one_anim.start();
//                second_two_anim.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {


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
//        mValueAnimator_progress_reverse.setDuration(mValueAnimator_progress_duration);
        mValueAnimator_progress_reverse.setDuration(7000);

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
                //关闭波浪

//                mLl_second_wave.setVisibility(View.INVISIBLE);
//                mLl_first_wave.setVisibility(View.INVISIBLE);
//                mLl_second_wave.clearAnimation();
//                mLl_first_wave.clearAnimation();

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
//                mSc_after_clean.setVisibility(View.VISIBLE);
               // mSc_after_clean.setVerticalScrollBarEnabled(true);
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
            (int) (mContext.getResources().getDimension(R.dimen.tv_size_afterCleanAnimation)
                   + 0.5f));
        mTv_name.setTranslationY(
            (int) (mContext.getResources().getDimension(R.dimen.tv_name_afterCleanAnimation)
                   + 0.5f));

//        mOa_mFl_av = ObjectAnimator.ofFloat(mFl_av,"translationY",0,-40);//-64
        mOa_mFl_av =
            ObjectAnimator.ofFloat(mFl_av, "translationY", 0, (int) (mContext.
                getResources().getDimension(R.dimen.fl_av_afterCleanAnimation) + 0.5f));//-64

        mOa_mFl_av.setDuration(1000);

//        mOa_ll_detail_clean = ObjectAnimator.ofFloat(ll_detail_clean, "translationY", 0, 5f);
        mOa_ll_detail_clean =
            ObjectAnimator.ofFloat(ll_detail_clean, "translationY", 0, (int) (mContext.
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
            ll_firstChild_for_DragViewForBooster.getHeight() - (int) (mContext.
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
            -move - (int) (mContext.getResources().getDimension(R.dimen.move_second_child) + 0.5f);
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
//        mSc_after_clean.setVisibility(View.VISIBLE);
        as_after_clean.start();
    }

    //初始化广告
    public void initMobvista() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, String> map =
            sdk.getMVConfigurationMap("22466", "686dfddcac68d078f4de704b947cff0c");
        sdk.init(map, mContext);
    }


    public void loadMobvistaNative() {
        if (nativeHandle == null) {
            Map<String, Object> properties = MvNativeHandler.getNativeProperties("544");
            properties
                .put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);//广告样式
            properties
                .put(MobVistaConstans.ID_FACE_BOOK_PLACEMENT, "826581090784415_870544966388027");
            properties.put(MobVistaConstans.PROPERTIES_AD_NUM, mAdCountMax);//请求广告条数，不设默认为1
            nativeHandle = new MvNativeHandler(properties, mContext);
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
                    Picasso.with(mContext)
                        .load(urlIcon)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon);
                    // Downloading and setting the ad icon.
                    final String urlImage = mNativeCampaign.get(0).getImageUrl();
                    Picasso.with(mContext)
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
                    Picasso.with(mContext)
                        .load(urlIcon2)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon2);
                    // Downloading and setting the ad icon.
                    final String urlImage2 = mNativeCampaign.get(1).getImageUrl();
                    Picasso.with(mContext)
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
                    Picasso.with(mContext)
                        .load(urlIcon3)
                        .skipMemoryCache()
                        .error(R.drawable.wallpaper_default)
                        .placeholder(R.drawable.wallpaper_default)
                        .into(nativeAdIcon3);
                    // Downloading and setting the ad icon.
                    final String urlImage3 = mNativeCampaign.get(2).getImageUrl();
                    Picasso.with(mContext)
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

            //显示波浪
//                mLl_second_wave.setVisibility(View.VISIBLE);
//                mLl_first_wave.setVisibility(View.VISIBLE);

        }
        mIv_inner.startAnimation(mAsInner_reverse);
        mIv_inner_total.startAnimation(mAaInner_total_reverse);
        MIv_outter.startAnimation(mAsOutter_reverse);



    }


    public void  stopCleanAnimation(){

        //MIv_outter.clearAnimation();

//        mRaOutter_reverse = null;
//        mRaOutter_reverse = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f,
//                            Animation.RELATIVE_TO_SELF, 0.5f);
        mRaOutter_reverse.setDuration(3000);
        mRaOutter_reverse.setRepeatCount(0);
        mRaOutter_reverse.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float v) {
                return 0;
            }
        });

//        mAaOutter_reverse = new  AlphaAnimation(1,0);
//       // mAaOutter_reverse.setRepeatMode(0);
//        mAaOutter_reverse.setDuration(3000);
//        mAaOutter_reverse.setFillAfter(true);
//
       //mAsOutter_reverse.addAnimation(mRaOutter_reverse);
         // mAsOutter_reverse.addAnimation(mAaOutter_reverse);

//
//        MIv_outter.startAnimation(mAsOutter_reverse);
          //MIv_outter.startAnimation(mAsOutter_reverse);

        MIv_outter.startAnimation(mRaOutter_reverse);
        MIv_outter.startAnimation(mAaOutter_reverse);

    }



}
