package com.cooeeui.brand.zenlauncher.scenes;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.cooeeui.basecore.utilities.ThreadUtil;
import com.cooeeui.brand.zenlauncher.Launcher;
import com.cooeeui.brand.zenlauncher.LauncherAppState;
import com.cooeeui.brand.zenlauncher.LauncherModel;
import com.cooeeui.brand.zenlauncher.apps.AppInfo;
import com.cooeeui.brand.zenlauncher.apps.IconCache;
import com.cooeeui.brand.zenlauncher.apps.ShortcutInfo;
import com.cooeeui.brand.zenlauncher.changeicon.ChangeAppIcon;
import com.cooeeui.brand.zenlauncher.changeicon.dbhelp.ChangeAppIconDBSearcherApp;
import com.cooeeui.brand.zenlauncher.favorite.FavoritesData;
import com.cooeeui.brand.zenlauncher.scenes.utils.BitmapUtils;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragController;
import com.cooeeui.brand.zenlauncher.scenes.utils.DragSource;
import com.cooeeui.zenlauncher.R;
import com.umeng.analytics.MobclickAgent;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SpeedDial extends FrameLayout implements DragSource, View.OnTouchListener {

    private final String DIAL_STRING = "#Intent;action=android.intent.action.DIAL;end";
    private final String SMS_STRING =
        "#Intent;action=android.intent.action.MAIN;type=vnd.android-dir/mms-sms;end";

    private Launcher mLauncher;
    private DragController mDragController;
    private IconCache mIconCache;

    private static final int SPEED_DIAL_STATE_NORMAL = 0;
    private static final int SPEED_DIAL_STATE_DRAG = 1;
    public static final String FRIST_SHOW_NOTICE = "firstshownotice";
    private int mState = SPEED_DIAL_STATE_NORMAL;

    private String dialPackageName = null;
    private String smsPackageName = null;

    private int mIconSize = 0;
    private int mPadding;
    private int mSize;

    private static final int BUBBLE_VIEW_CAPACITY = 16;
    private ArrayList<BubbleView> mBubbleViews = new ArrayList<BubbleView>(
        BUBBLE_VIEW_CAPACITY);

    public static final int EDIT_VIEW_ICON = 0x100;
    public static final int EDIT_VIEW_DELETE = 0x101;

    private float mSelectX;
    private float mSelectY;
    private BubbleView mSelect;
    private BubbleView mDrag = null;

    private View mSearchBar;
    private View mEditBottomView;

    private Bitmap mDefaultIcon;

    private static final int IN_DURATION = 300;
    private static final int OUT_DURATION = 200;
    private ValueAnimator mAnimatorSearch;
    private ValueAnimator mAnimatorEdit;
    private float mAnimatorValue;
    private Interpolator mDecelerate;
    private Interpolator mAccelerate;

    private static final int ALPHA_DURATION = 200;
    private BubbleView mAlphaView;
    private ValueAnimator mAlphaAnimator;
    private ValueAnimator mDelAnimator;
    private int mAnimatorHeight;

    public SpeedDial(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SpeedDial(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeedDial(Context context) {
        super(context);
    }

    public void setup(Launcher launcher, DragController controller) {
        mLauncher = launcher;
        mDragController = controller;
        mSearchBar = mLauncher.getDragLayer().findViewById(R.id.search_bar);

        setOnLongClickListener(mLauncher);
        LauncherAppState app = LauncherAppState.getInstance();
        mIconCache = app.getIconCache();
        if (mIconSize == 0) {
            mIconSize = mLauncher.getResources().getDimensionPixelSize(R.dimen.image_size);
        }

        mAnimatorHeight = mLauncher.getResources().getDimensionPixelSize(
            R.dimen.bottom_height);

        // setup edit bottom view.
        mEditBottomView = mLauncher.getDragLayer().findViewById(
            R.id.edit_bottom_view);
        int[] ids = {
            R.id.edit_bottom_change_icon, R.id.edit_bottom_delete
        };
        int[] tags = {
            EDIT_VIEW_ICON, EDIT_VIEW_DELETE
        };
        EditView v;
        for (int i = 0; i < ids.length; i++) {
            v = (EditView) mLauncher.getDragLayer().findViewById(ids[i]);
            v.setOnClickListener(mLauncher);
            v.setTag(tags[i]);
            v.setUp(this);
            mDragController.addDropWorkSpace(v);
        }

        mDecelerate = AnimationUtils.loadInterpolator(mLauncher,
                                                      android.R.anim.decelerate_interpolator);

        mAccelerate = AnimationUtils.loadInterpolator(mLauncher,
                                                      android.R.anim.accelerate_interpolator);

        mAnimatorSearch = ValueAnimator.ofFloat(0, 1f);
        mAnimatorSearch.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mSearchBar.setTranslationY(mAnimatorHeight * (1f - value));
                if (mAnimatorValue == value) {
                    int v = mSearchBar.getVisibility() == View.VISIBLE ? View.INVISIBLE
                                                                       : View.VISIBLE;
                    mSearchBar.setVisibility(v);

                    v = mEditBottomView.getVisibility() == View.VISIBLE ? View.INVISIBLE
                                                                        : View.VISIBLE;
                    mEditBottomView.setVisibility(v);
                    mAnimatorValue = 2f;
                }
            }
        });

        mAnimatorEdit = ValueAnimator.ofFloat(0, 1f);
        mAnimatorEdit.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                mEditBottomView.setTranslationY(mAnimatorHeight * (1f - value));
                if (mAnimatorValue == value) {
                    int v = mSearchBar.getVisibility() == View.VISIBLE ? View.INVISIBLE
                                                                       : View.VISIBLE;
                    mSearchBar.setVisibility(v);

                    v = mEditBottomView.getVisibility() == View.VISIBLE ? View.INVISIBLE
                                                                        : View.VISIBLE;
                    mEditBottomView.setVisibility(v);
                    mAnimatorValue = 2f;
                }
            }
        });

        mAlphaView = null;
        mAlphaAnimator = ValueAnimator.ofFloat(0, 0.7f);
        mAlphaAnimator.setDuration(ALPHA_DURATION);
        mAlphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                if (mAlphaView != null) {
                    mAlphaView.setAlpha(1f - alpha);
                } else {
                    int count = mBubbleViews.size() - 1;
                    for (int i = count; i >= 0; i--) {
                        BubbleView view = mBubbleViews.get(i);
                        if (view != mSelect) {
                            view.setAlpha(1f - alpha);
                        }
                    }
                }
            }
        });

        mDelAnimator = ValueAnimator.ofFloat(1f, 0);
        mDelAnimator.setDuration(OUT_DURATION);
        mDelAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue();
                mSelect.setAlpha(alpha);
            }
        });
        mDelAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeBubbleView();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationStart(Animator animation) {

            }

        });
        appIconDBSearcherApp = new ChangeAppIconDBSearcherApp(mLauncher);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mLauncher.onWindowVisibilityChanged(visibility);
    }

    public void startBind() {
        int count = mBubbleViews.size() - 1;
        for (int i = count; i >= 0; i--) {
            BubbleView view = mBubbleViews.get(i);
            mBubbleViews.remove(view);
            removeView(view);
            mDragController.removeDropWorkSpace(view);
        }
    }

    public void finishBind() {
        update();
    }

    public void updateFromBind() {
        int count = mBubbleViews.size() - 1;
        for (int i = count; i >= 0; i--) {
            BubbleView view = mBubbleViews.get(i);
            ShortcutInfo info = (ShortcutInfo) view.getTag();
            if (info.intent != null && info.mIconId == ShortcutInfo.NO_ID) {
                info.mIcon = mIconCache.getIcon(info.intent);
                Bitmap b = null;
                if (info.getIntent() != null) {
                    Bitmap bitmap = appIconDBSearcherApp
                        .ChangeAppIconDBSearcherApps(info.getIntent()
                                                         .getComponent().getPackageName()
                                                     + "/"
                                                     + info.getIntent().getComponent()
                                                         .getClassName(), 0, 0);
                    if (bitmap != null) {
                        info.mIcon = bitmap;
                    }
                } else if (ChangeAppIcon.BROWSER_NAME
                    .equals((String) info.title)) {
                    Bitmap bitmap = appIconDBSearcherApp
                        .ChangeAppIconDBSearcherApps(
                            ChangeAppIcon.BROWSER_NAME, 0, 0);
                    if (bitmap != null) {
                        info.mIcon = bitmap;
                    }
                }
                view.changeBitmap(info.mIcon);
            }
        }
    }

    public void removeBubbleViewFromBind(ArrayList<AppInfo> appInfos) {
        HashSet<ComponentName> cns = new HashSet<ComponentName>();
        for (AppInfo info : appInfos) {
            cns.add(info.componentName);
        }

        int count = mBubbleViews.size() - 1;
        for (int i = count; i >= 0; i--) {
            BubbleView view = mBubbleViews.get(i);
            ShortcutInfo info = (ShortcutInfo) view.getTag();
            if (info.intent != null && cns.contains(info.intent.getComponent())) {
                removeBubbleView(view);
                LauncherModel.deleteItemFromDatabase(mLauncher, info);
            }
        }
        update();
    }

    public void removeBubbleView(AppInfo appInfos) {
        int count = mBubbleViews.size() - 1;
        for (int i = count; i >= 0; i--) {
            BubbleView view = mBubbleViews.get(i);
            ShortcutInfo info = (ShortcutInfo) view.getTag();
            if (info.intent != null
                && appInfos.intent.getComponent().equals(
                info.intent.getComponent())) {
                removeBubbleView(view);
                LauncherModel.deleteItemFromDatabase(mLauncher, info);
            }
        }
        update();
    }

    private ChangeAppIconDBSearcherApp appIconDBSearcherApp;

    private void buildBubbleView(ShortcutInfo info) {
        Bitmap b = info.mIcon;

        if (b == null) {
            b = getDefaultIcon();
            info.mRecycle = false;
        }
        if (info.getIntent() != null) {
            Bitmap bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(
                info.getIntent().getComponent().getPackageName() == null ? "hahao" :
                info.getIntent().getComponent().getPackageName() + "/" + info.getIntent()
                    .getComponent().getClassName(), 0, 0);
            if (bitmap != null) {
                b = bitmap;
            }
        } else if (ChangeAppIcon.BROWSER_NAME.equals((String) info.title)) {
            Bitmap bitmap = appIconDBSearcherApp.ChangeAppIconDBSearcherApps(
                ChangeAppIcon.BROWSER_NAME, 0, 0);
            if (bitmap != null) {
                b = bitmap;
            }
        }
        BubbleView v = new BubbleView(mLauncher, b);
        v.setTag(info);
        v.setSize(mIconSize, mPadding);
        addView(v);
        mBubbleViews.add(v);
        v.setOnClickListener(mLauncher);
        v.setOnLongClickListener(mLauncher);

        v.setOnTouchListener(this);

        mDragController.addDropWorkSpace(v);

        //设置通知消息的数量，主要针对未接电话和未读短信等
        if (isSms(info)) {
            if (mLauncher.getSmsAndCalls() != null) {
                ThreadUtil.execute(new Runnable() {
                    @Override
                    public void run() {
                        int num = mLauncher.getSmsAndCalls().getSMSCount();
                        mLauncher.sendMissedMessage(Launcher.MSG_SMS_CHANGE, num);
                    }
                });
            }
        } else if (isDial(info)) {
            if (mLauncher.getSmsAndCalls() != null) {
                ThreadUtil.execute(new Runnable() {
                    @Override
                    public void run() {
                        int num = mLauncher.getSmsAndCalls()
                            .getCallsCount();
                        mLauncher.sendMissedMessage(Launcher.MSG_CALL_CHANGE, num);
                    }
                });

            }
        }
    }

    public void refreshNotSMS(int missSMS) {
        for (BubbleView v : mBubbleViews) {
            ShortcutInfo i = (ShortcutInfo) v.getTag();
            if (isSms(i)) {
                v.setNoticeCount(missSMS);
                v.invalidate();
            }
        }
    }

    public void refreshNotCall(int missCall) {
        for (BubbleView v : mBubbleViews) {
            ShortcutInfo i = (ShortcutInfo) v.getTag();
            if (isDial(i)) {
                v.setNoticeCount(missCall);
                v.invalidate();
            }
        }
    }

    public String getClassName(String name) {
        try {
            Intent intent = Intent.parseUri(name, 0);
            List<ResolveInfo> apps = mLauncher.getPackageManager()
                .queryIntentActivities(intent, 0);
            if (apps == null || apps.isEmpty()) {
                return null;
            }

            return apps.get(0).activityInfo.name;
        } catch (URISyntaxException e) {
            //
        }

        return null;
    }

    public boolean isDial(ShortcutInfo info) {
        if (info.intent == null || info.intent.getComponent() == null) {
            return false;
        }

        if (dialPackageName == null) {
            dialPackageName = getClassName(DIAL_STRING);
        }

        if (info.intent.getComponent().getClassName().equals(dialPackageName)) {
            return true;
        }

        return false;
    }

    public boolean isSms(ShortcutInfo info) {
        if (info.intent == null || info.intent.getComponent() == null) {
            return false;
        }

        if (smsPackageName == null) {
            smsPackageName = getClassName(SMS_STRING);
        }

        if (info.intent.getComponent().getClassName().equals(smsPackageName)) {
            return true;
        }

        return false;
    }

    public void addBubbleViewFromBind(ShortcutInfo info) {
        buildBubbleView(info);

        int p = mBubbleViews.size() - 1;
        if (info.position != p) {
            info.position = p;
            LauncherModel.updateItemInDatabase(mLauncher, info);
        }
    }

    public void addBubbleView(ShortcutInfo info) {
        int position = mBubbleViews.size();

        buildBubbleView(info);
        mSelect = mBubbleViews.get(position);

        LauncherModel.addItemToDatabase(mLauncher, info);

        if (info.intent != null && info.intent.getComponent() != null) {
            AppInfo app = FavoritesData.getAppInfo(info.intent.getComponent()
                                                       .getPackageName());
            if (app != null) {
                FavoritesData.remove(app);
                mLauncher.updateFavorite();
            }
        }
    }

    private Bitmap getDefaultIcon() {
        if (mDefaultIcon == null) {
            mDefaultIcon = BitmapUtils.getIcon(Resources.getSystem(),
                                               android.R.mipmap.sym_def_app_icon, mIconSize);
        }
        return mDefaultIcon;
    }

    public View buildSelectView() {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();
        Bitmap b = i.mIcon;
        if (b == null) {
            b = getDefaultIcon();
            i.mRecycle = false;
        }

        View v = new BubbleView(mLauncher, b, mIconSize);

        return v;
    }

    public ArrayList<String> getPackageNames() {
        ArrayList<String> names = new ArrayList<String>();
        int count = mBubbleViews.size() - 1;
        for (int i = count; i >= 0; i--) {
            BubbleView view = mBubbleViews.get(i);
            ShortcutInfo info = (ShortcutInfo) view.getTag();
            if (info.intent != null && info.intent.getComponent() != null) {
                names.add(info.intent.getComponent().getPackageName());
            }
        }
        return names;
    }

    public ArrayList<BubbleView> getBubbleViews() {
        return mBubbleViews;
    }

    public Rect getSelectRect() {
        Rect r = new Rect();
        getGlobalVisibleRect(r);
        r.left += mSelectX;
        r.right += mSelectX;
        r.top += mSelectY;
        r.bottom += mSelectY;

        return r;
    }

    public ShortcutInfo getSelectInfo() {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();

        return i;
    }

    public void changeIcon(Bitmap b) {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();

        if (i.mRecycle) {
            mSelect.clearBitmap();
            i.mRecycle = false;
        }
        i.mIconId = ShortcutInfo.NO_ID;
        i.mIcon = b;
        mSelect.changeBitmap(b);

        LauncherModel.updateItemInDatabase(mLauncher, i);
    }

    public void changeIcon(int iconId) {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();

        if (i.mIconId == iconId) { // same icon
            return;
        }
        if (i.mRecycle) {
            mSelect.clearBitmap();
        }

        Bitmap b = BitmapUtils.getIcon(mLauncher.getResources(), iconId,
                                       mIconSize);
        i.mRecycle = true;
        i.mIconId = iconId;
        i.mIcon = b;
        if (b == null) {
            b = getDefaultIcon();
            i.mRecycle = false;
        }

        mSelect.changeBitmap(b);

        LauncherModel.updateItemInDatabase(mLauncher, i);
    }

    public void changeBubbleView(ShortcutInfo info) {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();
        if (i.mRecycle) {
            mSelect.clearBitmap();
            i.mRecycle = false;
        }
        i.intent = info.intent;
        i.mIcon = info.mIcon;
        i.mIconId = ShortcutInfo.NO_ID;

        mSelect.changeBitmap(info.mIcon);

        LauncherModel.updateItemInDatabase(mLauncher, i);
    }

    public void removeBubbleView(BubbleView view) {
        ShortcutInfo i = (ShortcutInfo) view.getTag();
        mBubbleViews.remove(view);
        removeView(view);
        mDragController.removeDropWorkSpace(view);

        if (i.mRecycle) {
            view.clearBitmap();
            i.mRecycle = false;
        }
    }

    public void removeBubbleView() {
        ShortcutInfo i = (ShortcutInfo) mSelect.getTag();

        removeBubbleView(mSelect);
        LauncherModel.deleteItemFromDatabase(mLauncher, i);
        if (i.intent != null && i.intent.getComponent() != null) {
            if (FavoritesData.add(i.intent.getComponent().getPackageName())) {
                mLauncher.updateFavorite();
            } else {
                mLauncher.updateRecentApp();
            }
        }
        int s = mBubbleViews.size();

        if (s <= 0) {
            stopDrag();
            return;
        }
        update();
        mSelect = mBubbleViews.get(s - 1);
        mAlphaView = mSelect;
        mAlphaAnimator.setFloatValues(0.7f, 0);
        mAlphaAnimator.start();
        // 主页删除图标次数
    }

    public void deleteBubbleView() {
        if (mDelAnimator.isRunning()) {
            return;
        }
        mDelAnimator.start();
    }

    private void moveBubbleView(int position, float x, float y) {
        BubbleView v = mBubbleViews.get(position);
        if (v != null && v != mDrag) {
            v.move(x, y);
        }
    }

    void initSize() {
        mIconSize = mLauncher.getResources().getDimensionPixelSize(R.dimen.image_size);
        mPadding = (mSize - mIconSize * 4) / 4;

        if (mPadding < mIconSize / 4) {
            mIconSize = mSize / 6;
            mPadding = (mSize - mIconSize * 4) / 4;
        }

        // do not forget to change size of bubble views existed.
        for (BubbleView v : mBubbleViews) {
            v.setSize(mIconSize, mPadding);
        }

        update();
    }

    private void updateSite(int count, int column) {
        int row = (count + column - 1) / column;
        int w = column * mPadding + column * mIconSize;
        int h = row * mPadding + row * mIconSize;
        float startX = (mSize - w) / 2;
        float startY = (mSize - h) / 2;
        int r = count % column;
        int rStart = count - column;
        float iconX;
        float iconY = startY;

        if (r > 0) {
            iconX = startX + (column - r) * (mIconSize + mPadding) / 2;
            iconY = startY;
            rStart = count - r;
            for (int i = 0; i < r; i++) {
                moveBubbleView(rStart + i, iconX, iconY);
                iconX += (mIconSize + mPadding);
            }
            row--;
            iconY += (mIconSize + mPadding);
            rStart -= column;
        }

        for (int i = 0; i < row; i++) {
            iconX = startX;
            for (int j = 0; j < column; j++) {
                moveBubbleView(rStart + j, iconX, iconY);
                iconX += (mIconSize + mPadding);
            }
            iconY += (mIconSize + mPadding);
            rStart -= column;
        }
    }

    public void update() {
        int count = mBubbleViews.size();

        switch (count) {
            case 1:
                updateSite(count, 1);
                break;

            case 2:
            case 3:
            case 4:
                updateSite(count, 2);
                break;

            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                updateSite(count, 3);
                break;

            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                updateSite(count, 4);
                break;
        }
    }

    public void refresh() {
        appIconDBSearcherApp = new ChangeAppIconDBSearcherApp(mLauncher);
        for (int i = 0; i < mBubbleViews.size(); i++) {
            ShortcutInfo appInfo = (ShortcutInfo) mBubbleViews.get(i).getTag();
            if (appInfo.getIntent() != null) {
                Bitmap bitmap = appIconDBSearcherApp
                    .ChangeAppIconDBSearcherApps(appInfo.getIntent()
                                                     .getComponent().getPackageName()
                                                 + "/"
                                                 + appInfo.getIntent().getComponent()
                                                     .getClassName(), 0, 0);
                if (bitmap != null) {
                    mBubbleViews.get(i).changeBitmap(bitmap);
                    mBubbleViews.get(i).invalidate();
                }
            } else if (ChangeAppIcon.BROWSER_NAME
                .equals((String) appInfo.title)) {
                Bitmap bitmap = appIconDBSearcherApp
                    .ChangeAppIconDBSearcherApps(
                        ChangeAppIcon.BROWSER_NAME, 0, 0);
                if (bitmap != null) {
                    mBubbleViews.get(i).changeBitmap(bitmap);
                    mBubbleViews.get(i).invalidate();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // catch the size from onMeasure.
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSize != heightSize) {
            try {
                throw new Exception("width != height");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mSize != widthSize) {
            mSize = widthSize;
            initSize();
        }

        // set measured dimension of children to mIconSize for touch event.
        int sizeMeasureSpec = MeasureSpec.makeMeasureSpec(mIconSize,
                                                          MeasureSpec.EXACTLY);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.measure(sizeMeasureSpec, sizeMeasureSpec);
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (mState == SPEED_DIAL_STATE_NORMAL) {
            return false;
        }

        if (action == MotionEvent.ACTION_DOWN && v instanceof BubbleView) {
            BubbleView bv = (BubbleView) v;
            if (mAlphaAnimator.isRunning()) {
                mAlphaAnimator.end();
            }
            mAlphaView = mSelect;
            dragView(bv);
            return true;
        }
        return false;
    }

    private void showEditViews() {
        mAnimatorSearch.setFloatValues(1f, 0);
        mAnimatorSearch.setDuration(OUT_DURATION);
        mAnimatorSearch.setInterpolator(mAccelerate);
        mAnimatorValue = 0;
        mAnimatorEdit.setFloatValues(0, 1f);
        mAnimatorEdit.setDuration(IN_DURATION);
        mAnimatorEdit.setInterpolator(mDecelerate);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mAnimatorSearch).before(mAnimatorEdit);
        animatorSet.start();
        // 主页编辑图标次数
        MobclickAgent.onEvent(mLauncher, "HomeEditIcon");
    }

    private void hideEditViews() {
        mAnimatorEdit.setFloatValues(1f, 0);
        mAnimatorEdit.setDuration(OUT_DURATION);
        mAnimatorEdit.setInterpolator(mAccelerate);
        mAnimatorValue = 0;
        mAnimatorSearch.setFloatValues(0, 1f);
        mAnimatorSearch.setDuration(IN_DURATION);
        mAnimatorSearch.setInterpolator(mDecelerate);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mAnimatorEdit).before(mAnimatorSearch);
        animatorSet.start();
    }

    private void dragView(BubbleView view) {
        mSelect = view;
        mDrag = view;
        mSelectX = mSelect.getTranslationX();
        mSelectY = mSelect.getTranslationY();
        removeView(view);
        mDragController.removeDropWorkSpace(view);
        mDragController.startDrag(this, view);

        if (mAlphaView == view) {
            return;
        }
        mSelect.setAlpha(1f);
        mAlphaAnimator.setFloatValues(0, 0.7f);
        mAlphaAnimator.start();
    }

    public void startDrag(BubbleView view) {
        if (mState != SPEED_DIAL_STATE_DRAG) {
            mState = SPEED_DIAL_STATE_DRAG;
            showEditViews();
            dragView(view);
        }
    }

    public void stopDrag() {
        if (mState != SPEED_DIAL_STATE_NORMAL) {
            mState = SPEED_DIAL_STATE_NORMAL;
            hideEditViews();
            mAlphaView = null;
            mAlphaAnimator.setFloatValues(0.7f, 0);
            mAlphaAnimator.start();
        }
    }

    public int getIconSize() {
        return mIconSize;
    }

    public float getSelectX() {
        return mSelectX;
    }

    public float getSelectY() {
        return mSelectY;
    }

    public BubbleView getmSelect() {
        return mSelect;
    }

    public void setmSelect(BubbleView mSelect) {
        this.mSelect = mSelect;
    }

    public boolean isDragState() {
        return mState == SPEED_DIAL_STATE_DRAG;
    }

    public boolean isFull() {
        return mBubbleViews.size() >= BUBBLE_VIEW_CAPACITY;
    }

    @Override
    public void onDropCompleted(View targetView) {
        if (targetView instanceof BubbleView) {
            BubbleView target = (BubbleView) targetView;
            ShortcutInfo si = (ShortcutInfo) mSelect.getTag();
            ShortcutInfo ti = (ShortcutInfo) target.getTag();
            int tIndex = mBubbleViews.indexOf(target);
            int sIndex = mBubbleViews.indexOf(mSelect);
            int sp = si.position;
            int tp = ti.position;
            si.position = tp;
            LauncherModel.updateItemInDatabase(mLauncher, si);
            mBubbleViews.set(tIndex, mSelect);
            mBubbleViews.set(sIndex, target);
            ti.position = sp;
            LauncherModel.updateItemInDatabase(mLauncher, ti);
            // MobclickAgent.onEvent(mLauncher, "DragAppToSpeedDial");
        }

        mLauncher.getDragLayer().removeView(mSelect);
        mDragController.addDropWorkSpace(mSelect);
        addView(mSelect);

        if (targetView instanceof EditView) {
            mLauncher.onClick(targetView);
            Integer num = (Integer) targetView.getTag();
            if (num.intValue() == SpeedDial.EDIT_VIEW_DELETE) {
                return;
            }
        }

        mDrag = null;
        update();
    }
}
