package com.cooeeui.zenlauncher.common.ui.popujar;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.zenlauncher.R;

import java.util.ArrayList;
import java.util.List;

public class PopuJar extends PopupWindows implements OnDismissListener {

    private View mRootView;
    private ImageView mArrowUp;
    private ImageView mArrowDown;
    private LayoutInflater mInflater;
    private ViewGroup mTrack;
    private ScrollView mScroller;
    private OnPopuItemClickListener mItemClickListener;
    private OnDismissListener mDismissListener;
    // private FrameLayout frameLayout;
    private View uninstallView;

    private List<PopuItem> PopuItems = new ArrayList<PopuItem>();

    private boolean mDidAction;

    private int mChildPos;
    private int mInsertPos;
    private int mAnimStyle;
    private int mOrientation;
    private int rootWidth = 0;

    private AppInfo mSelected;

    public static final int POP_ON_AUTO = 0;
    public static final int POP_ON_TOP = 1;
    public static final int POP_ON_BOTTOM = 2;
    private int mShowSite = POP_ON_AUTO;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;
    public static final int ANIM_REFLECT = 4;
    public static final int ANIM_AUTO = 5;

    public static final int APP_TYPE_NORMAL = 0;
    public static final int APP_TYPE_SYS = 1;
    public static final int APP_TYPE_END = 2;

    /**
     * Constructor for default vertical layout
     *
     * @param context Context
     */
    public PopuJar(Context context) {
        this(context, VERTICAL);
    }

    /**
     * Constructor allowing orientation override
     *
     * @param context     Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public PopuJar(Context context, int orientation) {
        super(context);

        mOrientation = orientation;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mOrientation == HORIZONTAL) {
            setRootViewId(R.layout.popup_horizontal);
        } else {
            setRootViewId(R.layout.popup_vertical);
        }

        mAnimStyle = ANIM_AUTO;
        mChildPos = 0;
    }

    public AppInfo getSelected() {
        return mSelected;
    }

    public void setSelected(AppInfo selected) {
        mSelected = selected;
    }

    public void setShowSite(int site) {
        mShowSite = site;
    }

    /**
     * Get action item at an index
     *
     * @param index Index of item (position from callback)
     * @return Action Item at the position
     */
    public PopuItem getPopuItem(int index) {
        return PopuItems.get(index);
    }

    /**
     * Set root view.
     *
     * @param id Layout resource id
     */
    public void setRootViewId(int id) {
        mRootView = (ViewGroup) mInflater.inflate(id, null);
        mTrack = (ViewGroup) mRootView.findViewById(R.id.tracks);
        mArrowDown = (ImageView) mRootView.findViewById(R.id.arrow_down);
        mArrowUp = (ImageView) mRootView.findViewById(R.id.arrow_up);

        mScroller = (ScrollView) mRootView.findViewById(R.id.scroller);
        // This was previously defined on show() method, moved here to prevent
        // force close that occured
        // when tapping fastly on a view to show quickaction dialog.
        // Thanx to zammbi (github.com/zammbi)
        mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                                                   LayoutParams.WRAP_CONTENT));
        setContentView(mRootView);
    }

    /**
     * Set animation style
     *
     * @param mAnimStyle animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(int mAnimStyle) {
        this.mAnimStyle = mAnimStyle;
    }

    /**
     * Set listener for action item clicked.
     *
     * @param listener Listener
     */
    public void setOnPopuItemClickListener(OnPopuItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Add action item
     *
     * @param action {@link PopuItem}
     * @param type   这里为了考虑系统程序不能卸载，用以标记是否为系统程序
     */
    public void addPopuItem(PopuItem action, int type) {
        PopuItems.add(action);

        String title = action.getTitle();
        Drawable icon = action.getIcon();

        View container;

        if (mOrientation == HORIZONTAL) {
            container = mInflater.inflate(R.layout.action_item_horizontal, null);
        } else {
            container = mInflater.inflate(R.layout.action_item_vertical, null);
        }

        ImageView img = (ImageView) container.findViewById(R.id.iv_icon);

        // img.setPadding(30, 15, 0, 10);
        //
        // img.setScaleX(0.8f);
        //
        // img.setScaleY(0.8f);

        TextView text = (TextView) container.findViewById(R.id.tv_title);

        // text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

        if (icon != null) {
            img.setImageDrawable(icon);
        } else {
            img.setVisibility(View.GONE);
        }

        if (title != null) {
            text.setText(title);
        } else {
            text.setVisibility(View.GONE);
        }

        final int pos = mChildPos;
        final int actionId = action.getActionId();

        container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(PopuJar.this, pos, actionId);
                }

                if (!getPopuItem(pos).isSticky()) {
                    mDidAction = true;

                    dismiss();
                }
            }
        });

        container.setFocusable(true);
        container.setClickable(true);

        if (mOrientation == HORIZONTAL && mChildPos != 0) {
            View separator = mInflater.inflate(R.layout.horiz_separator, null);

            @SuppressWarnings("deprecation")
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);

            separator.setLayoutParams(params);
            separator.setPadding(5, 0, 5, 0);

            mTrack.addView(separator, mInsertPos);

            mInsertPos++;
        }

        mTrack.addView(container, mInsertPos);

        mChildPos++;
        mInsertPos++;

        if (type == APP_TYPE_NORMAL) {
            View line = mInflater.inflate(R.layout.vertical_separator, null);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            line.setLayoutParams(lp);
            mTrack.addView(line, mInsertPos);
            mInsertPos++;
        }

        if (type == APP_TYPE_SYS) {
            uninstallView = container;
        }
    }

    // 隐藏卸载选项
    public void hideUninstallView() {
        mTrack.getChildAt(mInsertPos - 2).setVisibility(View.INVISIBLE);
        mTrack.removeView(uninstallView);
    }

    // 重新显示卸载选项
    public void recoveryUninstallView() {
        mTrack.getChildAt(mInsertPos - 2).setVisibility(View.VISIBLE);
        mTrack.removeView(uninstallView);
        mTrack.addView(uninstallView);
    }

    /**
     * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
     */
    public void show(View anchor) {

        preShow();

        int xPos, yPos, arrowPos;

        mDidAction = false;

        int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(),
                                   location[1]
                                   + anchor.getHeight());

        // mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        // LayoutParams.WRAP_CONTENT));

        mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = mRootView.getMeasuredHeight();

        if (rootWidth == 0) {
            rootWidth = mRootView.getMeasuredWidth();
        }

        @SuppressWarnings("deprecation")
        int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        @SuppressWarnings("deprecation")
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

        // automatically get X coord of popup (top left)
        if ((anchorRect.centerX() + rootWidth / 2) > screenWidth) {

            xPos = anchorRect.left - (rootWidth - anchor.getWidth());

            xPos = (xPos < 0) ? 0 : xPos;

            arrowPos = anchorRect.centerX() - xPos;

        } else {

            if ((anchorRect.centerX() + rootWidth / 2 < screenHeight
                 && anchorRect.centerX() > screenWidth / 2)
                || (anchorRect.centerX() - rootWidth / 2 > 0
                    && anchorRect.centerX() < screenWidth / 2)
                || anchorRect.centerX() == screenWidth / 2) {

                xPos = anchorRect.centerX() - (rootWidth / 2);

            } else {

                xPos = anchorRect.left;
            }

            arrowPos = anchorRect.centerX() - xPos;
        }

        int dyTop = anchorRect.top;
        int dyBottom = screenHeight - anchorRect.bottom;

        boolean onTop = (dyTop - rootHeight > 100) ? true : false;

        if (mShowSite == POP_ON_TOP) {
            onTop = true;
        } else if (mShowSite == POP_ON_BOTTOM) {
            onTop = false;
        }

        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                LayoutParams l = mScroller.getLayoutParams();
                l.height = dyTop - anchor.getHeight();
            } else {
                yPos = anchorRect.top - rootHeight;
            }
        } else {
            yPos = anchorRect.bottom;

            if (rootHeight > dyBottom) {
                LayoutParams l = mScroller.getLayoutParams();
                l.height = dyBottom;
            }
        }

        showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

        Log.e("centerX", "centerX:  " + anchorRect.centerX());

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Set animation style
     *
     * @param screenWidth screen width
     * @param requestedX  distance from left edge
     * @param onTop       flag to indicate where the popup should be displayed. Set TRUE if
     *                    displayed on top of anchor view and vice versa
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;
        switch (mAnimStyle) {
            case ANIM_GROW_FROM_LEFT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
                                                  : R.style.Animations_PopDownMenu_Left);
                break;

            case ANIM_GROW_FROM_RIGHT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
                                                  : R.style.Animations_PopDownMenu_Right);
                break;

            case ANIM_GROW_FROM_CENTER:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
                                                  : R.style.Animations_PopDownMenu_Center);
                break;

            case ANIM_REFLECT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect
                                                  : R.style.Animations_PopDownMenu_Reflect);
                break;

            case ANIM_AUTO:
                if (arrowPos <= screenWidth / 4) {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
                                                      : R.style.Animations_PopDownMenu_Left);
                } else if (arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
                                                      : R.style.Animations_PopDownMenu_Center);
                } else {
                    mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
                                                      : R.style.Animations_PopDownMenu_Right);
                }

                break;
        }
    }

    /**
     * Show arrow
     *
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow
            .getLayoutParams();

        param.leftMargin = requestedX - arrowWidth / 2;

        hideArrow.setVisibility(View.GONE);
    }

    /**
     * Set listener for window dismissed. This listener will only be fired if the quicakction dialog
     * is dismissed by clicking outside the dialog or clicking on sticky item.
     */
    public void setOnDismissListener(PopuJar.OnDismissListener listener) {
        setOnDismissListener(this);

        mDismissListener = listener;
    }

    @Override
    public void onDismiss() {
        if (!mDidAction && mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    /**
     * Listener for item click
     */
    public interface OnPopuItemClickListener {

        public abstract void onItemClick(PopuJar source, int pos, int actionId);
    }

    /**
     * Listener for window dismiss
     */
    public interface OnDismissListener {

        public abstract void onDismiss();
    }
}
