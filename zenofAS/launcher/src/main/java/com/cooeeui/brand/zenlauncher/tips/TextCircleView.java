package com.cooeeui.brand.zenlauncher.tips;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cooeeui.zenlauncher.R;

public class TextCircleView extends TextView {

    private Paint mPaint;
    private int mColor = R.color.circle_green;

    public void setColor(int mColor) {
        this.mColor = mColor;
        invalidate();
    }

    public TextCircleView(Context context) {
        super(context);
        init();
    }

    public TextCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化画笔
     */
    public void init() {
        mPaint = new Paint();
    }

    /**
     * 调用onDraw绘制边框
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int w = this.getWidth();
        int h = this.getHeight();
        mPaint.setColor(mColor);
        // 设置画笔的样式，空心
        mPaint.setStyle(Paint.Style.FILL);
        // 设置抗锯齿
        mPaint.setAntiAlias(true);
        int r = w > h ? h : w;
        canvas.drawCircle(r / 2, r / 2, r / 2, mPaint);
        // 执行绘制文字
        super.onDraw(canvas);
    }
}
