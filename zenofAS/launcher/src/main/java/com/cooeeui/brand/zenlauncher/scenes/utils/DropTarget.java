package com.cooeeui.brand.zenlauncher.scenes.utils;

import android.graphics.Rect;

import com.cooeeui.brand.zenlauncher.scenes.BubbleView;

public interface DropTarget {

    class DragObject {

        public BubbleView dragView = null;

        public DragSource dragSource = null;

        public DragObject() {
        }
    }

    void onDrop(DragObject dragObject);

    void onDragEnter(DragObject dragObject);

    void onDragOver(DragObject dragObject);

    void onDragExit(DragObject dragObject);

    void getHitRectRelativeToDragLayer(Rect outRect);

}
