package com.cooeeui.brand.zenlauncher.scenes.utils;

import android.graphics.Rect;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.scenes.BubbleView;

import java.util.ArrayList;

public class DragController {

    private Launcher mLauncher;

    private boolean mDragging;

    private int mMotionDownX;

    private int mMotionDownY;

    private int mOffsetX;

    private int mOffsetY;

    private int mMidOffsetX;

    private int mMidOffsetY;

    private Rect mRectTemp = new Rect();

    private DropTarget.DragObject mDragObject;

    private ArrayList<DropTarget> mDropWorkSpace = new ArrayList<DropTarget>();

//    private ArrayList<DropTarget> mDropMainMenu = new ArrayList<DropTarget>();

    private ArrayList<DropTarget> mDropTargets;

    private DropTarget mLastDropTarget;

    public DragController(Launcher launcher) {
        mLauncher = launcher;
    }

    public void addDropWorkSpace(DropTarget target) {
        mDropWorkSpace.add(target);
    }

    public void removeDropWorkSpace(DropTarget target) {
        mDropWorkSpace.remove(target);
    }

//    public void addDropMainMenu(DropTarget target) {
//        mDropMainMenu.add(target);
//    }

//    public void removeDropMainMenu(DropTarget target) {
//        mDropMainMenu.remove(target);
//    }

    public void startDrag(DragSource source, BubbleView view) {
        mDropTargets = mDropWorkSpace;

        // get a offset rectangle of workspace.
        Rect r = new Rect();
        mLauncher.getSpeedDial().getGlobalVisibleRect(r);
        Rect rootRect = new Rect();
        mLauncher.getDragLayer().getGlobalVisibleRect(rootRect);
        r.offset(-rootRect.left, -rootRect.top);

        mDragging = true;
        mDragObject = new DropTarget.DragObject();
        mDragObject.dragView = view;
        mLauncher.getDragLayer().addView(view);
        mDragObject.dragSource = source;
        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        mOffsetX = mMotionDownX - (int) view.getTranslationX()
                   - r.left;
        mOffsetY = mMotionDownY - (int) view.getTranslationY()
                   - r.top;
        mMidOffsetX = mOffsetX - view.getWidth() / 2
                      + r.left;
        mMidOffsetY = mOffsetY - view.getHeight() / 2
                      + r.top;
        mDragObject.dragView.move(mMotionDownX - mOffsetX, mMotionDownY - mOffsetY);
    }

//    public void startDrag(DragSource source, BubbleView view, int width) {
//        mDropTargets = mDropMainMenu;
//
//        mDragging = true;
//        mDragObject = new DropTarget.DragObject();
//        mDragObject.dragView = view;
//        mLauncher.getDragLayer().addView(view);
//        mDragObject.dragSource = source;
//        mLauncher.getDragLayer().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
//
//        mOffsetX = width / 2;
//        mOffsetY = width / 2;
//        mMidOffsetX = 0;
//        mMidOffsetY = 0;
//
//        mDragObject.dragView.move(mMotionDownX - mOffsetX, mMotionDownY - mOffsetY);
//
//    }

//    public boolean isDragging() {
//        return mDragging;
//    }

    private void endDrag() {
        if (mDragging) {
            mDragging = false;
        }
    }

    public void cancelDrag() {
        if (mDragging) {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
            mDragObject.dragSource.onDropCompleted(null);
        }
        endDrag();
    }

    private DropTarget findDropTarget(int x, int y) {
        final Rect r = mRectTemp;
        final ArrayList<DropTarget> dropTargets = mDropTargets;
        final int count = dropTargets.size();
        for (int i = 0; i < count; i++) {
            DropTarget target = dropTargets.get(i);
            target.getHitRectRelativeToDragLayer(r);
            if (r.contains(x, y)) {
                return target;
            }
        }
        return null;
    }

    private void drop(int x, int y) {
        final DropTarget dropTarget = findDropTarget(x - mMidOffsetX, y - mMidOffsetY);

        if (dropTarget != null) {
            dropTarget.onDrop(mDragObject);
        }
        mDragObject.dragSource.onDropCompleted((View) dropTarget);
    }

    private int[] getClampedDragLayerPos(float x, float y) {
        int mTmpPoint[] = new int[2];
        Rect mDragLayerRect = new Rect();
        mLauncher.getDragLayer().getLocalVisibleRect(mDragLayerRect);
        mTmpPoint[0] = (int) Math.max(mDragLayerRect.left, Math.min(x, mDragLayerRect.right - 1));
        mTmpPoint[1] = (int) Math.max(mDragLayerRect.top, Math.min(y, mDragLayerRect.bottom - 1));
        return mTmpPoint;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                mLastDropTarget = null;
                break;
            case MotionEvent.ACTION_UP:
                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return mDragging;
    }

    private void checkTouchMove(DropTarget dropTarget) {
        if (dropTarget != null) {
            if (mLastDropTarget != dropTarget) {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragObject);
                }
                dropTarget.onDragEnter(mDragObject);
            }
            dropTarget.onDragOver(mDragObject);
        } else {
            if (mLastDropTarget != null) {
                mLastDropTarget.onDragExit(mDragObject);
            }
        }
        mLastDropTarget = dropTarget;
    }

    private void handleMoveEvent(int x, int y) {
        mDragObject.dragView.move(x - mOffsetX, y - mOffsetY);

        DropTarget dropTarget = findDropTarget(x - mMidOffsetX, y - mMidOffsetY);
        checkTouchMove(dropTarget);

    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }

        final int action = ev.getAction();
        final int[] dragLayerPos = getClampedDragLayerPos(ev.getX(), ev.getY());
        final int dragLayerX = dragLayerPos[0];
        final int dragLayerY = dragLayerPos[1];

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mMotionDownX = dragLayerX;
                mMotionDownY = dragLayerY;
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(dragLayerX, dragLayerY);
                break;
            case MotionEvent.ACTION_UP:
                // Ensure that we've processed a move event at the current
                // pointer location.
                handleMoveEvent(dragLayerX, dragLayerY);
                if (mDragging) {
                    drop(dragLayerX, dragLayerY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                cancelDrag();
                break;
        }

        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging;
    }

}
