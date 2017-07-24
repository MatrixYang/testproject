package com.cooeeui.brand.zenlauncher.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class LetterListView extends View {

    OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    float scopeHeiht;
    float singleHeight = 0;
    float padding = 0;
    float startY;

    String[] b;
    int choose = -1;
    Paint paint = new Paint();
    boolean showBkg = false;
    float fontHeight;
    int parentHeight;
    float fontBottom;
    float paddingRight = 8;
    int fontSize = 10;

    public LetterListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LetterListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LetterListView(Context context) {
        super(context);
    }

    public void setLetters(String[] newb, int fontSize) {
        b = newb;

        paddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                       paddingRight,
                                                       getResources().getDisplayMetrics());

        fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                   fontSize, getResources().getDisplayMetrics());

        paint.setColor(Color.parseColor("#c7c9cf"));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);
        paint.setTextSize(fontSize);

        FontMetrics fm = paint.getFontMetrics();

        // 计算出文字的大小
        fontHeight = (float) Math.ceil(fm.bottom - fm.top);
        // 文字+间距的大小
        singleHeight = fontHeight + padding;

        fontBottom = fm.bottom;

        parentHeight = this.getHeight();

        scopeHeiht = b.length * fontHeight + (b.length - 1) * padding;

        startY = (parentHeight - scopeHeiht) / 2;

    }

    // @Override
    // protected void onDraw(Canvas canvas) {
    // super.onDraw(canvas);
    // if (showBkg) {
    // canvas.drawColor(Color.parseColor("#00000000"));
    // }
    //
    // int height = getHeight();
    // int width = getWidth();
    // int singleHeight = height / b.length;
    //
    //
    //
    // FontMetrics fm = paint.getFontMetrics();
    // float fontHeight = (float) Math.ceil(fm.descent - fm.ascent);
    // //
    //
    //
    // for (int i = 0; i < b.length; i++) {
    // paint.setColor(Color.BLACK);
    // paint.setTypeface(Typeface.DEFAULT_BOLD);
    // paint.setFakeBoldText(true);
    // paint.setAntiAlias(true);
    // paint.setTextSize(25);
    // if (i == choose) {
    // paint.setColor(Color.parseColor("#3399ff"));
    // }
    // float xPos = width / 2 - paint.measureText(b[i]) / 2;
    // float yPos = singleHeight * i + singleHeight;
    // canvas.drawText(b[i], xPos, yPos, paint);
    // paint.reset();
    // }
    //
    // }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (b == null) {
            return;
        }
        if (showBkg) {
            canvas.drawColor(Color.parseColor("#00000000"));
        }
        int height = getHeight();
        int width = getWidth();

        for (int i = 0; i < b.length; i++) {
            float xPos = width - paint.measureText(b[i])
                         - paddingRight;

            float baseY = startY + (singleHeight - padding / 2 - fontBottom) + i * singleHeight;
            // float yPos = (height - fontHeight) / 2;
            canvas.drawText(b[i], xPos, baseY, paint);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        // final int c = (int) (y / getHeight() * b.length);
        final int c = (int) ((y - startY) / singleHeight);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                showBkg = true;
                if (oldChoose != c && listener != null) {
                    if (c >= 0 && c < b.length) {
                        listener.onTouchingLetterChanged(b[c]);
                        choose = c;
                        invalidate();
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != c && listener != null) {
                    if (c >= 0 && c < b.length) {
                        listener.onTouchingLetterChanged(b[c]);
                        choose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                showBkg = false;
                choose = -1;
                invalidate();
                break;
        }
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setOnTouchingLetterChangedListener(
        OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {

        public void onTouchingLetterChanged(String s);
    }

}
