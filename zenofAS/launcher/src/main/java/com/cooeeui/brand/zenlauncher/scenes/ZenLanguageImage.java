package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.cooeeui.zenlauncher.R;

public class ZenLanguageImage extends View {

    public static final int DOWNLOAD = 0;
    public static final int DOWNLOADING = 1;
    public static final int DOWNLOADED = 2;
    public static final int SELECTED = 3;

    private int mState;

    private int mProgress;

    private Drawable mBack;

    private Drawable mFore;

    private Drawable mProg;

    private Path mPath;

    private int mSize;

    public ZenLanguageImage(Context context) {
        super(context);
        init();
    }

    public ZenLanguageImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZenLanguageImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //硬件加速支持的clipPath方法API至少要18，低于18的关闭当前view的硬件加速
        if (Build.VERSION.SDK_INT < 18) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mState = DOWNLOAD;
        mBack = getResources().getDrawable(R.drawable.download_circle);
        mFore = getResources().getDrawable(R.drawable.download_now);
    }

    public void setState(int state) {
        mState = state;
        if (mState == DOWNLOAD) {
            mBack = getResources().getDrawable(R.drawable.download_circle);
            mFore = getResources().getDrawable(R.drawable.download_now);
        } else if (mState == DOWNLOADING) {
            mBack = getResources().getDrawable(R.drawable.download_circle);
            mFore = getResources().getDrawable(R.drawable.download_now);
            mProg = getResources().getDrawable(R.drawable.download_yes);
        } else if (mState == DOWNLOADED) {
            mBack = getResources().getDrawable(R.drawable.download_circle);
            mFore = getResources().getDrawable(R.drawable.download_ok);
        } else if (mState == SELECTED) {
            mBack = getResources().getDrawable(R.drawable.download_yes);
            mFore = getResources().getDrawable(R.drawable.download_select);
        }
        invalidate();
    }

    public void setProgress(int progress) {
        mProgress = progress;
        getPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBack.setBounds(0, 0, mSize, mSize);
        mBack.draw(canvas);
        int x = mSize / 4;
        int w = mSize - x;
        mFore.setBounds(x, x, w, w);
        mFore.draw(canvas);
        if (mState == DOWNLOADING) {
            canvas.save();
            mProg.setBounds(0, 0, mSize, mSize);
            canvas.clipPath(mPath);
            mProg.draw(canvas);
            canvas.restore();
        }
    }

    private void getPath() {
        int r = mSize / 2;
        float angle = mProgress * 360 / 100 - 90;

        mPath = new Path();
        mPath.moveTo(r, r);
        mPath.lineTo(r, 0);
        mPath.lineTo((float) (r + r * Math.cos(angle * Math.PI / 180)),
                     (float) (r + r * Math.sin(angle * Math.PI / 180)));
        mPath.close();

        RectF rect = new RectF(0, 0, mSize, mSize);
        mPath.addArc(rect, 270, angle + 90);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(mSize, mSize);
    }
}
