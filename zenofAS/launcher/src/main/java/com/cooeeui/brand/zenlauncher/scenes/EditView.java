package com.cooeeui.brand.zenlauncher.scenes;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.widget.ImageView;

import com.cooeeui.brand.zenlauncher.scenes.utils.DropTarget;

public class EditView extends ImageView implements DropTarget {

    SpeedDial speedDial;

    Rect r = new Rect();

    Rect rect = new Rect();

    public EditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditView(Context context) {
        super(context);
    }

    public void setUp(SpeedDial s) {
        speedDial = s;
    }

    private void getRect() {
        speedDial.getGlobalVisibleRect(r);

        getGlobalVisibleRect(rect);
        rect.offset(-r.left, -r.top);

        int padding = speedDial.getIconSize() / 2;
        rect.left -= padding;
        rect.top -= padding;
        rect.right += padding;
        rect.bottom += padding;
    }

    @Override
    public void onDrop(DragObject dragObject) {

    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    @Override
    public void onDragOver(DragObject dragObject) {

    }

    @Override
    public void onDragExit(DragObject dragObject) {

    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        getRect();
        outRect.set(rect);
    }

}
