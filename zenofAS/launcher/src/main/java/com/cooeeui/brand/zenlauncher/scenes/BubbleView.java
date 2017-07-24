package com.cooeeui.brand.zenlauncher.scenes;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;
import com.cooeeui.zenlauncher.R;

import java.lang.ref.WeakReference;

public class BubbleView extends View implements DropTarget {

    private Bitmap mBitmap;

    private Paint mPaint;

    private int mSize;
    private int mPadding;

    private float x;
    private float y;

    private Matrix mMatrix;
    private Launcher mLauncher;

    private int noticeCount;

    private static final int DRAG_ENTER_VIEW = 1;
    private static final int DRAG_EXIT_VIEW = 2;

    private static final int DELAYED_TIME = 220;

    private final Handler mHandler = new MyHandler(this);

    private long mTime;

    private boolean isShowNotice = false;
    private static final int ALPHA_DURATION = 100;
    private ValueAnimator mAlphaHide;
    private ValueAnimator mAlphaShow;
    private AnimatorSet mAnimatorSet;
    private int mWhat;
    private Paint mPaintCircle;
    private Paint mPaintText;
    private int mNoticeRadius;
    private int mNoticeBaseline;
    private int mNoticeX;
    private int mNoticeY;

    public BubbleView(Context context, Bitmap bitmap) {
        this(context, bitmap, bitmap.getWidth());
    }

    public BubbleView(Context context, Bitmap bitmap, int size) {
        super(context);
        mLauncher = (Launcher) context;
        mBitmap = bitmap;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mMatrix = new Matrix();
        mSize = size;
        noticeCount = 0;

        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setColor(Color.RED);

        mNoticeRadius = mLauncher.getResources().getDimensionPixelSize(R.dimen.notice_radius_size);
        int noticeSize = mLauncher.getResources().getDimensionPixelSize(R.dimen.notice_text_size);

        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setTextSize(noticeSize);

        mAlphaHide = ValueAnimator.ofFloat(0.3f, 0);
        mAlphaHide.setDuration(ALPHA_DURATION);
        mAlphaHide.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                setAlpha(alpha);
            }
        });

        mAlphaHide.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mWhat == DRAG_ENTER_VIEW) {
                    setTranslationX(mLauncher.getSpeedDial().getSelectX());
                    setTranslationY(mLauncher.getSpeedDial().getSelectY());
                } else {
                    setTranslationX(x);
                    setTranslationY(y);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });

        mAlphaShow = ValueAnimator.ofFloat(0, 0.3f);
        mAlphaShow.setDuration(ALPHA_DURATION);
        mAlphaShow.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                setAlpha(alpha);
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.play(mAlphaHide).before(mAlphaShow);

    }

    /**
     * 采用内部Handler类来更新UI，避免内存泄露
     *
     * Handler mHandler = new Handler() { public void handleMessage(Message msg) {
     * mImageView.setImageBitmap(mBitmap); } }
     *
     * 上面是一段简单的Handler的使用。当使用内部类（包括匿名类）来创建Handler的时候，Handler对象会隐式地持有一个外部类对象（
     * 通常是一个Activity）的引用（不然你怎么可能通过Handler来操作Activity中的View？）。 而Handler通常会伴随着一个耗时的后台线程（例如从网络拉取图片）一起出现，
     * 这个后台线程在任务执行完毕（例如图片下载完毕）之后，通过消息机制通知Handler，然后Handler把图片更新到界面。 然而，如果用户在网络请求过程中关闭了Activity，正常情况下，Activity不再被使用，它就有可能在GC检查时被回收掉，
     * 但由于这时线程尚未执行完，而该线程持有Handler的引用（不然它怎么发消息给Handler？）， 这个Handler又持有Activity的引用，
     * 就导致该Activity无法被回收（即内存泄露），直到网络请求结束（例如图片下载完毕）。 另外，如果你执行了Handler的postDelayed()方法，该方法会将你的Handler装入一个Message，并把这条Message推到MessageQueue中，
     * 那么在你设定的delay到达之前，会有一条MessageQueue -> Message -> Handler -> Activity的链，导致你的Activity被持有引用而无法被回收。
     */
    private class MyHandler extends Handler {

        private final WeakReference<BubbleView> mOuter;

        public MyHandler(BubbleView outer) {
            mOuter = new WeakReference<BubbleView>(outer);
        }

        @Override
        public void handleMessage(Message msg) {
            BubbleView outer = mOuter.get();
            if (outer != null) {
                switch (msg.what) {
                    case DRAG_ENTER_VIEW:
                        mWhat = DRAG_ENTER_VIEW;
                        mAnimatorSet.start();
                        break;
                    case DRAG_EXIT_VIEW:
                        mWhat = DRAG_EXIT_VIEW;
                        mAnimatorSet.start();
                        break;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            float scale = (float) mSize / (float) mBitmap.getWidth();
            mMatrix.setScale(scale, scale);
            mMatrix.postTranslate(mPadding / 2, mPadding / 2);
            canvas.drawBitmap(mBitmap, mMatrix, mPaint);
            if (!isShowNotice) {
                if (noticeCount > 0) {
                    canvas.drawCircle(mNoticeX, mNoticeY, mNoticeRadius, mPaintCircle);
                    canvas.drawText("" + noticeCount, mNoticeX, mNoticeBaseline, mPaintText);
                }
            } else {
                canvas.drawCircle(mNoticeX, mNoticeY, mNoticeRadius, mPaintCircle);
                canvas.drawText("!", mNoticeX, mNoticeBaseline, mPaintText);

            }
        }
    }

    public void setSize(int size, int padding) {
        mSize = size;
        mPadding = padding;
        mNoticeX = mSize + mPadding / 2 - mNoticeRadius / 2;
        mNoticeY = mSize + mPadding / 2 - mNoticeRadius / 2;
        Paint.FontMetricsInt fMetrics = mPaintText.getFontMetricsInt();
        mNoticeBaseline = mNoticeY - mNoticeRadius - fMetrics.top
                          + (mNoticeRadius + mNoticeRadius - fMetrics.bottom + fMetrics.top) / 2;

    }

    public void setNoticeCount(int noticeCount) {
        this.noticeCount = noticeCount;
    }

    public boolean getShowNotice() {
        return isShowNotice;
    }

    public void setShowNotice(boolean isShowNotice) {
        this.isShowNotice = isShowNotice;
    }

    public void move(float x, float y) {
        mHandler.removeMessages(DRAG_ENTER_VIEW);
        mHandler.removeMessages(DRAG_EXIT_VIEW);
        if (mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }
        setTranslationX(x);
        setTranslationY(y);
        this.x = x;
        this.y = y;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize + mPadding, mSize + mPadding);
    }

    @Override
    public void onDrop(DragObject dragObject) {
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        mHandler.removeMessages(DRAG_ENTER_VIEW);
        mHandler.removeMessages(DRAG_EXIT_VIEW);
        mHandler.sendEmptyMessageDelayed(DRAG_ENTER_VIEW, DELAYED_TIME);

        mTime = System.currentTimeMillis();
    }

    @Override
    public void onDragOver(DragObject dragObject) {
    }

    @Override
    public void onDragExit(DragObject dragObject) {
        long diff = System.currentTimeMillis() - mTime;
        if (diff < DELAYED_TIME) {
            mHandler.removeMessages(DRAG_ENTER_VIEW);
            return;
        }

        mHandler.removeMessages(DRAG_EXIT_VIEW);
        mHandler.sendEmptyMessageDelayed(DRAG_EXIT_VIEW, DELAYED_TIME);
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        outRect.left = (int) x;
        outRect.top = (int) y;
        outRect.right = outRect.left + mSize + mPadding;
        outRect.bottom = outRect.top + mSize + mPadding;
    }

    public void changeBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        invalidate();
    }

    public void clearBitmap() {
        if (mBitmap != null) {
            if (!mBitmap.isRecycled()) {
                mBitmap.recycle();
            }
            mBitmap = null;
        }
    }
}
