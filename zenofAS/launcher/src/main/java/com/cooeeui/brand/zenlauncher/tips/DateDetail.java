package com.cooeeui.brand.zenlauncher.tips;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.cooeeui.zenlauncher.R;
import com.umeng.analytics.MobclickAgent;

public class DateDetail extends View implements View.OnClickListener {

    private Bitmap mDetail;

    private Drawable mData;

    private Paint mPaint;

    private Matrix mMatrix;

    private Path mPath;

    private int mSize;

    private Context mContext;

    private float mAngle;

    public DateDetail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public DateDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public DateDetail(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        setOnClickListener(this);
        mDetail = BitmapFactory.decodeResource(getResources(), R.drawable.tips_detail);
        mAngle = TipsPopup.mAngle;
        if (TipsPopup.mDataId == -1) {
            mData = null;
        } else {
            mData = getResources().getDrawable(TipsPopup.mDataId);
        }

        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mMatrix = new Matrix();
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float scale = (float) mSize / (float) mDetail.getWidth();
        mMatrix.setScale(scale, scale);

        canvas.drawBitmap(mDetail, mMatrix, mPaint);

        if (mData != null) {
            canvas.save();
            if (mAngle >= 360) {
                mData.setBounds(0, 0, mSize, mSize);
                mData.draw(canvas);
            } else if (mAngle > 0) {
                mData.setBounds(0, 0, mSize, mSize);
                canvas.clipPath(mPath);
                mData.draw(canvas);
            }
            canvas.restore();
        }
    }

    private void getPath() {
        if (mAngle <= 0) {
            return;
        }

        int r = mSize / 2;
        float angle = mAngle - 90;

        mPath = new Path();
        mPath.moveTo(r, r);
        mPath.lineTo(r, 0);
        mPath.lineTo((float) (r + r * Math.cos(angle * Math.PI / 180)),
                     (float) (r + r * Math.sin(angle * Math.PI / 180)));
        mPath.close();

        RectF rect = new RectF(0, 0, mSize, mSize);
        mPath.addArc(rect, 270, mAngle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(mSize, mSize);
        getPath();
    }

    @Override
    public void onClick(View view) {
        // 智能提醒popupwindow点击应用使用详情
        MobclickAgent.onEvent(mContext, "SmartReminderPopupWindowClickUseofDetails");
        Intent intent = new Intent(mContext, TipsSetting.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}
