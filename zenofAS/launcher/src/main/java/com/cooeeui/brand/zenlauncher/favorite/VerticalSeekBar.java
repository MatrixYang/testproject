package com.cooeeui.brand.zenlauncher.favorite;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import java.lang.reflect.Method;

public class VerticalSeekBar extends SeekBar {

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int progress = getMax() - (int) (getMax() * event.getY() / getHeight());
                setSeekBarProgress(progress, true);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /**
     * 为了区分是代码还是用户手动改变进度值，这里采用反射调用
     *
     * @param newProgress 进度值
     * @param fromUser    是否是用户操作
     */
    private void setSeekBarProgress(int newProgress, boolean fromUser) {
        Method privateSetProgressMethod = null;
        try {
            privateSetProgressMethod = ProgressBar.class.getDeclaredMethod("setProgress",
                                                                           Integer.TYPE,
                                                                           Boolean.TYPE);
            privateSetProgressMethod.setAccessible(true);
            privateSetProgressMethod.invoke(this, newProgress, fromUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateThumb() {
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
}
