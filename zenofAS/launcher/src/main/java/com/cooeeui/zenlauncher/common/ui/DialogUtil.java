package com.cooeeui.zenlauncher.common.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooeeui.zenlauncher.R;

/**
 * loading Dialog工具类
 *
 * @author leexingwang
 */
public class DialogUtil {

    // 渐变退出的时间
    private static final int CANCEL_LOADING_TIME = 500;
    // 每旋转一圈的时间
    private static final int PER_REVOLUTION_TIME = 700;
    private SafeProgressDialog loadingDialog;
    private Context mContext;
    private LinearLayout ll_layout;
    private ImageView spaceshipImage;
    private TextView tipTextView;
    private int mDefaultTextSize = 20;
    private DialogCancelListener dialogCancelListener;

    public DialogUtil(Context context) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        ll_layout = (LinearLayout) view.findViewById(R.id.dialog_view);// 加载布局
        spaceshipImage = (ImageView) view.findViewById(R.id.img_dialog);// loading_dialog中的ImageView
        tipTextView = (TextView) view.findViewById(R.id.tipTextView);// 提示文字
    }

    public void setDialogCancelListener(
        DialogCancelListener dialogCancelListener) {
        this.dialogCancelListener = dialogCancelListener;
    }

    public boolean isShowing() {
        if (loadingDialog != null) {
            return loadingDialog.isShowing();
        }
        return false;
    }

    /**
     * 显示自定义的progressDialog 默认无文字显示，可点击空白处或者back键使Loading dialog消失
     */
    public void showLoadingDialog() {
        showLoadingDialog("", mDefaultTextSize, true);
    }

    /**
     * 显示自定义的progressDialog 默认无文字显示
     *
     * @param setCancelable 是否可以点击空白处或者back键使Loading dialog消失
     */
    public void showLoadingDialog(boolean setCancelable) {
        showLoadingDialog("", mDefaultTextSize, setCancelable);
    }

    /**
     * 显示自定义的progressDialog 默认无文字显示
     *
     * @param setCancelable 是否可以点击空白处消失
     */
    public void showLoadingDialogWithOutSideCancelable(boolean setCancelable,
                                                       DialogCancelListener listener) {
        dialogCancelListener = listener;
        showLoadingDialogOutsideCancelable("", mDefaultTextSize, setCancelable);
    }

    /**
     * 显示自定义的progressDialog
     *
     * @param msg           提示消息
     * @param textSize      提示消息的字体大小
     * @param setCancelable 是否可以点击空白处或者back键使Loading dialog消失
     */
    public void showLoadingDialog(String msg, int textSize,
                                  boolean setCancelable) {
        tipTextView.setTextSize(textSize);
        ObjectAnimator animator = ObjectAnimator
            .ofFloat(spaceshipImage, "rotation", 0.0f, 360.0f);// 给spaceshipImage添加旋转动画
        animator.setDuration(PER_REVOLUTION_TIME);
        animator.setRepeatCount(Integer.MAX_VALUE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator);
        animatorSet.start();// 开始动画
        tipTextView.setText(msg);// 设置加载信息
        loadingDialog = new SafeProgressDialog(mContext, R.style.loading_dialog);// 创建自定义样式dialog
        loadingDialog.setCanceledOnTouchOutside(false);//超出框触碰不取消.
        loadingDialog.setRelatedActivityFinishByBackKey(true);//当按back key时，相关activity也结束.
        //loadingDialog.setCancelable(setCancelable);// 是否可以用“返回键”取消
        loadingDialog.setContentView(ll_layout, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        loadingDialog.show();
    }

    /**
     * 显示自定义的progressDialog
     *
     * @param msg           提示消息
     * @param textSize      提示消息的字体大小
     * @param setCancelable 是否可以点击空白处使Loading dialog消失
     */
    public void showLoadingDialogOutsideCancelable(String msg, int textSize,
                                                   boolean setCancelable) {
        tipTextView.setTextSize(textSize);
        ObjectAnimator
            animator =
            ObjectAnimator
                .ofFloat(spaceshipImage, "rotation", 0.0f, 360.0f);// 给spaceshipImage添加旋转动画
        animator.setDuration(PER_REVOLUTION_TIME);
        animator.setRepeatCount(Integer.MAX_VALUE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator);
        animatorSet.start();// 开始动画
        tipTextView.setText(msg);// 设置加载信息
        loadingDialog = new SafeProgressDialog(mContext, R.style.loading_dialog);// 创建自定义样式dialog
        loadingDialog.setCanceledOnTouchOutside(setCancelable);
        loadingDialog.setRelatedActivityFinishByBackKey(true);//当按back key时，相关activity也结束.
        loadingDialog.setContentView(ll_layout, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (dialogCancelListener != null) {
                    dialogCancelListener.onCancelDialog();
                }
            }
        });
        loadingDialog.show();
    }

    /**
     * 取消加载动画带渐变动画退出
     */
    public void cancelLoadingDialog() {
        spaceshipImage.clearAnimation();
        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_layout, "alpha", 1.0f, 0.0f);
        animator.setDuration(CANCEL_LOADING_TIME);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator);
        animatorSet.start();
    }

    /**
     * 取消加载动画不带渐变动画退出
     */
    public void cancelLoadingDialogNoAnimation() {
        spaceshipImage.clearAnimation();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    public interface DialogCancelListener {

        public void onCancelDialog();
    }

    class SafeProgressDialog extends Dialog {

        Activity mParentActivity;
        boolean isRelatedActivityFinishByBackKey = false;//KEY BACK 是否能结束相关Activity

        public SafeProgressDialog(Context context) {
            super(context);
            mParentActivity = (Activity) context;
        }

        public SafeProgressDialog(Context context, int id) {
            super(context, id);
            mParentActivity = (Activity) context;
        }

        public void setRelatedActivityFinishByBackKey(boolean relatedActivityFinishByBackKey) {
            isRelatedActivityFinishByBackKey = relatedActivityFinishByBackKey;
        }

        @Override
        public void dismiss() {
            if (mParentActivity != null && !mParentActivity.isFinishing()) {
                super.dismiss(); // 调用超类对应方法
            }
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            boolean result = super.onKeyUp(keyCode, event);
            if (result && keyCode == KeyEvent.KEYCODE_BACK && isRelatedActivityFinishByBackKey) {
                if (mParentActivity != null && !mParentActivity.isFinishing()) {
                    mParentActivity.finish();
                }
            }
            return result;
        }
    }
}
