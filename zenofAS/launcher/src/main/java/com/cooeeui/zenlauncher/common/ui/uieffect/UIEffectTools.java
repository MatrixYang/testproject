package com.cooeeui.zenlauncher.common.ui.uieffect;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cooeeui.zenlauncher.R;

/**
 * 给点击事件添加点击效果，现在写了onClickEffect（）和onLongClickEffect（）两个函数但内容一样是为了以后方便扩充不同的点击效果, 特此说明 ！
 *
 * @author leexingwang
 * @version 1.0
 * @date 2015.04.23
 */
public class UIEffectTools {

    // 单击事件效果动画时长
    private final static int ON_CLICK_DURTION_TIME = 100;
    // 长按事件效果动画时长
    private final static int ON_LONG_CLICK_DURTION_TIME = 100;

    /**
     * 单击事件点击效果
     *
     * @param view 事件源
     */
    public static void onClickEffect(View view) {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(view, "scaleX",
                                                      0.7f, 1.0f);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(view, "scaleY",
                                                      0.7f, 1.0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(ON_CLICK_DURTION_TIME);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.playTogether(anim1, anim2);
        animSet.start();
    }

    /**
     * 长按事件点击效果
     *
     * @param view 事件源
     */
    public static void onLongClickEffect(View view) {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(view, "scaleX",
                                                      0.7f, 1.0f);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(view, "scaleY",
                                                      0.7f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(ON_LONG_CLICK_DURTION_TIME);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(anim1, anim2);
        animatorSet.start();

    }

    /**
     * 选中需要被隐藏的APP图标时
     */
    public static void onSelectedHiden(View view, boolean selected) {
        ObjectAnimator animation;
        ImageView icon = ((ImageView) view.findViewById(R.id.hideStatus));
        LinearLayout group = ((LinearLayout) view.findViewById(R.id.gridview_applicationGroup));

        if (selected) {
            animation = ObjectAnimator.ofFloat(group, "alpha", 1f, 0.5f);
        } else {
            animation = ObjectAnimator.ofFloat(group, "alpha", 0.5f, 1f);
        }
        icon.setImageResource(selected ? R.drawable.hide
                                       : R.drawable.unhide);
        animation.setDuration(200);// 设置动画时间
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
    }

}
