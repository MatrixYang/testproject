package com.cooeeui.brand.zenlauncher.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.cooeeui.basecore.utilities.DeviceUtils;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;

public class QsListViewLetters extends View {

    private final String NOMAL_COLOR = "#afafaf";
    private final String HIGHLIGHT_COLOR = "#ffffff";

    OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    float scopeHeiht;
    float singleHeight = 0;
    float verticalPaddingMax = 16;
    float verticalPadding;
    int lettersNumMax = 27;

    private ArrayList<String> mOrderedLetters;
    int choose = -1;
    Paint paint = new Paint();
    float fontHeight;
    float fontBottom;
    float marginRight = 8;
    int fontSize = 10;

    private int mFirstVisiblityIndex;
    private int mLastVisiblityIndex;

    private int mNavigationHeight;

    public QsListViewLetters(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(context)) {
            mNavigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
        }
    }

    public QsListViewLetters(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(context)) {
            mNavigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
        }
    }

    public QsListViewLetters(Context context) {
        super(context);
        if (!DeviceUtils.isSpecialDevicesForNavigationbar() && Build.VERSION.SDK_INT >= 19
            && LauncherAppState.HasNavigationBar(context)) {
            mNavigationHeight = getResources().getDimensionPixelSize(R.dimen.navigation_height);
        }
    }

    public void setLetters(ArrayList<String> orderedLetters, int fontSize) {
        mOrderedLetters = orderedLetters;

        marginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                      marginRight,
                                                      getResources().getDisplayMetrics());
        verticalPaddingMax = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                             verticalPaddingMax,
                                                             getResources().getDisplayMetrics());

        fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                   fontSize, getResources().getDisplayMetrics());

        paint.setColor(Color.parseColor(NOMAL_COLOR));
        paint.setAntiAlias(true);
        paint.setTextSize(fontSize);

        FontMetrics fm = paint.getFontMetrics();
        // 计算出文字的大小
        fontHeight = (float) Math.ceil(fm.bottom - fm.top);
        verticalPadding = (lettersNumMax - mOrderedLetters.size()) * fontHeight
                          / (mOrderedLetters.size() - 1);
        if (verticalPadding > verticalPaddingMax) {
            verticalPadding = verticalPaddingMax;
        }

        // 文字+间距的大小
        singleHeight = fontHeight + verticalPadding;
        fontBottom = fm.bottom;
        scopeHeiht = mOrderedLetters.size() * fontHeight + (mOrderedLetters.size() - 1)
                                                           * verticalPadding;
    }

    public void setVisiblityIndex(int firstIndex, int lastIndex) {
        this.mFirstVisiblityIndex = firstIndex;
        this.mLastVisiblityIndex = lastIndex;
    }

    @Override
    public void invalidate() {
        // TODO Auto-generated method stub
        if (mOrderedLetters != null && mOrderedLetters.size() != 0) {
            verticalPadding = (lettersNumMax - mOrderedLetters.size()) * fontHeight
                              / (mOrderedLetters.size() - 1);
            if (verticalPadding > verticalPaddingMax) {
                verticalPadding = verticalPaddingMax;
            }
            singleHeight = fontHeight + verticalPadding;
            scopeHeiht = mOrderedLetters.size() * fontHeight + (mOrderedLetters.size() - 1)
                                                               * verticalPadding;
        }
        super.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOrderedLetters == null) {
            return;
        }
        int width = getWidth();

        for (int i = 0; i < mOrderedLetters.size(); i++) {
            float xPos = width - paint.measureText(mOrderedLetters.get(0))
                         - marginRight;
            float baseY = (getHeight() - mNavigationHeight - scopeHeiht) / 2
                          + (singleHeight - verticalPadding / 2 - fontBottom) + i
                                                                                * singleHeight;
            if (i >= mFirstVisiblityIndex && i <= mLastVisiblityIndex) {
                paint.setColor(Color.parseColor(HIGHLIGHT_COLOR));
                paint.setFakeBoldText(true);
            } else {
                paint.setColor(Color.parseColor(NOMAL_COLOR));
                paint.setFakeBoldText(false);
            }
            canvas.drawText(mOrderedLetters.get(i), xPos, baseY, paint);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) ((y - (getHeight() - scopeHeiht) / 2) / singleHeight);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (oldChoose != c && listener != null) {
                    if (c >= 0 && c < mOrderedLetters.size()) {
                        listener.onTouchingLetterChanged(mOrderedLetters.get(c));
                        choose = c;
                        invalidate();
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != c && listener != null) {
                    if (c >= 0 && c < mOrderedLetters.size()) {
                        listener.onTouchingLetterChanged(mOrderedLetters.get(c));
                        choose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                choose = -1;
                invalidate();
                break;
        }
        return true;
    }

    public void setOnTouchingLetterChangedListener(
        OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {

        public void onTouchingLetterChanged(String s);
    }

}
